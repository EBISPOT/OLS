package uk.ac.ebi.spot.ols.loader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.PackageVersion;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.graphdb.schema.Schema;
import org.neo4j.helpers.collection.MapUtil;
import org.neo4j.index.lucene.unsafe.batchinsert.LuceneBatchInserterIndexProvider;
import org.neo4j.kernel.DefaultFileSystemAbstraction;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserterIndex;
import org.neo4j.unsafe.batchinsert.BatchInserterIndexProvider;
import org.neo4j.unsafe.batchinsert.BatchInserters;
import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.GraphDatabase;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.ols.config.OlsNeo4jConfiguration;
import uk.ac.ebi.spot.ols.exception.IndexingException;
import uk.ac.ebi.spot.ols.exception.OntologyLoadingException;
import uk.ac.ebi.spot.ols.model.OntologyIndexer;
import uk.ac.ebi.spot.ols.util.OBODefinitionCitation;
import uk.ac.ebi.spot.ols.util.OBOSynonym;
import uk.ac.ebi.spot.ols.util.OBOXref;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author Simon Jupp
 * @date 17/06/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@Component
public class BatchNeo4JIndexer implements OntologyIndexer {
    private Logger log = LoggerFactory.getLogger(getClass());
    private BatchInserterIndex entities;
    private BatchInserterIndexProvider indexProvider;

    public Logger getLog() {
        return log;
    }

    @Autowired
    private GraphDatabaseService db;

    private static Label obsoleteLabel = DynamicLabel.label("Obsolete");
    // this represents a unique term
    private static Label mergedClassLabel = DynamicLabel.label("Resource");
    // define a node label for ontology terms
    private static Label nodeLabel = DynamicLabel.label("Class");
    private static Label relationLabel = DynamicLabel.label("Property");
    private static Label instanceLabel = DynamicLabel.label("Individual");
    private static Label rootLabel = DynamicLabel.label("Root");
    private static Label _nodeLabel = DynamicLabel.label("_Class");
    private static Label _relationLabel = DynamicLabel.label("_Property");
    private static Label _instanceLabel = DynamicLabel.label("_Individual");

    // create relationship types
    private static RelationshipType refersTo = DynamicRelationshipType.withName("REFERSTO");
    private static RelationshipType isa = DynamicRelationshipType.withName("SUBCLASSOF");
    private static RelationshipType subpropertyof = DynamicRelationshipType.withName("SUBPROPERTYOF");
    private static RelationshipType typeOf = DynamicRelationshipType.withName("INSTANCEOF");
    private static RelationshipType related = DynamicRelationshipType.withName("Related");
    private static RelationshipType relatedIndividual = DynamicRelationshipType.withName("RelatedIndividual");
    private static RelationshipType treeRelation = DynamicRelationshipType.withName("RelatedTree");

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

    private Long getOrCreateNode(BatchInserter inserter, Map<String, Long> nodeMap, OntologyLoader loader, IRI classIri, Label ... nodeLabel) {

        if (!nodeMap.containsKey(classIri.toString())) {
            Map<String, Object> properties = new HashMap<>();
            properties.put("olsId", loader.getOntologyName().toLowerCase() + ":" + classIri.toString());
            properties.put("iri", classIri.toString());
            if (classIri.toString().equals("http://www.w3.org/2002/07/owl#Thing")) {
                properties.put("label", "Thing");
                properties.put("has_children", true );

            }
            else if (classIri.toString().equals("http://www.w3.org/2002/07/owl#TopObjectProperty")) {
                properties.put("label", "TopObjectProperty");
                properties.put("has_children", true );
            }
            else {
                if (!loader.getTermLabels().containsKey(classIri)) {
                    properties.put("label", loader.getShortForm(classIri));
                } else  {
                    properties.put("label", loader.getTermLabels().get(classIri));
                }
            }

            properties.put("ontology_name", loader.getOntologyName());
            properties.put("ontology_prefix", loader.getPreferredPrefix());
            properties.put("ontology_iri", loader.getOntologyIRI().toString());
            properties.put("is_obsolete", loader.isObsoleteTerm(classIri));
            properties.put("is_defining_ontology", loader.isLocalTerm(classIri));
            // && loader.getRelatedChildTerms(classIri).isEmpty()

            // if it's an individual, it can't have children, but it may be punned URI to a class to we need to check
            List<Label> labels = Lists.newArrayList(nodeLabel);
            if (labels.contains(instanceLabel)) {
                properties.put("has_children", false );
            }
            else  {
                properties.put("has_children", (!loader.getDirectChildTerms(classIri).isEmpty() || !loader.getRelatedChildTerms(classIri).isEmpty()) );
            }
            properties.put("is_root", loader.getDirectParentTerms(classIri).isEmpty() && loader.getRelatedParentTerms(classIri).isEmpty());

            // add shortforms
            if (loader.getShortForm(classIri) != null) {
                properties.put("short_form", loader.getShortForm(classIri));
            }

            // add oboid
            if (loader.getOboId(classIri) != null) {
                properties.put("obo_id", loader.getOboId(classIri));
            }

            // add synonyms
            if (loader.getTermSynonyms().containsKey(classIri)) {
                String [] synonyms = loader.getTermSynonyms().get(classIri).toArray(new String [loader.getTermSynonyms().get(classIri).size()]);
                properties.put("synonym", synonyms);
            }

            // add subsets
            if (!loader.getSubsets(classIri).isEmpty()) {
                String [] subsets = loader.getSubsets(classIri).toArray(new String [loader.getSubsets(classIri).size()]);
                properties.put("in_subset", subsets);
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

            if (loader.getTermReplacedBy(classIri) != null) {
                properties.put("term_replaced_by", loader.getTermReplacedBy(classIri).toString());
            }

            ObjectMapper mapper = new ObjectMapper();
            Collection<OBODefinitionCitation> definitionCitations = loader.getOBODefinitionCitations(classIri);
            if (!definitionCitations.isEmpty()) {
                List<String> defs = new ArrayList<>();
                for (OBODefinitionCitation citation : definitionCitations) {
                    try {
                        defs.add(mapper.writeValueAsString(citation));
                    } catch (JsonProcessingException e) {

                    }
                }
                properties.put("obo_definition_citation",  defs.toArray(new String [defs.size()]));
            }

            Collection<OBOSynonym> oboSynonyms = loader.getOBOSynonyms(classIri);
            if (!oboSynonyms.isEmpty()) {
                List<String> syns = new ArrayList<>();
                for (OBOSynonym synonym : oboSynonyms) {
                    try {
                        syns.add(mapper.writeValueAsString(synonym));
                    } catch (JsonProcessingException e) {
                    }
                }
                properties.put("obo_synonym", syns.toArray(new String [syns.size()]));
            }

            Collection<OBOXref> xrefs = loader.getOBOXrefs(classIri);
            if (!xrefs.isEmpty()) {
                List<String> refs = new ArrayList<>();
                for (OBOXref xref : xrefs) {
                    try {
                        refs.add(mapper.writeValueAsString(xref));
                    } catch (JsonProcessingException e) {
                    }
                }
                properties.put("obo_xref", refs.toArray(new String [refs.size()]));
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


            nodeMap.put(classIri.toString(), classNode);
        }
        return nodeMap.get(classIri.toString());
    }

    private Long getOrCreateMergedNode(BatchInserter inserter, Map<String, Long> mergedNodeMap, OntologyLoader loader, IRI classIri, Label ... nodeLabel) {

        if (!mergedNodeMap.containsKey(classIri.toString())) {

            // link to merged node
            IndexHits<Long> hits = entities.get("iri", classIri);
            if (hits.size() == 0) {
                Map<String, Object> properties = new HashMap<>();
                properties.put("iri", classIri.toString());
                properties.put("label", loader.getTermLabels().get(classIri));

                Long hit = inserter.createNode(properties, nodeLabel);
                entities.add(hit, properties);
                mergedNodeMap.put(classIri.toString(), hit);
            }
            else {
                if (hits.size() > 1) {
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
    private Label getNodeOntologyLabel () {
        return nodeOntologyLabel;
    }

    private BatchInserter getBatchIndexer (String ontologyName) {
        BatchInserter inserter = null;

        // connect to the database instance
        File file = new File(neo4jConfiguration.getNeo4JPath());
        inserter = BatchInserters.inserter(
                file.getAbsolutePath(),
                new DefaultFileSystemAbstraction());

        createSchemaIndexes(inserter);

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

        entities = getBatchInserterIndex(getIndexProvider(inserter));

        return inserter;

    }

    private void createSchemaIndexes(BatchInserter inserter) {
        // create schema for classes if not exists
        createSchemaIndexIfNotExists(inserter, mergedClassLabel, "iri");
        createSchemaIndexIfNotExists(inserter, nodeLabel, "olsId");
        createSchemaIndexIfNotExists(inserter, nodeLabel, "iri");
        createSchemaIndexIfNotExists(inserter, nodeLabel, "short_form");
        createSchemaIndexIfNotExists(inserter, nodeLabel, "obo_id");
        createSchemaIndexIfNotExists(inserter, nodeLabel, "ontology_name");

        // create schema for properties if not exists
        createSchemaIndexIfNotExists(inserter, relationLabel, "olsId");
        createSchemaIndexIfNotExists(inserter, relationLabel, "iri");
        createSchemaIndexIfNotExists(inserter, relationLabel, "short_form");
        createSchemaIndexIfNotExists(inserter, relationLabel, "obo_id");
        createSchemaIndexIfNotExists(inserter, relationLabel, "ontology_name");

        // create schema for instances if not exists
        createSchemaIndexIfNotExists(inserter, instanceLabel, "olsId");
        createSchemaIndexIfNotExists(inserter, instanceLabel, "iri");
        createSchemaIndexIfNotExists(inserter, instanceLabel, "short_form");
        createSchemaIndexIfNotExists(inserter, instanceLabel, "obo_id");
        createSchemaIndexIfNotExists(inserter, instanceLabel, "ontology_name");

    }

    private BatchInserterIndexProvider getIndexProvider (BatchInserter inserter) {

        // index for looking up merged classes
        indexProvider =
                new LuceneBatchInserterIndexProvider(inserter);
        return indexProvider;
    }

    private BatchInserterIndex getBatchInserterIndex(BatchInserterIndexProvider indexProvider) {
        BatchInserterIndex entites =
                indexProvider.nodeIndex("Resource", MapUtil.stringMap("type", "exact"));
        entites.setCacheCapacity("iri", 1000000);
        return entites;
    }

    private void indexProperties(BatchInserter inserter, OntologyLoader loader, Map<String, Long> nodeMap, Map<String, Long> mergedNodeMap) {

        // index relations
        Collection<IRI> allRelations = loader.getAllObjectPropertyIRIs();
        allRelations.addAll(loader.getAllDataPropertyIRIs());
        allRelations.addAll(loader.getAllAnnotationPropertyIRIs());
        getLog().debug("Creating Neo4j index for " + allRelations.size() + " properties");

        for (IRI objectPropertyIri : allRelations) {
            Long node = getOrCreateNode(inserter, nodeMap,loader, objectPropertyIri, relationLabel, _relationLabel,nodeOntologyLabel);
            Long mergedNode = getOrCreateMergedNode(inserter, mergedNodeMap, loader, objectPropertyIri, mergedClassLabel);

            // add refers link
            inserter.createRelationship( node, mergedNode, refersTo, null);

            // add parent nodes
            if (!loader.getDirectParentTerms(objectPropertyIri).isEmpty()) {
                for (IRI parent : loader.getDirectParentTerms().get(objectPropertyIri)) {
                    Long parentNode =  getOrCreateNode(inserter, nodeMap,loader, parent,relationLabel, _relationLabel,nodeOntologyLabel);
                    // create local relationship
                    inserter.createRelationship(node, parentNode, subpropertyof, subPropertyProperties);
                }
            }
            else {
                Long rootProperty = getOrCreateNode(inserter, nodeMap,loader, IRI.create("http://www.w3.org/2002/07/owl#TopObjectProperty"), relationLabel, _relationLabel,nodeOntologyLabel, rootLabel);
                inserter.createRelationship( node, rootProperty, subpropertyof, subPropertyProperties);
            }
        }
    }

    private void indexIndividuals(BatchInserter inserter, OntologyLoader loader, Map<String, Long> nodeMap, Map<String, Long> mergedNodeMap, Map<String, Long> classNodeMap) {
        getLog().debug("Creating Neo4j index for " + loader.getAllIndividualIRIs().size() + " individuals");

        for (IRI individualIri : loader.getAllIndividualIRIs()) {

            Long node = getOrCreateNode(inserter, nodeMap,loader, individualIri, instanceLabel, _instanceLabel,nodeOntologyLabel);
            Long mergedNode = getOrCreateMergedNode(inserter, mergedNodeMap, loader, individualIri, mergedClassLabel);

            // add refers link
            inserter.createRelationship( node, mergedNode, refersTo, null);

            // add parent nodes
            if (loader.getDirectTypes().containsKey(individualIri)) {
                for (IRI parent : loader.getDirectTypes().get(individualIri)) {
                    Long parentNode =  getOrCreateNode(inserter, classNodeMap,loader, parent, nodeLabel,nodeOntologyLabel,  _nodeLabel);
                    // create local relationship
                    inserter.createRelationship(node, parentNode, typeOf, rdfTypeProperties);
                }
            }
            else {
                Long defaultType = getOrCreateNode(inserter, nodeMap,loader, IRI.create("http://www.w3.org/2002/07/owl#Thing"),  nodeLabel,nodeOntologyLabel,  _nodeLabel, rootLabel);
                inserter.createRelationship( node, defaultType, typeOf, rdfTypeProperties);
            }

            // add relations
            indexRelations(node, loader.getRelatedIndividuals(individualIri),inserter,loader,nodeMap, instanceLabel,nodeOntologyLabel, _instanceLabel);
            indexRelations(node, loader.getRelatedClassesToIndividual(individualIri),inserter,loader,classNodeMap, nodeLabel,nodeOntologyLabel, _nodeLabel);
        }
    }

    private void indexRelations(Long node, Map<IRI, Collection<IRI>> relatedIndividuals, BatchInserter inserter, OntologyLoader loader, Map<String, Long> nodeMap, Label... nodeLabels) {
        for (IRI relation : relatedIndividuals.keySet()) {
            Map<String, Object> relatedProperties = new HashMap<>();
            relatedProperties.put("uri", relation.toString());
            relatedProperties.put("label", loader.getTermLabels().get(relation));
            relatedProperties.put("ontology_name", loader.getOntologyName());
            relatedProperties.put("__type__", "Related");

            for (IRI relatedTerm : relatedIndividuals.get(relation)) {
                //TODO review right parameters
                Long relatedNode =  getOrCreateNode(inserter, nodeMap,loader, relatedTerm, nodeLabels);
                inserter.createRelationship( node, relatedNode, related, relatedProperties);
            }

        }
    }

    private void indexRelatedIndividuals(Long node, Map<IRI, Collection<IRI>> relatedIndividuals, BatchInserter inserter, OntologyLoader loader, Map<String, Long> nodeMap, Label... nodeLabels) {
        for (IRI relation : relatedIndividuals.keySet()) {
            Map<String, Object> relatedProperties = new HashMap<>();
            relatedProperties.put("uri", relation.toString());
            relatedProperties.put("label", loader.getTermLabels().get(relation));
            relatedProperties.put("ontology_name", loader.getOntologyName());
            relatedProperties.put("__type__", "RelatedIndividual");

            for (IRI relatedTerm : relatedIndividuals.get(relation)) {
                //TODO review right parameters
                Long relatedNode =  getOrCreateNode(inserter, nodeMap,loader, relatedTerm, nodeLabels);
                inserter.createRelationship( node, relatedNode, relatedIndividual, relatedProperties);
            }

        }
    }

    private void indexClasses(BatchInserter inserter, OntologyLoader loader, Map<String, Long> nodeMap, Map<String, Long> mergedNodeMap) {
        getLog().debug("Creating Neo4j index for " + loader.getAllClasses().size() + " classes");

//        int counter = 0;

        for (IRI classIri : loader.getAllClasses()) {

            Long node = getOrCreateNode(inserter, nodeMap,loader, classIri, nodeLabel,nodeOntologyLabel,  _nodeLabel);

            Long mergedNode = getOrCreateMergedNode(inserter, mergedNodeMap, loader, classIri, mergedClassLabel);

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
            else if (loader.getRelatedParentTerms(classIri).isEmpty()) {
                Long thing = getOrCreateNode(inserter, nodeMap,loader, IRI.create("http://www.w3.org/2002/07/owl#Thing"), nodeLabel,nodeOntologyLabel, _nodeLabel, rootLabel);
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
                relatedProperties.put("ontology_name", loader.getOntologyName());
                relatedProperties.put("__type__", "Related");

                Map<String, Object> relatedTreeProperties = new HashMap<>();
                relatedTreeProperties.put("uri", relation.toString());
                relatedTreeProperties.put("label", loader.getTermLabels().get(relation));
                relatedTreeProperties.put("ontology_name", loader.getOntologyName());
                relatedTreeProperties.put("__type__", "RelatedTree");

                for (IRI relatedTerm : relatedterms.get(relation)) {
                    Long relatedNode =  getOrCreateNode(inserter, nodeMap,loader, relatedTerm, nodeLabel,nodeOntologyLabel, _nodeLabel);
                    // create local relationship
                    inserter.createRelationship( node, relatedNode, related, relatedProperties);
                    // add a hierarchical relation if it is a related parent term
                    if (!loader.getRelatedParentTerms(classIri).isEmpty()) {
                        if (loader.getRelatedParentTerms(classIri).containsKey(relation)) {
                            inserter.createRelationship( node, relatedNode, treeRelation, relatedTreeProperties);
                        }
                    }
                }

            }

            //Add relationships of the form A sub R some {a}
            indexRelatedIndividuals(node, loader.getRelatedIndividualsToClass(classIri),inserter,loader,nodeMap, instanceLabel,nodeOntologyLabel, _instanceLabel);

//            if (counter == BATCH_SIZE) {
//                inserter = restartIndexer(inserter, loader.getOntologyName());
//                counter = 0;
//            }
//            counter++;

        }

//        indexProvider.shutdown();
//        getLog().debug("Neo4j class index provider shutdown");
//        inserter.shutdown();
//        getLog().debug("Neo4j class batch indexer shutdown");

    }

//    private BatchInserter   restartIndexer(BatchInserter inserter, String ontologyName) {
//        indexProvider.shutdown();
//        getLog().debug(BATCH_SIZE  + " reached Neo4j index provider restarted");
//        inserter.shutdown();
//        getLog().debug(BATCH_SIZE  +  " reached Neo4j batch indexer shutdown");
//        inserter = getBatchIndexer(ontologyName);
//        return inserter;
//    }


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

            createSchemaIndexes(inserter);

            getLog().info("Neo4j index for " + loader.getAllClasses().size() + " classes complete");
            getLog().info("Neo4j index for " + loader.getAllObjectPropertyIRIs().size() + " object properties complete");
            getLog().info("Neo4j index for " + loader.getAllAnnotationPropertyIRIs().size() + " annotation  properties complete");
            getLog().info("Neo4j index for " + loader.getAllDataPropertyIRIs().size() + " data properties complete");
            getLog().info("Neo4j index for " + loader.getAllIndividualIRIs().size() + " individuals complete");

            indexProvider.shutdown();
            inserter.shutdown();
        }


        // check indexes online

        db.shutdown();
        db = getGraphDatabase();

        Transaction tx = db.beginTx();

        try {
            for (IndexDefinition indexDefinition : db.schema().getIndexes()) {
                Schema.IndexState state = db.schema().getIndexState(indexDefinition);
                if (state.equals(Schema.IndexState.POPULATING)) {
                    log.warn("One of the indexes has failed, attempting to rebuild: " + indexDefinition.getLabel().name());
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
            tx.failure();
            throw new IndexingException("Building Neo4j index failed as the schema index creation failed", e);
        }
        finally {
            tx.close();
            db.shutdown();
        }
    }

    private GraphDatabaseService getGraphDatabase () {
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
//        deleteRoots(loader.getOntologyName());

        db.shutdown();


    }

    private void deleteRoots(String ontologyName) {

        Transaction tx = db.beginTx();
        try {
            // clear up any roots
            String cypherDeleteRoot = "match (n:Root { ontology_name: '" + ontologyName + "'}) delete n";
            getLog().info("executing delete: " + cypherDeleteRoot);
            Result res2 = db.execute(cypherDeleteRoot);
            getLog().info(res2.resultAsString());


            tx.success();
        }
        catch (Exception e) {
            tx.failure();
            throw new IndexingException("Couldn't drop: " + ontologyName, e);
        }
        finally {
            tx.close();
        }
    }

    private void deleteNodes(String ontologyName) {

        int count = getNodeCount(
                "match (n:" + ontologyName.toUpperCase() + ")-[r]->() return count(r) as count", ontologyName);

        for (int x = 0; x < count ; x +=DELETE_SIZE) {

            Transaction tx = db.beginTx();

            try {
                String cypherDelete =
                        "match (n:" + ontologyName.toUpperCase() + ")-[r]->() with r limit " + DELETE_SIZE + " delete r";
                getLog().info("executing delete: " + cypherDelete);
                Result result = db.execute(cypherDelete);
                getLog().info(result.resultAsString());

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
                getLog().info("executing delete: " + cypherDelete);
                Result result = db.execute(cypherDelete);
                getLog().info(result.resultAsString());

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
            getLog().debug("executing count: " + nodeCountCypher);
            Result result = db.execute(nodeCountCypher);

            count = (Long) result.next().get("count");
            getLog().debug("query count " + count);
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

    private boolean createSchemaIndexIfNotExists(BatchInserter inserter, Label label, String name) {
        try {
            inserter.createDeferredSchemaIndex( label ).on(name).create();
            return true;
        } catch (ConstraintViolationException e) {
//            System.out.println("Couldn't create index for " + name + " as it already exists, continuing...");
        }
        return false;
    }
}
