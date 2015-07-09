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
import org.springframework.core.io.UrlResource;
import uk.ac.ebi.spot.ols.config.OntologyResourceConfig;
import uk.ac.ebi.spot.ols.config.YamlBasedLoadingService;
import uk.ac.ebi.spot.ols.config.YamlConfigParser;
import uk.ac.ebi.spot.ols.model.OntologyDocument;
import uk.ac.ebi.spot.ols.model.Status;
import uk.ac.ebi.spot.ols.service.OntologyRepositoryService;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;


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

    @Value("${ols.ontology.config:}")
    public String yamlPath;

    @Value("${ols.obofoundry.ontology.config:}")
    public String oboYamlPath;

    @Override
    public void run(String... args) throws Exception {

        Collection<String> configs = new HashSet<>();
        if (yamlPath != null) {
            for (String configPath : yamlPath.split(",")) {
                configs.add(configPath);
            }
        }

        for (String path : configs) {
            UrlResource resource = new UrlResource(path);

            YamlConfigParser yamlConfigParser = new YamlConfigParser(resource);
            updateDocument(yamlConfigParser);
        }

        if (oboYamlPath != null) {
            UrlResource resource = new UrlResource(oboYamlPath);
            YamlConfigParser yamlConfigParser = new YamlConfigParser(resource, true);
            updateDocument(yamlConfigParser);
        }

    }

    public void updateDocument(YamlConfigParser yamlConfigParser) throws IOException {

        for (YamlBasedLoadingService loadingService : yamlConfigParser.getDocumentLoadingServices()) {
            OntologyResourceConfig ontologyResourceConfig = loadingService.getConfiguration();
            OntologyDocument mongoOntologyDocument = ontologyRepositoryService.get(ontologyResourceConfig.getNamespace().toUpperCase());
            if (mongoOntologyDocument == null) {
                getLog().info("New ontology document to load found " + ontologyResourceConfig.getNamespace());
                OntologyDocument ontologyDocument = new OntologyDocument(ontologyResourceConfig.getNamespace(), ontologyResourceConfig);
                ontologyDocument.setStatus(Status.TOLOAD);
                ontologyRepositoryService.create(ontologyDocument);
            } else {
                if (ontologyResourceConfig.getNamespace().equals(mongoOntologyDocument.getOntologyId())) {
                    // if location has changed, update the info
                    if (!mongoOntologyDocument.getConfig().getFileLocation().equals(ontologyResourceConfig.getFileLocation())) {
                        getLog().info("Location of " + ontologyResourceConfig.getNamespace() + " changed from " + mongoOntologyDocument.getConfig().getFileLocation() + " to " + ontologyResourceConfig.getFileLocation());
                        mongoOntologyDocument.getConfig().setFileLocation(ontologyResourceConfig.getFileLocation());
                        mongoOntologyDocument.setStatus(Status.TOLOAD);
                        ontologyRepositoryService.update(mongoOntologyDocument);
                    }
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(YamlLoader.class, args);
    }
}
