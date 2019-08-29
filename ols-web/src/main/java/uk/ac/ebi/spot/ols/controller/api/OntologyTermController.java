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
    private HttpEntity<PagedResources<Term>> termsByOntology(
            @PathVariable("onto") String ontologyId,
            @RequestParam(value = "iri", required = false) String iri,
            @RequestParam(value = "short_form", required = false) String shortForm,
            @RequestParam(value = "obo_id", required = false) String oboId,
            Pageable pageable,
            PagedResourcesAssembler assembler) {

        Page<Term> terms = null;

        ontologyId = ontologyId.toLowerCase();
        if (iri != null) {
            Term term = ontologyTermGraphService.findByOntologyAndIri(ontologyId, iri);
            if (term == null) 
              throw new ResourceNotFoundException("No resource with " + oboId + " in " + ontologyId);
            terms =  new PageImpl<Term>(Arrays.asList(term));
        }
        else if (shortForm != null) {
            Term term = ontologyTermGraphService.findByOntologyAndShortForm(ontologyId, shortForm);
            if (term == null) 
              throw new ResourceNotFoundException("No resource with " + oboId + " in " + ontologyId);
            terms =  new PageImpl<Term>(Arrays.asList(term));
        }
        else if (oboId != null) {
            Term term = ontologyTermGraphService.findByOntologyAndOboId(ontologyId, oboId);
            if (term == null) 
              throw new ResourceNotFoundException("No resource with " + oboId + " in " + ontologyId);
            terms =  new PageImpl<Term>(Arrays.asList(term));
        }
        else {
            terms = ontologyTermGraphService.findAllByOntology(ontologyId, pageable);
            if (terms == null) throw new ResourceNotFoundException("Ontology not found");
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
    HttpEntity<PagedResources<Term>> getRoots(
            @PathVariable("onto") String ontologyId,
            @RequestParam(value = "includeObsoletes", defaultValue = "false", required = false) 
              boolean includeObsoletes,
            Pageable pageable,
            PagedResourcesAssembler assembler
    ) throws ResourceNotFoundException {
        ontologyId = ontologyId.toLowerCase();

        Page<Term> roots = ontologyTermGraphService.getRoots(ontologyId, includeObsoletes, pageable);
        if (roots == null) 
          throw new ResourceNotFoundException("No roots could be found for " + ontologyId );
        return new ResponseEntity<>( assembler.toResource(roots, termAssembler), HttpStatus.OK);
    }

    @RequestMapping(path = "/{onto}/terms/preferredRoots", produces = {MediaType.APPLICATION_JSON_VALUE, 
        MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<Term>> getPreferredRoots(
            @PathVariable("onto") String ontologyId,
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
        return new ResponseEntity<>(assembler.toResource(preferredRoots, preferredRootTermAssembler), 
            HttpStatus.OK);
    }    
    
    @RequestMapping(path = "/{onto}/terms/{id}", produces = {MediaType.APPLICATION_JSON_VALUE, 
        MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<Resource<Term>> getTerm(@PathVariable("onto") String ontologyId, 
        @PathVariable("id") String termId) 
            throws ResourceNotFoundException {
      
        ontologyId = ontologyId.toLowerCase();

        try {
            String decoded = UriUtils.decode(termId, "UTF-8");
            Term term = ontologyTermGraphService.findByOntologyAndIri(ontologyId, decoded);
            if (term == null) throw  new ResourceNotFoundException("No term with id " + decoded + 
                " in " + ontologyId);

            return new ResponseEntity<>( termAssembler.toResource(term), HttpStatus.OK);
        } catch (UnsupportedEncodingException e) {
            throw new ResourceNotFoundException();
        }
    }

    @RequestMapping(path = "/{onto}/terms/{id}/parents", produces = {MediaType.APPLICATION_JSON_VALUE, 
        MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<Term>> getParents(@PathVariable("onto") String ontologyId, 
        @PathVariable("id") String termId, Pageable pageable, PagedResourcesAssembler assembler) {
      
        ontologyId = ontologyId.toLowerCase();

        try {
            String decoded = UriUtils.decode(termId, "UTF-8");
            Page<Term> parents = ontologyTermGraphService.getParents(ontologyId, decoded, pageable);
            if (parents == null) throw  new ResourceNotFoundException();

            return new ResponseEntity<>( assembler.toResource(parents, termAssembler), HttpStatus.OK);
        }
        catch (UnsupportedEncodingException e) {
            throw new ResourceNotFoundException();
        }
    }

    @RequestMapping(path = "/{onto}/terms/{id}/hierarchicalParents", produces = 
      {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<Term>> getHierarchicalParents(@PathVariable("onto") String ontologyId, 
        @PathVariable("id") String termId, Pageable pageable, PagedResourcesAssembler assembler) {
      
        ontologyId = ontologyId.toLowerCase();

        try {
            String decoded = UriUtils.decode(termId, "UTF-8");
            Page<Term> parents = ontologyTermGraphService.getHierarchicalParents(ontologyId, decoded, pageable);
            if (parents == null) 
              throw new ResourceNotFoundException("No parents could be found for " + ontologyId
                  + " and " + termId);

            return new ResponseEntity<>(assembler.toResource(parents, termAssembler), HttpStatus.OK);
        }
        catch (UnsupportedEncodingException e) {
            throw new ResourceNotFoundException();
        }
    }

    @RequestMapping(path = "/{onto}/terms/{id}/hierarchicalAncestors", produces = 
      {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<Term>> getHierarchicalAncestors(@PathVariable("onto") String ontologyId, 
        @PathVariable("id") String termId, Pageable pageable, PagedResourcesAssembler assembler) {
      
        ontologyId = ontologyId.toLowerCase();

        try {
            String decoded = UriUtils.decode(termId, "UTF-8");
            Page<Term> parents = ontologyTermGraphService.getHierarchicalAncestors(ontologyId, 
                decoded, pageable);
            if (parents == null) 
              throw new ResourceNotFoundException("No ancestors could be found for " + ontologyId
                  + " and " + termId);

            return new ResponseEntity<>(assembler.toResource(parents, termAssembler), HttpStatus.OK);
        }
        catch (UnsupportedEncodingException e) {
            throw new ResourceNotFoundException();
        }
    }

    @RequestMapping(path = "/{onto}/terms/{id}/children", produces = {MediaType.APPLICATION_JSON_VALUE, 
        MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<Term>> children(@PathVariable("onto") String ontologyId, 
        @PathVariable("id") String termId, Pageable pageable, PagedResourcesAssembler assembler) {
      
        ontologyId = ontologyId.toLowerCase();

        try {
            String decoded = UriUtils.decode(termId, "UTF-8");
            Page<Term> children = ontologyTermGraphService.getChildren(ontologyId, decoded, pageable);
            if (children == null) 
              throw  new ResourceNotFoundException("No children could be found for " + ontologyId
                  + " and " + termId);

            return new ResponseEntity<>( assembler.toResource(children, termAssembler), HttpStatus.OK);
        }
        catch (UnsupportedEncodingException e) {
            throw new ResourceNotFoundException();
        }
    }

    @RequestMapping(path = "/{onto}/terms/{id}/hierarchicalChildren", produces = 
      {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<Term>> getHierarchicalChildren(@PathVariable("onto") String ontologyId, 
        @PathVariable("id") String termId, Pageable pageable, PagedResourcesAssembler assembler) {
      
        ontologyId = ontologyId.toLowerCase();

        try {
            String decoded = UriUtils.decode(termId, "UTF-8");
            Page<Term> children = ontologyTermGraphService.getHierarchicalChildren(ontologyId, 
                decoded, pageable);
            
            if (children == null) 
              throw new ResourceNotFoundException("No hierarchical children could be found for " 
                  + ontologyId + " and " + termId);

            return new ResponseEntity<>(assembler.toResource(children, termAssembler), HttpStatus.OK);
        }
        catch (UnsupportedEncodingException e) {
            throw new ResourceNotFoundException();
        }
    }

    @RequestMapping(path = "/{onto}/terms/{id}/hierarchicalDescendants", produces = 
      {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<Term>> getHierarchicalDescendants(@PathVariable("onto") String ontologyId, 
        @PathVariable("id") String termId, Pageable pageable, PagedResourcesAssembler assembler) {
        
        ontologyId = ontologyId.toLowerCase();

        try {
            String decoded = UriUtils.decode(termId, "UTF-8");
            Page<Term> children = ontologyTermGraphService.getHierarchicalDescendants(ontologyId, 
                decoded, pageable);
            if (children == null) 
              throw new ResourceNotFoundException("No hierarchical descendants could be found for " 
                  + ontologyId + " and " + termId);

            return new ResponseEntity<>( assembler.toResource(children, termAssembler), HttpStatus.OK);
        }
        catch (UnsupportedEncodingException e) {
            throw new ResourceNotFoundException();
        }
    }

    @RequestMapping(path = "/{onto}/terms/{id}/descendants", produces = {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<Term>> descendants(@PathVariable("onto") String ontologyId, @PathVariable("id") String termId, Pageable pageable,
                                                 PagedResourcesAssembler assembler) {
        ontologyId = ontologyId.toLowerCase();

        try {
            String decoded = UriUtils.decode(termId, "UTF-8");
            Page<Term> descendants = ontologyTermGraphService.getDescendants(ontologyId, decoded, pageable);
            if (descendants == null) throw  new ResourceNotFoundException();

            return new ResponseEntity<>( assembler.toResource(descendants, termAssembler), HttpStatus.OK);
        }
        catch (UnsupportedEncodingException e) {
            throw new ResourceNotFoundException();
        }
    }

    @RequestMapping(path = "/{onto}/terms/{id}/ancestors", 
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE}, 
        method = RequestMethod.GET)
    HttpEntity<PagedResources<Term>> ancestors(@PathVariable("onto") String ontologyId, @PathVariable("id") String termId, Pageable pageable,
                                               PagedResourcesAssembler assembler) {
        ontologyId = ontologyId.toLowerCase();

        try {
            String decoded = UriUtils.decode(termId, "UTF-8");
            Page<Term> ancestors = ontologyTermGraphService.getAncestors(ontologyId, decoded, pageable);
            if (ancestors == null) throw  new ResourceNotFoundException();

            return new ResponseEntity<>( assembler.toResource(ancestors, termAssembler), HttpStatus.OK);
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
            @RequestParam(value = "siblings", defaultValue = "false", required = false) boolean siblings,
            @RequestParam(value = "viewMode", defaultValue = "PreferredRoots", required = false) String viewMode){
      
        ontologyId = ontologyId.toLowerCase();

        try {
            String decodedTermId = UriUtils.decode(termId, "UTF-8");

            Object object= jsTreeBuilder.getJsTree(ontologyId, decodedTermId, siblings, ViewMode.getFromShortName(viewMode));
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
            @PathVariable("nodeid") String nodeId
    ) {
        ontologyId = ontologyId.toLowerCase();

        try {
            String decoded = UriUtils.decode(termId, "UTF-8");

            Object object= jsTreeBuilder.getJsTreeChildren(ontologyId, decoded, nodeId);
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
    HttpEntity<PagedResources<Term>> related(@PathVariable("onto") String ontologyId, @PathVariable("id") String termId, @PathVariable("relation") String relation, Pageable pageable,
                                             PagedResourcesAssembler assembler) {
        ontologyId = ontologyId.toLowerCase();

        try {
            String decodedTerm = UriUtils.decode(termId, "UTF-8");
            String decodedRelation = UriUtils.decode(relation, "UTF-8");
            Page<Term> related = ontologyTermGraphService.getRelated(ontologyId, decodedTerm, decodedRelation, pageable);

            return new ResponseEntity<>( assembler.toResource(related, termAssembler), HttpStatus.OK);
        }
        catch (UnsupportedEncodingException e) {
            throw new ResourceNotFoundException();
        }
    }

    @RequestMapping(path = "/{onto}/children", produces = {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<Term>> termChildrenByOntology(
            @PathVariable("onto") String ontologyId,
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
        return new ResponseEntity<>( assembler.toResource(terms, termAssembler), HttpStatus.OK);
    }

    @RequestMapping(path = "/{onto}/descendants", produces = {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<Term>> termDescendantsByOntology(
            @PathVariable("onto") String ontologyId,
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
        return new ResponseEntity<>( assembler.toResource(terms, termAssembler), HttpStatus.OK);
    }

    @RequestMapping(path = "/{onto}/hierarchicalChildren", produces = {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<Term>> termHierarchicalChildrenByOntology(
            @PathVariable("onto") String ontologyId,
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
        return new ResponseEntity<>( assembler.toResource(terms, termAssembler), HttpStatus.OK);
    }

    @RequestMapping(path = "/{onto}/hierarchicalDescendants", produces = {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<Term>> termHierarchicalDescendantsByOntology(
            @PathVariable("onto") String ontologyId,
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
        return new ResponseEntity<>( assembler.toResource(terms, termAssembler), HttpStatus.OK);
    }

    @RequestMapping(path = "/{onto}/parents", produces = {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<Term>> termParentsByOntology(
            @PathVariable("onto") String ontologyId,
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
        return new ResponseEntity<>( assembler.toResource(terms, termAssembler), HttpStatus.OK);
    }

    @RequestMapping(path = "/{onto}/ancestors", produces = {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<Term>> termAncestorsByOntology(
            @PathVariable("onto") String ontologyId,
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
        return new ResponseEntity<>( assembler.toResource(terms, termAssembler), HttpStatus.OK);
    }

    @RequestMapping(path = "/{onto}/hierarchicalAncestors", produces = {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<Term>> termHierarchicalAncestorsByOntology(
            @PathVariable("onto") String ontologyId,
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
        return new ResponseEntity<>( assembler.toResource(terms, termAssembler), HttpStatus.OK);
    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Resource not found")
    @ExceptionHandler(ResourceNotFoundException.class)
    public void handleError(HttpServletRequest req, Exception exception) {

    }

}
