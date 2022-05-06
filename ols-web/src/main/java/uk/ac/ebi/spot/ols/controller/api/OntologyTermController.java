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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriUtils;

import uk.ac.ebi.spot.ols.controller.api.localization.LocalizedTerm;
import uk.ac.ebi.spot.ols.neo4j.model.Term;
import uk.ac.ebi.spot.ols.neo4j.service.ClassJsTreeBuilder;
import uk.ac.ebi.spot.ols.neo4j.service.OntologyTermGraphService;
import uk.ac.ebi.spot.ols.neo4j.service.ViewMode;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;

/**
 * @author Simon Jupp
 * @date 02/11/15
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@RestController
@RequestMapping("/api/ontologies")
public class OntologyTermController {

    @Autowired
    private OntologyTermGraphService ontologyTermGraphService;

    @Autowired 
    TermAssembler termAssembler;

    @Autowired 
    PreferredRootTermAssembler preferredRootTermAssembler;
    
    @Autowired
    ClassJsTreeBuilder jsTreeBuilder;


    @RequestMapping(path = "/{onto}/terms", produces = {MediaType.APPLICATION_JSON_VALUE, 
        MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    private HttpEntity<PagedResources<LocalizedTerm>> termsByOntology(
            @PathVariable("onto") String ontologyId,
            @RequestParam(value = "iri", required = false) String iri,
            @RequestParam(value = "short_form", required = false) String shortForm,
            @RequestParam(value = "obo_id", required = false) String oboId,
            @RequestParam(value = "lang", required = false, defaultValue = "en") String lang,
            Pageable pageable,
            PagedResourcesAssembler assembler) {

        Page<LocalizedTerm> terms = null;

        ontologyId = ontologyId.toLowerCase();
        if (iri != null) {
            Term term = ontologyTermGraphService.findByOntologyAndIri(ontologyId, iri);
            if (term == null) 
              throw new ResourceNotFoundException("No resource with " + oboId + " in " + ontologyId);
            terms =  new PageImpl<LocalizedTerm>(Arrays.asList(LocalizedTerm.fromTerm(lang, term)));
        }
        else if (shortForm != null) {
            Term term = ontologyTermGraphService.findByOntologyAndShortForm(ontologyId, shortForm);
            if (term == null) 
              throw new ResourceNotFoundException("No resource with " + oboId + " in " + ontologyId);
            terms =  new PageImpl<LocalizedTerm>(Arrays.asList(LocalizedTerm.fromTerm(lang, term)));
        }
        else if (oboId != null) {
            Term term = ontologyTermGraphService.findByOntologyAndOboId(ontologyId, oboId);
            if (term == null) 
              throw new ResourceNotFoundException("No resource with " + oboId + " in " + ontologyId);
            terms =  new PageImpl<LocalizedTerm>(Arrays.asList(LocalizedTerm.fromTerm(lang, term)));
        }
        else {
	    Page<Term> res = null;
            res = ontologyTermGraphService.findAllByOntology(ontologyId, pageable);
            if (res == null) throw new ResourceNotFoundException("Ontology not found");
	    terms = res.map(term -> LocalizedTerm.fromTerm(lang, term));
        }

        return new ResponseEntity<>( assembler.toResource(terms, termAssembler), HttpStatus.OK);
    }

    private Term getOneById(String ontologyId, String id) {

        Term term = null;

        term = ontologyTermGraphService.findByOntologyAndIri(ontologyId, id);
        if (term == null) {
            term = ontologyTermGraphService.findByOntologyAndShortForm(ontologyId, id);
            if (term == null) {
                term = ontologyTermGraphService.findByOntologyAndOboId(ontologyId, id);
            }
        }
        return term;
    }

    private String getIdFromMultipleOptions (String iri, String shortForm, String oboId, String id) {
        if (id == null) {

            if (iri != null) {
                id = iri;
            }
            else if (shortForm != null) {
                id = shortForm;
            }
            else if (oboId != null) {
                id = oboId;
            }
        }
        return id;
    }

    @RequestMapping(path = "/{onto}/terms/roots", produces = {MediaType.APPLICATION_JSON_VALUE, 
        MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<LocalizedTerm>> getRoots(
            @PathVariable("onto") String ontologyId,
            @RequestParam(value = "lang", defaultValue = "en", required = false) String lang,
            @RequestParam(value = "includeObsoletes", defaultValue = "false", required = false)
                    boolean includeObsoletes,
            Pageable pageable,
            PagedResourcesAssembler assembler
    ) throws ResourceNotFoundException {
        ontologyId = ontologyId.toLowerCase();

        Page<Term> roots = ontologyTermGraphService.getRoots(ontologyId, includeObsoletes, pageable);
        if (roots == null) 
          throw new ResourceNotFoundException("No roots could be found for " + ontologyId );

	Page<LocalizedTerm> localized = roots.map(term -> LocalizedTerm.fromTerm(lang, term));

        return new ResponseEntity<>( assembler.toResource(localized, termAssembler), HttpStatus.OK);
    }

    @RequestMapping(path = "/{onto}/terms/preferredRoots", produces = {MediaType.APPLICATION_JSON_VALUE, 
        MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<LocalizedTerm>> getPreferredRoots(
            @PathVariable("onto") String ontologyId,
            @RequestParam(value = "lang", defaultValue = "en", required = false) String lang,
            @RequestParam(value = "includeObsoletes", defaultValue = "false", required = false) 
              boolean includeObsoletes,
            Pageable pageable,
            PagedResourcesAssembler assembler
    ) throws ResourceNotFoundException {
        ontologyId = ontologyId.toLowerCase();

        Page<Term> preferredRoots = ontologyTermGraphService.getPreferredRootTerms(ontologyId,
            includeObsoletes, pageable);
        
        if (preferredRoots == null) 
          throw new ResourceNotFoundException("No preferred roots could be found for " + ontologyId);

	Page<LocalizedTerm> localized = preferredRoots.map(term -> LocalizedTerm.fromTerm(lang, term));

        return new ResponseEntity<>(assembler.toResource(localized, preferredRootTermAssembler), 
            HttpStatus.OK);
    }    
    
    @RequestMapping(path = "/{onto}/terms/{id}", produces = {MediaType.APPLICATION_JSON_VALUE, 
        MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<Resource<LocalizedTerm>> getTerm(@PathVariable("onto") String ontologyId, 
            @RequestParam(value = "lang", defaultValue = "en", required = false) String lang,
        @PathVariable("id") String termId) 
            throws ResourceNotFoundException {
      
        ontologyId = ontologyId.toLowerCase();

        try {
            String decoded = UriUtils.decode(termId, "UTF-8");
            Term term = ontologyTermGraphService.findByOntologyAndIri(ontologyId, decoded);
            if (term == null) throw  new ResourceNotFoundException("No term with id " + decoded + 
                " in " + ontologyId);

            return new ResponseEntity<>( termAssembler.toResource(LocalizedTerm.fromTerm(lang, term)), HttpStatus.OK);
        } catch (UnsupportedEncodingException e) {
            throw new ResourceNotFoundException();
        }
    }

    @RequestMapping(path = "/{onto}/terms/{id}/parents", produces = {MediaType.APPLICATION_JSON_VALUE, 
        MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<LocalizedTerm>> getParents(@PathVariable("onto") String ontologyId, 
            @RequestParam(value = "lang", defaultValue = "en", required = false) String lang,
        @PathVariable("id") String termId, Pageable pageable, PagedResourcesAssembler assembler) {
      
        ontologyId = ontologyId.toLowerCase();

        try {
            String decoded = UriUtils.decode(termId, "UTF-8");
            Page<Term> parents = ontologyTermGraphService.getParents(ontologyId, decoded, pageable);
            if (parents == null) throw  new ResourceNotFoundException();

	    Page<LocalizedTerm> localized = parents.map(term -> LocalizedTerm.fromTerm(lang, term));

            return new ResponseEntity<>( assembler.toResource(localized, termAssembler), HttpStatus.OK);
        }
        catch (UnsupportedEncodingException e) {
            throw new ResourceNotFoundException();
        }
    }

    @RequestMapping(path = "/{onto}/terms/{id}/hierarchicalParents", produces = 
      {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<LocalizedTerm>> getHierarchicalParents(@PathVariable("onto") String ontologyId, 
            @RequestParam(value = "lang", defaultValue = "en", required = false) String lang,
        @PathVariable("id") String termId, Pageable pageable, PagedResourcesAssembler assembler) {
      
        ontologyId = ontologyId.toLowerCase();

        try {
            String decoded = UriUtils.decode(termId, "UTF-8");
            Page<Term> parents = ontologyTermGraphService.getHierarchicalParents(ontologyId, decoded, pageable);
            if (parents == null) 
              throw new ResourceNotFoundException("No parents could be found for " + ontologyId
                  + " and " + termId);

	    Page<LocalizedTerm> localized = parents.map(term -> LocalizedTerm.fromTerm(lang, term));

            return new ResponseEntity<>(assembler.toResource(localized, termAssembler), HttpStatus.OK);
        }
        catch (UnsupportedEncodingException e) {
            throw new ResourceNotFoundException();
        }
    }

    @RequestMapping(path = "/{onto}/terms/{id}/hierarchicalAncestors", produces = 
      {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<LocalizedTerm>> getHierarchicalAncestors(@PathVariable("onto") String ontologyId, 
            @RequestParam(value = "lang", defaultValue = "en", required = false) String lang,
        @PathVariable("id") String termId, Pageable pageable, PagedResourcesAssembler assembler) {
      
        ontologyId = ontologyId.toLowerCase();

        try {
            String decoded = UriUtils.decode(termId, "UTF-8");
            Page<Term> parents = ontologyTermGraphService.getHierarchicalAncestors(ontologyId, 
                decoded, pageable);
            if (parents == null) 
              throw new ResourceNotFoundException("No ancestors could be found for " + ontologyId
                  + " and " + termId);

	    Page<LocalizedTerm> localized = parents.map(term -> LocalizedTerm.fromTerm(lang, term));

            return new ResponseEntity<>(assembler.toResource(localized, termAssembler), HttpStatus.OK);
        }
        catch (UnsupportedEncodingException e) {
            throw new ResourceNotFoundException();
        }
    }

    @RequestMapping(path = "/{onto}/terms/{id}/children", produces = {MediaType.APPLICATION_JSON_VALUE, 
        MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<LocalizedTerm>> children(@PathVariable("onto") String ontologyId, 
            @RequestParam(value = "lang", defaultValue = "en", required = false) String lang,
        @PathVariable("id") String termId, Pageable pageable, PagedResourcesAssembler assembler) {
      
        ontologyId = ontologyId.toLowerCase();

        try {
            String decoded = UriUtils.decode(termId, "UTF-8");
            Page<Term> children = ontologyTermGraphService.getChildren(ontologyId, decoded, pageable);
            if (children == null) 
              throw  new ResourceNotFoundException("No children could be found for " + ontologyId
                  + " and " + termId);

	    Page<LocalizedTerm> localized = children.map(term -> LocalizedTerm.fromTerm(lang, term));

            return new ResponseEntity<>( assembler.toResource(localized, termAssembler), HttpStatus.OK);
        }
        catch (UnsupportedEncodingException e) {
            throw new ResourceNotFoundException();
        }
    }

    @RequestMapping(path = "/{onto}/terms/{id}/hierarchicalChildren", produces = 
      {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<LocalizedTerm>> getHierarchicalChildren(@PathVariable("onto") String ontologyId, 
            @RequestParam(value = "lang", defaultValue = "en", required = false) String lang,
        @PathVariable("id") String termId, Pageable pageable, PagedResourcesAssembler assembler) {
      
        ontologyId = ontologyId.toLowerCase();

        try {
            String decoded = UriUtils.decode(termId, "UTF-8");
            Page<Term> children = ontologyTermGraphService.getHierarchicalChildren(ontologyId, 
                decoded, pageable);
            
            if (children == null) 
              throw new ResourceNotFoundException("No hierarchical children could be found for " 
                  + ontologyId + " and " + termId);

	    Page<LocalizedTerm> localized = children.map(term -> LocalizedTerm.fromTerm(lang, term));

            return new ResponseEntity<>(assembler.toResource(localized, termAssembler), HttpStatus.OK);
        }
        catch (UnsupportedEncodingException e) {
            throw new ResourceNotFoundException();
        }
    }

    @RequestMapping(path = "/{onto}/terms/{id}/hierarchicalDescendants", produces = 
      {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<LocalizedTerm>> getHierarchicalDescendants(@PathVariable("onto") String ontologyId, 
            @RequestParam(value = "lang", defaultValue = "en", required = false) String lang,
        @PathVariable("id") String termId, Pageable pageable, PagedResourcesAssembler assembler) {
        
        ontologyId = ontologyId.toLowerCase();

        try {
            String decoded = UriUtils.decode(termId, "UTF-8");
            Page<Term> children = ontologyTermGraphService.getHierarchicalDescendants(ontologyId, 
                decoded, pageable);
            if (children == null) 
              throw new ResourceNotFoundException("No hierarchical descendants could be found for " 
                  + ontologyId + " and " + termId);

	    Page<LocalizedTerm> localized = children.map(term -> LocalizedTerm.fromTerm(lang, term));

            return new ResponseEntity<>( assembler.toResource(localized, termAssembler), HttpStatus.OK);
        }
        catch (UnsupportedEncodingException e) {
            throw new ResourceNotFoundException();
        }
    }

    @RequestMapping(path = "/{onto}/terms/{id}/descendants", produces = {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<LocalizedTerm>> descendants(@PathVariable("onto") String ontologyId,
            @RequestParam(value = "lang", defaultValue = "en", required = false) String lang,
    @PathVariable("id") String termId, Pageable pageable,
                                                 PagedResourcesAssembler assembler) {
        ontologyId = ontologyId.toLowerCase();

        try {
            String decoded = UriUtils.decode(termId, "UTF-8");
            Page<Term> descendants = ontologyTermGraphService.getDescendants(ontologyId, decoded, pageable);
            if (descendants == null) throw  new ResourceNotFoundException();

	    Page<LocalizedTerm> localized = descendants.map(term -> LocalizedTerm.fromTerm(lang, term));

            return new ResponseEntity<>( assembler.toResource(localized, termAssembler), HttpStatus.OK);
        }
        catch (UnsupportedEncodingException e) {
            throw new ResourceNotFoundException();
        }
    }

    @RequestMapping(path = "/{onto}/terms/{id}/ancestors", 
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE}, 
        method = RequestMethod.GET)
    HttpEntity<PagedResources<LocalizedTerm>> ancestors(@PathVariable("onto") String ontologyId, 
            @RequestParam(value = "lang", defaultValue = "en", required = false) String lang,
    @PathVariable("id") String termId, Pageable pageable,
                                               PagedResourcesAssembler assembler) {
        ontologyId = ontologyId.toLowerCase();

        try {
            String decoded = UriUtils.decode(termId, "UTF-8");
            Page<Term> ancestors = ontologyTermGraphService.getAncestors(ontologyId, decoded, pageable);
            if (ancestors == null) throw  new ResourceNotFoundException();

	    Page<LocalizedTerm> localized = ancestors.map(term -> LocalizedTerm.fromTerm(lang, term));

            return new ResponseEntity<>( assembler.toResource(localized, termAssembler), HttpStatus.OK);
        }
        catch (UnsupportedEncodingException e) {
            throw new ResourceNotFoundException();
        }
    }

    @RequestMapping(path = "/{onto}/terms/{id}/jstree", 
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE}, 
        method = RequestMethod.GET)
    HttpEntity<String> graphJsTree(
            @PathVariable("onto") String ontologyId,
            @PathVariable("id") String termId,
            @RequestParam(value = "lang", defaultValue = "en", required = false) String lang,
            @RequestParam(value = "siblings", defaultValue = "false", required = false) boolean siblings,
            @RequestParam(value = "viewMode", defaultValue = "PreferredRoots", required = false) String viewMode){
      
        ontologyId = ontologyId.toLowerCase();

        try {
            String decodedTermId = UriUtils.decode(termId, "UTF-8");

            Object object= jsTreeBuilder.getJsTree(lang, ontologyId, decodedTermId, siblings, ViewMode.getFromShortName(viewMode));
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            return new HttpEntity<String>(ow.writeValueAsString(object));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        throw new ResourceNotFoundException();
    }

    @RequestMapping(path = "/{onto}/terms/{id}/jstree/children/{nodeid}", produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<String> graphJsTreeChildren(
            @PathVariable("onto") String ontologyId,
            @PathVariable("id") String termId,
            @PathVariable("nodeid") String nodeId,
            @RequestParam(value = "lang", defaultValue = "en", required = false) String lang
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

    @RequestMapping(path = "/{onto}/terms/{id}/graph", produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<String> graphJson(
            @PathVariable("onto") String ontologyId,
            @PathVariable("id") String termId ) {
        ontologyId = ontologyId.toLowerCase();

        try {
            String decoded = UriUtils.decode(termId, "UTF-8");

            Object object= ontologyTermGraphService.getGraphJson(ontologyId, decoded);
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            return new HttpEntity<String>(ow.writeValueAsString(object));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        throw new ResourceNotFoundException();
    }

    @RequestMapping(path = "/{onto}/terms/{id}/{relation}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<LocalizedTerm>> related(
            @RequestParam(value = "lang", defaultValue = "en", required = false) String lang,
	    @PathVariable("onto") String ontologyId, @PathVariable("id") String termId, @PathVariable("relation") String relation, Pageable pageable,
                                             PagedResourcesAssembler assembler) {
        ontologyId = ontologyId.toLowerCase();

        try {
            String decodedTerm = UriUtils.decode(termId, "UTF-8");
            String decodedRelation = UriUtils.decode(relation, "UTF-8");
            Page<Term> related = ontologyTermGraphService.getRelated(ontologyId, decodedTerm, decodedRelation, pageable);

	    Page<LocalizedTerm> localized = related.map(term -> LocalizedTerm.fromTerm(lang, term));

            return new ResponseEntity<>( assembler.toResource(localized, termAssembler), HttpStatus.OK);
        }
        catch (UnsupportedEncodingException e) {
            throw new ResourceNotFoundException();
        }
    }

    @RequestMapping(path = "/{onto}/children", produces = {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<LocalizedTerm>> termChildrenByOntology(
            @PathVariable("onto") String ontologyId,
            @RequestParam(value = "lang", defaultValue = "en", required = false) String lang,
            @RequestParam(value = "iri", required = false) String iri,
            @RequestParam(value = "short_form", required = false) String shortForm,
            @RequestParam(value = "obo_id", required = false) String oboId,
            @RequestParam(value = "id", required = false) String id,
            Pageable pageable,
            PagedResourcesAssembler assembler) {


        id = getIdFromMultipleOptions(iri, shortForm, oboId, id);
        if (id == null) {
            return new ResponseEntity<>( assembler.toResource(new PageImpl<Term>(Collections.emptyList()), termAssembler), HttpStatus.OK);
        }
        Term target = getOneById(ontologyId, id);
        ontologyId = ontologyId.toLowerCase();
        if (target == null) throw new ResourceNotFoundException("No resource with " + id + " in " + ontologyId);

        Page<Term>  terms = ontologyTermGraphService.getChildren(ontologyId, target.getIri(), pageable);

	Page<LocalizedTerm> localized = terms.map(term -> LocalizedTerm.fromTerm(lang, term));
	    
        return new ResponseEntity<>( assembler.toResource(localized, termAssembler), HttpStatus.OK);
    }

    @RequestMapping(path = "/{onto}/descendants", produces = {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<LocalizedTerm>> termDescendantsByOntology(
            @PathVariable("onto") String ontologyId,
            @RequestParam(value = "lang", defaultValue = "en", required = false) String lang,
            @RequestParam(value = "iri", required = false) String iri,
            @RequestParam(value = "short_form", required = false) String shortForm,
            @RequestParam(value = "obo_id", required = false) String oboId,
            @RequestParam(value = "id", required = false) String id,
            Pageable pageable,
            PagedResourcesAssembler assembler) {


        id = getIdFromMultipleOptions(iri, shortForm, oboId, id);
        if (id == null) {
            return new ResponseEntity<>( assembler.toResource(new PageImpl<Term>(Collections.emptyList()), termAssembler), HttpStatus.OK);
        }
        Term target = getOneById(ontologyId, id);
        ontologyId = ontologyId.toLowerCase();
        if (target == null) throw new ResourceNotFoundException("No resource with " + id + " in " + ontologyId);

        Page<Term>  terms = ontologyTermGraphService.getDescendants(ontologyId, target.getIri(), pageable);

	Page<LocalizedTerm> localized = terms.map(term -> LocalizedTerm.fromTerm(lang, term));

        return new ResponseEntity<>( assembler.toResource(localized, termAssembler), HttpStatus.OK);
    }

    @RequestMapping(path = "/{onto}/hierarchicalChildren", produces = {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<LocalizedTerm>> termHierarchicalChildrenByOntology(
            @PathVariable("onto") String ontologyId,
            @RequestParam(value = "lang", defaultValue = "en", required = false) String lang,
            @RequestParam(value = "iri", required = false) String iri,
            @RequestParam(value = "short_form", required = false) String shortForm,
            @RequestParam(value = "obo_id", required = false) String oboId,
            @RequestParam(value = "id", required = false) String id,
            Pageable pageable,
            PagedResourcesAssembler assembler) {


        id = getIdFromMultipleOptions(iri, shortForm, oboId, id);
        if (id == null) {
            return new ResponseEntity<>( assembler.toResource(new PageImpl<Term>(Collections.emptyList()), termAssembler), HttpStatus.OK);
        }
        Term target = getOneById(ontologyId, id);
        ontologyId = ontologyId.toLowerCase();
        if (target == null) throw new ResourceNotFoundException("No resource with " + id + " in " + ontologyId);

        Page<Term>  terms = ontologyTermGraphService.getHierarchicalChildren(ontologyId, target.getIri(), pageable);

	Page<LocalizedTerm> localized = terms.map(term -> LocalizedTerm.fromTerm(lang, term));

        return new ResponseEntity<>( assembler.toResource(localized, termAssembler), HttpStatus.OK);
    }

    @RequestMapping(path = "/{onto}/hierarchicalDescendants", produces = {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<LocalizedTerm>> termHierarchicalDescendantsByOntology(
            @PathVariable("onto") String ontologyId,
            @RequestParam(value = "lang", defaultValue = "en", required = false) String lang,
            @RequestParam(value = "iri", required = false) String iri,
            @RequestParam(value = "short_form", required = false) String shortForm,
            @RequestParam(value = "obo_id", required = false) String oboId,
            @RequestParam(value = "id", required = false) String id,
            Pageable pageable,
            PagedResourcesAssembler assembler) {


        id = getIdFromMultipleOptions(iri, shortForm, oboId, id);
        if (id == null) {
            return new ResponseEntity<>( assembler.toResource(new PageImpl<Term>(Collections.emptyList()), termAssembler), HttpStatus.OK);
        }
        Term target = getOneById(ontologyId, id);
        ontologyId = ontologyId.toLowerCase();
        if (target == null) throw new ResourceNotFoundException("No resource with " + id + " in " + ontologyId);

        Page<Term>  terms = ontologyTermGraphService.getHierarchicalDescendants(ontologyId, target.getIri(), pageable);
	Page<LocalizedTerm> localized = terms.map(term -> LocalizedTerm.fromTerm(lang, term));

        return new ResponseEntity<>( assembler.toResource(localized, termAssembler), HttpStatus.OK);
    }

    @RequestMapping(path = "/{onto}/parents", produces = {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<LocalizedTerm>> termParentsByOntology(
            @PathVariable("onto") String ontologyId,
            @RequestParam(value = "lang", defaultValue = "en", required = false) String lang,
            @RequestParam(value = "iri", required = false) String iri,
            @RequestParam(value = "short_form", required = false) String shortForm,
            @RequestParam(value = "obo_id", required = false) String oboId,
            @RequestParam(value = "id", required = false) String id,
            Pageable pageable,
            PagedResourcesAssembler assembler) {


        id = getIdFromMultipleOptions(iri, shortForm, oboId, id);
        if (id == null) {
            return new ResponseEntity<>( assembler.toResource(new PageImpl<Term>(Collections.emptyList()), termAssembler), HttpStatus.OK);
        }
        Term target = getOneById(ontologyId, id);
        ontologyId = ontologyId.toLowerCase();
        if (target == null) throw new ResourceNotFoundException("No resource with " + id + " in " + ontologyId);

        Page<Term>  terms = ontologyTermGraphService.getParents(ontologyId, target.getIri(), pageable);
	Page<LocalizedTerm> localized = terms.map(term -> LocalizedTerm.fromTerm(lang, term));

        return new ResponseEntity<>( assembler.toResource(localized, termAssembler), HttpStatus.OK);
    }

    @RequestMapping(path = "/{onto}/ancestors", produces = {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<LocalizedTerm>> termAncestorsByOntology(
            @PathVariable("onto") String ontologyId,
            @RequestParam(value = "lang", defaultValue = "en", required = false) String lang,
            @RequestParam(value = "iri", required = false) String iri,
            @RequestParam(value = "short_form", required = false) String shortForm,
            @RequestParam(value = "obo_id", required = false) String oboId,
            @RequestParam(value = "id", required = false) String id,
            Pageable pageable,
            PagedResourcesAssembler assembler) {


        id = getIdFromMultipleOptions(iri, shortForm, oboId, id);
        if (id == null) {
            return new ResponseEntity<>( assembler.toResource(new PageImpl<Term>(Collections.emptyList()), termAssembler), HttpStatus.OK);
        }
        Term target = getOneById(ontologyId, id);
        ontologyId = ontologyId.toLowerCase();
        if (target == null) throw new ResourceNotFoundException("No resource with " + id + " in " + ontologyId);

        Page<Term>  terms = ontologyTermGraphService.getAncestors(ontologyId, target.getIri(), pageable);
	Page<LocalizedTerm> localized = terms.map(term -> LocalizedTerm.fromTerm(lang, term));

        return new ResponseEntity<>( assembler.toResource(localized, termAssembler), HttpStatus.OK);
    }

    @RequestMapping(path = "/{onto}/hierarchicalAncestors", produces = {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<LocalizedTerm>> termHierarchicalAncestorsByOntology(
            @PathVariable("onto") String ontologyId,
            @RequestParam(value = "lang", defaultValue = "en", required = false) String lang,
            @RequestParam(value = "iri", required = false) String iri,
            @RequestParam(value = "short_form", required = false) String shortForm,
            @RequestParam(value = "obo_id", required = false) String oboId,
            @RequestParam(value = "id", required = false) String id,
            Pageable pageable,
            PagedResourcesAssembler assembler) {


        id = getIdFromMultipleOptions(iri, shortForm, oboId, id);
        if (id == null) {
            return new ResponseEntity<>( assembler.toResource(new PageImpl<Term>(Collections.emptyList()), termAssembler), HttpStatus.OK);
        }
        Term target = getOneById(ontologyId, id);
        ontologyId = ontologyId.toLowerCase();
        if (target == null) throw new ResourceNotFoundException("No resource with " + id + " in " + ontologyId);

        Page<Term>  terms = ontologyTermGraphService.getHierarchicalAncestors(ontologyId, target.getIri(), pageable);
	Page<LocalizedTerm> localized = terms.map(term -> LocalizedTerm.fromTerm(lang, term));

        return new ResponseEntity<>( assembler.toResource(localized, termAssembler), HttpStatus.OK);
    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Resource not found")
    @ExceptionHandler(ResourceNotFoundException.class)
    public void handleError(HttpServletRequest req, Exception exception) {

    }

}
