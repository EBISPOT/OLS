package uk.ac.ebi.spot.ols.config;

public enum OntologyResourceConfigEnum {

    ID("id"),
    PREFERRED_PREFIX("preferredPrefix"),
    TITLE("title"),
    ONTOLOGY_PURL("ontology_purl"),
    ONTOLOGY_URI("uri"),
    LABEL_PROPERTY("label_property"),
    DEFINITION_PROPERTY("definition_property"),
    SYNONYM_PROPERTY("synonym_property"),
    HIERARCHICAL_PROPERTY("hierarchical_property"),
    BASE_URI("base_uri"),
    REASONER("reasoner"),
    OBO_SLIMS("oboSlims"),
    DESCRIPTION("description"),
    HOMEPAGE("homepage"),
    MAILING_LIST("mailing_list"),
    TRACKER("tracker"),
    LOGO("logo"),
    CREATOR("creator"),
    PREFERRED_ROOT_TERM("preferred_root_term"),
    ALLOW_DOWNLOAD("allow_download");

    private final String propertyName;

    OntologyResourceConfigEnum(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getPropertyName() {
        return propertyName;
    }
}
