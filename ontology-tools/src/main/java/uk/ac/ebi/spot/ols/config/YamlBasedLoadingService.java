package uk.ac.ebi.spot.ols.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.spot.ols.exception.ConfigParsingException;
import uk.ac.ebi.spot.ols.util.ReasonerType;

import java.net.URI;
import java.util.*;

/**
 * @author Simon Jupp
 * @date 09/07/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 *
 * Creates an Ontology configuration from a configuration define in Yaml
 *
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

        try {

            String prefix = id.toUpperCase();
            if (ontology.containsKey("preferredPrefix"))  {
                prefix =  (String) ontology.get("preferredPrefix");
            }


            String ontologyTitle = (String)ontology.get("title");

            if (ontologyTitle == null) {
                ontologyTitle = id;
            }


            URI location = null;

            if (ontology.containsKey("ontology_purl")) {
                location = URI.create((String) ontology.get("ontology_purl"));
            }

            String uri;
            if (ontology.containsKey("uri")) {
                uri = (String) ontology.get("uri");
            }
            else if (base != null) {
                uri = base + id;
            }
            else {
                uri = location.toString();
            }


            //Build the OntologyResourceConfig and add it to the Collection.
            OntologyResourceConfig.OntologyResourceConfigBuilder builder = new  OntologyResourceConfig.OntologyResourceConfigBuilder(uri, ontologyTitle, id, location);


            builder.setPreferredPrefix(prefix);

            if (ontology.containsKey("label_property"))  {
                String labelProperty = (String) ontology.get("label_property");
                builder.setLabelProperty(URI.create(labelProperty));
            }

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
                builder.setSynonymProperties(Collections.singleton(URI.create(OboDefaults.EXACT_SYNONYM)));
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
            else  {
                builder.setHierarchicalProperties(OboDefaults.hierarchical_relations);
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

            if (ontology.containsKey("reasoner")) {
                String reasonerType = (String) ontology.get("reasoner");
                ReasonerType type = ReasonerType.valueOf(reasonerType.toUpperCase());
                if (type == null) {
                    log.warn("Unknown reasoner type, defaulting to structural reasoner " + reasonerType);
                }
                else  {
                    builder.setReasonerType(type);
                }
            }
            else if (isObo) {
                builder.setReasonerType(ReasonerType.EL);
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
        catch (Exception e) {
            throw new ConfigParsingException("problem parsing yaml for " + id);
        }

    }
}
