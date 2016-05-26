package uk.ac.ebi.spot.ols.api;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.ac.ebi.spot.ols.OlsWebApp;


import javax.servlet.RequestDispatcher;
import java.net.URLEncoder;

import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.halLinks;
import static org.springframework.restdocs.request.RequestDocumentation.*;

/**
 * @author Simon Jupp
 * @date 16/09/15
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = OlsWebApp.class)
@WebAppConfiguration
@Ignore
public class ApiDocumentation {

    @Rule
    public final RestDocumentation restDocumentation = new RestDocumentation("src/main/asciidoc/generated-snippets");

    private RestDocumentationResultHandler document;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        this.document = document("{method-name}"
                ,
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint())
        );

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
                .apply(documentationConfiguration(this.restDocumentation).uris()
                                .withScheme("http")
                                .withHost("www.ebi.ac.uk/ols")
                                .withPort(80)
                )
                .alwaysDo(this.document)
                .build();
    }

    @Test
    public void pageExample () throws Exception {

        this.document.snippets(
                responseFields(
                        fieldWithPath("_links").description("<<resources-page-links,Links>> to other resources"),
                        fieldWithPath("_embedded").description("The list of resources"),
                        fieldWithPath("page.size").description("The number of resources in this page"),
                        fieldWithPath("page.totalElements").description("The total number of resources"),
                        fieldWithPath("page.totalPages").description("The total number of pages"),
                        fieldWithPath("page.number").description("The page number")
                ),
                links(halLinks(),
                        linkWithRel("self").description("This resource list"),
                        linkWithRel("first").description("The first page in the resource list"),
                        linkWithRel("next").description("The next page in the resource list"),
                        linkWithRel("prev").description("The previous page in the resource list"),
                        linkWithRel("last").description("The last page in the resource list")
                )

        );

        this.mockMvc.perform(get("/api/ontologies?page=1&size=1"))
                .andExpect(status().isOk());
    }


    @Test
    public void errorExample() throws Exception {
        this.document.snippets(
                responseFields(
                        fieldWithPath("error").description("The HTTP error that occurred, e.g. `Bad Request`").optional(),
//                fieldWithPath("exception").description("A description of the cause of the error").optional(),
                        fieldWithPath("message").description("A description of the cause of the error").optional(),
                        fieldWithPath("path").description("The path to which the request was made").optional(),
                        fieldWithPath("status").description("The HTTP status code, e.g. `400`").optional(),
                        fieldWithPath("timestamp").description("The time, in milliseconds, at which the error occurred").optional()));

        this.mockMvc
                .perform(get("/error")
                        .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 404)
                        .requestAttr(RequestDispatcher.ERROR_REQUEST_URI,
                                "/api/ontologies/foobar")
                        .requestAttr(RequestDispatcher.ERROR_MESSAGE,
                                "Resource not found"))
        ;
    }

    @Test
    public void apiExample () throws Exception {

        this.document.snippets(
                responseFields(
                        fieldWithPath("_links").description("<<resources-ontologies-links,Links>> to other resources")
                ),
                links(halLinks(),
                        linkWithRel("ontologies").description("Link to the ontologies in OLS"),
                        linkWithRel("terms").description("Link to all the terms in OLS"),
                        linkWithRel("properties").description("Link to all the properties in OLS"),
                        linkWithRel("individuals").description("Link to all the individuals in OLS"),
                        linkWithRel("profile").description("ALPS is not currently supported")
                        )
        );
        this.mockMvc.perform(get("/api").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }


    @Test
    public void ontologiesListExample () throws Exception {

        this.mockMvc.perform(get("/api/ontologies").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

    }

    @Test
    public void ontologiesExample () throws Exception {

        this.document.snippets(
                pathParameters(
                        parameterWithName("ontology_id").description("The ontology id in OLS")),

                responseFields(
                        fieldWithPath("_links").description("<<ontologies-links,Links>> to other resources"),
                        fieldWithPath("ontologyId").description("The short unique id for the ontology"),
                        fieldWithPath("updated").description("Date the ontology was checked for updates"),
                        fieldWithPath("loaded").description("Date the ontology was succesfully loaded"),
                        fieldWithPath("version").description("Version name associated with the ontology"),
                        fieldWithPath("status").description("Status of the ontology {LOADED,LOADING,FAILED}"),
                        fieldWithPath("message").description("Any message relating to the status of the ontology"),
                        fieldWithPath("numberOfTerms").description("Number of terms/classes in the ontology "),
                        fieldWithPath("numberOfProperties").description("Number of properties/relations in the ontology "),
                        fieldWithPath("numberOfIndividuals").description("Number of individuals/instances in the ontology "),
                        fieldWithPath("config").description(
                                "Basic meta-data about the ontology such as its title, description and any other ontology"
                                        + " annotations extracted from the file. It also includes the and download location. and information used by OLS at index time (such as the synonym and description predicates)")
                ),
                links(halLinks(),
                        linkWithRel("self").description("This ontology"),
                        linkWithRel("terms").description("<<overview-pagination,Paginated>> list of <<resources-terms,terms>> in the ontology"),
                        linkWithRel("properties").description("<<overview-pagination,Paginated>> list of <<properties-resources,properties>> in the ontology"),
                        linkWithRel("individuals").description("<<overview-pagination,Paginated>> list of <<individuals-resources,individuals>> in the ontology")
                )

        );

        this.mockMvc.perform(get("/api/ontologies/{ontology_id}", "efo").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void rootsExample () throws Exception {


        this.document.snippets(
                pathParameters(
                          parameterWithName("ontology_id").description("The ontology id in OLS"))
        );
        this.mockMvc.perform(get("/api/ontologies/{ontology_id}/terms/roots", "efo").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void termsListExample() throws Exception {

        this.document.snippets(
                pathParameters(
                        parameterWithName("ontology_id").description("The ontology id in OLS"))
        );

        this.mockMvc.perform(get("/api/ontologies/{ontology_id}/terms", "efo").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void termsListIriExample() throws Exception {

        this.document.snippets(
                pathParameters(
                        parameterWithName("ontology_id").description("The ontology id in OLS"),
                        parameterWithName("iri").description("Filter by IRI, when using IRI the result will always be one")
                )

                );

        this.mockMvc.perform(get("/api/ontologies/{ontology_id}/terms?iri={iri}", "go", "http://purl.obolibrary.org/obo/GO_0043226").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void termsListShortformExample() throws Exception {

        this.document.snippets(
                pathParameters(
                        parameterWithName("ontology_id").description("The ontology id in OLS")
                        ,
                        parameterWithName("short_form").description("Filter by IRI shortform, these values aren't guaranteed to be unique")
                )
        );

        this.mockMvc.perform(get("/api/ontologies/{ontology_id}/terms?short_form={short_form}", "go","GO_0043226").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void termsListOboExample() throws Exception {

        this.document.snippets(
                pathParameters(
                        parameterWithName("ontology_id").description("The ontology id in OLS")
                        ,
                        parameterWithName("obo_id").description("Filter by OBO id. This is OBO style id taht aren't guaranteed to be unique within a given ontology")
                )
        );

        this.mockMvc.perform(get("/api/ontologies/{ontology_id}/terms?obo_id={obo_id}", "go","GO:0043226").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void termsExample () throws Exception {

        this.document.snippets(
                pathParameters(
                        parameterWithName("ontology").description("The OLS ontology id e.g. go"),
                        parameterWithName("iri").description("The IRI of the terms, this value must be double URL encoded")),

                links(halLinks(),
                        linkWithRel("self").description("Link to this resource"),
                        linkWithRel("parents").description("Link to the direct parent resources for this term"),
                        linkWithRel("hierarchicalParents").description("Link to the direct hierarchical parent resources for this term. Hierarchical parents include is-a and other related parents, such as part-of/develops-from, that imply a hierarchical relationship"),
                        linkWithRel("hierarchicalAncestors").description("Link to all hierarchical ancestors (all parents's parents) resources for this term. Hierarchical ancestors include is-a and other related parents, such as part-of/develops-from, that imply a hierarchical relationship"),
                        linkWithRel("ancestors").description("Link to all parent resources for this term"),
                        linkWithRel("children").description("Link to the direct children resources for this term"),
                        linkWithRel("hierarchicalChildren").description("Link to the direct hierarchical children resources for this term. Hierarchical children include is-a and other related children, such as part-of/develops-from, that imply a hierarchical relationship"),
                        linkWithRel("hierarchicalDescendants").description("Link to all hierarchical children resources for this term. Hierarchical children include is-a and other related children, such as part-of/develops-from, that imply a hierarchical relationship"),
                        linkWithRel("descendants").description("Link to all child resources for this term"),
                        linkWithRel("jstree").description("A JSON tree structure of the term hierarchy"),
                        linkWithRel("graph").description("A JSON graph structure of the immediately related nodes")));


        this.mockMvc.perform(get("/api/ontologies/{ontology}/terms/{iri}", "go", URLEncoder.encode("http://purl.obolibrary.org/obo/GO_0043226", "UTF-8")).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

    }

    @Test
    public void propertiesExample () throws Exception {

        this.document.snippets(
                pathParameters(
                        parameterWithName("ontology").description("The OLS ontology id e.g. go"),
                        parameterWithName("iri").description("The IRI of the relation, this value must be double URL encoded"))
                );

        this.mockMvc.perform(get("/api/ontologies/{ontology}/properties/{iri}", "go", URLEncoder.encode("http://purl.obolibrary.org/obo/BFO_0000050", "UTF-8")).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

    }

    @Test
    public void individualsExample () throws Exception {

        this.document.snippets(
                pathParameters(
                        parameterWithName("ontology").description("The OLS ontology id e.g. go"),
                        parameterWithName("iri").description("The IRI of the individual, this value must be double URL encoded"))
                );

        this.mockMvc.perform(get("/api/ontologies/{ontology}/individuals/{iri}", "ro", URLEncoder.encode("http://purl.obolibrary.org/obo/RO_0001901", "UTF-8")).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

    }
}
