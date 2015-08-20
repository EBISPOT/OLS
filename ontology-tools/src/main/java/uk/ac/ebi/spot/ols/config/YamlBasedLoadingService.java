package uk.ac.ebi.spot.ols.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.spot.ols.exception.ConfigParsingException;

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

    private Logger log = LoggerFactory.getLogger(getClass());

    public Logger getLog() {
        return log;
    }


    public YamlBasedLoadingService (LinkedHashMap ontology, String base, boolean isObo) {

        this.ontology = ontology;
        this.base = base;
        this.isObo = isObo;
    }

    @Override
    public OntologyResourceConfig getConfiguration() throws ConfigParsingException {

        String id = ((String)ontology.get("id")); // e.g. Uberon
        String prefix = id.toUpperCase();
        if (ontology.containsKey("preferredPrefix"))  {
            prefix =  (String) ontology.get("preferredPrefix");
//            id = prefix.toLowerCase();
        }


        String ontologyTitle = (String)ontology.get("title");

        ArrayList<LinkedHashMap> products = (ArrayList<LinkedHashMap>)ontology.get("products");

        String productId = null;

        if (products == null) {
            getLog().warn("No product defined in OBO Yaml for " + id);
           productId = id + ".owl";
        }
        else {
            for(LinkedHashMap<String,String> product : products){
                //Get the product id property which will be the the last part of the url to the owl file of the ontology.
                //(providing the suffix of the file name is .owl).

                productId = product.get("id");
                if (product.containsKey("is_canonical")) {
                    break;
                }

                if(productId.contains(".owl")) {
                    productId = product.get("id");
                    break;
                }
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
            throw new ConfigParsingException("Can't determine ontology URI for " + ontologyTitle);
        }

        String location = uri;

        if (base == null && productId != null) {
            location = productId;
        }
        else {
            location = base + productId;
        }

        //Build the OntologyResourceConfig and add it to the Collection.
        OntologyResourceConfig.OntologyResourceConfigBuilder builder = new  OntologyResourceConfig.OntologyResourceConfigBuilder(uri, ontologyTitle, id, URI.create(location));


        builder.setPreferredPrefix(prefix);

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
            builder.setBaseUris(Collections.singleton(base + id.toUpperCase() + "_"));
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

        if (ontology.containsKey("description")) {
            builder.setDescription((String) ontology.get("description"));
        }

        if (ontology.containsKey("homepage")) {
            if (ontology.get("homepage") instanceof ArrayList) {
                ArrayList pages = (ArrayList) ontology.get("homepage");
                builder.setHomepage((String) pages.get(0));
            } else {
                builder.setHomepage((String) ontology.get("homepage"));
            }
        }

        if (ontology.containsKey("mailing_list")) {
            builder.setMailingList((String) ontology.get("mailing_list"));
        }

        if (ontology.containsKey("creator"))  {
            Set<String> creators = new HashSet<>();
            for (String creator :  (ArrayList<String>) ontology.get("creator")) {
                creators.add(creator);
            }
            builder.setCreators(creators);
        }

        return builder.build();

    }
}
