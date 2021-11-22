package uk.ac.ebi.spot.ols.controller.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.propertyeditors.CustomCollectionEditor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.rest.webmvc.RepositoryLinksResource;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.*;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriUtils;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import uk.ac.ebi.spot.ols.exception.ErrorMessage;
import uk.ac.ebi.spot.ols.model.OntologyDocument;
import uk.ac.ebi.spot.ols.neo4j.model.Term;
import uk.ac.ebi.spot.ols.neo4j.service.OntologyTermGraphService;
import uk.ac.ebi.spot.ols.service.OntologyRepositoryService;
import uk.ac.ebi.spot.ols.util.OLSEnv;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Simon Jupp
 * @date 19/08/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@Controller
@RequestMapping("/api/ontologies")
@ExposesResourceFor(OntologyDocument.class)
public class OntologyController implements
        ResourceProcessor<RepositoryLinksResource> {

    private Logger log = LoggerFactory.getLogger(getClass());

    public Logger getLog() {
        return log;
    }

    @Autowired
    private OntologyRepositoryService ontologyRepositoryService;

    @Autowired DocumentAssembler documentAssembler;

    @Autowired TermAssembler termAssembler;
    
    @InitBinder()
    public void initBinder(WebDataBinder binder) throws Exception
    {
       binder.registerCustomEditor(Collection.class, new CustomCollectionEditor(List.class));
    }

    @Override
    public RepositoryLinksResource process(RepositoryLinksResource resource) {
        resource.add(ControllerLinkBuilder.linkTo(OntologyController.class).withRel("ontologies"));
        return resource;
    }
    @ApiOperation(value = "List all ontologies")
    @RequestMapping(path = "", produces = {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<OntologyDocument>> getOntologies(
            @PageableDefault(size = 20, page = 0) Pageable pageable,
            PagedResourcesAssembler assembler
    ) throws ResourceNotFoundException {
        Page<OntologyDocument> document = ontologyRepositoryService.getAllDocuments(pageable);
        return new ResponseEntity<>( assembler.toResource(document, documentAssembler), HttpStatus.OK);
    }
    
    @ApiOperation(value = "List available schema keys")
    @RequestMapping(path = "/schemakeys", produces = {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<String>> getAvailableSchemaKeys(
            @PageableDefault(size = 20, page = 0) Pageable pageable,
            PagedResourcesAssembler assembler
    ) throws ResourceNotFoundException { 	
    	Set<String> keys = new HashSet<String>();
    	
        try {
        	
        	for (OntologyDocument document : ontologyRepositoryService.getAllDocuments(new Sort(new Sort.Order(Sort.Direction.ASC, "ontologyId")))) {
				document.getConfig().getClassifications().forEach(x -> keys.addAll(x.keySet()));
			}
        } catch (Exception e) {
        }
        
        List<String> keyList = new ArrayList<String>();
        keyList.addAll(keys);
        
        final int start = (int)pageable.getOffset();
        final int end = Math.min((start + pageable.getPageSize()), keys.size());
        Page<String> document = new PageImpl<>(keyList.subList(start, end), pageable, keys.size());
       
       return new ResponseEntity<>( assembler.toResource(document), HttpStatus.OK);
    }
    
    @ApiOperation(value = "List available classification values for particular schema keys", notes = "Possible schema keys can be inquired with /api/ontologies/schemakeys method.")
    @RequestMapping(path = "/schemavalues", produces = {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<String>> getClassificationsForSchemas(
    		@RequestParam(value = "schema", required = true) Collection<String> schemas,
            @PageableDefault(size = 20, page = 0) Pageable pageable,
            PagedResourcesAssembler assembler
    ) throws ResourceNotFoundException { 	
    	Set<String> classifications = new HashSet<String>();
    	
        try {
        	for (OntologyDocument document : ontologyRepositoryService.getAllDocuments(new Sort(new Sort.Order(Sort.Direction.ASC, "ontologyId")))) {
				document.getConfig().getClassifications().forEach(x -> x.forEach((k, v) -> {if (schemas.contains(k)) classifications.addAll(x.get(k));} ));
			}
        } catch (Exception e) {
        }
        
        List<String> classificationList = new ArrayList<String>();
        classificationList.addAll(classifications);
        
        final int start = (int)pageable.getOffset();
        final int end = Math.min((start + pageable.getPageSize()), classifications.size());
        Page<String> document = new PageImpl<>(classificationList.subList(start, end), pageable, classifications.size());
       
       return new ResponseEntity<>( assembler.toResource(document), HttpStatus.OK);
    }
    
    
    @ApiOperation(value = "Filter list of ontologies by particular schema keys and classification values", notes = "Possible schema keys and possible classification values of particular keys can be inquired with /api/ontologies/schemakeys and /api/ontologies/schemavalues methods respectively.")
    @RequestMapping(path = "/filterby", produces = {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<OntologyDocument>> filterOntologiesByClassification(
    		@RequestParam(value = "schema", required = true) Collection<String> schemas,
    		@RequestParam(value = "classification", required = true) Collection<String> classifications,
            @PageableDefault(size = 20, page = 0) Pageable pageable,
            PagedResourcesAssembler assembler
    ) throws ResourceNotFoundException { 	
    	
    	List<OntologyDocument> filteredList = new ArrayList<OntologyDocument>();
    	
    	 for (OntologyDocument ontologyDocument : ontologyRepositoryService.getAllDocuments(new Sort(new Sort.Order(Sort.Direction.ASC, "ontologyId")))) {
    		for(Map<String, Collection<String>> classificationSchema : ontologyDocument.getConfig().getClassifications()) {
    			for (String schema: schemas)
    			    if(classificationSchema.containsKey(schema))
    				    for (String classification: classifications)
    				      if (classificationSchema.get(schema).contains(classification)) {
    					      filteredList.add(ontologyDocument);
    					      break;
    				  }
    			    
    			}
		} 
        
        final int start = (int)pageable.getOffset();
        final int end = Math.min((start + pageable.getPageSize()), filteredList.size());
        Page<OntologyDocument> document = new PageImpl<>(filteredList.subList(start, end), pageable, filteredList.size());
       
       return new ResponseEntity<>( assembler.toResource(document, documentAssembler), HttpStatus.OK);
    }

    @ApiOperation(value = "Retrieve a particular ontology")
    @RequestMapping(path = "/{onto}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<Resource<OntologyDocument>> getOntology(@PathVariable("onto") String ontologyId) throws ResourceNotFoundException {
        ontologyId = ontologyId.toLowerCase();
        OntologyDocument document = ontologyRepositoryService.get(ontologyId);
        if (document == null) throw new ResourceNotFoundException();
        return new ResponseEntity<>( documentAssembler.toResource(document), HttpStatus.OK);
    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Resource not found")
    @ExceptionHandler(ResourceNotFoundException.class)
    public void handleError(HttpServletRequest req, Exception exception) {
    }



}
