package uk.ac.ebi.spot.ols.config;


import uk.ac.ebi.spot.ols.util.LocalizedStrings;
import uk.ac.ebi.spot.ols.util.ReasonerType;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * @author Simon Jupp
 * @date 29/01/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
public class OntologyResourceConfig  {

    // ontology IRI
    private  String id;
    // ontology version IRI
    private  String versionIri;
    private  String namespace;
    private  String preferredPrefix;

    private String title;
    private Map<String, String> localizedTitles; // lang -> value


    private String description;
    private Map<String, String> localizedDescriptions;

    private String homepage;
    private String version;
    private String mailingList;
    private Collection<String> creators;

    // Map<language, Map<key, List<value>>>
    private Map<String, List<String>> annotations;
    private Map<String, Map<String, List<String>>> localizedAnnotations;

    private  URI fileLocation;

    private ReasonerType reasonerType;
    private  boolean oboSlims;
    private  URI labelProperty;
    private  Collection<URI> definitionProperties;
    private  Collection<URI> synonymProperties;
    private  Collection<URI> hierarchicalProperties;
    private  Collection<String> baseUris;
    private  Collection<URI> hiddenProperties;
    private  Collection<URI> preferredRootTerms = new HashSet<>();
    private boolean isSkos;

    private  Collection<String> languages;

    private boolean allowDownload;

    // these are any metadata properties for the ontology, such as title or definition that are included in the ontology as OWL ontology annotation
    private Collection<String> internalMetadataProperties;

    public OntologyResourceConfig() {
    }

    private OntologyResourceConfig(OntologyResourceConfigBuilder builder) {
        this.id = builder.id;
        this.versionIri = builder.versionIri;
        this.title = builder.title;
        this.localizedTitles = builder.localizedTitles;
        this.description = builder.description;
        this.localizedDescriptions = builder.localizedDescriptions;
        this.namespace = builder.namespace;
        this.preferredPrefix = builder.preferredPrefix;
        this.fileLocation = builder.fileLocation;
        this.reasonerType = builder.reasonerType;
        this.oboSlims = builder.oboSlims;
        this.labelProperty = builder.labelProperty;
        this.synonymProperties = builder.synonymProperties;
        this.definitionProperties = builder.definitionProperties;
        this.hierarchicalProperties = builder.hierarchicalProperties;
        this.baseUris = builder.baseUris;
        this.hiddenProperties = builder.hiddenProperties;
        this.version = builder.version;
        this.isSkos = builder.isSkos;
        this.homepage = builder.homepage;
        this.mailingList = builder.mailingList;
        this.creators = builder.creators;
        this.annotations = builder.annotations;
        this.localizedAnnotations = builder.localizedAnnotations;
        this.internalMetadataProperties = builder.internalMetadatProperties;
        this.preferredRootTerms = builder.preferredRootTerms;
        this.allowDownload = builder.allowDownload;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getVersionIri() {
        return versionIri;
    }

    public String getTitle() {
	    return title;
    }

    public Map<String,String> getLocalizedTitles() {
        return localizedTitles != null ? localizedTitles : new HashMap<>();
    }
    public String getLocalizedTitle(String lang) {

	if(localizedTitles != null) {
		String loc = localizedTitles.get(lang);

		if(loc != null)
			return loc;
	}

	return title;
    }

    public String getDescription() {
	    return description;
    }

    public Map<String,String> getLocalizedDescriptions() {
        return localizedDescriptions != null ? localizedDescriptions : new HashMap<>();
    }
    public String getLocalizedDescription(String lang) {

	if(localizedDescriptions != null) {
		String loc = localizedDescriptions.get(lang);

		if(loc != null)
			return loc;
	}

	return description;
    }

    public String getVersion() {
        return version;
    }

    public String getNamespace() {
        return namespace.toLowerCase();
    }

    public String getPreferredPrefix() {
        if (preferredPrefix == null) {
            return namespace.toUpperCase();
        }
        return preferredPrefix;
    }

    public URI getFileLocation() {
        return fileLocation;
    }

    public ReasonerType getReasonerType() {
        return reasonerType;
    }

    public boolean isOboSlims() {
        return oboSlims;
    }

    public Collection<URI> getHierarchicalProperties() {
        return hierarchicalProperties;
    }

    public Collection<String> getBaseUris() {
        return baseUris;
    }

    public Collection<URI> getHiddenProperties() {
        return hiddenProperties;
    }

    public URI getLabelProperty() {
        return labelProperty;
    }

    public Collection<URI> getDefinitionProperties() {
        return definitionProperties;
    }

    public Collection<URI> getSynonymProperties() {
        return synonymProperties;
    }

    public boolean isSkos() {
        return isSkos;
    }

    public void setSkos(boolean isSkos) {

        this.isSkos = isSkos;
    }

    public String getHomepage() {
        return homepage;
    }

    public String getMailingList() {
        return mailingList;
    }

    public Collection<String> getCreators() {
        return creators;
    }

    public Map<String, List<String>> getLocalizedAnnotations(String lang) {
	if (localizedAnnotations != null) {
		Map<String, List<String>> annos = localizedAnnotations.get(lang);

		if(annos != null)
			return annos;
	}

	return annotations;
    }

    public void setFileLocation(URI fileLocation) {
        this.fileLocation = fileLocation;
    }

    public void setMailingList(String mailingList) {
        this.mailingList = mailingList;
    }

    public void setHomepage(String homepage) {
        this.homepage = homepage;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setLocalizedTitles(Map<String,String> titles) {
        this.localizedTitles = titles;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLocalizedDescriptions(Map<String,String> descriptions) {
        this.localizedDescriptions = descriptions;
    }

    public void setAnnotations(Map<String, List<String>> annotations) {
        this.annotations = annotations;
    }

    public void setLocalizedAnnotations(Map<String, Map<String, List<String>>> annotations) {
        this.localizedAnnotations = annotations;
    }

    public void setReasonerType(ReasonerType reasonerType) {
        this.reasonerType = reasonerType;
    }

    public void setCreators(Collection<String> creators) {
        this.creators = creators;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setVersionIri(String versionIri) {
        this.versionIri = versionIri;
    }

    public Collection<String> getInternalMetadataProperties() {
        return internalMetadataProperties;
    }

    public void setInternalMetadataProperties(Collection<String> internalMetadataProperties) {
        this.internalMetadataProperties = internalMetadataProperties;
    }

    public void setPreferredPrefix(String preferredPrefix) {
        this.preferredPrefix = preferredPrefix;
    }

    public void setOboSlims(boolean oboSlims) {
        this.oboSlims = oboSlims;
    }

    public void setLabelProperty(URI labelProperty) {
        this.labelProperty = labelProperty;
    }

    public void setDefinitionProperties(Collection<URI> definitionProperties) {
        this.definitionProperties = definitionProperties;
    }

    public void setSynonymProperties(Collection<URI> synonymProperties) {
        this.synonymProperties = synonymProperties;
    }

    public void setHierarchicalProperties(Collection<URI> hierarchicalProperties) {
        this.hierarchicalProperties = hierarchicalProperties;
    }

    public void setBaseUris(Collection<String> baseUris) {
        this.baseUris = baseUris;
    }

    public void setHiddenProperties(Collection<URI> hiddenProperties) {
        this.hiddenProperties = hiddenProperties;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public void setPreferredRootTerms(Collection<URI> preferredRootTerms) {
        this.preferredRootTerms = preferredRootTerms;
    }

    public Collection<URI> getPreferredRootTerms() {
        return this.preferredRootTerms;
    }

    public boolean getAllowDownload() {
        return this.allowDownload;
    }

    public void setAllowDownload(boolean allowDownload) {
        this.allowDownload = allowDownload;
    }

    public void setLanguages(Collection<String> languages) {
        this.languages = languages;
    }

    public Collection<String> getLanguages() {
        return this.languages;
    }

    public static class OntologyResourceConfigBuilder {
        private  String id;
        private  String versionIri;
        private  String title;
        private Map<String, String> localizedTitles;
        private  String description;
        private Map<String, String> localizedDescriptions;
        private  String namespace;
        private String preferredPrefix;
        private  URI fileLocation;
        private  boolean isSkos = false;
        private ReasonerType reasonerType = ReasonerType.NONE;
        private  boolean oboSlims = false;
        private  URI labelProperty = URI.create(OntologyDefaults.LABEL);
        private  Collection<URI> definitionProperties  = Collections.singleton(URI.create(OntologyDefaults.DEFINITION));
        private  Collection<URI> synonymProperties = Collections.emptySet();;
        private  Collection<URI> hierarchicalProperties = Collections.emptySet();
        private  Collection<String> baseUris = Collections.emptySet();
        private  Collection<URI> hiddenProperties = Collections.emptySet();
        private  Collection<String> languages = Collections.emptySet();
        private String homepage;
        private String version;
        private String mailingList;
        private Collection<String> creators = Collections.emptySet();
        private Map<String,List<String>> annotations = Collections.emptyMap();
        private Map<String, Map<String,List<String>>> localizedAnnotations = Collections.emptyMap();
        private Collection<String> internalMetadatProperties = Collections.emptySet();
        private Collection<URI> preferredRootTerms = Collections.emptySet();
        private boolean allowDownload = true;

        public OntologyResourceConfigBuilder(String id, String namespace, URI fileLocation) {
            this.id = id;
            this.namespace = namespace.toLowerCase();
            this.fileLocation = fileLocation;
            this.preferredPrefix = namespace;
        }

        public OntologyResourceConfigBuilder setId(String id) {
            this.id = id;
            return this;
        }

        public OntologyResourceConfigBuilder setVersionIri(String versionIri) {
            this.versionIri = versionIri;
            return this;
        }

        public OntologyResourceConfigBuilder setTitle(String title) {
            this.title = title;
            return this;
        }

        public OntologyResourceConfigBuilder setLocalizedTitles(Map<String,String> titles) {
            this.localizedTitles = titles;
            return this;
        }

        public OntologyResourceConfigBuilder setVersion(String version) {
            this.version = version;
            return this;
        }

        public OntologyResourceConfigBuilder setNamespace(String namespace) {
            this.namespace = namespace.toLowerCase();
            return this;
        }

        public OntologyResourceConfigBuilder setPreferredPrefix(String prefix) {
            this.preferredPrefix = prefix;
            return this;
        }

        public OntologyResourceConfigBuilder setFileLocation(URI fileLocation) {
            this.fileLocation = fileLocation;
            return this;
        }

        public OntologyResourceConfigBuilder setIsSkos(boolean isSkos) {
            this.isSkos = isSkos;
            return this;
        }

        public OntologyResourceConfigBuilder setReasonerType(ReasonerType reasonerType) {
            this.reasonerType = reasonerType;
            return this;
        }

        public OntologyResourceConfigBuilder setOboSlims(boolean oboSlims) {
            this.oboSlims = oboSlims;
            return this;
        }

        public OntologyResourceConfigBuilder setLabelProperty(URI labelProperty) {
            this.labelProperty = labelProperty;
            return this;
        }

        public OntologyResourceConfigBuilder setDefinitionProperties(Collection<URI> definitionProperties) {
            this.definitionProperties = definitionProperties;
            return this;
        }

        public OntologyResourceConfigBuilder setSynonymProperties(Collection<URI> synonymProperties) {
            this.synonymProperties = synonymProperties;
            return this;
        }

        public OntologyResourceConfigBuilder setHierarchicalProperties(Collection<URI> hierarchicalProperties) {
            this.hierarchicalProperties = hierarchicalProperties;
            return this;
        }

        public OntologyResourceConfigBuilder setBaseUris(Collection<String> baseUris) {
            this.baseUris = baseUris;
            return this;
        }

        public OntologyResourceConfigBuilder setHiddenProperties(Collection<URI> hiddenProperties) {
            this.hiddenProperties = hiddenProperties;
            return this;
        }

        public OntologyResourceConfigBuilder setDescription(String description) {
            this.description = description;
            return this;
        }

        public OntologyResourceConfigBuilder setLocalizedDescriptions(Map<String,String> descriptions) {
            this.localizedDescriptions = descriptions;
            return this;
        }

        public OntologyResourceConfigBuilder setHomepage(String homepage) {
            this.homepage = homepage;
            return this;
        }

        public OntologyResourceConfigBuilder setMailingList(String mailingList) {
            this.mailingList = mailingList;
            return this;
        }

        public OntologyResourceConfigBuilder setCreators(Collection<String> creators) {
            this.creators = creators;
            return this;
        }

	public OntologyResourceConfigBuilder setAnnotations(Map<String, List<String>> annotations) {
		this.annotations = annotations;
		return this;
	}

	public OntologyResourceConfigBuilder setLocalizedAnnotations(Map<String, Map<String, List<String>>> annotations) {
		this.localizedAnnotations = annotations;
		return this;
	}

        public OntologyResourceConfigBuilder setInternalMetadatProperties(Collection<String> internalMetadatProperties) {
            this.internalMetadatProperties = internalMetadatProperties;
            return this;
        }

        public void setPreferredRootTerms(Collection<URI> preferredRootTerms) {
            this.preferredRootTerms = preferredRootTerms;
        }

        public void setAllowDownload(boolean allowDownload) {
            this.allowDownload = allowDownload;
        }

        public OntologyResourceConfig build() {
            return new OntologyResourceConfig(this);
        }

        public OntologyResourceConfigBuilder setLanguages(Collection<String> languages) {
		this.languages = languages;
		return this;
        }
    }
}
