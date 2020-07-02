package uk.ac.ebi.spot.ols.neo4j.service;

import org.neo4j.graphdb.GraphDatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.ols.neo4j.model.OlsIndividual;
import uk.ac.ebi.spot.ols.neo4j.model.OlsProperty;
import uk.ac.ebi.spot.ols.neo4j.model.OlsTerm;
import uk.ac.ebi.spot.ols.neo4j.repository.OntologyIndividualRepository;

/**
 * @author Simon Jupp
 * @date 18/08/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@Service
public class OntologyIndividualService {

    @Autowired(required = false)
    OntologyIndividualRepository individualRepository;

    @Autowired(required = false)
    GraphDatabaseService graphDatabaseService;

    public Page<OlsIndividual> findAll(Pageable pageable) {
        return individualRepository.findAll(pageable);
    }

    public Page<OlsIndividual> findAllByIsDefiningOntology(Pageable pageable) {
        return individualRepository.findAllByIsDefiningOntology(pageable);
    }
    
    public Page<OlsIndividual> findAllByIri(String iri, Pageable pageable) {
        return individualRepository.findAllByIri(iri, pageable);
    }

    public Page<OlsIndividual> findAllByIriAndIsDefiningOntology(String iri, Pageable pageable) {
        return individualRepository.findAllByIriAndIsDefiningOntology(iri, pageable);
    }
    
    public Page<OlsIndividual> findAllByShortForm(String shortForm, Pageable pageable) {
        return individualRepository.findAllByShortForm(shortForm, pageable);
    }

    public Page<OlsIndividual> findAllByShortFormAndIsDefiningOntology(String shortForm, Pageable pageable) {
        return individualRepository.findAllByShortFormAndIsDefiningOntology(shortForm, pageable);
    }    
    
    public Page<OlsIndividual> findAllByOboId(String oboId, Pageable pageable) {
        return individualRepository.findAllByOboId(oboId, pageable);
    }

    public Page<OlsIndividual> findAllByOboIdAndIsDefiningOntology(String oboId, Pageable pageable) {
        return individualRepository.findAllByOboIdAndIsDefiningOntology(oboId, pageable);
    }    
    
    public Page<OlsIndividual> findAllByOntology(String ontologyId, Pageable pageable) {
        return individualRepository.findAllByOntology(ontologyId, pageable);
    }

    public OlsIndividual findByOntologyAndIri(String ontologyname, String iri) {
        return individualRepository.findByOntologyAndIri(ontologyname, iri);
    }

    public Page<OlsTerm> getDirectTypes(String ontologyName, String iri, Pageable pageable) {
        return individualRepository.getDirectTypes(ontologyName, iri, pageable);
    }

    public Page<OlsTerm> getAllTypes(String ontologyName, String iri, Pageable pageable) {
        return individualRepository.getAllTypes(ontologyName, iri, pageable);
    }

    public OlsIndividual findByOntologyAndShortForm(String ontologyId, String shortForm) {
        return individualRepository.findByOntologyAndShortForm(ontologyId, shortForm);
    }
    public OlsIndividual findByOntologyAndOboId(String ontologyId, String oboId) {
        return individualRepository.findByOntologyAndOboId(ontologyId, oboId);
    }

}
