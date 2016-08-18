package uk.ac.ebi.spot.ols.model;

import java.io.*;
import java.util.*;

public class TermDocumentBuilder {
    private String id;
    private String uri;
    private int uri_key;
    private String label;
    private List<String> synonyms = new ArrayList<>();
    private List<String> description = new ArrayList<>();
    private String shortForm;
    private String oboId;
    private String ontologyName;
    private String ontologyPrefix;
    private String ontologyUri;
    private String type;
    private boolean isDefiningOntology;
    private List<String> subsets = new ArrayList<>();
    private boolean isObsolete = false;
    private boolean hasChildren = false;
    private boolean isRoot = false;
    private List<String> equivalentUris = new ArrayList<>();
    private Map<String, List<String>> annotation = new HashMap<>();
    private List<String> logicalDescription = new ArrayList<>();
    private List<String> parents = new ArrayList<>();
    private List<String> ancestors = new ArrayList<>();
    private List<String> children = new ArrayList<>();
    private List<String> descendants = new ArrayList<>();
    private List<String> hierarchical_parents = new ArrayList<>();
    private List<String> hierarchical_ancestors = new ArrayList<>();
    private Map<String, List<String>> relatedTerms = new HashMap<>();

    public TermDocumentBuilder setId(String id) {
        this.id = id;
        return this;
    }


    public TermDocumentBuilder setUri(String uri) {
        this.uri = uri;
        return this;
    }

    public TermDocumentBuilder setUri_key(int uri_key) {
        this.uri_key = uri_key;
        return this;
    }

    public TermDocumentBuilder setLabel(String label) {
        this.label = label;
        return this;
    }

    public TermDocumentBuilder setSynonyms(Collection<String> synonyms) {
        this.synonyms = new ArrayList<>(synonyms);
        return this;
    }

    public TermDocumentBuilder setDescription(Collection<String> description) {
        this.description = new ArrayList<>(description);
        return this;
    }

    public TermDocumentBuilder setShortForm(String shortForm) {
        this.shortForm = shortForm;
        return this;
    }

    public TermDocumentBuilder setOboId(String oboId) {
        this.oboId = oboId;
        return this;
    }

    public TermDocumentBuilder setOntologyName(String ontologyName) {
        this.ontologyName = ontologyName;
        return this;
    }

    public TermDocumentBuilder setOntologyPrefix(String ontologyPrefix) {
        this.ontologyPrefix = ontologyPrefix;
        return this;
    }
    public TermDocumentBuilder setOntologyUri(String ontologyUri) {
        this.ontologyUri = ontologyUri;
        return this;
    }

    public TermDocumentBuilder setType(String type) {
        this.type = type;
        return this;
    }

    public TermDocumentBuilder setIsDefiningOntology(boolean isDefiningOntology) {
        this.isDefiningOntology = isDefiningOntology;
        return this;
    }

    public TermDocumentBuilder setSubsets(List<String> subsets) {
        this.subsets = subsets;
        return this;
    }

    public TermDocumentBuilder setIsObsolete(boolean isObsolete) {
        this.isObsolete = isObsolete;
        return this;
    }

    public TermDocumentBuilder setHasChildren(boolean hasChildren) {
        this.hasChildren = hasChildren;
        return this;
    }

    public TermDocumentBuilder setIsRoot(boolean isRoot) {
        this.isRoot = isRoot;
        return this;
    }

    public TermDocumentBuilder setEquivalentUris(Collection<String> equivalentUris) {
        this.equivalentUris = new ArrayList<>(equivalentUris);
        return this;
    }


    public TermDocumentBuilder setLogicalDescription(Collection<String> logicalDescription) {
        this.logicalDescription = new ArrayList<>(logicalDescription);
        return this;
    }

    public TermDocumentBuilder setParentUris(Collection<String> parentUris) {
        this.parents = new ArrayList<>(parentUris);
        return this;
    }

    public TermDocumentBuilder setAncestorUris(Collection<String> ancestorUris) {
        this.ancestors = new ArrayList<>(ancestorUris);
        return this;
    }

    public TermDocumentBuilder setChildUris(Collection<String> childUris) {
        this.children = new ArrayList<>(childUris);
        return this;
    }

    public TermDocumentBuilder setDescendantUris(Collection<String> descendantUris) {
        this.descendants = new ArrayList<>(descendantUris);
        return this;
    }

    public TermDocumentBuilder setHierarchicalParentUris(Collection<String> hierarchicalParentUris) {
        this.hierarchical_parents = new ArrayList<>(hierarchicalParentUris);
        return this;
    }

    public TermDocumentBuilder setHierarchicalAncestorUris(Collection<String> hierarchicalAncestorUris) {
        this.hierarchical_ancestors = new ArrayList<>(hierarchicalAncestorUris);
        return this;
    }

    public TermDocumentBuilder setAnnotation(Map<String, Collection<String>> annotations) {
        for (String key : annotations.keySet()) {
            this.annotation.put(key, new ArrayList<>(annotations.get(key)));
        }
        return this;
    }

    public TermDocumentBuilder setRelatedTerms(Map<String, Collection<String>> relatedTerms) {
        for (String key : relatedTerms.keySet()) {
            this.relatedTerms.put(key, new ArrayList<>(relatedTerms.get(key)));
        }
        return this;
    }

    public TermDocument createTermDocument() {
        return new TermDocument(
                id,
                uri,
                uri_key,
                label,
                synonyms,
                description,
                shortForm,
                oboId,
                ontologyName,
                ontologyPrefix,
                ontologyUri,
                type,
                isDefiningOntology,
                subsets,
                isObsolete,
                hasChildren,
                isRoot,
                equivalentUris,
                logicalDescription,
                annotation,
                parents,
                ancestors,
                children,
                descendants,
                hierarchical_parents,
                hierarchical_ancestors,
                relatedTerms
                );
    }
}