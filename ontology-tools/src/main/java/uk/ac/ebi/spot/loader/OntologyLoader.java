package uk.ac.ebi.spot.loader;

/**
 * @author Simon Jupp
 * @date 03/02/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */

import org.semanticweb.owlapi.model.IRI;
import org.springframework.core.io.Resource;

import java.util.Collection;
import java.util.Map;

/**
 * An ontology loader that provides some abstraction around core concepts in ontologies.  We essentially
 * try and flatten the ontology to graph and assume all nodes and relations can be identifierd by an IRI.
 * This interface provides a mechanism to decouple ontology loading and processing from specific implementations.
 * Typically we use the OWL API as a base implementation, but we may also provide other implementations for
 * loading different types of vocabularies such as OBO or SKOS, or something entirely bespoke.
 *
 * @author Tony Burdett
 * @author Simon Jupp
 * @date 03/06/13
 */
public interface OntologyLoader {


    /**
     * override where the ontology is loaded from
     * @param ontologyResource
     */
    public void setOntologyResource(Resource ontologyResource);

    /**
     * override where the ontology imports are loaded from
     * @param ontologyImportMappings
     */
    public void setOntologyImportMappings(Map<IRI, IRI> ontologyImportMappings);

    /**
     * Get the IRIs of the property used to indicate a term or class definition
     * @return definitionProperty A collection of definition IRIs used in the ontology
     */
    Collection<IRI> getDefinitionIRIs();

    /**
     * Get the ontology IRI.  This returns the IRI of the ontology that was actually loaded, and may be different from
     * the ontologyIRI specified if declared differently in the loaded file.
     *
     * @return IRI of the ontology
     */
    IRI getOntologyIRI();

    /**
     * Get the ontology name.  This is a short name for the ontology, for example "efo" for the experimental factor
     * ontology
     *
     * @return IRI of the ontology
     */
    String getOntologyName();

    Collection<IRI> getAllClasses();
    Collection<IRI> getAllObjectPropertyIRIs();
    Collection<IRI> getAllDataPropertyIRIs();
    Collection<IRI> getAllIndividualIRIs();
    Collection<IRI> getAllAnnotationPropertyIRIs();


    /**
     * Returns a mapping between the IRIs that identify classes in the loaded ontology and the corresponding class
     * rdfs:label.
     *
     * @return the class labels in this ontology, indexed by class IRI
     */
    Map<IRI, String> getTermLabels();

    /**
     * Get all synonyms for a given IRI
     *
     * @return the class labels in this ontology, indexed by class IRI
     */
    Map<IRI, Collection<String>> getTermSynonyms();

    /**
     * Returns the class "accession" - or a user friendly 'short name' or identifier.  This will normally be the IRI
     * fragment or path part of a full IRI. This will also include an OBO style accession if the fragment matches s/(([A-Za-z_]*)_(\d+))/$1:$2/
     *
     * @return a user friendly representation of the class IRI
     */
    Collection<String> getAccessions(IRI ontologyTermIRI);
    Collection<String> getSubsets(IRI ontologyTermIRI);

    Map<IRI, Collection<String>> getAnnotations(IRI entityIRI);

    /**
     * Get a map of IRI definitions
     * @return definitionProperty A collection of definition IRIs used in the ontology
     */
    Map<IRI, Collection<String>> getTermDefinitions();

    Map<IRI, Collection<IRI>> getDirectParentTerms();
    Collection<IRI> getDirectParentTerms(IRI iri);
    Map<IRI, Collection<IRI>> getAllParentTerms();



    Map<IRI, Collection<IRI>> getDirectChildTerms();
    Collection<IRI> getDirectChildTerms(IRI iri);
    Map<IRI, Collection<IRI>> getAllChildTerms();

    Map<IRI, Collection<String>> getLogicalSuperClassDescriptions();
    Map<IRI, Collection<String>> getLogicalEquivalentClassDescriptions();

    Map<IRI, Collection<IRI>> getEquivalentTerms();

    boolean isObsoleteTerm(IRI entityIRI);
    boolean isLocalTerm(IRI entityIRI);

    /**
     * Returns related terms from superclass expressions. Only direct relations are
     * included (i.e. no nested class expressions)
     *
     * @return the relationship IRI and the set of related terms
     */
    Map<IRI, Collection<IRI>> getAllRelatedTerms(IRI entityIRI);

    Map<IRI, Collection<IRI>> getRelatedTerms(IRI entityIRI);


}
