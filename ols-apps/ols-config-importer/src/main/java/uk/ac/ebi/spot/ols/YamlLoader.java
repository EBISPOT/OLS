package uk.ac.ebi.spot.ols; /**
 * Created by catherineleroy on 09/06/2015.
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.*;
import uk.ac.ebi.spot.ols.config.OntologyResourceConfig;
import uk.ac.ebi.spot.ols.config.YamlBasedLoadingService;
import uk.ac.ebi.spot.ols.config.YamlConfigParser;
import uk.ac.ebi.spot.ols.exception.ConfigParsingException;
import uk.ac.ebi.spot.ols.model.OntologyDocument;
import uk.ac.ebi.spot.ols.model.Status;
import uk.ac.ebi.spot.ols.service.OntologyRepositoryService;
import uk.ac.ebi.spot.ols.util.ReasonerType;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


@SpringBootApplication
@EnableAutoConfiguration
@Configuration
public class YamlLoader implements CommandLineRunner {

    private Logger log = LoggerFactory.getLogger(getClass());

    public Logger getLog() {
        return log;
    }

    @Autowired
    private OntologyRepositoryService ontologyRepositoryService;

    @Value("${ols.ontology.config:ols-config.yaml}")
    public String yamlPath;

    @Value("${ols.obofoundry.ontology.config:obo-config.yaml}")
    public String oboYamlPath;

    @Value("${ols.obofoundry.dontclassify:}")
    public String dontClassify;

    @Autowired
    ResourceLoader resourceLoader;

    public Set<String> dontClassifySet = new HashSet<>();

    @Override
    public void run(String... args) throws Exception {
        if (!dontClassify.equals("")) {
            for (String dontClassifyName : dontClassify.split(",")) {
                dontClassifySet.add(dontClassifyName);
            }
        }

        for (String configPath : yamlPath.split(",")) {
            getLog().info("Ontologies will be imported using config at '" + configPath + "'");
            Resource olsResource = resourceLoader.getResource(configPath);
            if (olsResource.exists()) {
                YamlConfigParser yamlConfigParser = new YamlConfigParser(olsResource);
                updateDocument(yamlConfigParser);
            }
            else {
                getLog().warn("Resource '" + olsResource + "' could not be found and will not be loaded");
            }
        }

        getLog().info("OBO Ontologies will be imported using config at '" + oboYamlPath + "'");
        Resource oboResource = resourceLoader.getResource(oboYamlPath);
        if (oboResource.exists()) {
            YamlConfigParser yamlConfigParser = new YamlConfigParser(oboResource, true);
            updateDocument(yamlConfigParser);
        }
        else {
            getLog().warn("Resource '" + oboResource + "' could not be found and will therefore not be loaded");
        }
    }

    public void updateDocument(YamlConfigParser yamlConfigParser) throws IOException {

        for (YamlBasedLoadingService loadingService : yamlConfigParser.getDocumentLoadingServices()) {
            try {
                OntologyResourceConfig ontologyResourceConfig = loadingService.getConfiguration();
                OntologyDocument mongoOntologyDocument = ontologyRepositoryService.get(ontologyResourceConfig.getNamespace());
                if (mongoOntologyDocument == null) {
                    getLog().info("New ontology document to load found " + ontologyResourceConfig.getNamespace());
                    OntologyDocument ontologyDocument = new OntologyDocument(ontologyResourceConfig.getNamespace(), ontologyResourceConfig);
                    ontologyDocument.setStatus(Status.TOLOAD);

                    if (dontClassifySet.contains(ontologyDocument.getOntologyId())) {
                        ontologyDocument.getConfig().setReasonerType(ReasonerType.NONE);
                    }

                    ontologyRepositoryService.create(ontologyDocument);


                } else {
                    // if location has changed, update the info
                    if (!mongoOntologyDocument.getConfig().getFileLocation().equals(ontologyResourceConfig.getFileLocation())) {
                        getLog().info("Location of " + ontologyResourceConfig.getNamespace() + " changed from " + mongoOntologyDocument.getConfig().getFileLocation() + " to " + ontologyResourceConfig.getFileLocation());
                        mongoOntologyDocument.getConfig().setFileLocation(ontologyResourceConfig.getFileLocation());
                        mongoOntologyDocument.setStatus(Status.TOLOAD);
                    }
                    if (dontClassifySet.contains(mongoOntologyDocument.getOntologyId())) {
                        mongoOntologyDocument.getConfig().setReasonerType(ReasonerType.NONE);
                    }
                    // todo validation and check for breaking changes
                    ontologyRepositoryService.update(mongoOntologyDocument);
                }
            } catch (ConfigParsingException e) {
                getLog().error("Can't read config:" + e.getMessage());
            }
        }
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(YamlLoader.class, args);
    }
}
