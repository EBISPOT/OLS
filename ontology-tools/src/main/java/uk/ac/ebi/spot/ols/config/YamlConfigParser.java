package uk.ac.ebi.spot.ols.config;

import org.springframework.core.io.Resource;
import org.yaml.snakeyaml.Yaml;
import uk.ac.ebi.spot.ols.loader.DocumentLoadingService;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;

/**
 * @author Simon Jupp
 * @date 09/07/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
public class YamlConfigParser {

    private Resource yamlFile;
    private boolean isObo = false;

    public YamlConfigParser(Resource yamlFile)  {

        this(yamlFile, false);

    }

    public YamlConfigParser(Resource yamlFile, boolean isObo)  {
        this.yamlFile = yamlFile;
        this.isObo = isObo;
    }

    public Collection<YamlBasedLoadingService> getDocumentLoadingServices() throws IOException {

        Yaml yaml = new Yaml();

        LinkedHashMap linkedHashMap = (LinkedHashMap)yaml.load(yamlFile.getInputStream());

        Collection<YamlBasedLoadingService> documentLoadingServices = new ArrayList<YamlBasedLoadingService>();

        LinkedHashMap contextInfos = (LinkedHashMap)linkedHashMap.get("@context");
        //Get the @base property which will be the first part of the url to the owl file of the ontology.
        String base = null;
        if (contextInfos != null) {
            base = (String)contextInfos.get("@base");
        }



        ArrayList<LinkedHashMap> ontologies = (ArrayList<LinkedHashMap>)linkedHashMap.get("ontologies");
        if(ontologies ==null){
            return documentLoadingServices;
        }
        for (LinkedHashMap ontology : ontologies) {

            boolean _isObo = isObo;
            if ( ontology.containsKey("is_foundry")) {
                _isObo = ((Boolean)ontology.get("is_foundry"));
            }

            boolean obsolete = false;
            if ( ontology.containsKey("is_obsolete")) {
                obsolete = ((Boolean)ontology.get("is_obsolete"));
            }

            if  (base == null && _isObo) {
                base = "http://purl.obolibrary.org/obo/";
            }

            if (!obsolete) {
                YamlBasedLoadingService yamlBasedLoadingService = new YamlBasedLoadingService(ontology, base, _isObo);
                documentLoadingServices.add(yamlBasedLoadingService);
            }

        }


        return documentLoadingServices;


    }

}
