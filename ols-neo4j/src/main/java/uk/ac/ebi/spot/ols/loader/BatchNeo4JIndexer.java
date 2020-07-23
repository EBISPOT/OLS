package uk.ac.ebi.spot.ols.loader;

import static uk.ac.ebi.spot.ols.loader.Neo4JIndexerConstants.*;
import static uk.ac.ebi.spot.ols.config.OntologyDefaults.THING;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.graphdb.schema.Schema;
import org.neo4j.helpers.collection.MapUtil;
import org.neo4j.index.lucene.unsafe.batchinsert.LuceneBatchInserterIndexProvider;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserterIndex;
import org.neo4j.unsafe.batchinsert.BatchInserterIndexProvider;
import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.ac.ebi.spot.ols.config.OlsNeo4jConfiguration;
import uk.ac.ebi.spot.ols.exception.IndexingException;
import uk.ac.ebi.spot.ols.model.OntologyIndexer;

/**
 * @author Simon Jupp
 * @date 17/06/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@Component
public class BatchNeo4JIndexer implements OntologyIndexer {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private BatchInserter inserter;
    private BatchInserterIndex index;
    private BatchInserterIndexProvider indexProvider;

    public Logger getLogger() {
        return logger;
    }

    @Autowired
    private GraphDatabaseService db;

    private Map<String, Object> isaProperties = new HashMap<>();
    private Map<String, Object> subPropertyProperties = new HashMap<>();
    private Map<String, Object> rdfTypeProperties = new HashMap<>();

    private Label nodeOntologyLabel;


    private static int BATCH_SIZE = 1000000;
    private static int DELETE_SIZE = 100000;

    @Autowired
    OlsNeo4jConfiguration neo4jConfiguration;

    public BatchNeo4JIndexer() {

    }

    protected BatchNeo4JIndexer(String ontologyName, BatchInserterIndex batchInserterIndex,
    		BatchInserterIndexProvider batchInserterIndexProvider, BatchInserter batchInserter) {
    	
    	nodeOntologyLabel = DynamicLabel.label(ontologyName.toUpperCase());
    	index = batchInserterIndex;
    	indexProvider = batchInserterIndexProvider;
    	inserter = batchInserter;
    }
    
    private Long getOrCreateMergedNode(BatchInserter inserter, Map<String, Long> mergedNodeMap,
    		OntologyLoader loader, IRI classIri, Label ... nodeLabel) {

        if (!mergedNodeMap.containsKey(classIri.toString())) {

            // link to merged node
            IndexHits<Long> hits = null;

            try {
                getLogger().debug("classIri = " + classIri);
                hits = index.get("iri", classIri);
            } catch (Throwable t) {
                getLogger().error(t.getMessage(), t);
            }

            if (hits != null && hits.size() == 0) {
                Map<String, Object> properties = new HashMap<>();
                properties.put("iri", classIri.toString());
                properties.put("label", loader.getTermLabels().get(classIri));

                Long hit = inserter.createNode(properties, nodeLabel);
                index.add(hit, properties);
                mergedNodeMap.put(classIri.toString(), hit);
            }
            else {
                if (hits != null && hits.size() > 1) {
                    System.out.println("WARING: found more than one iri in merged terms for: " + classIri);
                }
                Long mergedNode = hits.getSingle();
                mergedNodeMap.put(classIri.toString(), mergedNode);
            }
        }
        return mergedNodeMap.get(classIri.toString());
    }

    private void setOntologyLabel (String ontologyName) {
        nodeOntologyLabel   = DynamicLabel.label(ontologyName.toUpperCase());
    }

    private BatchInserter getBatchIndexer (String ontologyName) {
        inserter = OLSBatchIndexerCreator.createBatchInserter(inserter, 
        		OlsNeo4jConfiguration.getNeo4JPath());

        OLSBatchIndexerCreator.createSchemaIndexes(inserter);

        isaProperties.put("uri", "http://www.w3.org/2000/01/rdf-schema#subClassOf");
        isaProperties.put("label", "is a");
        isaProperties.put("ontology_name", ontologyName);
        isaProperties.put("__type__", "SubClassOf");

        subPropertyProperties.put("uri", "http://www.w3.org/2000/01/rdf-schema#subPropertyOf");
        subPropertyProperties.put("label", "sub property of");
        subPropertyProperties.put("ontology_name", ontologyName);
        subPropertyProperties.put("__type__", "SubPropertyOf");

        rdfTypeProperties.put("uri", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type>");
        rdfTypeProperties.put("label", "type");
        rdfTypeProperties.put("ontology_name", ontologyName);
        rdfTypeProperties.put("__type__", "Type");

       	index = getBatchInserterIndex(getIndexProvider(inserter));

        return inserter;

    }

    private BatchInserterIndexProvider getIndexProvider (BatchInserter inserter) {

        indexProvider =
            new LuceneBatchInserterIndexProvider(inserter);
        return indexProvider;
    }

    private BatchInserterIndex getBatchInserterIndex(BatchInserterIndexProvider indexProvider) {
        BatchInserterIndex entities = null;
        try {
            entities = indexProvider.nodeIndex("Resource", MapUtil.stringMap("type", "exact"));
            entities.setCacheCapacity("iri", 1000000);
        } catch (Throwable t) {
            getLogger().error(t.getMessage(), t);
        }
        return entities;
    }

    private void indexProperties(BatchInserter inserter, OntologyLoader loader, Map<String, Long> nodeMap, Map<String, Long> mergedNodeMap) {

        // index relations
        Collection<IRI> allRelations = loader.getAllObjectPropertyIRIs();
        allRelations.addAll(loader.getAllDataPropertyIRIs());
        allRelations.addAll(loader.getAllAnnotationPropertyIRIs());
        getLogger().debug("Creating Neo4j index for " + allRelations.size() + " properties");

        for (IRI objectPropertyIri : allRelations) {
            Long node = NodeCreator.getOrCreateNode(inserter, nodeMap,loader, objectPropertyIri, 
            		new LinkedList<Label>(Arrays.asList(relationLabel, _relationLabel, 
            				nodeOntologyLabel)));
            Long mergedNode = getOrCreateMergedNode(inserter, mergedNodeMap, loader, 
            		objectPropertyIri, mergedClassLabel);

            // add refers link
            inserter.createRelationship( node, mergedNode, refersTo, null);

            // add parent nodes
            if (!loader.getDirectParentTerms(objectPropertyIri).isEmpty()) {
                for (IRI parent : loader.getDirectParentTerms().get(objectPropertyIri)) {
                    Long parentNode = NodeCreator.getOrCreateNode(inserter, nodeMap,loader, parent,
                    		new LinkedList<Label>(Arrays.asList(relationLabel, _relationLabel, 
                    				nodeOntologyLabel)));	
                    
                    // create local relationship
                    inserter.createRelationship(node, parentNode, subpropertyof, subPropertyProperties);
                }
            }
            else {
                Long rootProperty = NodeCreator.getOrCreateNode(inserter, nodeMap, loader, 
                		IRI.create("http://www.w3.org/2002/07/owl#TopObjectProperty"), 
                		new LinkedList<Label>(Arrays.asList(relationLabel, _relationLabel, 
                				nodeOntologyLabel, rootLabel)));	
                inserter.createRelationship(node, rootProperty, subpropertyof, subPropertyProperties);
            }
        }
    }

    private void indexIndividuals(BatchInserter inserter, OntologyLoader loader, 
    		Map<String, Long> nodeMap, Map<String, Long> mergedNodeMap, Map<String, Long> classNodeMap) {
        
    	getLogger().debug("Creating Neo4j index for " + loader.getAllIndividualIRIs().size() + " individuals");

        for (IRI individualIri : loader.getAllIndividualIRIs()) {

            Long node = NodeCreator.getOrCreateNode(inserter, nodeMap,loader, individualIri, 
            		new LinkedList<Label>(Arrays.asList(instanceLabel, _instanceLabel, 
            				nodeOntologyLabel)));

            Long mergedNode = getOrCreateMergedNode(inserter, mergedNodeMap, loader, individualIri, 
            		mergedClassLabel);

            // add refers link
            inserter.createRelationship( node, mergedNode, refersTo, null);

            // add parent nodes
            if (loader.getDirectTypes().containsKey(individualIri)) {
                for (IRI parent : loader.getDirectTypes().get(individualIri)) {
                    Long parentNode =  NodeCreator.getOrCreateNode(inserter, classNodeMap,loader, parent,
                    		new LinkedList<Label>(Arrays.asList(nodeLabel, nodeOntologyLabel, 
                    				_nodeLabel)));
                    		
                    // create local relationship
                    inserter.createRelationship(node, parentNode, typeOf, rdfTypeProperties);
                }
            }
            else {
                Long defaultType = NodeCreator.getOrCreateNode(inserter, nodeMap,loader, 
                		IRI.create("http://www.w3.org/2002/07/owl#Thing"),  
                		new LinkedList<Label>(Arrays.asList(nodeLabel, nodeOntologyLabel, 
                				_nodeLabel, rootLabel)));
                
                inserter.createRelationship( node, defaultType, typeOf, rdfTypeProperties);
            }

            // add relations
            indexRelations(node, loader.getRelatedIndividuals(individualIri),
            		inserter,loader,nodeMap, 
            		new LinkedList<Label>(Arrays.asList(instanceLabel, nodeOntologyLabel, _instanceLabel)));
            
            indexRelations(node, loader.getRelatedClassesToIndividual(individualIri),
            		inserter,loader,classNodeMap, 
            		new LinkedList<Label>(Arrays.asList(nodeLabel, nodeOntologyLabel, _nodeLabel)));
        }
    }

    private void indexRelations(Long node, Map<IRI, Collection<IRI>> relatedIndividuals, 
    		BatchInserter inserter, OntologyLoader loader, Map<String, Long> nodeMap, 
    		Collection<Label> nodeLabels) {
    	
        for (IRI relation : relatedIndividuals.keySet()) {
            Map<String, Object> relatedProperties = new HashMap<>();
            relatedProperties.put("uri", relation.toString());
            relatedProperties.put("label", loader.getTermLabels().get(relation));
            relatedProperties.put("ontology_name", loader.getOntologyName());
            relatedProperties.put("__type__", "Related");

            for (IRI relatedTerm : relatedIndividuals.get(relation)) {
                //TODO review right parameters
                Long relatedNode =  NodeCreator.getOrCreateNode(inserter, nodeMap,loader, 
                		relatedTerm, nodeLabels);
                inserter.createRelationship( node, relatedNode, related, relatedProperties);
            }

        }
    }

    /**
     * Add relationships of the form A sub R some {a}
     * 
     * @param node
     * @param relatedIndividuals
     * @param inserter
     * @param loader
     * @param nodeMap
     * @param nodeLabels
     */
    private void indexRelatedIndividuals(Long node, Map<IRI, Collection<IRI>> relatedIndividuals, 
    		BatchInserter inserter, OntologyLoader loader, Map<String, Long> nodeMap, 
    		Collection<Label> nodeLabels) {
        
    	for (IRI relation : relatedIndividuals.keySet()) {
            Map<String, Object> relatedProperties = new HashMap<>();
            relatedProperties.put("uri", relation.toString());
            relatedProperties.put("label", loader.getTermLabels().get(relation));
            relatedProperties.put("ontology_name", loader.getOntologyName());
            relatedProperties.put("__type__", "RelatedIndividual");

            for (IRI relatedTerm : relatedIndividuals.get(relation)) {
                //TODO review right parameters
                Long relatedNode =  NodeCreator.getOrCreateNode(inserter, nodeMap,loader, 
                		relatedTerm, nodeLabels);
                inserter.createRelationship( node, relatedNode, relatedIndividual, relatedProperties);
            }

        }
    }

    void indexClasses(BatchInserter inserter, OntologyLoader loader, Map<String, Long> nodeMap, 
    		Map<String, Long> mergedNodeMap) {
        getLogger().debug("Creating Neo4j index for " + loader.getAllClasses().size() + " classes");

        for (IRI classIri : loader.getAllClasses()) {

            Long node = NodeCreator.getOrCreateNode(inserter, nodeMap,loader, classIri, 
            		new LinkedList<Label>(Arrays.asList(nodeLabel, nodeOntologyLabel, _nodeLabel)));

            Long mergedNode = getOrCreateMergedNode(inserter, mergedNodeMap, loader, classIri, 
            		mergedClassLabel);

            // add refers link
            inserter.createRelationship( node, mergedNode, refersTo, null);

            addParentAndRelatedParentNodesWithRelationships(inserter, loader, nodeMap, classIri, node);
        }

        for (IRI classIri : loader.getAllClasses()) {

            Long node = nodeMap.get(classIri.toString());

            indexRelatedNodes(inserter, loader, nodeMap, classIri, node);

            indexRelatedIndividuals(node, loader.getRelatedIndividualsToClass(classIri), inserter,
            		loader,nodeMap, new LinkedList<Label>(Arrays.asList(
                            instanceLabel, nodeOntologyLabel, _instanceLabel)));
        }
    }

	private void addParentAndRelatedParentNodesWithRelationships(BatchInserter inserter, OntologyLoader loader,
			Map<String, Long> nodeMap, IRI classIri, Long node) {
		// add parent nodes
		if (!loader.getDirectParentTerms(classIri).isEmpty()) {
		    for (IRI parent : loader.getDirectParentTerms().get(classIri)) {
		        Long parentNode =  NodeCreator.getOrCreateNode(inserter, nodeMap, loader, parent, 
		        		new LinkedList<Label>(Arrays.asList(nodeLabel, nodeOntologyLabel, _nodeLabel)));
		        // create local relationship
		        inserter.createRelationship(node, parentNode, isa, isaProperties);
		    }
		}
		else if (loader.getRelatedParentTerms(classIri).isEmpty()) {
		    Long thing = NodeCreator.getOrCreateNode(inserter, nodeMap, loader, 
		    		IRI.create(THING), new LinkedList<Label>(Arrays.asList(nodeLabel, nodeOntologyLabel, 
		    		    _nodeLabel, rootLabel)));
		    inserter.createRelationship( node, thing, isa, isaProperties);
		}
	}
    
    void indexClassesDeprecated(BatchInserter inserter, OntologyLoader loader, Map<String, Long> nodeMap, 
    		Map<String, Long> mergedNodeMap) {
    	
        getLogger().debug("Creating Neo4j index for " + loader.getAllClasses().size() + " classes");

        for (IRI classIri : loader.getAllClasses()) {

            Long node = NodeCreator.getOrCreateNode(inserter, nodeMap,loader, classIri, 
            		new LinkedList<Label>(Arrays.asList(nodeLabel, nodeOntologyLabel, _nodeLabel)));

            Long mergedNode = getOrCreateMergedNode(inserter, mergedNodeMap, loader, classIri, 
            		mergedClassLabel);

            // add refers link
            inserter.createRelationship( node, mergedNode, refersTo, null);

            addParentAndRelatedParentNodesWithRelationships(inserter, loader, nodeMap, classIri, node);
            

            indexRelatedNodes(inserter, loader, nodeMap, classIri, node);

            indexRelatedIndividuals(node, loader.getRelatedIndividualsToClass(classIri), inserter,
            		loader,nodeMap, new LinkedList<Label>(Arrays.asList(
            				instanceLabel, nodeOntologyLabel, _instanceLabel)));
        }

    }

	private void indexRelatedNodes(BatchInserter inserter, OntologyLoader loader, 
			Map<String, Long> nodeMap, IRI classIri, Long node) {
		
		Map<IRI, Collection<IRI>> relatedterms = loader.getRelatedTerms(classIri);


		for (IRI relation : relatedterms.keySet()) {
		    Map<String, Object> relatedProperties = new HashMap<>();
		    relatedProperties.put("uri", relation.toString());
		    relatedProperties.put("label", loader.getTermLabels().get(relation));
		    relatedProperties.put("ontology_name", loader.getOntologyName());
		    relatedProperties.put("__type__", "Related");

		    Map<String, Object> relatedTreeProperties = new HashMap<>();
		    relatedTreeProperties.put("uri", relation.toString());
		    relatedTreeProperties.put("label", loader.getTermLabels().get(relation));
		    relatedTreeProperties.put("ontology_name", loader.getOntologyName());
		    relatedTreeProperties.put("__type__", "RelatedTree");

		    for (IRI relatedTerm : relatedterms.get(relation)) {
		        Long relatedNode =  NodeCreator.getOrCreateNode(inserter, nodeMap,loader, relatedTerm, 
		        		new LinkedList<Label>(Arrays.asList(nodeLabel, nodeOntologyLabel, _nodeLabel)));
		        // create local relationship
		        inserter.createRelationship(node, relatedNode, related, relatedProperties);
		        // add a hierarchical relation if it is a related parent term
		        if (!loader.getRelatedParentTerms(classIri).isEmpty()) {
		            if (loader.getRelatedParentTerms(classIri).containsKey(relation)) {
		                inserter.createRelationship(node, relatedNode, treeRelation, 
		                		relatedTreeProperties);
		            }
		        }
		    }

		}
	}


    @Override
    public void createIndex(Collection<OntologyLoader> loaders) throws IndexingException {
        // store a local cache of new local term nodes
        Map<String, Long> classNodeMap = new HashMap<>();
        Map<String, Long> propertyNodeMap = new HashMap<>();
        Map<String, Long> individualNodeMap = new HashMap<>();

        // store a local cache of merged term nodes
        Map<String, Long> mergedNodeMap = new HashMap<>();


        for (OntologyLoader loader : loaders) {

            BatchInserter inserter = getBatchIndexer(loader.getOntologyName());
            
            setOntologyLabel(loader.getOntologyName());
            // index classes
            indexClasses(inserter, loader, classNodeMap, mergedNodeMap);
            // index properties
            indexProperties(inserter, loader, propertyNodeMap, mergedNodeMap);
            // index individuals
            // avoid duplicating Thing in the graph
            if (classNodeMap.containsKey("http://www.w3.org/2002/07/owl#Thing")) {
                individualNodeMap.put("http://www.w3.org/2002/07/owl#Thing", classNodeMap.get("http://www.w3.org/2002/07/owl#Thing"));
            }
            indexIndividuals(inserter, loader, individualNodeMap, mergedNodeMap, classNodeMap);

            OLSBatchIndexerCreator.createSchemaIndexes(inserter);

            getLogger().info("Neo4j index for " + loader.getAllClasses().size() + " classes complete");
            getLogger().info("Neo4j index for " + loader.getAllObjectPropertyIRIs().size() + " object properties complete");
            getLogger().info("Neo4j index for " + loader.getAllAnnotationPropertyIRIs().size() + " annotation  properties complete");
            getLogger().info("Neo4j index for " + loader.getAllDataPropertyIRIs().size() + " data properties complete");
            getLogger().info("Neo4j index for " + loader.getAllIndividualIRIs().size() + " individuals complete");

            indexProvider.shutdown();
            inserter.shutdown();
        }


        // check indexes online

//        db.shutdown();
        db = getGraphDatabase();

        Transaction tx = db.beginTx();

        try {
            for (IndexDefinition indexDefinition : db.schema().getIndexes()) {
                Schema.IndexState state = db.schema().getIndexState(indexDefinition);
                if (state.equals(Schema.IndexState.POPULATING)) {
                    logger.warn("One of the indexes has failed, attempting to rebuild: " + indexDefinition.getLabel().name());
                    try {
                        db.schema().awaitIndexOnline(indexDefinition, 10, TimeUnit.MINUTES);
                    } catch (IllegalStateException e) {
                        throw new IndexingException("Building Neo4j index failed as the schema index didn't finish in time", e);
                    }
                }
                else if (state.equals(Schema.IndexState.FAILED)) {
                    throw new Exception("Index failed: " + indexDefinition.getLabel().name());
                }
            }

            tx.success();
        }
        catch (Exception e) {
        	logger.debug(e.getMessage(), e);
            tx.failure();
            throw new IndexingException("Building Neo4j index failed as the schema index creation failed", e);
        }
        finally {
            tx.close();
            db.shutdown();
        }
    }

    protected GraphDatabaseService getGraphDatabase () {
   		return new GraphDatabaseFactory().newEmbeddedDatabase(neo4jConfiguration.getNeo4JPath());
    }

    public void dropIndex(OntologyLoader loader) throws IndexingException {
        dropIndex(loader.getOntologyName());
    }

    @Override
    public void dropIndex(String ontologyId) throws IndexingException {

        // shutdown any autowired graph dbs for batch loading
        db.shutdown();
        db = getGraphDatabase();

        deleteNodes(ontologyId);

        db.shutdown();


    }

    private void deleteNodes(String ontologyName) {

        int count = getNodeCount(
                "match (n:" + ontologyName.toUpperCase() + ")-[r]->() return count(r) as count", ontologyName);

        for (int x = 0; x < count ; x +=DELETE_SIZE) {

            Transaction tx = db.beginTx();

            try {
                String cypherDelete =
                        "match (n:" + ontologyName.toUpperCase() + ")-[r]->() with r limit " + 
                        		DELETE_SIZE + " delete r";
                getLogger().info("executing delete: " + cypherDelete);
                Result result = db.execute(cypherDelete);
                getLogger().info(result.resultAsString());

                tx.success();
            } catch (Exception e) {
                tx.failure();
                throw new IndexingException("Couldn't drop: " + ontologyName, e);
            }
            tx.close();
        }

        count = getNodeCount(
                "match (n:" + ontologyName.toUpperCase() + ") return count(n) as count", ontologyName
        );
        for (int x = 0; x < count ; x +=DELETE_SIZE) {

            Transaction tx = db.beginTx();

            try {
                String cypherDelete =
                        "match (n:" + ontologyName.toUpperCase() + ") with n limit " + DELETE_SIZE + " delete n";
                getLogger().info("executing delete: " + cypherDelete);
                Result result = db.execute(cypherDelete);
                getLogger().info(result.resultAsString());

                tx.success();
            } catch (Exception e) {
                tx.failure();
                throw new IndexingException("Couldn't drop: " + ontologyName, e);
            }
            tx.close();
        }
    }

    private int getNodeCount(String nodeCountCypher, String ontologyName) {

        Long count;

        Transaction tx = db.beginTx();
        try {
            getLogger().debug("executing count: " + nodeCountCypher);
            Result result = db.execute(nodeCountCypher);

            count = (Long) result.next().get("count");
            getLogger().debug("query count " + count);
            tx.success();
        }
        catch (Exception e) {
            tx.failure();
            throw new IndexingException("Couldn't count: " + ontologyName, e);
        }
        finally {
            tx.close();
        }
        return count.intValue();
    }


    @Override
    public void createIndex(OntologyLoader loader) throws IndexingException {
        createIndex(Collections.singleton(loader));
    }
}
