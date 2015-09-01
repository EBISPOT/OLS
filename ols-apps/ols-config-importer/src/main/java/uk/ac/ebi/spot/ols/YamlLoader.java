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
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.StringUtils;
import uk.ac.ebi.spot.ols.config.OntologyResourceConfig;
import uk.ac.ebi.spot.ols.config.YamlBasedLoadingService;
import uk.ac.ebi.spot.ols.config.YamlConfigParser;
import uk.ac.ebi.spot.ols.exception.ConfigParsingException;
import uk.ac.ebi.spot.ols.model.OntologyDocument;
import uk.ac.ebi.spot.ols.model.Status;
import uk.ac.ebi.spot.ols.service.OntologyRepositoryService;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
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
    public String yamlPath = "";

    @Value("${ols.obofoundry.ontology.config:}")
    public String oboYamlPath = "";

    @Override
    public void run(String... args) throws Exception {

        Collection<String> configs = new HashSet<>();
        if (!yamlPath.equals("")) {
            for (String configPath : yamlPath.split(",")) {
                configs.add(configPath);
            }
        } else if (getClass().getClassLoader().getResource("ols-config.yaml") != null) {
            File olsYamlFile = new File(getClass().getClassLoader().getResource("ols-config.yaml").getPath());
            if (Files.exists(olsYamlFile.toPath())) {
                configs.add(olsYamlFile.getAbsolutePath());
            }
        }

        for (String path : configs) {
            YamlConfigParser yamlConfigParser = new YamlConfigParser(getResourceFromPath(path));
            updateDocument(yamlConfigParser);
        }

        if (!oboYamlPath.equals("")) {
            Resource resource = getResourceFromPath(oboYamlPath);
            YamlConfigParser yamlConfigParser = new YamlConfigParser(resource, true);
            updateDocument(yamlConfigParser);
        }
        else if (getClass().getClassLoader().getResource("obo-config.yaml") != null) {
            File oboYaml = new File(getClass().getClassLoader().getResource("obo-config.yaml").getPath());
            if (Files.exists(oboYaml.toPath())) {
                Resource resource = new FileSystemResource(oboYaml);
                YamlConfigParser yamlConfigParser = new YamlConfigParser(resource, true);
                updateDocument(yamlConfigParser);
            }
        }

    }

    private Resource getResourceFromPath (String path) throws MalformedURLException {
        if (path.startsWith("http") || path.startsWith("ftp")) {
            return new UrlResource(path);
        }
        else {
            return new FileSystemResource(path);
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
            } catch (ConfigParsingException e) {
                getLog().error("Can't read config:" + e.getMessage());
            }
        }
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(YamlLoader.class, args);
    }
}
