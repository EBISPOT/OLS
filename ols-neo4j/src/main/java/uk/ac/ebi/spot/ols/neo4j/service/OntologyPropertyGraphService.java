package uk.ac.ebi.spot.ols.neo4j.service;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.spot.ols.neo4j.model.Property;
import uk.ac.ebi.spot.ols.neo4j.model.Term;
import uk.ac.ebi.spot.ols.neo4j.repository.OntologyPropertyRepository;

import java.util.*;

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


    public Page<Property> findAll(Pageable pageable) {
        return ontologyPropertyRepository.findAll(pageable);
    }

    public Page<Property> findAllByIri(String iri, Pageable pageable) {
        return ontologyPropertyRepository.findAllByIri(iri, pageable);
    }

    public Page<Property> findAllByShortForm(String shortForm, Pageable pageable) {
        return ontologyPropertyRepository.findAllByShortForm(shortForm, pageable);
    }

    public Page<Property> findAllByOboId(String oboId, Pageable pageable) {
        return ontologyPropertyRepository.findAllByOboId(oboId, pageable);
    }

    public Page<Property> findAllByOntology(String ontologyId, Pageable pageable) {
        return ontologyPropertyRepository.findAllByOntology(ontologyId, pageable);
    }

    public Property findByOntologyAndIri(String ontologyname, String iri) {
        return ontologyPropertyRepository.findByOntologyAndIri(ontologyname, iri);
    }

    public Page<Property> getParents(String ontologyName, String iri, Pageable pageable) {
        return ontologyPropertyRepository.getParents(ontologyName, iri, pageable);
    }

    public Page<Property> getChildren(String ontologyName, String iri, Pageable pageable) {
        return ontologyPropertyRepository.getChildren(ontologyName, iri, pageable);
    }

    public Page<Property> getDescendants(String ontologyName, String iri, Pageable pageable) {
        return ontologyPropertyRepository.getDescendants(ontologyName, iri, pageable);
    }

    public Page<Property> getAncestors(String ontologyName, String iri, Pageable pageable) {
        return ontologyPropertyRepository.getAncestors(ontologyName, iri, pageable);
    }

    public Property findByOntologyAndShortForm(String ontologyId, String shortForm) {
        return ontologyPropertyRepository.findByOntologyAndShortForm(ontologyId, shortForm);
    }
    public Property findByOntologyAndOboId(String ontologyId, String oboId) {
        return ontologyPropertyRepository.findByOntologyAndOboId(ontologyId, oboId);
    }

    public Page<Property> getRoots(String ontologyId, boolean includeObsoletes, Pageable pageable) {
        return ontologyPropertyRepository.getRoots(ontologyId, includeObsoletes, pageable);
    }
}
