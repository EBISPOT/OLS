package uk.ac.ebi.spot.ols.controller.ui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.spot.ols.exception.ErrorMessage;
import uk.ac.ebi.spot.ols.model.OntologyDocument;
import uk.ac.ebi.spot.ols.neo4j.model.Term;
import uk.ac.ebi.spot.ols.neo4j.service.OntologyTermGraphService;
import uk.ac.ebi.spot.ols.service.OntologyRepositoryService;
import uk.ac.ebi.spot.ols.util.OLSEnv;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;

/**
 * @author Simon Jupp
 * @date 15/07/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@Controller
@RequestMapping("/ontologies")
public class OntologyControllerUI {

    @Autowired
    private HomeController homeController;

    @Autowired
    OntologyRepositoryService repositoryService;


    // Reading these from application.properties
    @Value("${ols.downloads.folder:}")
    private String downloadsFolder;

    @RequestMapping(path = "/{onto}", produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET)
    String getTerm(
            @PathVariable("onto") String ontologyId,
            Model model) throws ResourceNotFoundException {

        ontologyId = ontologyId.toLowerCase();
        if (ontologyId != null) {
            OntologyDocument document = repositoryService.get(ontologyId);
            if (document == null) {
                throw new ResourceNotFoundException("Ontology called " + ontologyId + " not found");
            }
            model.addAttribute("ontologyDocument", document);
        }
        else {
            return homeController.doSearch(
                    "*",
                    null,
                    null,null, null, false, null, false, false, null, 10,0,model);
        }
        return "ontology";
    }

    @RequestMapping(path = "/{onto}", produces = "application/rdf+xml", method = RequestMethod.GET)

    public @ResponseBody FileSystemResource getOntologyDirectDownload(@PathVariable("onto") String ontologyId) throws ResourceNotFoundException {
        return getDownloadOntology(ontologyId);
    }


    @RequestMapping(path = "/{onto}/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE, method = RequestMethod.GET)

    public @ResponseBody  FileSystemResource getDownloadOntology(@PathVariable("onto") String ontologyId) throws ResourceNotFoundException {
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
        if (downloadsFolder.equals("")) {
            return OLSEnv.getOLSHome() + File.separator + "downloads";
        }
        return downloadsFolder;
    }

}
