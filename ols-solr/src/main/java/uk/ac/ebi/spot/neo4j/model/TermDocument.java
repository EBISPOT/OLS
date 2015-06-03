package uk.ac.ebi.spot.neo4j.model;

import org.apache.solr.client.solrj.beans.Field;
import org.springframework.data.solr.core.mapping.SolrDocument;

import java.util.List;
import java.util.Map;

/**
 * @author Simon Jupp
 * @date 29/01/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@SolrDocument(solrCoreName = "ontology")
public class TermDocument {

    @Field("id")
    private String id;

    @Field("uri")
    private String uri;

    @Field("uri_key")
    private int uri_key;

    @Field("short_form")
    private List<String> shortForm;

    @Field("label")
    private String label;

    @Field("synonym")
    private List<String> synonym;

    @Field("description")
    private List<String> description;

    @Field("ontology_name")
    private String ontologyName;

    @Field("ontology_uri")
    private String ontologyUri;

    @Field("type")
    private String type;

    @Field("is_defining_ontology")
    private boolean isDefiningOntology;

    @Field("subset")
    private List<String> subsets;

    @Field("is_obsolete")
    private boolean isObsolete;

    @Field("has_children")
    private boolean hasChildren;

    @Field("is_root")
    private boolean isRoot;

    @Field("equivalent_uri")
   	private List<String> equivalentUris;

    @Field("logical_description")
   	private List<String> logicalDescription;

    @Field("*_annotation")
   	private Map<String, List<String>> annotation;

    @Field("parent_uri")
   	private List<String> parents;

    @Field("ancestor_uri")
   	private List<String> ancestors;

    @Field("child_uri")
   	private List<String> children;

    @Field("descendant_uri")
   	private List<String> descendants;

    @Field("*_related")
   	private Map<String, List<String>> related;

    public TermDocument() {

    }

    public TermDocument(
            String id,
            String uri,
            int uri_key,
            String label,
            List<String> synonym,
            List<String> description,
            List<String> shortForm,
            String ontologyName,
            String ontologyUri,
            String type,
            boolean isDefiningOntology,
            List<String> subsets,
            boolean isObsolete,
            boolean hasChildren,
            boolean isRoot,
            List<String> equivalentUris,
            List<String> logicalDescription,
            Map<String, List<String>> annotation,
            List<String> parents,
            List<String> ancestors,
            List<String> children,
            List<String> descendants,
            Map<String, List<String>> related
           ) {
        this.id = id;
        this.uri = uri;
        this.uri_key = uri_key;
        this.label = label;
        this.synonym = synonym;
        this.description = description;
        this.shortForm = shortForm;
        this.ontologyName = ontologyName;
        this.ontologyUri = ontologyUri;
        this.type = type;
        this.isDefiningOntology = isDefiningOntology;
        this.subsets = subsets;
        this.isObsolete = isObsolete;
        this.hasChildren = hasChildren;
        this.isRoot = isRoot;
        this.equivalentUris = equivalentUris;
        this.logicalDescription = logicalDescription;
        this.annotation = annotation;
        this.parents = parents;
        this.ancestors = ancestors;
        this.children = children;
        this.descendants = descendants;
        this.related = related;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public int getUri_key() {
        return uri_key;
    }

    public void setUri_key(int uri_key) {
        this.uri_key = uri_key;
    }

    public List<String> getShortForm() {
        return shortForm;
    }

    public void setShortForm(List<String> shortForm) {
        this.shortForm = shortForm;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<String> getSynonym() {
        return synonym;
    }

    public void setSynonym(List<String> synonym) {
        this.synonym = synonym;
    }

    public List<String> getDescription() {
        return description;
    }

    public void setDescription(List<String> description) {
        this.description = description;
    }

    public String getOntologyName() {
        return ontologyName;
    }

    public void setOntologyName(String ontologyName) {
        this.ontologyName = ontologyName;
    }

    public String getOntologyUri() {
        return ontologyUri;
    }

    public void setOntologyUri(String ontologyUri) {
        this.ontologyUri = ontologyUri;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isDefiningOntology() {
        return isDefiningOntology;
    }

    public void setDefiningOntology(boolean isDefiningOntology) {
        this.isDefiningOntology = isDefiningOntology;
    }

    public List<String> getSubsets() {
        return subsets;
    }

    public void setSubsets(List<String> subsets) {
        this.subsets = subsets;
    }

    public boolean isObsolete() {
        return isObsolete;
    }

    public void setObsolete(boolean isObsolete) {
        this.isObsolete = isObsolete;
    }

    public boolean isHasChildren() {
        return hasChildren;
    }

    public void setHasChildren(boolean hasChildren) {
        this.hasChildren = hasChildren;
    }

    public boolean isRoot() {
        return isRoot;
    }

    public void setRoot(boolean isRoot) {
        this.isRoot = isRoot;
    }

    public List<String> getEquivalentUris() {
        return equivalentUris;
    }

    public void setEquivalentUris(List<String> equivalentUris) {
        this.equivalentUris = equivalentUris;
    }

    public List<String> getLogicalDescription() {
        return logicalDescription;
    }

    public void setLogicalDescription(List<String> logicalDescription) {
        this.logicalDescription = logicalDescription;
    }

    public Map<String, List<String>> getAnnotation() {
        return annotation;
    }

    public void setAnnotation(Map<String, List<String>> annotation) {
        this.annotation = annotation;
    }

    public List<String> getParents() {
        return parents;
    }

    public void setParents(List<String> parents) {
        this.parents = parents;
    }

    public List<String> getAncestors() {
        return ancestors;
    }

    public void setAncestors(List<String> ancestors) {
        this.ancestors = ancestors;
    }

    public List<String> getChildren() {
        return children;
    }

    public void setChildren(List<String> children) {
        this.children = children;
    }

    public List<String> getDescendants() {
        return descendants;
    }

    public void setDescendants(List<String> descendants) {
        this.descendants = descendants;
    }

    public Map<String, List<String>> getRelated() {
        return related;
    }

    public void setRelated(Map<String, List<String>> related) {
        this.related = related;
    }
}
