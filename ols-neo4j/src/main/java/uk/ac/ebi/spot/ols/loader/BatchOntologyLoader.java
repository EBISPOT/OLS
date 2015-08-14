package uk.ac.ebi.spot.ols.loader;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.helpers.collection.MapUtil;
import org.neo4j.index.lucene.unsafe.batchinsert.LuceneBatchInserterIndexProvider;
import org.neo4j.io.fs.FileUtils;
import org.neo4j.kernel.DefaultFileSystemAbstraction;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserterIndex;
import org.neo4j.unsafe.batchinsert.BatchInserterIndexProvider;
import org.neo4j.unsafe.batchinsert.BatchInserters;
import org.semanticweb.owlapi.model.IRI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.ols.config.OlsNeo4jConfiguration;
import uk.ac.ebi.spot.ols.exception.IndexingException;
import uk.ac.ebi.spot.ols.model.OntologyIndexer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

/**
 * @author Simon Jupp
 * @date 17/06/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@Component
public class BatchOntologyLoader implements OntologyIndexer {


    public BatchOntologyLoader() {

    }

    static Long getOrCreateNode(BatchInserter inserter, Map<IRI, Long> nodeMap, OntologyLoader loader, IRI classIri, Label ... nodeLabel) {


        if (!nodeMap.containsKey(classIri)) {
            Map<String, Object> properties = new HashMap<>();
            properties.put("olsId", loader.getOntologyName().toLowerCase() + ":" + classIri.toString());
            properties.put("iri", classIri.toString());
            if (classIri.toString().equals("http://www.w3.org/2002/07/owl#Thing")) {
                properties.put("label", "Thing");
            }
            else {
                properties.put("label", loader.getTermLabels().get(classIri));
            }

            properties.put("ontologyName", loader.getOntologyName());
            properties.put("ontologyIri", loader.getOntologyIRI().toString());
            properties.put("isObsolete", loader.isObsoleteTerm(classIri));
            properties.put("isLocal", loader.isLocalTerm(classIri));
            // && loader.getRelatedChildTerms(classIri).isEmpty()
            properties.put("isLeafNode", loader.getDirectChildTerms(classIri).isEmpty() );
            properties.put("isRoot", loader.getDirectParentTerms(classIri).isEmpty());

            // add shortforms
            if (loader.getShortForm(classIri) != null) {
                properties.put("shortForm", loader.getShortForm(classIri));
            }

            // add oboid
            if (loader.getOboId(classIri) != null) {
                properties.put("oboId", loader.getOboId(classIri));
            }

            // add synonyms
            if (loader.getTermSynonyms().containsKey(classIri)) {
                String [] synonyms = loader.getTermSynonyms().get(classIri).toArray(new String [loader.getTermSynonyms().get(classIri).size()]);
                properties.put("synonym", synonyms);
            }

            // add definitions
            if (loader.getTermDefinitions().containsKey(classIri)) {
                String [] definition = loader.getTermDefinitions().get(classIri).toArray(new String [loader.getTermDefinitions().get(classIri).size()]);
                properties.put("description", definition);
            }

            if (loader.getLogicalSuperClassDescriptions().containsKey(classIri)) {
                String [] descriptions = loader.getLogicalSuperClassDescriptions().get(classIri).toArray(new String [loader.getLogicalSuperClassDescriptions().get(classIri).size()]);
                properties.put("superClassDescription", descriptions);
            }

            if (loader.getLogicalEquivalentClassDescriptions().containsKey(classIri)) {
                String [] descriptions = loader.getLogicalEquivalentClassDescriptions().get(classIri).toArray(new String [loader.getLogicalEquivalentClassDescriptions().get(classIri).size()]);
                properties.put("equivalentClassDescription", descriptions);
            }

            // add annotations
            Map<IRI, Collection<String>> annotations = loader.getAnnotations(classIri);
            if (!annotations.isEmpty()) {
                for (IRI keys : annotations.keySet()) {
                    String annotationLabel = loader.getTermLabels().get(keys);
                    String [] value = annotations.get(keys).toArray(new String [annotations.get(keys).size()]);
                    properties.put("annotation-" + annotationLabel, value);
                }
            }

            long classNode;
            if (loader.isObsoleteTerm(classIri)) {
                Label[] newArr = new Label[nodeLabel.length + 1];
                System.arraycopy(nodeLabel, 0, newArr,0, nodeLabel.length);
                newArr[nodeLabel.length] = obsoleteLabel;
                classNode = inserter.createNode(properties, newArr);
            }
            else {
                classNode = inserter.createNode(properties,nodeLabel);
            }


            nodeMap.put(classIri, classNode);
        }
        return nodeMap.get(classIri);
    }

    static Long getOrCreateMergedNode(BatchInserterIndex entites, BatchInserter inserter, Map<IRI, Long> mergedNodeMap, OntologyLoader loader, IRI classIri, Label ... nodeLabel) {

        if (!mergedNodeMap.containsKey(classIri)) {

            // link to merged node
            IndexHits<Long> hits = entites.get("iri", classIri);
            if (hits.size() == 0) {
                Map<String, Object> properties = new HashMap<>();
                properties.put("iri", classIri.toString());
                properties.put("label", loader.getTermLabels().get(classIri));

                Long hit = inserter.createNode(properties, nodeLabel);
                entites.add(hit, properties);
                mergedNodeMap.put(classIri, hit);
            }
            else {
                if (hits.size() > 1) {
                    System.out.println("WARING: found more than one iri in merged terms for: " + classIri);
                }
                Long mergedNode = hits.getSingle();
                mergedNodeMap.put(classIri, mergedNode);
            }
        }
        return mergedNodeMap.get(classIri);
    }

    @Override
    public void createIndex(Collection<OntologyLoader> loader) throws IndexingException {

    }

    @Autowired
    private GraphDatabaseService db;

    @Override
    public void dropIndex(OntologyLoader loader) throws IndexingException {

        // shutdown any autowired graph dbs for batch loading
        db.shutdown();


        GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(OlsNeo4jConfiguration.getNeo4JPath());

        Transaction tx = db.beginTx();
        try {
            String cypherDelete = "match (n:" + loader.getOntologyName() + ")-[r]->(p) delete n,r";
            System.out.println("executing delete: " + cypherDelete);
            Result result = db.execute(cypherDelete);
            System.out.println(result.resultAsString());
            tx.success();
        }
        catch (Exception e) {
            tx.failure();
            throw new IndexingException("Couldn't drop: " + loader.getOntologyName(), e);
        }
        finally {
            tx.close();
            db.shutdown();
        }

    }

    private static Label obsoleteLabel = DynamicLabel.label("Obsolete");

    @Override
    public void createIndex(OntologyLoader loader) throws IndexingException {
        System.setProperty("entityExpansionLimit", "10000000");

        BatchInserter inserter = null;
        // store a local cache of new local term nodes
        Map<IRI, Long> nodeMap = new HashMap<>();
        // store a local cache of merged term nodes
        Map<IRI, Long> mergedNodeMap = new HashMap<>();
        try
        {
            // this represents a unique term
            Label mergedClassLabel = DynamicLabel.label("Resource");

            // define a node label for ontology terms
            Label nodeLabel = DynamicLabel.label("Class");
            Label nodeOntologyLabel = DynamicLabel.label(loader.getOntologyName());
            Label _nodeLabel = DynamicLabel.label("_Class");

            File file = new File(OlsNeo4jConfiguration.getNeo4JPath());
            inserter = BatchInserters.inserter(
                    file.getAbsolutePath(),
                    new DefaultFileSystemAbstraction());

            createSchemaIndexIfNotExists(inserter, mergedClassLabel, "iri");
            createSchemaIndexIfNotExists(inserter, nodeLabel, "olsId");
            createSchemaIndexIfNotExists(inserter, nodeLabel, "iri");
            createSchemaIndexIfNotExists(inserter, nodeLabel, "shortForm");
            createSchemaIndexIfNotExists(inserter, nodeLabel, "oboId");
            createSchemaIndexIfNotExists(inserter, nodeLabel, "ontologyName");

            // index for looking up merged classes
            BatchInserterIndexProvider indexProvider =
                    new LuceneBatchInserterIndexProvider( inserter );

            BatchInserterIndex entites =
                    indexProvider.nodeIndex("Resource", MapUtil.stringMap("type", "exact") );
            entites.setCacheCapacity( "iri", 1000000 );

            RelationshipType refersTo = DynamicRelationshipType.withName("REFERSTO");
            RelationshipType isa = DynamicRelationshipType.withName("SUBCLASSOF");
            RelationshipType related = DynamicRelationshipType.withName("Related");
            RelationshipType treeRelation = DynamicRelationshipType.withName("RelatedTree");

            Map<String, Object> isaProperties = new HashMap<>();
            isaProperties.put("uri", "http://www.w3.org/2000/01/rdf-schema#subClassOf");
            isaProperties.put("label", "is a");
            isaProperties.put("ontologyName", loader.getOntologyName());
            isaProperties.put("__type__", "SubClassOf");

            for (IRI classIri : loader.getAllClasses()) {

                Long node = getOrCreateNode(inserter, nodeMap,loader, classIri, nodeLabel,nodeOntologyLabel,  _nodeLabel);

                Long mergedNode = getOrCreateMergedNode(entites, inserter, mergedNodeMap, loader, classIri, mergedClassLabel);

                // add refers link
                inserter.createRelationship( node, mergedNode, refersTo, null);

                // add parent nodes
                if (!loader.getDirectParentTerms(classIri).isEmpty()) {
                    for (IRI parent : loader.getDirectParentTerms().get(classIri)) {
                        Long parentNode =  getOrCreateNode(inserter, nodeMap,loader, parent, nodeLabel, nodeOntologyLabel, _nodeLabel);
//                        Long mergedParentNode = getOrCreateMergedNode(entites, inserter, mergedClassLabel, mergedNodeMap, loader, parent);
                        // create local relationship
                        inserter.createRelationship(node, parentNode, isa, isaProperties);
                        // create merged relationship
//                        inserter.createRelationship( mergedNode, mergedParentNode, isa, properties);
                    }
                }
                else {
                    Long thing = getOrCreateNode(inserter, nodeMap,loader, IRI.create("http://www.w3.org/2002/07/owl#Thing"), nodeLabel,nodeOntologyLabel, _nodeLabel);
//                    Long mergedThingNode = getOrCreateMergedNode(entites, inserter, mergedClassLabel, mergedNodeMap, loader, IRI.create("http://www.w3.org/2002/07/owl#Thing"));
//                    RelationshipType isa = DynamicRelationshipType.withName("Parent");
                    inserter.createRelationship( node, thing, isa, isaProperties);
//                    inserter.createRelationship( mergedNode, mergedThingNode, isa, properties);
                }


                // add related nodes
                Map<IRI, Collection<IRI>> relatedterms = loader.getRelatedTerms(classIri);


                for (IRI relation : relatedterms.keySet()) {
                    Map<String, Object> relatedProperties = new HashMap<>();
                    relatedProperties.put("uri", relation.toString());
                    relatedProperties.put("label", loader.getTermLabels().get(relation));
                    relatedProperties.put("ontologyName", loader.getOntologyName());
                    relatedProperties.put("__type__", "Related");

                    for (IRI relatedTerm : relatedterms.get(relation)) {
                        Long relatedNode =  getOrCreateNode(inserter, nodeMap,loader, relatedTerm, nodeLabel,nodeOntologyLabel, _nodeLabel);
                        // create local relationship
                        inserter.createRelationship( node, relatedNode, related, relatedProperties);
                        // add a hierarchical relation if it is a related parent term
                        if (!loader.getRelatedParentTerms(classIri).isEmpty()) {
                            if (loader.getRelatedParentTerms(classIri).containsKey(relation)) {
                                inserter.createRelationship( node, relatedNode, treeRelation, relatedProperties);
                            }
                        }
                    }

                }
            }
            indexProvider.shutdown();
        } catch (Exception e) {
            throw new IndexingException("Neo4 indexing exception", e);
        }
        finally
        {
            if ( inserter != null )
            {
                inserter.shutdown();

            }
        }
    }

    private boolean createSchemaIndexIfNotExists(BatchInserter inserter, Label label, String name) {
        try {
            inserter.createDeferredSchemaIndex( label ).on(name).create();
            return true;
        } catch (ConstraintViolationException e) {
            System.out.println("Couldn't create index for " + name + " as it already exists, continuing...");
        }
        return false;
    }
}
