package uk.ac.ebi.spot.ols.synchroniser;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;
import uk.ac.ebi.spot.ols.config.OntologyResourceConfig;

import java.io.*;
import java.net.URI;
import java.util.*;

/**
 *
 * To function and be up to date OLS need to maintain a list of ontology together with some explanation about what they
 * are and where to find their owl file.
 * The first system put in place for ols to do that is to update its own Ontology configurations list from a file
 * which is maintained by Chris Mungall in the Berkeley Bioinformatics Open-source Projects.
 * Given a path to a local version of Chris yaml file (https://github.com/cmungall/omb/blob/master/data/repository.yaml)
 * the CmungallOntologyResourceConfigsGetter.getOntologyResourceConfigs() method analyses and return a Collection of
 * OntologyResourceConfig
 *
 * Created by catherineleroy on 09/06/2015.
 */
@Component
public class CmungallOntologyResourceConfigsGetter implements OntologyRessourceConfigsGetter {

    /**
     * Path to file containing the ontologies information. <br>
     * Example of yaml file : <br>
     *                 ex of valid yaml file:<br>
     *                 "@context":<br>
     *                  "@base": http://purl.obolibrary.org/obo/<br>
     *                 ontologies:<br>
     *                   - id: uberon<br>
     *                     label: Uberon<br>
     *                     products:<br>
     *                      - id: uberon.owl<br>
     *                   - id: go<br>
     *                     label: GO<br>
     *                     products:<br>
     *                      - id: go.owl<br>
     */
    @Value("${ols.ontology.synchroniser.yaml_path}")
//    private String yamlPath;
    private String yamlPath;

    /**
     * Path to file containing the ontologies information. <br>
     * Example of yaml file : <br>
     *                 ex of valid yaml file:<br>
     *                 "@context":<br>
     *                  "@base": http://purl.obolibrary.org/obo/<br>
     *                 ontologies:<br>
     *                   - id: uberon<br>
     *                     label: Uberon<br>
     *                     products:<br>
     *                      - id: uberon.owl<br>
     *                   - id: go<br>
     *                     label: GO<br>
     *                     products:<br>
     *                      - id: go.owl<br>
     */

    public static void main(String[] args) throws IOException {

        CmungallOntologyResourceConfigsGetter cmungallOntologyResourceConfigsGetter = new CmungallOntologyResourceConfigsGetter();
        cmungallOntologyResourceConfigsGetter.getOntologyResourceConfigs();

    }

    /**
     * From the yaml file pointed by the yamlPath variable it builds and returns a Collection of OntologyResourceConfig
     * object.<br>
     * Example of yaml file : <br>
     *                 ex of valid yaml file:<br>
     *                 "@context":<br>
     *                  "@base": http://purl.obolibrary.org/obo/<br>
     *                 ontologies:<br>
     *                   - id: uberon<br>
     *                     label: Uberon<br>
     *                     products:<br>
     *                      - id: uberon.owl<br>
     *                   - id: go<br>
     *                     label: GO<br>
     *                     products:<br>
     *                      - id: go.owl<br>
     * @return a Collection of OntologyResourceConfig object
     * @throws IOException
     */
    public Collection<OntologyResourceConfig> getOntologyResourceConfigs() throws IOException{

        FileReader fileReader = new FileReader(yamlPath);

        Yaml yaml = new Yaml();

        LinkedHashMap linkedHashMap = (LinkedHashMap)yaml.load(fileReader);

        Collection<OntologyResourceConfig> ontologyResourceConfigs = new ArrayList<OntologyResourceConfig>();
        LinkedHashMap contextInfos = (LinkedHashMap)linkedHashMap.get("@context");
        //Get the @base property which will be the first part of the url to the owl file of the ontology.
        String base = (String)contextInfos.get("@base");

        ArrayList<LinkedHashMap> ontologies = (ArrayList<LinkedHashMap>)linkedHashMap.get("ontologies");

        //Loop over the ontologies stored in the yaml file.
        for(LinkedHashMap ontologie : ontologies){


            String ontologieId = (String)ontologie.get("id");
            String ontologieLabel = (String)ontologie.get("label");

            ArrayList<LinkedHashMap> products = (ArrayList<LinkedHashMap>)ontologie.get("products");
            String productId = "";
            for(LinkedHashMap<String,String> product : products){
                //Get the product id property which will be the the last part of the url to the owl file of the ontology.
                //(providing the suffix of the file name is .owl).
                productId = product.get("id");
                if(productId.contains(".owl")) {
                    break;
                }
            }
            //Build the OntologyResourceConfig and add it to the Collection.
            OntologyResourceConfig.OntologyResourceConfigBuilder builder = new  OntologyResourceConfig.OntologyResourceConfigBuilder(ontologieId, ontologieLabel, ontologieId, URI.create(base + productId));
            OntologyResourceConfig ontologyResourceConfig = builder.build();
            ontologyResourceConfigs.add(ontologyResourceConfig);
        }

        //return the Collection of OntologyResourceConfig
        return ontologyResourceConfigs;


    }

}
