package uk.ac.ebi.spot.ols.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Collection;
import java.util.HashSet;

/**
 * @author Simon Jupp
 * @date 04/03/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@Document(collection = "olstree")
public class TreeDocument {

    @Id
    private String id;


    // short form
    private String uri;
    private String shortFrom;
    private String ontologyName;

    // Collection<String> properties;
    // Collection<String> hierarchical;

    private Collection<TreeNode> pathToRoot;
    private Collection<TreeNode> ancestralTree;
    private Collection<TreeNode> decendantTree;

    public TreeDocument(String id) {
        this.id = id;
        this.ancestralTree = new HashSet<>();
        this.decendantTree = new HashSet<>();
        this.pathToRoot = new HashSet<>();
    }

    public String getId() {
        return id;
    }

    public Collection<TreeNode> getDecendantTree() {

        return decendantTree;
    }


    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getShortFrom() {
        return shortFrom;
    }

    public void setShortFrom(String shortFrom) {
        this.shortFrom = shortFrom;
    }

    public String getOntologyName() {
        return ontologyName;
    }

    public void setOntologyName(String ontologyName) {
        this.ontologyName = ontologyName;
    }

    public void setDecendantTree(Collection<TreeNode> decendantTree) {
        this.decendantTree = decendantTree;
    }

    public Collection<TreeNode> getAncestralTree() {
        return ancestralTree;
    }

    public void setAncestralTree(Collection<TreeNode> ancestralTree) {
        this.ancestralTree = ancestralTree;
    }

    public Collection<TreeNode> getPathToRoot() {
        return pathToRoot;
    }

    public void setPathToRoot(Collection<TreeNode> pathToRoot) {
        this.pathToRoot = pathToRoot;
    }


}
