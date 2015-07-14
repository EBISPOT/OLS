package uk.ac.ebi.spot.ols.config;

import org.yaml.snakeyaml.Yaml;
import uk.ac.ebi.spot.ols.exception.OntologyLoadingException;
import uk.ac.ebi.spot.ols.loader.DocumentLoadingService;
import uk.ac.ebi.spot.ols.loader.OntologyLoader;

import java.net.URI;
import java.util.*;

/**
 * @author Simon Jupp
 * @date 09/07/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
public class YamlBasedLoadingService extends AbstractLoadingService {

    private LinkedHashMap ontology;
    private String base;
    private boolean isObo;

    public YamlBasedLoadingService (LinkedHashMap ontology, String base, boolean isObo) {

        this.ontology = ontology;
        this.base = base;
        this.isObo = isObo;
    }

    @Override
    public OntologyResourceConfig getConfiguration() {

        String id = ((String)ontology.get("id")); // e.g. Uberon
        String namespace = id.toUpperCase(); // e.g. UBERON

        if (ontology.containsKey("preferredPrefix"))  {
            namespace =  (String) ontology.get("preferredPrefix");
        }

        String ontologyTitle = (String)ontology.get("title");

        ArrayList<LinkedHashMap> products = (ArrayList<LinkedHashMap>)ontology.get("products");
        String productId = null;
        for(LinkedHashMap<String,String> product : products){
            //Get the product id property which will be the the last part of the url to the owl file of the ontology.
            //(providing the suffix of the file name is .owl).
            productId = product.get("id");
            if(productId.contains(".owl")) {
                break;
            }
        }

        String uri;
        if (ontology.containsKey("uri")) {
            uri = (String) ontology.get("uri");
        }
        else if (base != null & productId != null ) {
            uri = base + productId;
        }
        else {
            throw new RuntimeException("Can't determine ontology URI for " + ontologyTitle);
        }

        String location = uri;

        if (base == null && productId != null) {
            location = productId;
        }
        else {
            location = base + productId;
        }

        //Build the OntologyResourceConfig and add it to the Collection.
        OntologyResourceConfig.OntologyResourceConfigBuilder builder = new  OntologyResourceConfig.OntologyResourceConfigBuilder(uri, ontologyTitle, namespace, URI.create(location));


        if (ontology.containsKey("definition_property"))  {
            Collection<URI> definitionUris = new HashSet<>();
            for (String definition : (ArrayList<String>) ontology.get("definition_property")) {
                definitionUris.add(URI.create(definition));
            }
            builder.setDefinitionProperties(definitionUris);
        }
        else if (isObo) {
            builder.setDefinitionProperties(Collections.singleton(URI.create(OboDefaults.DEFINITION)));
        }

        if (ontology.containsKey("synonym_property"))  {
            Set<URI> synonymsUris = new HashSet<>();
            for (String synonym_property :  (ArrayList<String>) ontology.get("synonym_property")) {
                synonymsUris.add(URI.create(synonym_property));
            }
            builder.setSynonymProperties(synonymsUris);
        }
        else if (isObo) {
            builder.setSynonymProperties(Collections.singleton(URI.create(OboDefaults.SYNONYM)));
        }

        if (ontology.containsKey("hidden_property"))  {
            Set<URI> hiddenUris = new HashSet<>();
            for (String hidden :  (ArrayList<String>) ontology.get("hidden_property")) {
                hiddenUris.add(URI.create(hidden));
            }
            builder.setHiddenProperties(hiddenUris);
        }

        if (ontology.containsKey("hierarchical_property"))  {
            Set<URI> hierarchicalUris = new HashSet<>();
            for (String hierarchical :  (ArrayList<String>) ontology.get("hierarchical_property")) {
                hierarchicalUris.add(URI.create(hierarchical));
            }
            builder.setHierarchicalProperties(hierarchicalUris);
        }

        if (ontology.containsKey("base_uri"))  {
            Set<String> baseUris = new HashSet<>();
            for (String baseUri :  (ArrayList<String>) ontology.get("base_uri")) {
                baseUris.add(baseUri);
            }
            builder.setBaseUris(baseUris);
        }
        else if (isObo) {
            builder.setBaseUris(Collections.singleton(base + namespace + "_"));
        }
        else {
            builder.setBaseUris(Collections.singleton(uri));
        }

        if (ontology.containsKey("isInferred")) {
            builder.setInferred((boolean) ontology.get("isInferred"));
        }

        if (ontology.containsKey("classify")) {
            builder.setClassify((boolean) ontology.get("classify"));
        }

        if (ontology.containsKey("oboSlims")) {
            builder.setOboSlims((boolean) ontology.get("oboSlims"));
        }
        else if (isObo) {
            builder.setOboSlims(true);
        }


        return builder.build();

    }
}
