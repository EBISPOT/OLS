package uk.ac.ebi.spot.ols.controller.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriUtils;
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
import java.util.Map;

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

    // Reading these from application.properties
    @Value("${ols.downloads.folder:}")
    private String downloadsFolder;


    @Override
    public RepositoryLinksResource process(RepositoryLinksResource resource) {
        resource.add(ControllerLinkBuilder.linkTo(OntologyController.class).withRel("ontologies"));
        return resource;
    }

    @RequestMapping(path = "", produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<OntologyDocument>> getOntologies(
            @PageableDefault(size = 20, page = 0) Pageable pageable,
            PagedResourcesAssembler assembler
    ) throws ResourceNotFoundException {
        Page<OntologyDocument> document = ontologyRepositoryService.getAllDocuments(pageable);
        return new ResponseEntity<>( assembler.toResource(document, documentAssembler), HttpStatus.OK);
    }


    @RequestMapping(path = "/{onto}", produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<Resource<OntologyDocument>> getOntology(@PathVariable("onto") String ontologyId) throws ResourceNotFoundException {
        ontologyId = ontologyId.toLowerCase();
        OntologyDocument document = ontologyRepositoryService.get(ontologyId);
        if (document == null) throw new ResourceNotFoundException();
        return new ResponseEntity<>( documentAssembler.toResource(document), HttpStatus.OK);
    }

    @RequestMapping(path = "/{onto}", produces = "application/rdf+xml", method = RequestMethod.GET)
    FileSystemResource getOntologyDirectDownload(@PathVariable("onto") String ontologyId) throws ResourceNotFoundException {
        return getDownloadOntology(ontologyId);
    }


    @RequestMapping(path = "/{onto}/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE, method = RequestMethod.GET)
    public FileSystemResource getDownloadOntology(@PathVariable("onto") String ontologyId) throws ResourceNotFoundException {
        try {
            return new FileSystemResource(getDownloadFile(ontologyId));
        } catch (FileNotFoundException e) {
            throw new ResourceNotFoundException("This ontology is not available for download");
        }
    }


    private File getDownloadFile (String ontologyId) throws FileNotFoundException {
        File file = new File (getDownloadsFolder(), ontologyId.toLowerCase());
        if (!file.exists()) {
            throw new FileNotFoundException();
        }
        return file;
    }


    private String getDownloadsFolder ( ) {
        if (downloadsFolder == null) {
            return OLSEnv.getOLSHome() + File.pathSeparator + "downloads";
        }
        return downloadsFolder;
    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Resource not found")
    @ExceptionHandler(ResourceNotFoundException.class)
    public void handleError(HttpServletRequest req, Exception exception) {
    }



}
