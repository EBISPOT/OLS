package uk.ac.ebi.spot.indexer;

import org.codehaus.jackson.map.ObjectMapper;
import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.loader.OntologyLoader;
import uk.ac.ebi.spot.neo4j.model.TreeDocument;
import uk.ac.ebi.spot.neo4j.model.TreeNode;
import uk.ac.ebi.spot.ols.exception.IndexingException;
import uk.ac.ebi.spot.ols.model.OntologyIndexer;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Simon Jupp
 * @date 04/03/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@Component

public class MongoTreeIndexer implements OntologyIndexer{

    private Logger log = LoggerFactory.getLogger(getClass());

    public Logger getLog() {
        return log;
    }

    @Override
    public void createIndex(Collection<OntologyLoader> loaders) throws IndexingException {

        for (OntologyLoader loader : loaders) {
            loader.getAllClasses();
            Instant start = Instant.now();

            Collection<TreeDocument> treeDocuments =
                    loader.getAllClasses().stream().map((iri) -> {

//                        System.out.println("processing: " + iri + " -> " + loader.getTermLabels().get(iri));
                        Map<IRI, TreeNode> roots = new HashMap<IRI, TreeNode>();
                        if (iri.toString().equals("http://purl.obolibrary.org/obo/GO_0017148")) {
                            System.out.println("i'm here");

                        Collection<String> relations = new HashSet<String>();
                        Map<IRI, TreeNode> allNodes = new HashMap<IRI, TreeNode>();
                        TreeNode leafNodes = pathToRoot(loader, iri, roots, relations, allNodes);

                        // add sibling to leaf nodes
//                        for (TreeNode node : leafNodes) {
//
//                            for (Collection<IRI> parents: getParentNode(loader, IRI.create(node.getUri()) ) {
//
//                            }
//
//                        }
                        }
//                        createAncestralTree(loader, iri, roots, allNodes);
                        TreeDocument document = new TreeDocument(loader.getOntologyName() + ":" + iri);
                        document.setAncestralTree(roots.values());

                        ObjectMapper mapper = new ObjectMapper();
                        try {
                            getLog().info(mapper.writeValueAsString(document)) ;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        return document;

                    }).collect(Collectors.toSet());

            Instant end = Instant.now();
            getLog().info(loader.getOntologyName() + " tree indexed in " + Duration.between(start, end));


        }


    }

//    private void createAncestralTree(OntologyLoader loader, IRI iri, Collection<TreeNode> roots, Map<IRI, TreeNode> allNodes) {
//
//        pathToRoot(loader, iri, roots, allNodes);
//
//
//        // get the direct parents
//        Collection<TreeNode> parentNodes = getParentNode(loader, iri, allNodes);
//
//        // stop now if we start with a root term
//        if (parentNodes.isEmpty()) {
//            TreeNode node = new TreeNode(iri.toString(), "is-a", loader.getTermLabels().get(iri), loader.getDirectChildTerms().containsKey(iri));
//            allNodes.put(iri, node);
//            roots.add(node);
//        }
//        else {
//            // for each parent node get direct children, these will be the siblings of @iri
//            for (TreeNode node : parentNodes) {
//                Collection<TreeNode> parentChildren = getChildNodes(loader, IRI.create(node.getUri()), allNodes);
//                node.getChildren().addAll(parentChildren);
//                pathToRoot(loader, node, roots, allNodes);
//            }
//        }
//    }

//    private void pathToRoot(OntologyLoader loader, TreeNode node, Collection<TreeNode> roots, Map<IRI, TreeNode> allNodes) {
//        Collection<TreeNode> parentNodes = getParentNode(loader, IRI.create(node.getUri()), allNodes);
//        // if no more parents, we've hit a root
//        if (parentNodes.isEmpty()) {
//            roots.add(node);
//        }
//        else {
//            for (TreeNode parents : parentNodes) {
//                parents.getChildren().add(node);
//                pathToRoot(loader, parents, roots, allNodes);
//            }
//        }
//    }

    private TreeNode pathToRoot(OntologyLoader loader, IRI iri, Map<IRI, TreeNode> roots, Collection<String> relations, Map<IRI, TreeNode> allNodes) {

//        System.out.println("walking: " + iri + " -> " + loader.getTermLabels().get(iri));
        if (allNodes.containsKey(iri)) {
            return allNodes.get(iri);
        }
        TreeNode thisNode = new TreeNode(iri.toString(), loader.getTermLabels().get(iri), loader.getDirectChildTerms().containsKey(iri));
        allNodes.put(iri, thisNode);
        Collection<IRI> parentIRIs = getParentNode(loader, iri);
        Map<IRI, Collection<IRI>> relatedParentIRIs = getRelatedParents(loader, iri);

        // if no more parents, we're at a root
        if (parentIRIs.isEmpty() && relatedParentIRIs.isEmpty()) {
            if (!roots.containsKey(iri)) {
                roots.put(iri, thisNode);
            }
        }
        else {
            for (IRI parent : parentIRIs) {
                TreeNode node = pathToRoot(loader, parent, roots,relations, allNodes);
                node.getChildren().add(thisNode);
            }

            // get related nodes
            if (!relatedParentIRIs.isEmpty()) {
                for (IRI relation : relatedParentIRIs.keySet()) {
                    // get relation label
                    String relationLabel =  loader.getTermLabels().get(relation);
                    // collect all labels
                    relations.add(relationLabel);

                    // get related nodes
                    for (IRI relatedIri : relatedParentIRIs.get(relation)) {
                        TreeNode relatedNode = pathToRoot(loader, relatedIri, roots, relations, allNodes);

                        if (!relatedNode.getRelatedNodes().containsKey(relationLabel)) {
                            relatedNode.getRelatedNodes().put(relationLabel, new HashSet<>());
                        }
                        relatedNode.getRelatedNodes().get(relationLabel).add(thisNode);
                    }
                }
            }
        }
        return thisNode;
    }

//    private Collection<TreeNode> getChildNodes(OntologyLoader loader, IRI iri,  Map<IRI, TreeNode> allNodes) {
//        Collection<TreeNode> children = new HashSet<>();
//        if (loader.getDirectChildTerms().containsKey(iri)) {
//            for (IRI childIri : loader.getDirectChildTerms().get(iri)) {
//
//                if (!allNodes.containsKey(childIri)) {
//                    TreeNode node = new TreeNode(childIri.toString(), "is-a", loader.getTermLabels().get(childIri), loader.getDirectChildTerms().containsKey(childIri));
//                    allNodes.put(childIri, node);
//                }
//                children.add(allNodes.get(childIri));
//            }
//        }
//        return children;
//    }
//
//    private Collection<TreeNode> getParentNode (OntologyLoader loader, IRI iri,  Map<IRI, TreeNode> allNodes) {
//        Collection<TreeNode> parents = new HashSet<>();
//        if (loader.getDirectParentTerms().containsKey(iri)) {
//            for (IRI parentIri : loader.getDirectParentTerms().get(iri)) {
//
//                if (!allNodes.containsKey(parentIri)) {
//                    TreeNode node = new TreeNode(parentIri.toString(), "is-a", loader.getTermLabels().get(parentIri), loader.getDirectChildTerms().containsKey(parentIri));
//                    allNodes.put(parentIri, node);
//                }
//                parents.add(allNodes.get(parentIri));
//            }
//        }
//
//        Map<IRI, Collection<IRI>> relatedTerms = loader.getRelatedTerms(iri);
//        for (IRI relationIri : relatedTerms.keySet()) {
//            String relationLabel = loader.getTermLabels().get(relationIri);
//            for (IRI relatedIri : relatedTerms.get(relationIri))  {
//                if (!allNodes.containsKey(relatedIri)) {
//                    TreeNode node = new TreeNode(relatedIri.toString(), relationLabel, loader.getTermLabels().get(relatedIri), loader.getDirectChildTerms().containsKey(relatedIri));
//                    allNodes.put(relatedIri, node);
//                }
//                parents.add(allNodes.get(relatedIri));
//            }
//        }
//        return parents;
//    }

    private Collection<IRI> getParentNode (OntologyLoader loader, IRI iri) {
        Collection<IRI> parents = new HashSet<>();
        if (loader.getDirectParentTerms().containsKey(iri)) {
            for (IRI parentIri : loader.getDirectParentTerms().get(iri)) {
                parents.add(parentIri);
            }
        }
        return parents;
    }

    private Map<IRI, Collection<IRI>> getRelatedParents (OntologyLoader loader, IRI iri) {
        Map<IRI, Collection<IRI>> relatedParents = new HashMap<>();

        Map<IRI, Collection<IRI>> relatedTerms = loader.getRelatedTerms(iri);
        for (IRI relationIri : relatedTerms.keySet()) {
            for (IRI relatedIri : relatedTerms.get(relationIri))  {
                if (!relatedParents.containsKey(relationIri)) {
                    relatedParents.put(relationIri, new HashSet<>());
                }
                relatedParents.get(relationIri).add(relatedIri);
            }
        }
        return relatedParents;
    }

    @Override
    public void createIndex(OntologyLoader loader) throws IndexingException {
        createIndex(Collections.singleton(loader));
    }

    @Override
    public void dropIndex(OntologyLoader loader) throws IndexingException {

    }
}
