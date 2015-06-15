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
import uk.ac.ebi.spot.ols.synchroniser.CmungallOntologyResourceConfigsGetter;

import java.util.Collection;
import java.util.List;


@SpringBootApplication
public class SynchronizerApplication implements CommandLineRunner {
    @Autowired
    CmungallOntologyResourceConfigsGetter cmungallOntologyResourceConfigsGetter;

    @Autowired
    OntologyRepositoryService ontologyRepositoryService;


    @Value("${ols.ontology.synchroniser.yaml_path}")
    private String yamlPath;

    @Override
    public void run(String... args) throws Exception {

        //Loop over cungall yaml file, get necessary data and add each ontologyDocument to mango database.
        Collection<OntologyResourceConfig> ontologyResourceConfigs = cmungallOntologyResourceConfigsGetter.getOntologyResourceConfigs(yamlPath);

        List<OntologyDocument> documents = ontologyRepositoryService.getAllDocuments();

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
