package uk.ac.ebi.spot.ols.controller.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.spot.ols.config.OntologyResourceConfig;
import uk.ac.ebi.spot.ols.model.OntologyDocument;
import uk.ac.ebi.spot.ols.service.OntologyRepositoryService;
import uk.ac.ebi.spot.ols.util.ReasonerType;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.*;

/**
 * @author Simon Jupp
 * @date 10/07/2019
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@RestController
@RequestMapping("/api/ols-config")
public class OntologyConfigController {

    private Logger log = LoggerFactory.getLogger(getClass());

    public Logger getLog() {
        return log;
    }

    @Autowired
    private OntologyRepositoryService ontologyRepositoryService;


    @RequestMapping(path = "", produces = {"text/yaml"}, method = RequestMethod.GET)
    String getOntologies(
            @RequestParam(value = "ids", required = false, defaultValue = "") Collection<String> ids
    ) {

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        List<OntologyResourceConfig> configs = new ArrayList<OntologyResourceConfig>();
        for (OntologyDocument document : ontologyRepositoryService.getAllDocuments()) {

            if (ids.isEmpty()) {
                OntologyResourceConfigFormatter ex = new OntologyResourceConfigFormatter(document.getConfig());
                configs.add(ex);
            }
            else if (ids.contains(document.getOntologyId())) {
                OntologyResourceConfigFormatter ex = new OntologyResourceConfigFormatter(document.getConfig());
                configs.add(ex);
            }
        }

        try {
            return mapper.writeValueAsString(new OntologyResourceConfigWrapper(configs));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return "";

    }


    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Resource not found")
    @ExceptionHandler(ResourceNotFoundException.class)
    public void handleError(HttpServletRequest req, Exception exception) {
    }

    private class OntologyResourceConfigWrapper {

        private List<OntologyResourceConfig> ontologies;

        public OntologyResourceConfigWrapper(List<OntologyResourceConfig> ontologies) {
            this.ontologies = ontologies;
        }

        public List<OntologyResourceConfig> getOntologies() {
            return ontologies;
        }

        public void setOntologies(List<OntologyResourceConfig> ontologies) {
            this.ontologies = ontologies;
        }
    }

    private class OntologyResourceConfigFormatter extends OntologyResourceConfig {

        private OntologyResourceConfigFormatter(OntologyResourceConfig config) {
            this.setId(config.getId());
            this.setVersionIri(config.getVersionIri());
            this.setTitle(config.getTitle());
            this.setNamespace(config.getNamespace());
            this.setPreferredPrefix(config.getPreferredPrefix());
            this.setDescription(config.getDescription());
            this.setHomepage(config.getHomepage());
            this.setMailingList(config.getMailingList());
            this.setFileLocation(config.getFileLocation());
            this.setReasonerType(config.getReasonerType());
            this.setLabelProperty(config.getLabelProperty());
            this.setDefinitionProperties(config.getDefinitionProperties());
            this.setSynonymProperties(config.getSynonymProperties());
            this.setHierarchicalProperties(config.getHierarchicalProperties());
            this.setBaseUris(config.getBaseUris());
        }

        @Override
        @JsonIgnore
        public String getVersionIri() {
            return super.getVersionIri();
        }

        @Override
        @JsonIgnore
        public String getVersion() {
            return super.getVersion();
        }

        @Override
        @JsonIgnore
        public Collection<String> getCreators() {
            return super.getCreators();
        }

        @Override
        @JsonIgnore
        public Collection<String> getInternalMetadataProperties() {
            return super.getInternalMetadataProperties();
        }

        @Override
        @JsonIgnore
        public Collection<URI> getHiddenProperties() {
            return super.getHiddenProperties();
        }

        @Override
        @JsonIgnore
        public boolean isSkos() {
            return super.isSkos();
        }

        @Override
        @JsonProperty("uri")
        public String getId() {
            return super.getId();
        }

        @Override
        @JsonProperty("id")
        public String getNamespace() {
            return super.getNamespace();
        }

        @Override
        @JsonProperty("ontology_purl")
        public URI getFileLocation() {
            return super.getFileLocation();
        }

        @Override
        @JsonProperty("mailing_list")
        public String getMailingList() {
            return super.getMailingList();
        }

        @Override
        @JsonProperty("label_property")
        public URI getLabelProperty() {
            return super.getLabelProperty();
        }

        @Override
        @JsonProperty("synonym_property")
        public Collection<URI> getSynonymProperties() {
            return super.getSynonymProperties();
        }

        @Override
        @JsonProperty("definition_property")
        public Collection<URI> getDefinitionProperties() {
            return super.getDefinitionProperties();
        }

        @Override
        @JsonProperty("base_uri")
        public Collection<String> getBaseUris() {
            return super.getBaseUris();
        }

        @Override
        @JsonProperty("hierarchical_property")
        public Collection<URI> getHierarchicalProperties() {
            return super.getHierarchicalProperties();
        }

        @Override
        @JsonProperty("reasoner")
        public ReasonerType getReasonerType() {
            return super.getReasonerType();
        }
    }


}

