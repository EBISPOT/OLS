package uk.ac.ebi.spot.ols;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.SpringApplicationContextLoader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.ac.ebi.spot.ols.model.OntologyDocument;
import uk.ac.ebi.spot.ols.model.Status;
import uk.ac.ebi.spot.ols.service.MongoOntologyRepositoryService;
import uk.ac.ebi.spot.ols.service.OntologyRepositoryService;

import java.util.Properties;

/**
 * Created by catherineleroy on 11/06/2015.
 */

@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(classes = SynchronizerApplicationTestsConfig.class)
//@SpringApplicationConfiguration(classes = SynchronizerApplicationTestsConfig.class)
//@ContextConfiguration

@SpringApplicationConfiguration(classes = SynchronizerApplication.class)

public class SynchronizerApplicationTests {

    @Autowired
    OntologyRepositoryService ontologyRepositoryService;


    @Test
    // Test that all the Ontology Configuration extracted from the test repository-test1.yaml file get created in the
    //mongo db if it was previously empty.
    public void synchronize() throws Exception {

        //Start by deleting all the document in mongo to start a fresh test.
        for(OntologyDocument ontologyDocument : ontologyRepositoryService.getAllDocuments()){
            ontologyRepositoryService.delete(ontologyDocument);
        }
        
        Properties properties = System.getProperties();
        properties.setProperty("ols.ontology.synchroniser.yaml_path", "/Users/catherineleroy/Documents/github_project/OLS/ols-apps/ols-ontology-library-synchronizer/src/main/resources/repository-test1.yaml");
        properties.setProperty("spring.data.mongodb.database","ols");
        properties.setProperty("ols.loader.filedir", "data");
        SpringApplication.run(SynchronizerApplication.class);


        boolean uberonFound = false;
        boolean goFound = false;
        boolean clFound = false;
        boolean roFound = false;

        for(OntologyDocument ontologyDocument : ontologyRepositoryService.getAllDocuments()){
            if("uberon".equals(ontologyDocument.getOntologyId())){
                uberonFound = true;
            }else if("go".equals(ontologyDocument.getOntologyId())){
                goFound = true;
            }else if("cl".equals(ontologyDocument.getOntologyId())){
                clFound = true;
            }else if("ro".equals(ontologyDocument.getOntologyId())){
                roFound = true;
            }
            ontologyDocument.setStatus(Status.LOADED);
            ontologyRepositoryService.update(ontologyDocument);
        }

        Assert.assertTrue(uberonFound);
        Assert.assertTrue(goFound);
        Assert.assertTrue(clFound);
        Assert.assertTrue(roFound);


    }



    @Test
    //By switching to a different test yaml file, make sure that if something change in the ontology it is reflected
    // in the mongo database by a TOLOAD status on the ontology document.
    public void synchronizeBis() throws Exception {

        Properties properties = System.getProperties();
        properties.setProperty("ols.ontology.synchroniser.yaml_path", "/Users/catherineleroy/Documents/github_project/OLS/ols-apps/ols-ontology-library-synchronizer/src/main/resources/repository-test2.yaml");
        properties.setProperty("spring.data.mongodb.database", "ols");
        properties.setProperty("ols.loader.filedir", "data");

        SpringApplication.run(SynchronizerApplication.class);


        boolean uberonFound = false;
        boolean goFound = false;
        boolean clFound = false;
        boolean roFound = false;



        boolean uberonStatusIsLoaded = false;
        boolean goStatusIsLoaded = false;
        boolean clStatusIsLoaded = false;
        boolean roStatusIsToLoad = false;

        for(OntologyDocument ontologyDocument : ontologyRepositoryService.getAllDocuments()){
            String ontoId = ontologyDocument.getOntologyId();

            if("uberon".equals(ontoId)){
                uberonFound = true;
                if(Status.LOADED.equals(ontologyDocument.getStatus())){
                    uberonStatusIsLoaded = true;
                }
            }else if("go".equals(ontoId)){
                goFound = true;
                if(Status.LOADED.equals(ontologyDocument.getStatus())){
                    goStatusIsLoaded = true;
                }
            }else if("cl".equals(ontoId)){
                clFound = true;
                if(Status.LOADED.equals(ontologyDocument.getStatus())){
                    clStatusIsLoaded = true;
                }
            }else if("ro".equals(ontoId)){
                roFound = true;
                if(Status.TOLOAD.equals(ontologyDocument.getStatus())){
                    roStatusIsToLoad = true;
                }
            }


        }

        Assert.assertTrue(uberonFound);
        Assert.assertTrue(goFound);
        Assert.assertTrue(clFound);
        Assert.assertTrue(roFound);


        Assert.assertTrue(uberonStatusIsLoaded);
        Assert.assertTrue(goStatusIsLoaded);
        Assert.assertTrue(clStatusIsLoaded);
        Assert.assertTrue(roStatusIsToLoad);


    }

}
