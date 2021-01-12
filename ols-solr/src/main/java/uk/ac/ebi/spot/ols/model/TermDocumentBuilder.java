package uk.ac.ebi.spot.ols.model;

import java.io.*;
import java.util.*;

import org.mockito.internal.debugging.Localized;

import uk.ac.ebi.spot.ols.util.LocalizedStrings;

public class TermDocumentBuilder {
    private String id;
    private String uri;
    private int uri_key;
    private LocalizedStrings labels;
    private LocalizedStrings synonyms;
    private LocalizedStrings descriptions;
    private String shortForm;
    private String oboId;
    private String ontologyName;
    private Map<String, String> ontologyTitles;
    private String ontologyPrefix;
    private String ontologyUri;
    private String type;
    private boolean isDefiningOntology;
    private List<String> subsets = new ArrayList<>();
    private boolean isObsolete = false;
    private boolean hasChildren = false;
    private boolean isRoot = false;
    private List<String> equivalentUris = new ArrayList<>();

    // Map<language, Map<key, List<value>>>
    private Map<String, Map<String, List<String>>> annotations = new HashMap<>();

    private List<String> logicalDescription = new ArrayList<>();
    private List<String> parents = new ArrayList<>();
    private List<String> ancestors = new ArrayList<>();
    private List<String> children = new ArrayList<>();
    private List<String> descendants = new ArrayList<>();
    private List<String> hierarchical_parents = new ArrayList<>();
    private List<String> hierarchical_ancestors = new ArrayList<>();
    private Map<String, List<String>> relatedTerms = new HashMap<>();
    private boolean isPreferredRoot = false;

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

    public TermDocumentBuilder setLabels(LocalizedStrings labels) {
        this.labels = labels;
        return this;
    }

    public TermDocumentBuilder setSynonyms(LocalizedStrings synonyms) {
        this.synonyms = synonyms;
        return this;
    }

    public TermDocumentBuilder setDescriptions(LocalizedStrings descriptions) {
        this.descriptions = descriptions;
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

    public TermDocumentBuilder setOntologyTitles(Map<String,String> ontologyTitles) {
        this.ontologyTitles = ontologyTitles;
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

    public TermDocumentBuilder setAnnotations(Map<String, Map<String, List<String>>> annotations) {
        this.annotations = annotations;
        return this;
    }

    public TermDocumentBuilder setRelatedTerms(Map<String, Collection<String>> relatedTerms) {
        for (String key : relatedTerms.keySet()) {
            this.relatedTerms.put(key, new ArrayList<>(relatedTerms.get(key)));
        }
        return this;
    }
    
    

    public TermDocumentBuilder setPreferredRoot(boolean isPreferredRoot) {
		this.isPreferredRoot = isPreferredRoot;
		return this;
	}

	public Collection<TermDocument> createTermDocuments() {

        Set<String> languages = new HashSet<>();

        languages.addAll(labels.getLanguages());
        languages.addAll(ontologyTitles.keySet());
        languages.addAll(synonyms.getLanguages());
        languages.addAll(descriptions.getLanguages());
        languages.addAll(annotations.keySet());

        List<TermDocument> docs = new ArrayList<>();

        for(String lang : languages) {

            docs.add(
                new TermDocument(
                    id,
                    uri,
                    lang,
                    uri_key,
                    labels.getFirstString(lang),
                    synonyms.getStrings(lang),
                    descriptions.getStrings(lang),
                    shortForm,
                    oboId,
                    ontologyName,
                    ontologyTitles.get(lang),
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
                    annotations.get(lang),
                    parents,
                    ancestors,
                    children,
                    descendants,
                    hierarchical_parents,
                    hierarchical_ancestors,
                    relatedTerms,
                    isPreferredRoot
                    )
            );
        }

        return docs;
    }
}