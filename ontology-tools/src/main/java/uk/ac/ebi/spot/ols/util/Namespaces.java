package uk.ac.ebi.spot.ols.util;

import org.semanticweb.owlapi.model.IRI;

/**
 * @author Simon Jupp
 * @date 04/02/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
public enum Namespaces {
    OWL("http://www.w3.org/2002/07/owl#"),
    RDFS("http://www.w3.org/2000/01/rdf-schema#"),
    RDF("http://www.w3.org/1999/02/22-rdf-syntax-ns#"),
    XSD("http://www.w3.org/2001/XMLSchema#"),
    XML("http://www.w3.org/XML/1998/namespace"),
    SWRL("http://www.w3.org/2003/11/swrl#"),
    SWRLB("http://www.w3.org/2003/11/swrlb#"),
    SKOS("http://www.w3.org/2004/02/skos/core#"),
    DC("http://purl.org/dc/elements/1.1/"),
    OAC("http://www.openannotation.org/ns/"),
    PROV("http://www.w3.org/TR/prov-o/"),
    ZOOMA("http://www.ebi.ac.uk/fgpt/zooma/"),
    ZOOMA_TERMS("http://rdf.ebi.ac.uk/terms/zooma/"),
    ZOOMA_RESOURCE("http://rdf.ebi.ac.uk/resource/zooma/"),
    EBI("http://www.ebi.ac.uk/"),
    EBIRESOURCE("http://rdf.ebi.ac.uk/resource/"),

    EFO("http://www.ebi.ac.uk/efo/"),
    SNAP("http://www.ifomis.org/bfo/1.1/snap#"),
    SPAN("http://www.ifomis.org/bfo/1.1/span#"),
    CL("http://purl.org/obo/owl/CL#"),
    OBO("http://purl.obolibrary.org/obo/"),
    OBOINOWL("http://www.geneontology.org/formats/oboInOwl#");

    private String ns;

    private Namespaces(String ns) {
        this.ns = ns;
    }

    public IRI createIRI(String name) {
            return IRI.create(ns + name);
        }

    public IRI getIRI() {
        return IRI.create(ns);
    }

    public String toString() {
        return ns;
    }
}
