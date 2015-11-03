package uk.ac.ebi.spot.ols.neo4j.service;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.spot.ols.neo4j.model.Individual;
import uk.ac.ebi.spot.ols.neo4j.model.Property;
import uk.ac.ebi.spot.ols.neo4j.model.Term;
import uk.ac.ebi.spot.ols.neo4j.repository.OntologyIndividualRepository;

import java.util.HashMap;
import java.util.Map;

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

    public Page<Individual> findAll(Pageable pageable) {
        return individualRepository.findAll(pageable);
    }

    public Page<Individual> findAllByIri(String iri, Pageable pageable) {
        return individualRepository.findAllByIri(iri, pageable);
    }

    public Page<Individual> findAllByShortForm(String shortForm, Pageable pageable) {
        return individualRepository.findAllByShortForm(shortForm, pageable);
    }

    public Page<Individual> findAllByOboId(String oboId, Pageable pageable) {
        return individualRepository.findAllByOboId(oboId, pageable);
    }

    public Page<Individual> findAllByOntology(String ontologyId, Pageable pageable) {
        return individualRepository.findAllByOntology(ontologyId, pageable);
    }

    public Individual findByOntologyAndIri(String ontologyname, String iri) {
        return individualRepository.findByOntologyAndIri(ontologyname, iri);
    }

    public Page<Term> getDirectTypes(String ontologyName, String iri, Pageable pageable) {
        return individualRepository.getDirectTypes(ontologyName, iri, pageable);
    }

    public Page<Term> getAllTypes(String ontologyName, String iri, Pageable pageable) {
        return individualRepository.getAllTypes(ontologyName, iri, pageable);
    }

    public Individual findByOntologyAndShortForm(String ontologyId, String shortForm) {
        return individualRepository.findByOntologyAndShortForm(ontologyId, shortForm);
    }
    public Individual findByOntologyAndOboId(String ontologyId, String oboId) {
        return individualRepository.findByOntologyAndOboId(ontologyId, oboId);
    }

}
