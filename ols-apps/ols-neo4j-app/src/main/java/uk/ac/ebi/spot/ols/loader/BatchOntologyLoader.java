package uk.ac.ebi.spot.ols.loader;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;
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
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.ols.exception.IndexingException;
import uk.ac.ebi.spot.ols.model.OntologyIndexer;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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
            properties.put("ols_id", loader.getOntologyName().toLowerCase() + ":" + classIri.toString());
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
            properties.put("isLeafNode", loader.getDirectChildTerms(classIri).isEmpty());
            properties.put("isRoot", loader.getDirectParentTerms(classIri).isEmpty());


            // add synonyms
            if (loader.getTermSynonyms().containsKey(classIri)) {
                String [] synonyms = loader.getTermSynonyms().get(classIri).toArray(new String [loader.getTermSynonyms().get(classIri).size()]);
                properties.put("synonym", synonyms);
            }

            // add definitions
            if (loader.getTermDefinitions().containsKey(classIri)) {
                String [] definition = loader.getTermDefinitions().get(classIri).toArray(new String [loader.getTermDefinitions().get(classIri).size()]);
                properties.put("definition", definition);
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

            long classNode = inserter.createNode(properties, nodeLabel);


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

    @Override
    public void dropIndex(OntologyLoader loader) throws IndexingException {

    }

    @Override
    public void createIndex(OntologyLoader loader) throws IndexingException {
        System.setProperty("entityExpansionLimit", "10000000");
        try {
            FileUtils.deleteRecursively(new File("target/batchinserter-example"));
        } catch (IOException e) {
            throw new IndexingException();
        }

        BatchInserter inserter = null;
        try
        {
            inserter = BatchInserters.inserter(
                    new File("target/batchinserter-example").getAbsolutePath(),
                    new DefaultFileSystemAbstraction());

            // this represents a unique term
            Label mergedClassLabel = DynamicLabel.label("MergedClass");
            inserter.createDeferredSchemaIndex( mergedClassLabel ).on( "iri" ).create();

            // index for looking up merged classes
            BatchInserterIndexProvider indexProvider =
                    new LuceneBatchInserterIndexProvider( inserter );


            BatchInserterIndex entites =
                    indexProvider.nodeIndex("MergedClass", MapUtil.stringMap("type", "exact") );
            entites.setCacheCapacity( "iri", 1000000 );

            // store a local cache of new local term nodes
            Map<IRI, Long> nodeMap = new HashMap<>();

            // store a local cache of merged term nodes
            Map<IRI, Long> mergedNodeMap = new HashMap<>();

            // define a node label for ontology terms
            Label nodeLabel = DynamicLabel.label("Class");
            Label _nodeLabel = DynamicLabel.label("_Class");
            inserter.createDeferredSchemaIndex( nodeLabel ).on( "ols_id" ).create();
//            inserter.createDeferredSchemaIndex( nodeLabel ).on( "iri" ).create();
//            inserter.createDeferredSchemaIndex( nodeLabel ).on( "label" ).create();

            RelationshipType refersTo = DynamicRelationshipType.withName("REFERS_TO");
            RelationshipType isa = DynamicRelationshipType.withName("Parent");
            RelationshipType childOf = DynamicRelationshipType.withName("CHILD");
            RelationshipType related = DynamicRelationshipType.withName("RELATED");

            Map<String, Object> isaProperties = new HashMap<>();
            isaProperties.put("uri", "http://www.w3.org/2000/01/rdf-schema#subClassOf");
            isaProperties.put("label", "is a");
            isaProperties.put("ontology_name", loader.getOntologyName());
            isaProperties.put("__type__", "Parent");
            isaProperties.put("type", "Parent");

            Map<String, Object> childOfProperties = new HashMap<>();
            childOfProperties.put("label", "superclass of");
            childOfProperties.put("ontology_name", loader.getOntologyName());
            childOfProperties.put("__type__", "CHILD");
            childOfProperties.put("__rel_types__", "CHILD");

            for (IRI classIri : loader.getAllClasses()) {

                Long node = getOrCreateNode(inserter, nodeMap,loader, classIri, nodeLabel, _nodeLabel);

                Long mergedNode = getOrCreateMergedNode(entites, inserter, mergedNodeMap, loader, classIri, mergedClassLabel);

                // add refers link
                inserter.createRelationship( node, mergedNode, refersTo, null);

                // add parent nodes
                if (!loader.getDirectParentTerms(classIri).isEmpty()) {
                    for (IRI parent : loader.getDirectParentTerms().get(classIri)) {
                        Long parentNode =  getOrCreateNode(inserter, nodeMap,loader, parent, nodeLabel, _nodeLabel);
//                        Long mergedParentNode = getOrCreateMergedNode(entites, inserter, mergedClassLabel, mergedNodeMap, loader, parent);
                        // create local relationship
                        inserter.createRelationship(node, parentNode, isa, isaProperties);
                        // create merged relationship
//                        inserter.createRelationship( mergedNode, mergedParentNode, isa, properties);
                    }
                }
                else {
                    Long thing = getOrCreateNode(inserter, nodeMap,loader, IRI.create("http://www.w3.org/2002/07/owl#Thing"), nodeLabel, _nodeLabel);
//                    Long mergedThingNode = getOrCreateMergedNode(entites, inserter, mergedClassLabel, mergedNodeMap, loader, IRI.create("http://www.w3.org/2002/07/owl#Thing"));
//                    RelationshipType isa = DynamicRelationshipType.withName("Parent");
                    inserter.createRelationship( node, thing, isa, isaProperties);
//                    inserter.createRelationship( mergedNode, mergedThingNode, isa, properties);
                }

                // add child nodes
                if (!loader.getDirectChildTerms(classIri).isEmpty()) {
                    for (IRI parent : loader.getDirectChildTerms().get(classIri)) {
                        Long childNode =  getOrCreateNode(inserter, nodeMap,loader, parent, nodeLabel, _nodeLabel);
                        // create local relationship
                        inserter.createRelationship( node, childNode, childOf, childOfProperties);
                    }
                }


                // add related nodes
                Map<IRI, Collection<IRI>> relatedterms = loader.getRelatedTerms(classIri);

                for (IRI relation : relatedterms.keySet()) {
                    Map<String, Object> relatedProperties = new HashMap<>();
                    relatedProperties.put("uri", relation.toString());
                    relatedProperties.put("label", loader.getTermLabels().get(relation));
                    relatedProperties.put("ontology_name", loader.getOntologyName());
                    relatedProperties.put("__type__", "RELATED");

                    for (IRI relatedTerm : relatedterms.get(relation)) {
                        Long relatedNode =  getOrCreateNode(inserter, nodeMap,loader, relatedTerm, nodeLabel, _nodeLabel);
                        // create local relationship
                        inserter.createRelationship( node, relatedNode, related, relatedProperties);
                    }

                }


            }


            indexProvider.shutdown();

        }
        finally
        {
            if ( inserter != null )
            {
                inserter.shutdown();
            }
        }

    }


}
