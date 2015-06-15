package uk.ac.ebi.spot.ols.synchroniser;

import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;
import uk.ac.ebi.spot.ols.config.OntologyResourceConfig;

import java.io.*;
import java.net.URI;
import java.util.*;

/**
 * Created by catherineleroy on 09/06/2015.
 */
@Component
public class CmungallOntologyResourceConfigsGetter  {
//    @Value("${title:}")





    public static void main(String[] args) throws IOException {

        String yamlPath = "/Users/catherineleroy/Documents/github_project/OLS/ols-apps/ols-ontology-library-synchronizer/repository.yaml";

        CmungallOntologyResourceConfigsGetter cmungallOntologyResourceConfigsGetter = new CmungallOntologyResourceConfigsGetter();
        cmungallOntologyResourceConfigsGetter.getOntologyResourceConfigs(yamlPath);

    }

    public Collection<OntologyResourceConfig> getOntologyResourceConfigs(String yamlPath) throws IOException{

        FileReader fileReader = new FileReader(yamlPath);

        Yaml yaml = new Yaml();

        LinkedHashMap linkedHashMap = (LinkedHashMap)yaml.load(fileReader);

        Collection<OntologyResourceConfig> ontologyResourceConfigs = new ArrayList<OntologyResourceConfig>();
        LinkedHashMap contextInfos = (LinkedHashMap)linkedHashMap.get("@context");
        String base = (String)contextInfos.get("@base");

        ArrayList<LinkedHashMap> ontologies = (ArrayList<LinkedHashMap>)linkedHashMap.get("ontologies");

        for(LinkedHashMap ontologie : ontologies){


            String ontologieId = (String)ontologie.get("id");
            String ontologieLabel = (String)ontologie.get("label");

            ArrayList<LinkedHashMap> products = (ArrayList<LinkedHashMap>)ontologie.get("products");
            String productId = "";
            for(LinkedHashMap<String,String> product : products){
                productId = product.get("id");
                if(productId.contains(".owl")) {
                    break;
                }
            }
            OntologyResourceConfig.OntologyResourceConfigBuilder builder = new  OntologyResourceConfig.OntologyResourceConfigBuilder(ontologieId, ontologieLabel, ontologieId, URI.create(base + productId));
            OntologyResourceConfig ontologyResourceConfig = builder.build();
            ontologyResourceConfigs.add(ontologyResourceConfig);
        }

        return ontologyResourceConfigs;


    }

}
