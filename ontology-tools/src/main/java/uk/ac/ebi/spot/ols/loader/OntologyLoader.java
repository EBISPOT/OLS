package uk.ac.ebi.spot.ols.loader;

/**
 * @author Simon Jupp
 * @date 03/02/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */

import org.semanticweb.owlapi.model.IRI;
import org.springframework.core.io.Resource;
import uk.ac.ebi.spot.ols.util.OBODefinitionCitation;
import uk.ac.ebi.spot.ols.util.OBOSynonym;
import uk.ac.ebi.spot.ols.util.OBOXref;

import java.util.Collection;
import java.util.Map;

/**
 * An ontology loader that provides some abstraction around core concepts in ontologies.  We essentially
 * try and flatten the ontology to a graph and assume all nodes and relations can be identified by an IRI.
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
    void setOntologyResource(Resource ontologyResource);

    /**
     * override where the ontology imports are loaded from
     * @param ontologyImportMappings
     */
     void setOntologyImportMappings(Map<IRI, IRI> ontologyImportMappings);

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
     * Get the ontology version IRI.  This returns the version IRI of the ontology that was actually loaded
     *
     * @return IRI of the ontology
     */
    IRI getOntologyVersionIRI();


    /**
     * Get the ontology name.  This is a short name for the ontology, for example "efo" for the experimental factor
     * ontology
     *
     * @return IRI of the ontology
     */
    String getOntologyName();

    /**
     * Get the ontology preferred prefix.  This is a short name for the ontology with preferred casing, for example "FBcv" for Flybase
     * ontology
     *
     * @return IRI of the ontology
     */
    String getPreferredPrefix();


    Collection<IRI> getAllClasses();
    Collection<IRI> getAllObjectPropertyIRIs();
    Collection<IRI> getAllDataPropertyIRIs();
    Collection<IRI> getAllIndividualIRIs();
    Collection<IRI> getAllAnnotationPropertyIRIs();

    /**
     * Returns a mapping between the IRIs that identify classes in the loaded ontology and the 
     * corresponding class rdfs:label.
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
     * fragment or path part of a full IRI.
     *
     * @return a user friendly representation of the class IRI
     */
    String getShortForm(IRI ontologyTermIRI);

    /**
     * Returns the OBO style identifier.  This will normally be the IRI
     * fragment or path that matches s/(([A-Za-z_]*)_(\d+))/$1:$2/
     *
     * @return a OBO accession for the class IRI
     */

    String getOboId(IRI ontologyTermIRI);

    Collection<String> getSubsets(IRI ontologyTermIRI);

    String getTermReplacedBy(IRI entityIRI);

    Map<IRI, Collection<String>> getAnnotations(IRI entityIRI);

    Collection<OBODefinitionCitation> getOBODefinitionCitations(IRI entityIRI);

    Collection<OBOXref> getOBOXrefs(IRI entityIRI);

    Collection<OBOSynonym> getOBOSynonyms(IRI entityIRI);


    /**
     * Get a map of IRI definitions
     * @return definitionProperty A collection of definition IRIs used in the ontology
     */
    Map<IRI, Collection<String>> getTermDefinitions();

    Map<IRI, Collection<IRI>> getDirectParentTerms();
    Collection<IRI> getDirectParentTerms(IRI iri);
    Map<IRI, Collection<IRI>> getAllParentTerms();

    /**
     * Get direct type for owl individuals
     * @return
     */
    Map<IRI, Collection<IRI>> getDirectTypes();

    Collection<String> getInternalMetadataProperties ();

    /**
     * Get a map of ontology annotations
     * @return annotation a map of ontology annotations
     */
    String getTitle();

    /**
     * Get a map of the ontology annotation. The key is the label or short form of the annotation predicate.
     * @return
     */
    Map<String, Collection<String>> getOntologyAnnotations();

    /**
     * Get the long description of this ontology
     * @return
     */
    String getOntologyDescription();

    /**
     * Get the ontology homepage
     * @return
     */
    String getHomePage();

    /**
     * Get a mailing list associated with this ontology
     * @return
     */
    String getMailingList();

    /**
     * Get an issue tracker associated with this ontology
     * @return
     */
    String getTracker();

    /**
     * Get a logo associated with this ontology
     * @return
     */
    String getLogo();

    /**
     * Get a version number for the ontology
     * @return
     */
    String getVersionNumber();
    Collection<String> getCreators();

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
    Map<IRI, Collection<IRI>> getRelatedTerms(IRI entityIRI);

    /**
     * Returns all related terms that can be considered parents. Any relations that are considered 
     * hierarchical in nature (e.g. part-of, develops-from etc..). Excludes supeclass/is-a parents
     *
     * @return the relationship IRI and the set of related terms
     */
    Map<IRI, Collection<IRI>> getRelatedParentTerms(IRI entityIRI);

    /**
     * Returns all transitive related terms that can be considered parents. Any relations that are 
     * considered hierarchical in nature (e.g. part-of, develops-from etc..). This includes super 
     * classes.
     *
     * @return the relationship IRI and the set of related terms
     */
    Collection<IRI> getAllRelatedParentTerms(IRI entityIRI);


    /**
     * Returns all related terms that can be considered children. Any relations that are considered 
     * hierarchical in nature (e.g. part-of, develops-from etc..)
     *
     * @return the relationship IRI and the set of related terms
     */
    Collection<IRI> getRelatedChildTerms(IRI entityIRI);


    /**
     * Returns related individuals for a given individual IRI.
     *
     * @return the relationship IRI and the set of related terms
     */
    Map<IRI, Collection<IRI>> getRelatedIndividuals(IRI entityIRI);

    /**
     * Returns related individuals to a given class.
     *
     * @return the relationship IRI and the set of related classes
     */
    Map<IRI, Collection<IRI>> getRelatedIndividualsToClass(IRI entityIRI);


    /**
     * Returns related classes to a given individual.
     *
     * @return the relationship IRI and the set of related terms
     */
    Map<IRI, Collection<IRI>> getRelatedClassesToIndividual(IRI entityIRI);


    /**
     * Returns the terms that the ontology designers deem to be the roots of their ontology.
     * 
     * @return a list of terms that are considered to be the preferred roots of the ontology.
     */
    Collection<IRI> getPreferredRootTerms();
}
