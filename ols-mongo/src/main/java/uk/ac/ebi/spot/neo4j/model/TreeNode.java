package uk.ac.ebi.spot.neo4j.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * @author Simon Jupp
 * @date 04/03/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
public class TreeNode {

    public String uri;
//    public String relation;
    public String label;
    public boolean hasChildren;
    public Collection<TreeNode> children;
    public Map<String, Collection<TreeNode>> relatedNodes;

    public TreeNode(String uri, String label, boolean hasChildren) {
        this.uri = uri;
        this.label = label;
//        this.relation = relation;
        this.children = new HashSet<TreeNode>();
        relatedNodes = new HashMap<>();
        this.hasChildren = hasChildren;
    }


    public Map<String, Collection<TreeNode>> getRelatedNodes() {
        return relatedNodes;
    }


    public boolean isHasChildren() {
        return hasChildren;
    }

    public void setHasChildren(boolean hasChildren) {
        this.hasChildren = hasChildren;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Collection<TreeNode> getChildren() {
        return children;
    }

//    public String getRelation() {
//        return relation;
//    }
//
//    public void setRelation(String relation) {
//        this.relation = relation;
//    }

    public void setChildren(Collection<TreeNode> children) {
        this.children = children;
    }
}

