package uk.ac.ebi.spot.ols.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.spot.ols.exception.ConfigParsingException;
import uk.ac.ebi.spot.ols.util.ReasonerType;

import java.net.URI;
import java.util.*;

import static uk.ac.ebi.spot.ols.config.OntologyResourceConfigEnum.*;

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
        String id = ((String)ontology.get(ID.getPropertyName())); // e.g. Uberon

        try {
            String prefix = getPrefix(id);
            String ontologyTitle = getTitle(id);
            URI ontologyPURL = getOntologyPURL();
            String uri = getOntologyURI(id, ontologyPURL);


            //Build the OntologyResourceConfig and add it to the Collection.
            OntologyResourceConfig.OntologyResourceConfigBuilder builder =
                    new OntologyResourceConfig.OntologyResourceConfigBuilder(uri, ontologyTitle, id, ontologyPURL);

            builder.setPreferredPrefix(prefix);

            populateLabelProperty(builder);
            populateDefinitionProperty(builder);
            populateSynonymProperty(builder);

            // Henriette To do: Remove since this is not used.
            if (ontology.containsKey("hidden_property"))  {
                Set<URI> hiddenUris = new HashSet<>();
                for (String hidden :  (ArrayList<String>) ontology.get("hidden_property")) {
                    hiddenUris.add(URI.create(hidden));
                }
                builder.setHiddenProperties(hiddenUris);
            }

            populateHierarchicalProperty(builder);
            populateBaseURI(id, builder);
            populateReasoner(builder);
            populateOBOSlims(builder);
            populateDescription(builder);
            populateHomepage(builder);
            populateMailingList(builder);
            populateTracker(builder);
            populateCreator(builder);
            populatePreferredRootTerms(builder);
            populateAllowDownload(builder);

            return builder.build();
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ConfigParsingException("problem parsing yaml for " + id, e);
        }

    }


    private void populatePreferredRootTerms(OntologyResourceConfig.OntologyResourceConfigBuilder builder) {
        if (ontology.containsKey(PREFERRED_ROOT_TERM.getPropertyName()))  {
            Set<URI> preferredRootTerms = new HashSet<>();
            for (String hierarchical :  (ArrayList<String>) ontology.get(PREFERRED_ROOT_TERM.getPropertyName())) {
                preferredRootTerms.add(URI.create(hierarchical));
            }
            builder.setPreferredRootTerms(preferredRootTerms);
        }
    }

    private void populateCreator(OntologyResourceConfig.OntologyResourceConfigBuilder builder) {
        if (ontology.containsKey(CREATOR.getPropertyName()))  {
            Set<String> creators = new HashSet<>();
            for (String creator :  (ArrayList<String>) ontology.get(CREATOR.getPropertyName())) {
                creators.add(creator);
            }
            builder.setCreators(creators);
        }
    }

    private void populateMailingList(OntologyResourceConfig.OntologyResourceConfigBuilder builder) {
        if (ontology.containsKey(MAILING_LIST.getPropertyName())) {
            builder.setMailingList((String) ontology.get(MAILING_LIST.getPropertyName()));
        }
    }

    private void populateTracker(OntologyResourceConfig.OntologyResourceConfigBuilder builder) {
        if (ontology.containsKey(TRACKER.getPropertyName())) {
            builder.setMailingList((String) ontology.get(TRACKER.getPropertyName()));
        }
    }

    private void populateHomepage(OntologyResourceConfig.OntologyResourceConfigBuilder builder) {
        if (ontology.containsKey(HOMEPAGE.getPropertyName())) {
            if (ontology.get(HOMEPAGE.getPropertyName()) instanceof ArrayList) {
                ArrayList pages = (ArrayList) ontology.get(HOMEPAGE.getPropertyName());
                builder.setHomepage((String) pages.get(0));
            } else {
                builder.setHomepage((String) ontology.get(HOMEPAGE.getPropertyName()));
            }
        }
    }

    private void populateDescription(OntologyResourceConfig.OntologyResourceConfigBuilder builder) {
        if (ontology.containsKey(DESCRIPTION.getPropertyName())) {
            builder.setDescription((String) ontology.get(DESCRIPTION.getPropertyName()));
        }
    }

    private void populateOBOSlims(OntologyResourceConfig.OntologyResourceConfigBuilder builder) {
        if (ontology.containsKey(OBO_SLIMS.getPropertyName())) {
            builder.setOboSlims((boolean) ontology.get(OBO_SLIMS.getPropertyName()));
        }
        else if (isObo) {
            builder.setOboSlims(true);
        }
    }

    private void populateReasoner(OntologyResourceConfig.OntologyResourceConfigBuilder builder) {
        if (ontology.containsKey(REASONER.getPropertyName())) {
            String reasonerType = (String) ontology.get(REASONER.getPropertyName());
            ReasonerType type = ReasonerType.valueOf(reasonerType.toUpperCase());
            if (type == null) {
                log.warn("Unknown reasoner type, defaulting to structural reasoner " + reasonerType);
                builder.setReasonerType(ReasonerType.NONE);
            }
            else  {
                builder.setReasonerType(type);
            }
        }
        else if (isObo) {
            builder.setReasonerType(ReasonerType.NONE);
        }
    }

    private void populateBaseURI(String id, OntologyResourceConfig.OntologyResourceConfigBuilder builder) {
        if (ontology.containsKey(BASE_URI.getPropertyName()))  {
            Set<String> baseUris = new HashSet<>();
            for (String baseUri :  (ArrayList<String>) ontology.get(BASE_URI.getPropertyName())) {
                baseUris.add(baseUri);
            }
            builder.setBaseUris(baseUris);
        }
        else if (isObo) {
            builder.setBaseUris(Collections.singleton(base + id.toUpperCase() + "_"));
        }
    }

    private void populateHierarchicalProperty(OntologyResourceConfig.OntologyResourceConfigBuilder builder) {
        if (ontology.containsKey(HIERARCHICAL_PROPERTY.getPropertyName()))  {
            Set<URI> hierarchicalUris = new HashSet<>();
            for (String hierarchical :  (ArrayList<String>) ontology.get(HIERARCHICAL_PROPERTY.getPropertyName())) {
                hierarchicalUris.add(URI.create(hierarchical));
            }
            builder.setHierarchicalProperties(hierarchicalUris);
        }
        else  {
            builder.setHierarchicalProperties(OboDefaults.hierarchical_relations);
        }
    }

    private void populateSynonymProperty(OntologyResourceConfig.OntologyResourceConfigBuilder builder) {
        if (ontology.containsKey(OntologyResourceConfigEnum.SYNONYM_PROPERTY.getPropertyName()))  {
            Set<URI> synonymsUris = new HashSet<>();
            for (String synonymProperty :  (ArrayList<String>) ontology.get(SYNONYM_PROPERTY.getPropertyName())) {
                synonymsUris.add(URI.create(synonymProperty));
            }
            builder.setSynonymProperties(synonymsUris);
        }
        else if (isObo) {
            builder.setSynonymProperties(Collections.singleton(URI.create(OboDefaults.EXACT_SYNONYM)));
        }
    }

    private void populateDefinitionProperty(OntologyResourceConfig.OntologyResourceConfigBuilder builder) {
        if (ontology.containsKey(OntologyResourceConfigEnum.DEFINITION_PROPERTY.getPropertyName()))  {
            Collection<URI> definitionUris = new HashSet<>();
            for (String definition : (ArrayList<String>) ontology.get(DEFINITION_PROPERTY.getPropertyName())) {
                definitionUris.add(URI.create(definition));
            }
            builder.setDefinitionProperties(definitionUris);
        }
        else if (isObo) {
            builder.setDefinitionProperties(Collections.singleton(URI.create(OboDefaults.DEFINITION)));
        }
    }

    private void populateLabelProperty(OntologyResourceConfig.OntologyResourceConfigBuilder builder) {
        if (ontology.containsKey(LABEL_PROPERTY.getPropertyName()))  {
            String labelProperty = (String) ontology.get(LABEL_PROPERTY.getPropertyName());
            builder.setLabelProperty(URI.create(labelProperty));
        }
    }

    private String getOntologyURI(String id, URI ontologyPURL) {
        String uri;
        if (ontology.containsKey(ONTOLOGY_URI.getPropertyName())) {
            uri = (String) ontology.get(ONTOLOGY_URI.getPropertyName());
        }
        else if (base != null) {
            uri = base + id;
        }
        else if (ontologyPURL != null) {
            uri = ontologyPURL.toString();
        } else {
            uri = null;
        }
        return uri;
    }

    private URI getOntologyPURL() {
        URI location = null;

        if (ontology.containsKey(ONTOLOGY_PURL.getPropertyName())) {
            location = URI.create((String) ontology.get(ONTOLOGY_PURL.getPropertyName()));
        }
        return location;
    }

    private String getTitle(String id) {
        String ontologyTitle = (String)ontology.get(TITLE.getPropertyName());

        if (ontologyTitle == null) {
            ontologyTitle = id;
        }
        return ontologyTitle;
    }

    private String getPrefix(String id) {
        String prefix = id.toUpperCase();
        if (ontology.containsKey(PREFERRED_PREFIX.getPropertyName()))  {
            prefix =  (String) ontology.get(PREFERRED_PREFIX.getPropertyName());
        }
        return prefix;
    }

    private void populateAllowDownload(OntologyResourceConfig.OntologyResourceConfigBuilder builder) {
        if (ontology.containsKey(ALLOW_DOWNLOAD.getPropertyName())) {
            builder.setAllowDownload((boolean) ontology.get(ALLOW_DOWNLOAD.getPropertyName()));
        } else {
            builder.setAllowDownload(true);
        }
    }
}
