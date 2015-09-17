package uk.ac.ebi.spot.ols.api;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentation;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.ac.ebi.spot.ols.OlsWebApp;
import uk.ac.ebi.spot.ols.controller.api.OntologyController;
import uk.ac.ebi.spot.ols.service.OntologyRepositoryService;


import java.net.URL;
import java.net.URLEncoder;

import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
public class ApiDocumentation {

    @Rule
    public final RestDocumentation restDocumentation = new RestDocumentation("build/generated-snippets");

    @Autowired
    private WebApplicationContext context;

    @InjectMocks
    OntologyController ontologyController;

    @InjectMocks
    OntologyRepositoryService ontologyRepositoryService;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
                .apply(documentationConfiguration(this.restDocumentation))
                .build();
    }

    @Test
    public void ontologiesExample () throws Exception {

        this.mockMvc.perform(get("/api/ontologies").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("ontologies"));

    }

    @Test
    public void termsExample () throws Exception {

        this.mockMvc.perform(get("/api/ontologies/{ontology}/terms/{iri}", "efo", URLEncoder.encode("http://www.ebi.ac.uk/efo/EFO_0000001", "UTF-8")).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("terms",
                        pathParameters(
                                parameterWithName("ontology").description("The OLS ontology id e.g. go"),
                                parameterWithName("iri").description("The URI of the terms, this value must be double URL encoded")),

                        links(halLinks(),
                                linkWithRel("children").description("Link to the child resources"),
                                linkWithRel("self").description("Link to this resource"),
                                linkWithRel("descendants").description("Link to the descenednats"))));

    }
}
