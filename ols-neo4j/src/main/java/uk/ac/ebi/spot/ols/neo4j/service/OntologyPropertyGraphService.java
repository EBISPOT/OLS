package uk.ac.ebi.spot.ols.neo4j.service;

import org.neo4j.graphdb.GraphDatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import uk.ac.ebi.spot.ols.neo4j.model.OlsProperty;
import uk.ac.ebi.spot.ols.neo4j.repository.OntologyPropertyRepository;

/**
 * @author Simon Jupp
 * @date 18/08/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@Service
public class OntologyPropertyGraphService {

    @Autowired(required = false)
    OntologyPropertyRepository ontologyPropertyRepository;

    @Autowired(required = false)
    GraphDatabaseService graphDatabaseService;


    public Page<OlsProperty> findAll(Pageable pageable) {
        return ontologyPropertyRepository.findAll(pageable);
    }

    public Page<OlsProperty> findAllByIri(String iri, Pageable pageable) {
        return ontologyPropertyRepository.findAllByIri(iri, pageable);
    }

    public Page<OlsProperty> findAllByShortForm(String shortForm, Pageable pageable) {
        return ontologyPropertyRepository.findAllByShortForm(shortForm, pageable);
    }

    public Page<OlsProperty> findAllByOboId(String oboId, Pageable pageable) {
        return ontologyPropertyRepository.findAllByOboId(oboId, pageable);
    }

    
    public Page<OlsProperty> findAllByIsDefiningOntology(Pageable pageable) {
        return ontologyPropertyRepository.findAllByIsDefiningOntology(pageable);
    }

    public Page<OlsProperty> findAllByIriAndIsDefiningOntology(String iri, Pageable pageable) {
        return ontologyPropertyRepository.findAllByIriAndIsDefiningOntology(iri, pageable);
    }

    public Page<OlsProperty> findAllByShortFormAndIsDefiningOntology(String shortForm, Pageable pageable) {
        return ontologyPropertyRepository.findAllByShortFormAndIsDefiningOntology(shortForm, pageable);
    }

    public Page<OlsProperty> findAllByOboIdAndIsDefiningOntology(String oboId, Pageable pageable) {
        return ontologyPropertyRepository.findAllByOboIdAndIsDefiningOntology(oboId, pageable);
    }
    
    
    
    public Page<OlsProperty> findAllByOntology(String ontologyId, Pageable pageable) {
        return ontologyPropertyRepository.findAllByOntology(ontologyId, pageable);
    }

    public OlsProperty findByOntologyAndIri(String ontologyname, String iri) {
        return ontologyPropertyRepository.findByOntologyAndIri(ontologyname, iri);
    }

    public Page<OlsProperty> getParents(String ontologyName, String iri, Pageable pageable) {
        return ontologyPropertyRepository.getParents(ontologyName, iri, pageable);
    }

    public Page<OlsProperty> getChildren(String ontologyName, String iri, Pageable pageable) {
        return ontologyPropertyRepository.getChildren(ontologyName, iri, pageable);
    }

    public Page<OlsProperty> getDescendants(String ontologyName, String iri, Pageable pageable) {
        return ontologyPropertyRepository.getDescendants(ontologyName, iri, pageable);
    }

    public Page<OlsProperty> getAncestors(String ontologyName, String iri, Pageable pageable) {
        return ontologyPropertyRepository.getAncestors(ontologyName, iri, pageable);
    }

    public OlsProperty findByOntologyAndShortForm(String ontologyId, String shortForm) {
        return ontologyPropertyRepository.findByOntologyAndShortForm(ontologyId, shortForm);
    }
    public OlsProperty findByOntologyAndOboId(String ontologyId, String oboId) {
        return ontologyPropertyRepository.findByOntologyAndOboId(ontologyId, oboId);
    }

    public Page<OlsProperty> getRoots(String ontologyId, boolean includeObsoletes, Pageable pageable) {
        return ontologyPropertyRepository.getRoots(ontologyId, includeObsoletes, pageable);
    }
}
