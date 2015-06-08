package uk.ac.ebi.spot.ols.config;


import uk.ac.ebi.spot.ols.util.DLExpressivity;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Simon Jupp
 * @date 29/01/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
public class OntologyResourceConfig  {

    private  String id;
    private  String title;
    private  String namespace;
    private  URI fileLocation;
    private  boolean isInferred;
    private  boolean classify;
    private  DLExpressivity expressivity;
    private  boolean oboSlims;
    private  URI labelProperty;
    private  Collection<URI> definitionProperties;
    private  Collection<URI> synonymProperties;
    private  Collection<URI> hierarchicalProperties;
    private  Collection<String> baseUris;
    private  Collection<URI> hiddenProperties;
    private boolean isSkos;

    public OntologyResourceConfig(String id, String title, String namespace, URI fileLocation, boolean isInferred, boolean classify, DLExpressivity expressivity, boolean oboSlims, URI labelProperty, Collection<URI> definitionProperties, Collection<URI> synonymProperties, Collection<URI> hierarchicalProperties, Collection<String> baseUris, Collection<URI> hiddenProperties, boolean isSkos) {
        this.id = id;
        this.title = title;
        this.namespace = namespace;
        this.fileLocation = fileLocation;
        this.isInferred = isInferred;
        this.classify = classify;
        this.expressivity = expressivity;
        this.oboSlims = oboSlims;
        this.labelProperty = labelProperty;
        this.definitionProperties = definitionProperties;
        this.synonymProperties = synonymProperties;
        this.hierarchicalProperties = hierarchicalProperties;
        this.baseUris = baseUris;
        this.hiddenProperties = hiddenProperties;
        this.isSkos = isSkos;
    }

    public OntologyResourceConfig() {
    }

    private OntologyResourceConfig(OntologyResourceConfigBuilder builder) {
        this.id = builder.id;
        this.title = builder.title;
        this.namespace = builder.namespace;
        this.fileLocation = builder.fileLocation;
        this.isInferred = builder.isInferred;
        this.classify = builder.classify;
        this.expressivity = builder.expressivity;
        this.oboSlims = builder.oboSlims;
        this.labelProperty = builder.labelProperty;
        this.synonymProperties = builder.synonymProperties;
        this.definitionProperties = builder.definitionProperties;
        this.hierarchicalProperties = builder.hierarchicalProperties;
        this.baseUris = builder.baseUris;
        this.hiddenProperties = builder.hiddenProperties;
        this.isSkos = builder.isSkos;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getNamespace() {
        return namespace;
    }

    public URI getFileLocation() {
        return fileLocation;
    }

    public boolean isInferred() {
        return isInferred;
    }

    public boolean isClassify() {
        return classify;
    }

    public DLExpressivity getExpressivity() {
        return expressivity;
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

    public static class OntologyResourceConfigBuilder {
        private  String id;
        private  String title;
        private  String namespace;
        private  URI fileLocation;
        private  boolean isInferred = true;
        private  boolean classify = false;
        private  boolean isSkos = false;
        private  DLExpressivity expressivity = DLExpressivity.UNKNOWN;
        private  boolean oboSlims = false;
        private  URI labelProperty = URI.create(OntologyDefaults.LABEL);
        private  Collection<URI> definitionProperties  = Collections.singleton(URI.create(OntologyDefaults.DEFINITION));
        private  Collection<URI> synonymProperties = Collections.emptySet();;
        private  Collection<URI> hierarchicalProperties = Collections.emptySet();
        private  Collection<String> baseUris = Collections.emptySet();
        private  Collection<URI> hiddenProperties = Collections.emptySet();

        public OntologyResourceConfigBuilder(String id, String title, String namespace, URI fileLocation) {
            this.id = id;
            this.title = title;
            this.namespace = namespace;
            this.fileLocation = fileLocation;
        }

        public OntologyResourceConfigBuilder setId(String id) {
            this.id = id;
            return this;
        }

        public OntologyResourceConfigBuilder setTitle(String title) {
            this.title = title;
            return this;
        }

        public OntologyResourceConfigBuilder setNamespace(String namespace) {
            this.namespace = namespace;
            return this;
        }

        public OntologyResourceConfigBuilder setFileLocation(URI fileLocation) {
            this.fileLocation = fileLocation;
            return this;
        }

        public OntologyResourceConfigBuilder setInferred(boolean isInferred) {
            this.isInferred = isInferred;
            return this;
        }

        public OntologyResourceConfigBuilder setClassify(boolean classify) {
            this.classify = classify;
            return this;
        }

        public OntologyResourceConfigBuilder setIsSkos(boolean isSkos) {
            this.isSkos = isSkos;
            return this;
        }

        public OntologyResourceConfigBuilder setExpressivity(DLExpressivity expressivity) {
            this.expressivity = expressivity;
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

        public OntologyResourceConfig build() {
            return new OntologyResourceConfig(this);
        }
    }
}
