package uk.ac.ebi.spot.ols; /**
 * Created by catherineleroy on 09/06/2015.
 */

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import uk.ac.ebi.spot.ols.config.OntologyResourceConfig;
import uk.ac.ebi.spot.ols.model.OntologyDocument;
import uk.ac.ebi.spot.ols.model.Status;
import uk.ac.ebi.spot.ols.service.OntologyRepositoryService;
import uk.ac.ebi.spot.ols.synchroniser.OntologyRessourceConfigsGetter;

import java.util.Collection;
import java.util.List;


@SpringBootApplication
public class SynchronizerApplication implements CommandLineRunner {
    @Autowired
    OntologyRessourceConfigsGetter ontologyResourceConfigsGetter;

    @Autowired
    OntologyRepositoryService ontologyRepositoryService;


    @Value("${ols.ontology.synchroniser.yaml_path}")
    private String yamlPath;

    @Override
    public void run(String... args) throws Exception {

        //Get the Collection of OntologyResourceConfig to update/save into the mongo database.
        Collection<OntologyResourceConfig> ontologyResourceConfigs = ontologyResourceConfigsGetter.getOntologyResourceConfigs(yamlPath);

        //Get all the OntologyDocument (and therefore ontology configuration) already in the Mongo database.
        List<OntologyDocument> documents = ontologyRepositoryService.getAllDocuments();

        //For all the ontologyConfiguration from the collection check through the id if they are already in the
        //mongo database.
        //If they are and the information from the Collection is the same then the information in Mongo db, don't do anything
        //If they are and the information from the Collection is different then the one in Mondo db, update the OntologyDocument
        //in Mongo db and update the OntologyDocument status to TOLOAD.
        //If they are not then add them.
        for(OntologyResourceConfig ontologyResourceConfig : ontologyResourceConfigs) {
            boolean found = false;
            for (OntologyDocument mongoOntologyDocument : documents) {
                if(ontologyResourceConfig.getId().equals(mongoOntologyDocument.getOntologyId())){
                    found=true;
                    if(!mongoOntologyDocument.getConfig().getFileLocation().equals(ontologyResourceConfig.getFileLocation())){
                        ontologyRepositoryService.delete(mongoOntologyDocument);
                        OntologyDocument ontologyDocument = new OntologyDocument(ontologyResourceConfig.getId(), ontologyResourceConfig);
                        ontologyDocument.setStatus(Status.TOLOAD);
                        ontologyRepositoryService.create(ontologyDocument);
                    }
                }
            }
            if(!found){
                OntologyDocument ontologyDocument = new OntologyDocument(ontologyResourceConfig.getId(), ontologyResourceConfig);
                ontologyDocument.setStatus(Status.TOLOAD);
                ontologyRepositoryService.create(ontologyDocument);
            }
        }
    }


    public static void main(String[] args) throws Exception {
        SpringApplication.run(SynchronizerApplication.class, args);
    }
}
