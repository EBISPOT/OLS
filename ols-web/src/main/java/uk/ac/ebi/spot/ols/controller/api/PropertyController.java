package uk.ac.ebi.spot.ols.controller.api;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.util.UriUtils;

import uk.ac.ebi.spot.ols.neo4j.model.Property;
import uk.ac.ebi.spot.ols.neo4j.service.OntologyPropertyGraphService;

/**
 * @author Simon Jupp
 * @date 23/06/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@Controller
@RequestMapping("/api/properties")
@ExposesResourceFor(Property.class)
public class PropertyController implements
        ResourceProcessor<RepositoryLinksResource> {

    @Autowired
    private OntologyPropertyGraphService ontologyPropertyGraphService;

    @Autowired
    PropertyAssembler termAssembler;

    @Override
    public RepositoryLinksResource process(RepositoryLinksResource resource) {
        resource.add(ControllerLinkBuilder.linkTo(PropertyController.class).withRel("properties"));
        return resource;
    }

    @RequestMapping(path = "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<Property>> getPropertiesByIri( @PathVariable("id") String termId,
                                                             Pageable pageable,
                                                             PagedResourcesAssembler assembler

    ) throws ResourceNotFoundException {

        String decoded = null;
        try {
            decoded = UriUtils.decode(termId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new ResourceNotFoundException("Can't decode IRI: " + termId);
        }
        return getAllProperties(decoded, null, null, pageable, assembler);
    }

    @RequestMapping(path = "", produces = {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<Property>> getAllProperties(
            @RequestParam(value = "iri", required = false) String iri,
            @RequestParam(value = "short_form", required = false) String shortForm,
            @RequestParam(value = "obo_id", required = false) String oboId,
            Pageable pageable,
            PagedResourcesAssembler assembler) {

        Page<Property> terms = null;

        if (iri != null) {
            terms = ontologyPropertyGraphService.findAllByIri(iri, pageable);
        }
        else if (shortForm != null) {
            terms = ontologyPropertyGraphService.findAllByShortForm(shortForm, pageable);
        }
        else if (oboId != null) {
            terms = ontologyPropertyGraphService.findAllByOboId(oboId, pageable);
        }
        else {
            terms = ontologyPropertyGraphService.findAll(pageable);
        }

        return new ResponseEntity<>( assembler.toResource(terms, termAssembler), HttpStatus.OK);
    }


    @RequestMapping(path = "/findByIdAndIsDefiningOntology/{id}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<Property>> getPropertiesByIriAndIsDefiningOntology( @PathVariable("id") String termId,
                                                             Pageable pageable,
                                                             PagedResourcesAssembler assembler

    ) throws ResourceNotFoundException {

        String decoded = null;
        try {
            decoded = UriUtils.decode(termId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new ResourceNotFoundException("Can't decode IRI: " + termId);
        }
        return getPropertiesByIdAndIsDefiningOntology(decoded, null, null, pageable, assembler);
    }    
    
    @RequestMapping(path = "/findByIdAndIsDefiningOntology", produces = {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<Property>> getPropertiesByIdAndIsDefiningOntology(
            @RequestParam(value = "iri", required = false) String iri,
            @RequestParam(value = "short_form", required = false) String shortForm,
            @RequestParam(value = "obo_id", required = false) String oboId,
            Pageable pageable,
            PagedResourcesAssembler assembler) {

        Page<Property> terms = null;

        if (iri != null) {
            terms = ontologyPropertyGraphService.findAllByIriAndIsDefiningOntology(iri, pageable);
        }
        else if (shortForm != null) {
            terms = ontologyPropertyGraphService.findAllByShortFormAndIsDefiningOntology(shortForm, pageable);
        }
        else if (oboId != null) {
            terms = ontologyPropertyGraphService.findAllByOboIdAndIsDefiningOntology(oboId, pageable);
        }
        else {
            terms = ontologyPropertyGraphService.findAllByIsDefiningOntology(pageable);
        }

        return new ResponseEntity<>( assembler.toResource(terms, termAssembler), HttpStatus.OK);
    }
    
    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Resource not found")
    @ExceptionHandler(ResourceNotFoundException.class)
    public void handleError(HttpServletRequest req, Exception exception) {
    }
}
