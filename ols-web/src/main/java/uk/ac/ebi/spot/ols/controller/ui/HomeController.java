package uk.ac.ebi.spot.ols.controller.ui;

/**
 * @author Simon Jupp
 * @date 08/07/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */

import org.neo4j.graphdb.GraphDatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.ac.ebi.spot.ols.indexer.OntologySolrRepository;
import uk.ac.ebi.spot.ols.model.OntologyDocument;
import uk.ac.ebi.spot.ols.model.Status;
import uk.ac.ebi.spot.ols.neo4j.repository.OntologyTermRepository;
import uk.ac.ebi.spot.ols.neo4j.service.OntologyTermGraphService;
import uk.ac.ebi.spot.ols.service.OntologyRepositoryService;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("")
public class HomeController {

    @Autowired
    OntologyRepositoryService repositoryService;

    @ModelAttribute("all_ontologies")
    public List<OntologyDocument> getOntologies() {
        return repositoryService.getAllDocumentsByStatus(Status.LOADED, new Sort(new Sort.Order(Sort.Direction.ASC, "ontologyId")));
    }

    @RequestMapping({"", "/index"})
    public String showHome(Model model) {

        Date lastUpdated = repositoryService.getLastUpdated();
        int numberOfOntologies = repositoryService.getNumberOfOntologies();
        int numberOfTerms = repositoryService.getNumberOfTerms();
        int numberOfProperties = repositoryService.getNumberOfProperties();
        int numberOfIndividuals = repositoryService.getNumberOfIndividuals();

        SummaryInfo summaryInfo = new SummaryInfo(lastUpdated, numberOfOntologies, numberOfTerms, numberOfProperties, numberOfIndividuals, getClass().getPackage().getImplementationVersion());

        model.addAttribute("summary", summaryInfo);
        return "index";
    }


    @RequestMapping("/search")
    public String doSearch(
            @RequestParam(value = "q", defaultValue = "*") String query,
            @RequestParam(value = "ontology", required = false) Collection<String> ontologies,
            @RequestParam(value = "type", required = false) Collection<String> types,
            @RequestParam(value= "slim", required = false) Collection<String> slims,
            @RequestParam(value = "queryFields", required = false) Collection<String> queryFields,
            @RequestParam(value = "exact", required = false) boolean exact,
            @RequestParam(value = "groupField", required = false) String groupField,
            @RequestParam(value = "obsoletes", defaultValue = "false") boolean queryObsoletes,
            @RequestParam(value = "local", defaultValue = "false") boolean isLocal,
            @RequestParam(value = "childrenOf", required = false) Collection<String> childrenOf,
            @RequestParam(value = "rows", defaultValue = "10") Integer rows,
            @RequestParam(value = "start", defaultValue = "0") Integer start,
            Model model

            ) {

        AdvancedSearchOptions searchOptions = new AdvancedSearchOptions(
                query,
                queryObsoletes,
                exact,
                isLocal,
                rows,
                start
        );

        if (ontologies != null) {
            searchOptions.setOntologies(ontologies);
        }

        if (queryFields != null) {
            searchOptions.setQueryField(queryFields);
        }

        if (types != null) {
            searchOptions.setTypes(types);
        }

        if (slims != null) {
            searchOptions.setSlims(slims);
        }

        if (groupField != null) {
            searchOptions.setGroupField(groupField);
        }


        model.addAttribute("searchOptions", searchOptions);
        return "search";
    }


    @RequestMapping({"contact"})
    public String showContact() {
        return "contact";
    }

    @RequestMapping({"sparql"})
    public String showSparql() {
        return "comingsoon";
    }
    @RequestMapping({"about"})
    public String showAbout() {
        return "about";
    }

    @RequestMapping({"docs"})
    public String showDocs() {
        return "comingsoon";
    }

    @RequestMapping({"docs/api"})
    public String showApiDocs() {

        return "forward:../docs/api-template.html";
    }

    private class SummaryInfo {
        Date lastUpdated;
        int numberOfOntologies;
        int numberOfTerms;
        int numberOfProperties;
        int numberOfIndividuals;
        String softwareVersion;

        public SummaryInfo(Date lastUpdated, int numberOfOntologies, int numberOfTerms, int numberOfProperties, int numberOfIndividuals, String softwareVersion) {
            this.lastUpdated = lastUpdated;
            this.numberOfOntologies = numberOfOntologies;
            this.numberOfTerms = numberOfTerms;
            this.numberOfProperties = numberOfProperties;
            this.numberOfIndividuals = numberOfIndividuals;
            this.softwareVersion = softwareVersion;
        }

        public Date getLastUpdated() {
            return lastUpdated;
        }

        public void setLastUpdated(Date lastUpdated) {
            this.lastUpdated = lastUpdated;
        }

        public int getNumberOfOntologies() {
            return numberOfOntologies;
        }

        public void setNumberOfOntologies(int numberOfOntologies) {
            this.numberOfOntologies = numberOfOntologies;
        }

        public int getNumberOfTerms() {
            return numberOfTerms;
        }

        public void setNumberOfTerms(int numberOfTerms) {
            this.numberOfTerms = numberOfTerms;
        }

        public int getNumberOfProperties() {
            return numberOfProperties;
        }

        public void setNumberOfProperties(int numberOfProperties) {
            this.numberOfProperties = numberOfProperties;
        }

        public int getNumberOfIndividuals() {
            return numberOfIndividuals;
        }

        public void setNumberOfIndividuals(int numberOfIndividuals) {
            this.numberOfIndividuals = numberOfIndividuals;
        }

        public String getSoftwareVersion() {
            return softwareVersion;
        }

        public void setSoftwareVersion(String softwareVersion) {
            this.softwareVersion = softwareVersion;
        }
    }


}
