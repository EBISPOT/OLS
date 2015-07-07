package uk.ac.ebi.spot.ols.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.util.UriUtils;
import uk.ac.ebi.spot.ols.neo4j.model.Term;
import uk.ac.ebi.spot.ols.neo4j.service.OntologyGraphService;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Objects;

/**
 * @author Simon Jupp
 * @date 23/06/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@Controller
@RequestMapping("/ontology")
//@ExposesResourceFor(Term.class)
public class TermController {

    @Autowired
    private OntologyGraphService ontologyGraphService;

    @Autowired TermAssembler termAssembler;

    @RequestMapping(path = "{onto}/terms", produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<Term>> terms(@PathVariable("onto") String ontologyId, Pageable pageable,
                                           PagedResourcesAssembler assembler) {

        Page<Term> terms = ontologyGraphService.findAllByOntology(ontologyId, pageable);
        return new ResponseEntity<>( assembler.toResource(terms, termAssembler), HttpStatus.OK);
    }

    @RequestMapping(path = "/{onto}/terms/{id}", produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<Resource<Term>> getTerm(@PathVariable("onto") String ontologyId, @PathVariable("id") String termId) throws ResourceNotFoundException {
        try {
            String decoded = UriUtils.decode(termId, "UTF-8");
            Term term = ontologyGraphService.findByOntologyAndIri(ontologyId, decoded);
            return new ResponseEntity<>( termAssembler.toResource(term), HttpStatus.OK);
        } catch (UnsupportedEncodingException e) {
            throw new ResourceNotFoundException();
        }
    }

    @RequestMapping(path = "/{onto}/terms/{id}/parents", produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<Term>> getParents(@PathVariable("onto") String ontologyId, @PathVariable("id") String termId, Pageable pageable,
                                                PagedResourcesAssembler assembler) {
        try {
            String decoded = UriUtils.decode(termId, "UTF-8");
            Page<Term> parents = ontologyGraphService.getParents(ontologyId, decoded, pageable);
            return new ResponseEntity<>( assembler.toResource(parents, termAssembler), HttpStatus.OK);
        }
        catch (UnsupportedEncodingException e) {
            throw new ResourceNotFoundException();
        }
    }

    @RequestMapping(path = "/{onto}/terms/{id}/children", produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<Term>> children(@PathVariable("onto") String ontologyId, @PathVariable("id") String termId, Pageable pageable,
                                              PagedResourcesAssembler assembler) {
        try {
            String decoded = UriUtils.decode(termId, "UTF-8");
            Page<Term> children = ontologyGraphService.getChildren(ontologyId, decoded, pageable);
            return new ResponseEntity<>( assembler.toResource(children, termAssembler), HttpStatus.OK);
        }
        catch (UnsupportedEncodingException e) {
            throw new ResourceNotFoundException();
        }
    }

    @RequestMapping(path = "/{onto}/terms/{id}/descendants", produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<Term>> descendants(@PathVariable("onto") String ontologyId, @PathVariable("id") String termId, Pageable pageable,
                                                 PagedResourcesAssembler assembler) {
        try {
            String decoded = UriUtils.decode(termId, "UTF-8");
            Page<Term> descendants = ontologyGraphService.getDescendants(ontologyId, decoded, pageable);
            return new ResponseEntity<>( assembler.toResource(descendants, termAssembler), HttpStatus.OK);
        }
        catch (UnsupportedEncodingException e) {
            throw new ResourceNotFoundException();
        }
    }

    @RequestMapping(path = "/{onto}/terms/{id}/ancestors", produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<Term>> ancestors(@PathVariable("onto") String ontologyId, @PathVariable("id") String termId, Pageable pageable,
                                               PagedResourcesAssembler assembler) {
        try {
            String decoded = UriUtils.decode(termId, "UTF-8");
            Page<Term> ancestors = ontologyGraphService.getAncestors(ontologyId, decoded, pageable);
            return new ResponseEntity<>( assembler.toResource(ancestors, termAssembler), HttpStatus.OK);
        }
        catch (UnsupportedEncodingException e) {
            throw new ResourceNotFoundException();
        }
    }

    @RequestMapping(path = "/{onto}/terms/{id}/jstree", produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<String> graphD3(@PathVariable("onto") String ontologyId, @PathVariable("id") String termId) {
        try {
            String decoded = UriUtils.decode(termId, "UTF-8");

            Object object= ontologyGraphService.getJsTree(ontologyId, decoded);
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            return new HttpEntity<String>(ow.writeValueAsString(object));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        throw new ResourceNotFoundException();
    }

    @RequestMapping(path = "/{onto}/terms/{id}/{relation}", produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<Term>> related(@PathVariable("onto") String ontologyId, @PathVariable("id") String termId, @PathVariable("relation") String relation, Pageable pageable,
                                             PagedResourcesAssembler assembler) {
        try {
            String decodedTerm = UriUtils.decode(termId, "UTF-8");
            String decodedRelation = UriUtils.decode(relation, "UTF-8");
            Page<Term> related = ontologyGraphService.getRelated(ontologyId, decodedTerm, decodedRelation, pageable);

            return new ResponseEntity<>( assembler.toResource(related, termAssembler), HttpStatus.OK);
        }
        catch (UnsupportedEncodingException e) {
            throw new ResourceNotFoundException();
        }
    }

    private String decode (String iri) throws UnsupportedEncodingException {
        return UriUtils.decode(iri, "UTF-8");
    }


}
