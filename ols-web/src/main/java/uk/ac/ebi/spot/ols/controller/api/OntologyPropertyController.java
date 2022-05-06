package uk.ac.ebi.spot.ols.controller.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriUtils;
import uk.ac.ebi.spot.ols.neo4j.model.Property;
import uk.ac.ebi.spot.ols.neo4j.service.JsTreeBuilder;
import uk.ac.ebi.spot.ols.neo4j.service.OntologyPropertyGraphService;
import uk.ac.ebi.spot.ols.neo4j.service.PropertyJsTreeBuilder;
import uk.ac.ebi.spot.ols.neo4j.service.ViewMode;
import uk.ac.ebi.spot.ols.controller.api.localization.LocalizedProperty;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * @author Simon Jupp
 * @date 02/11/15
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@Controller
@RequestMapping("/api/ontologies")
public class OntologyPropertyController {

    @Autowired
    private OntologyPropertyGraphService ontologyPropertyGraphService;

    @Autowired
    PropertyAssembler termAssembler;

    @Autowired
    PropertyJsTreeBuilder jsTreeBuilder;

    @RequestMapping(path = "/{onto}/properties", produces = {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<LocalizedProperty>> getAllPropertiesByOntology(
            @PathVariable("onto") String ontologyId,
            @RequestParam(value = "lang", required = false, defaultValue = "en") String lang,
            @RequestParam(value = "iri", required = false) String iri,
            @RequestParam(value = "short_form", required = false) String shortForm,
            @RequestParam(value = "obo_id", required = false) String oboId,
            Pageable pageable,
            PagedResourcesAssembler assembler) {

        Page<LocalizedProperty> terms = null;

        ontologyId = ontologyId.toLowerCase();
        if (iri != null) {
            Property term = ontologyPropertyGraphService.findByOntologyAndIri(ontologyId, iri);
            if (term != null) {
		terms = new PageImpl<LocalizedProperty>(Arrays.asList(LocalizedProperty.fromProperty(lang, term)));
            }
        }
        else if (shortForm != null) {
            Property term = ontologyPropertyGraphService.findByOntologyAndShortForm(ontologyId, shortForm);
            if (term != null) {
		terms = new PageImpl<LocalizedProperty>(Arrays.asList(LocalizedProperty.fromProperty(lang, term)));
            }
        }
        else if (oboId != null) {
            Property term = ontologyPropertyGraphService.findByOntologyAndOboId(ontologyId, oboId);
            if (term != null) {
		terms = new PageImpl<LocalizedProperty>(Arrays.asList(LocalizedProperty.fromProperty(lang, term)));
            }
        }
        else {
	    Page<Property> res = null;
            res = ontologyPropertyGraphService.findAllByOntology(ontologyId, pageable);
            if (res == null) throw new ResourceNotFoundException("Ontology not found");
	    terms = res.map(term -> LocalizedProperty.fromProperty(lang, term));
        }

        return new ResponseEntity<>( assembler.toResource(terms, termAssembler), HttpStatus.OK);
    }

    @RequestMapping(path = "/{onto}/properties/roots", produces = {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<LocalizedProperty>> getRoots(
            @PathVariable("onto") String ontologyId,
            @RequestParam(value = "lang", required = false, defaultValue = "en") String lang,
            @RequestParam(value = "includeObsoletes", defaultValue = "false", required = false) boolean includeObsoletes,
            Pageable pageable,
            PagedResourcesAssembler assembler
    ) throws ResourceNotFoundException {
        ontologyId = ontologyId.toLowerCase();

        Page<Property> roots = ontologyPropertyGraphService.getRoots(ontologyId, includeObsoletes, pageable);
        if (roots == null) throw  new ResourceNotFoundException();
	Page<LocalizedProperty> localized = roots.map(term -> LocalizedProperty.fromProperty(lang, term));
        return new ResponseEntity<>( assembler.toResource(localized, termAssembler), HttpStatus.OK);
    }

    @RequestMapping(path = "/{onto}/properties/{id}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<Resource<LocalizedProperty>> getProperty(@PathVariable("onto") String ontologyId, 
            @RequestParam(value = "lang", required = false, defaultValue = "en") String lang,
    @PathVariable("id") String termId) throws ResourceNotFoundException {
        ontologyId = ontologyId.toLowerCase();

        try {
            String decoded = UriUtils.decode(termId, "UTF-8");
            Property term = ontologyPropertyGraphService.findByOntologyAndIri(ontologyId, decoded);
            return new ResponseEntity<>( termAssembler.toResource(LocalizedProperty.fromProperty(lang, term)), HttpStatus.OK);
        } catch (UnsupportedEncodingException e) {
            throw new ResourceNotFoundException();
        }
    }

    @RequestMapping(path = "/{onto}/properties/{id}/parents", produces = {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<LocalizedProperty>> getParents(@PathVariable("onto") String ontologyId,
            @RequestParam(value = "lang", required = false, defaultValue = "en") String lang,
    @PathVariable("id") String termId, Pageable pageable,
                                                    PagedResourcesAssembler assembler) {
        ontologyId = ontologyId.toLowerCase();

        try {
            String decoded = UriUtils.decode(termId, "UTF-8");
            Page<Property> parents = ontologyPropertyGraphService.getParents(ontologyId, decoded, pageable);

		Page<LocalizedProperty> localized = parents.map(term -> LocalizedProperty.fromProperty(lang, term));
		return new ResponseEntity<>(assembler.toResource(localized, termAssembler), HttpStatus.OK);
        }
        catch (UnsupportedEncodingException e) {
            throw new ResourceNotFoundException();
        }
    }

    @RequestMapping(path = "/{onto}/properties/{id}/children", produces = {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<LocalizedProperty>> children(@PathVariable("onto") String ontologyId, 
            @RequestParam(value = "lang", required = false, defaultValue = "en") String lang,
    @PathVariable("id") String termId, Pageable pageable,
                                                  PagedResourcesAssembler assembler) {
        ontologyId = ontologyId.toLowerCase();

        try {
            String decoded = UriUtils.decode(termId, "UTF-8");
            Page<Property> children = ontologyPropertyGraphService.getChildren(ontologyId, decoded, pageable);

		Page<LocalizedProperty> localized = children.map(term -> LocalizedProperty.fromProperty(lang, term));
		return new ResponseEntity<>(assembler.toResource(localized, termAssembler), HttpStatus.OK);
        }
        catch (UnsupportedEncodingException e) {
            throw new ResourceNotFoundException();
        }
    }

    @RequestMapping(path = "/{onto}/properties/{id}/descendants", produces = {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<LocalizedProperty>> descendants(@PathVariable("onto") String ontologyId,
            @RequestParam(value = "lang", required = false, defaultValue = "en") String lang,
     @PathVariable("id") String termId, Pageable pageable,
                                                     PagedResourcesAssembler assembler) {
        ontologyId = ontologyId.toLowerCase();

        try {
            String decoded = UriUtils.decode(termId, "UTF-8");
            Page<Property> descendants = ontologyPropertyGraphService.getDescendants(ontologyId, decoded, pageable);

		Page<LocalizedProperty> localized = descendants.map(term -> LocalizedProperty.fromProperty(lang, term));
		return new ResponseEntity<>(assembler.toResource(localized, termAssembler), HttpStatus.OK);
        }
        catch (UnsupportedEncodingException e) {
            throw new ResourceNotFoundException();
        }
    }

    @RequestMapping(path = "/{onto}/properties/{id}/ancestors", produces = {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<LocalizedProperty>> ancestors(@PathVariable("onto") String ontologyId,
            @RequestParam(value = "lang", required = false, defaultValue = "en") String lang,
     @PathVariable("id") String termId, Pageable pageable,
                                                   PagedResourcesAssembler assembler) {
        ontologyId = ontologyId.toLowerCase();

        try {
            String decoded = UriUtils.decode(termId, "UTF-8");
            Page<Property> ancestors = ontologyPropertyGraphService.getAncestors(ontologyId, decoded, pageable);

		Page<LocalizedProperty> localized = ancestors.map(term -> LocalizedProperty.fromProperty(lang, term));
		return new ResponseEntity<>(assembler.toResource(localized, termAssembler), HttpStatus.OK);
        }
        catch (UnsupportedEncodingException e) {
            throw new ResourceNotFoundException();
        }
    }


    @RequestMapping(path = "/{onto}/properties/{id}/jstree/children/{nodeid}", produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<String> graphJsTreeChildren(
            @PathVariable("onto") String ontologyId,
            @RequestParam(value = "lang", required = false, defaultValue = "en") String lang,
            @PathVariable("id") String termId,
            @PathVariable("nodeid") String nodeId
    ) {
        ontologyId = ontologyId.toLowerCase();

        try {
            String decoded = UriUtils.decode(termId, "UTF-8");

            Object object= jsTreeBuilder.getJsTreeChildren(lang, ontologyId, decoded, nodeId);
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            return new HttpEntity<String>(ow.writeValueAsString(object));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        throw new ResourceNotFoundException();
    }

    @RequestMapping(path = "/{onto}/properties/{id}/jstree",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE},
            method = RequestMethod.GET)
    HttpEntity<String> getJsTree(
            @PathVariable("onto") String ontologyId,
            @RequestParam(value = "lang", required = false, defaultValue = "en") String lang,
            @PathVariable("id") String termId,
            @RequestParam(value = "siblings", defaultValue = "false", required = false) boolean siblings,
            @RequestParam(value = "viewMode", defaultValue = "PreferredRoots", required = false) String viewMode)
    {
        ontologyId = ontologyId.toLowerCase();

        try {
            String decoded = UriUtils.decode(termId, "UTF-8");

            Object object= jsTreeBuilder.getJsTree(lang, ontologyId, decoded, siblings, ViewMode.getFromShortName(viewMode));
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            return new HttpEntity<String>(ow.writeValueAsString(object));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        throw new ResourceNotFoundException();
    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Resource not found")
    @ExceptionHandler(ResourceNotFoundException.class)
    public void handleError(HttpServletRequest req, Exception exception) {
    }
}
