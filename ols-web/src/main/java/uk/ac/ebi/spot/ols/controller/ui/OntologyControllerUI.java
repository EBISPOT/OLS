package uk.ac.ebi.spot.ols.controller.ui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Sort;
import uk.ac.ebi.spot.ols.model.OntologyDocument;
import uk.ac.ebi.spot.ols.neo4j.service.OntologyTermGraphService;
import uk.ac.ebi.spot.ols.service.OntologyRepositoryService;
import uk.ac.ebi.spot.ols.util.OLSEnv;

import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

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

    @Autowired
    private OntologyTermGraphService ontologyTermGraphService;

    @Autowired
    private CustomisationProperties customisationProperties;

    // Reading these from application.properties
    @Value("${ols.downloads.folder:}")
    private String downloadsFolder;

    @RequestMapping(path = "", method = RequestMethod.GET)
    String getAll(Model model) {
        List list = repositoryService.getAllDocuments(new Sort(new Sort.Order(Sort.Direction.ASC, "ontologyId")));
        model.addAttribute("all_ontologies", list);
        customisationProperties.setCustomisationModelAttributes(model);
        return "browse";
    }

    @RequestMapping(path = "/{onto}", method = RequestMethod.GET)
    String getTerm(
            @PathVariable("onto") String ontologyId,
            Model model) throws ResourceNotFoundException {

        ontologyId = ontologyId.toLowerCase();
        if (ontologyId != null) {
            OntologyDocument document = repositoryService.get(ontologyId);
            if (document == null) {
                throw new ResourceNotFoundException("Ontology called " + ontologyId + " not found");
            }

            String contact = document.getConfig().getMailingList();
            try {
                InternetAddress address = new InternetAddress(contact, true);
                contact = "mailto:" + contact;
            } catch (Exception e) {
              // only thrown if not valid e-mail, so contact must be URL of some sort
            }
            model.addAttribute("contact", contact);

            model.addAttribute("ontologyDocument", document);

            customisationProperties.setCustomisationModelAttributes(model);
            DisplayUtils.setPreferredRootTermsModelAttributes(ontologyId, document, ontologyTermGraphService, model);
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
    public @ResponseBody FileSystemResource getOntologyDirectDownload(@PathVariable("onto") String ontologyId, HttpServletResponse response) throws ResourceNotFoundException {
        return getDownloadOntology(ontologyId, response);
    }


    @RequestMapping(path = "/{onto}/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE, method = RequestMethod.GET)
    public @ResponseBody  FileSystemResource getDownloadOntology(@PathVariable("onto") String ontologyId, HttpServletResponse response) throws ResourceNotFoundException {

        ontologyId = ontologyId.toLowerCase();

        OntologyDocument document = repositoryService.get(ontologyId);

        if (document == null) {
            throw new ResourceNotFoundException("Ontology called " + ontologyId + " not found");
        }

        if(document.getConfig().getAllowDownload() == false) {
            throw new ResourceNotFoundException("This ontology is not available for download");
        }

        try {
            response.setHeader( "Content-Disposition", "filename=" + ontologyId + ".owl" );
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
