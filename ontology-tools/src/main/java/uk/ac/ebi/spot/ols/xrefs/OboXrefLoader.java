package uk.ac.ebi.spot.ols.xrefs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

/**
 * @author Simon Jupp
 * @date 11/06/2016
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@Component
public class OboXrefLoader {

    Map<String, Database> databases;

    private Logger log = LoggerFactory.getLogger(getClass());

    public Logger getLog() {
        return log;
    }

    public OboXrefLoader() {
        databases = new HashMap<String, Database>();
    }
    @Value("${obo.db.xrefs:}")
    private String oboxrefs;
    public OboXrefLoader(String oboXrefsPath) {
        this.oboxrefs = oboXrefsPath;
        databases = new HashMap<String, Database>();
    }

    public Collection<Database> getDatabases() {

        if (oboxrefs != null && databases.keySet().isEmpty())  {
            Yaml yaml = new Yaml();
            DefaultResourceLoader fileSystemResourceLoader = new DefaultResourceLoader();
            Resource resource = fileSystemResourceLoader.getResource(oboxrefs);
            if (resource  != null) {
                try {
                    List xrefMap = (List) yaml.load(resource.getInputStream());
                    for (Object entry : xrefMap) {

                        LinkedHashMap entryMap = (LinkedHashMap) entry;
                        String databaseId = (String) entryMap.get("database");
                        String databaseName = (String) entryMap.get("name");
                        if (entryMap.containsKey("entity_types")) {
                            List entityO = (List) entryMap.get("entity_types");

                            for (Object type : entityO ) {
                                LinkedHashMap entityMap = (LinkedHashMap) type;
                                if (entityMap.containsKey("url_syntax")) {
                                    OboDatabaseImpl db = new OboDatabaseImpl();
                                    db.setDatabaseId(databaseId);
                                    db.setDatabaseName(databaseName);
                                    db.setUrlSyntax((String) entityMap.get("url_syntax"));
                                    databases.put(databaseId, db);
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }



        }
        return databases.values();
    }

    public Optional<Database> findByName(String name) {
        if (databases.keySet().isEmpty()) {
            getDatabases();
        }
        if (databases.get(name) == null) {
            return Optional.empty();
        }
        return Optional.of(databases.get(name));
    }
}
