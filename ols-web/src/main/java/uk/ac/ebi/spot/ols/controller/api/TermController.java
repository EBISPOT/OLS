package uk.ac.ebi.spot.ols.controller.api;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.RepositoryLinksResource;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriUtils;

import uk.ac.ebi.spot.ols.neo4j.model.Term;
import uk.ac.ebi.spot.ols.neo4j.service.OntologyTermGraphService;

/**
 * @author Simon Jupp
 * @date 23/06/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@RestController
@RequestMapping("/api/terms")
@ExposesResourceFor(Term.class)
public class TermController implements
        ResourceProcessor<RepositoryLinksResource> {

    @Autowired
    private OntologyTermGraphService ontologyTermGraphService;

    @Autowired TermAssembler termAssembler;

    @RequestMapping(path = "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    private HttpEntity<PagedResources<Term>> getTermsByIri( @PathVariable("id") String termId,
                                                    Pageable pageable,
                                                    PagedResourcesAssembler assembler
    ) throws ResourceNotFoundException {
    	
        String decoded = null;
        try {
            decoded = UriUtils.decode(termId, "UTF-8");
        	
        } catch (UnsupportedEncodingException e) {
            throw new ResourceNotFoundException("Can't decode IRI: " + termId);
        }
        return getTerms(decoded, null, null,null, pageable, assembler);
    }

    @RequestMapping(path = "", produces = {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    private HttpEntity<PagedResources<Term>> getTerms(
            @RequestParam(value = "iri", required = false) String iri,
            @RequestParam(value = "short_form", required = false) String shortForm,
            @RequestParam(value = "obo_id", required = false) String oboId,
            @RequestParam(value = "id", required = false) String id,
            Pageable pageable,
            PagedResourcesAssembler assembler) {

        Page<Term> terms = null;

        if (iri != null) {
            terms = ontologyTermGraphService.findAllByIri(iri, pageable);
        }
        else if (shortForm != null) {
            terms = ontologyTermGraphService.findAllByShortForm(shortForm, pageable);
        }
        else if (oboId != null) {
            terms = ontologyTermGraphService.findAllByOboId(oboId, pageable);
        }
        else if (id != null) {
            terms = ontologyTermGraphService.findAllByIri(id,pageable);
            if (terms.getContent().isEmpty()) {
                terms = ontologyTermGraphService.findAllByShortForm(id,pageable);
                if (terms.getContent().isEmpty()) {
                    terms = ontologyTermGraphService.findAllByOboId(id,pageable);
                }
            }
        }
        else {
            terms = ontologyTermGraphService.findAll(pageable);
            if (terms == null) throw new ResourceNotFoundException("Ontology not found");
        }

        return new ResponseEntity<>( assembler.toResource(terms, termAssembler), HttpStatus.OK);
    }
    
    @RequestMapping(path = "/findByIdAndIsDefiningOntology/{id}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    private HttpEntity<PagedResources<Term>> getTermsByIdAndIsDefiningOntology( @PathVariable("id") String termId,
                                                    Pageable pageable,
                                                    PagedResourcesAssembler assembler
    ) throws ResourceNotFoundException {

        String decoded = null;
        try {
            decoded = UriUtils.decode(termId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new ResourceNotFoundException("Can't decode IRI: " + termId);
        }
        return getTermsByIdAndIsDefiningOntology(decoded, null, null,null, pageable, assembler);
    }    

    @RequestMapping(path = "/findByIdAndIsDefiningOntology", produces = {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    private HttpEntity<PagedResources<Term>> getTermsByIdAndIsDefiningOntology(
            @RequestParam(value = "iri", required = false) String iri,
            @RequestParam(value = "short_form", required = false) String shortForm,
            @RequestParam(value = "obo_id", required = false) String oboId,
            @RequestParam(value = "id", required = false) String id,
            Pageable pageable,
            PagedResourcesAssembler assembler) {

        Page<Term> terms = null;

        if (iri != null) {
        	terms = ontologyTermGraphService.findAllByIriAndIsDefiningOntology(iri, pageable);
        }
        else if (shortForm != null) {
        	terms = ontologyTermGraphService.findAllByShortFormAndIsDefiningOntology(shortForm, pageable);
        }
        else if (oboId != null) {
        	terms = ontologyTermGraphService.findAllByOboIdAndIsDefiningOntology(oboId, pageable);
        }
        else if (id != null) {
            terms = ontologyTermGraphService.findAllByIriAndIsDefiningOntology(id,pageable);
            if (terms.getContent().isEmpty()) {
                terms = ontologyTermGraphService.findAllByShortFormAndIsDefiningOntology(id,pageable);
                if (terms.getContent().isEmpty()) {
                    terms = ontologyTermGraphService.findAllByOboIdAndIsDefiningOntology(id,pageable);
                }
            }
        }       
        else {
        	terms = ontologyTermGraphService.findAllByIsDefiningOntology(pageable);
        	if (terms == null) throw new ResourceNotFoundException("Ontology not found");
        }
        return new ResponseEntity<>( assembler.toResource(terms, termAssembler), HttpStatus.OK);
    }
    
    
    
    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Resource not found")
    @ExceptionHandler(ResourceNotFoundException.class)
    public void handleError(HttpServletRequest req, Exception exception) {
    }

    @Override
    public RepositoryLinksResource process(RepositoryLinksResource resource) {
        resource.add(ControllerLinkBuilder.linkTo(TermController.class).withRel("terms"));
        return resource;
    }
}
