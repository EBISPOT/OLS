package uk.ac.ebi.spot.ols.api;

import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.halLinks;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URLEncoder;

import javax.servlet.RequestDispatcher;

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
                                .withHost("www.ebi.ac.uk")
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

        this.mockMvc.perform(get("/ols/api/ontologies?page=1&size=1").contextPath("/ols"))
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
                                "/ols/api/ontologies/foobar")
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
        this.mockMvc.perform(get("/ols/api").contextPath("/ols").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }


    @Test
    public void ontologiesListExample () throws Exception {

        this.mockMvc.perform(get("/ols/api/ontologies").contextPath("/ols").accept(MediaType.APPLICATION_JSON))
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

        this.mockMvc.perform(get("/ols/api/ontologies/{ontology_id}", "efo").contextPath("/ols").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void rootsExample () throws Exception {


        this.document.snippets(
                pathParameters(
                          parameterWithName("ontology_id").description("The ontology id in OLS"))
        );
        this.mockMvc.perform(get("/ols/api/ontologies/{ontology_id}/terms/roots", "efo").contextPath("/ols").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void termsListExample() throws Exception {

        this.document.snippets(
                pathParameters(
                        parameterWithName("ontology_id").description("The ontology id in OLS"))
        );

        this.mockMvc.perform(get("/ols/api/ontologies/{ontology_id}/terms", "efo").contextPath("/ols").accept(MediaType.APPLICATION_JSON))
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

        this.mockMvc.perform(get("/ols/api/ontologies/{ontology_id}/terms?iri={iri}", "go", "http://purl.obolibrary.org/obo/GO_0043226").contextPath("/ols").accept(MediaType.APPLICATION_JSON))
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

        this.mockMvc.perform(get("/ols/api/ontologies/{ontology_id}/terms?short_form={short_form}", "go","GO_0043226").contextPath("/ols").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void termsListOboExample() throws Exception {

        this.document.snippets(
                pathParameters(
                        parameterWithName("ontology_id").description("The ontology id in OLS")
                        ,
                        parameterWithName("obo_id").description("Filter by OBO id. This is OBO style id that aren't guaranteed to be unique within a given ontology")
                )
        );

        this.mockMvc.perform(get("/ols/api/ontologies/{ontology_id}/terms?obo_id={obo_id}", "go","GO:0043226").contextPath("/ols").accept(MediaType.APPLICATION_JSON))
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


        this.mockMvc.perform(get("/ols/api/ontologies/{ontology}/terms/{iri}", "go", URLEncoder.encode("http://purl.obolibrary.org/obo/GO_0043226", "UTF-8")).contextPath("/ols").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

    }

    @Test
    public void termParentsById () throws Exception {
        this.document.snippets(
                pathParameters(
                        parameterWithName("ontology").description("The OLS ontology id e.g. go"),
                        parameterWithName("id").description("The id of the term, can be URI, short form or obo id")));

        this.mockMvc.perform(get("/ols/api/ontologies/{ontology}/parents?id={id}", "go","GO:0043226").contextPath("/ols").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void termChildrenById () throws Exception {
        this.document.snippets(
                pathParameters(
                        parameterWithName("ontology").description("The OLS ontology id e.g. go"),
                        parameterWithName("id").description("The id of the term, can be URI, short form or obo id")));

        this.mockMvc.perform(get("/ols/api/ontologies/{ontology}/children?id={id}", "go","GO:0043226").contextPath("/ols").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void termAncestorsById () throws Exception {
        this.document.snippets(
                pathParameters(
                        parameterWithName("ontology").description("The OLS ontology id e.g. go"),
                        parameterWithName("id").description("The id of the term, can be URI, short form or obo id")));

        this.mockMvc.perform(get("/ols/api/ontologies/{ontology}/ancestors?id={id}", "go","GO:0043226").contextPath("/ols").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void termDescendantsById () throws Exception {
        this.document.snippets(
                pathParameters(
                        parameterWithName("ontology").description("The OLS ontology id e.g. go"),
                        parameterWithName("id").description("The id of the term, can be URI, short form or obo id")));

        this.mockMvc.perform(get("/ols/api/ontologies/{ontology}/descendants?id={id}", "go","GO:0043226").contextPath("/ols").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void termHierarchicalAncestorsById () throws Exception {
        this.document.snippets(
                pathParameters(
                        parameterWithName("ontology").description("The OLS ontology id e.g. go"),
                        parameterWithName("id").description("The id of the term, can be URI, short form or obo id")));

        this.mockMvc.perform(get("/ols/api/ontologies/{ontology}/hierarchicalAncestors?id={id}", "go","GO:0043226").contextPath("/ols").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void termHierarchicalDescendantsById () throws Exception {
        this.document.snippets(
                pathParameters(
                        parameterWithName("ontology").description("The OLS ontology id e.g. go"),
                        parameterWithName("id").description("The id of the term, can be URI, short form or obo id")));

        this.mockMvc.perform(get("/ols/api/ontologies/{ontology}/hierarchicalDescendants?id={id}", "go","GO:0043226").contextPath("/ols").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void propertiesExample () throws Exception {

        this.document.snippets(
                pathParameters(
                        parameterWithName("ontology").description("The OLS ontology id e.g. go"),
                        parameterWithName("iri").description("The IRI of the relation, this value must be double URL encoded"))
                );

        this.mockMvc.perform(get("/ols/api/ontologies/{ontology}/properties/{iri}", "go", URLEncoder.encode("http://purl.obolibrary.org/obo/BFO_0000050", "UTF-8")).contextPath("/ols").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

    }

    @Test
    public void individualsExample () throws Exception {

        this.document.snippets(
                pathParameters(
                        parameterWithName("ontology").description("The OLS ontology id e.g. go"),
                        parameterWithName("iri").description("The IRI of the individual, this value must be double URL encoded"))
                );

        this.mockMvc.perform(get("/ols/api/ontologies/{ontology}/individuals/{iri}", "ro", URLEncoder.encode("http://purl.obolibrary.org/obo/RO_0001901", "UTF-8")).contextPath("/ols").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

    }
    
    
    /* TODO Henriette: Checking status().isOK() is probably not sufficient. I.e., it is possible 
     * that through, for example copy and paste errors, that a call may be accidentally successful
     * because it uses a pre-existing call. Hence, it may be a good idea to check the response to 
     * determine that the data is indeed correct.
     */
    @Test
    public void termsByIriPath() throws Exception {
    	 this.document.snippets(
                 pathParameters(
                         parameterWithName("id").description("The double UTF-8 encoded IRI of a term")
                 ),

                 responseFields(
                		 fieldWithPath("_embedded").description("The list of terms"),
                         fieldWithPath("_links").description("<<terms-links,Links>> to other terms"),
                         fieldWithPath("page.size").description("The number of terms in this page"),
                         fieldWithPath("page.totalElements").description("The total number of terms"),
                         fieldWithPath("page.totalPages").description("The total number of pages"),
                         fieldWithPath("page.number").description("The page number")
                 ),
                 
                 links(halLinks(),
                         linkWithRel("self").description("Link to this term"))

         );

         this.mockMvc.perform(
        		 get("/ols/api/terms/{id}", 
        				 URLEncoder.encode("http://www.ebi.ac.uk/efo/EFO_0000001", "UTF-8"))
        		 	.contextPath("/ols")
        		 	.accept(MediaType.APPLICATION_JSON))
                 	.andExpect(status().isOk());
    }
    
    
    @Test
    public void termsByIriParam() throws Exception {
    	 this.document.snippets(
                 pathParameters(
                         parameterWithName("iri").description("The IRI of the term to find")
                 ),

                 responseFields(
                		 fieldWithPath("_embedded").description("The list of terms"),
                         fieldWithPath("_links").description("<<terms-links,Links>> to other terms"),
                         fieldWithPath("page.size").description("The number of terms in this page"),
                         fieldWithPath("page.totalElements").description("The total number of terms"),
                         fieldWithPath("page.totalPages").description("The total number of pages"),
                         fieldWithPath("page.number").description("The page number")
                 ),
                 
                 links(halLinks(),
                         linkWithRel("self").description("Link to this term"))

         );

         this.mockMvc.perform(
        		 get("/ols/api/terms?iri={iri}", "http://www.ebi.ac.uk/efo/EFO_0000001")
        		 	.contextPath("/ols")
        		 	.accept(MediaType.APPLICATION_JSON))
                 	.andExpect(status().isOk());
    }    
   
    @Test
    public void termsByShortFormParam() throws Exception {
    	 this.document.snippets(
                 pathParameters(
                         parameterWithName("short_form").description("This typically refers to the "
                         		+ "last part of an IRI. They are not necessarily unique, e.g. "
                         		+ "GO_0098743")
                 ),

                 responseFields(
                		 fieldWithPath("_embedded").description("The list of terms"),
                         fieldWithPath("_links").description("<<terms-links,Links>> to other terms"),
                         fieldWithPath("page.size").description("The number of terms in this page"),
                         fieldWithPath("page.totalElements").description("The total number of terms"),
                         fieldWithPath("page.totalPages").description("The total number of pages"),
                         fieldWithPath("page.number").description("The page number")
                 ),
                 
                 links(halLinks(),
                         linkWithRel("self").description("Link to this term"))

         );

         this.mockMvc.perform(
        		 get("/ols/api/terms?short_form={short_form}", "EFO_0000001")
        		 	.contextPath("/ols")
        		 	.accept(MediaType.APPLICATION_JSON))
                 	.andExpect(status().isOk());
    } 

    @Test
    public void termsByOboIdParam() throws Exception {
    	 this.document.snippets(
                 pathParameters(
                         parameterWithName("obo_id").description("The OBO id of the term to find. "
                         		+ "This is the OBO style id that is not guaranteed to be unique "
                         		+ "within a given ontology")
                 ),

                 responseFields(
                		 fieldWithPath("_embedded").description("The list of terms"),
                         fieldWithPath("_links").description("<<terms-links,Links>> to other terms"),
                         fieldWithPath("page.size").description("The number of terms in this page"),
                         fieldWithPath("page.totalElements").description("The total number of terms"),
                         fieldWithPath("page.totalPages").description("The total number of pages"),
                         fieldWithPath("page.number").description("The page number")
                 ),
                 
                 links(halLinks(),
                         linkWithRel("self").description("Link to this term"))

         );

         this.mockMvc.perform(
        		 get("/ols/api/terms?obo_id={obo_id}", "EFO:0000001")
        		 	.contextPath("/ols")
        		 	.accept(MediaType.APPLICATION_JSON))
                 	.andExpect(status().isOk());
    }    

    @Test
    public void termsByIdParam() throws Exception {
   	 this.document.snippets(
                pathParameters(
                        parameterWithName("id").description("Id here refers to a term identified either"
                        		+ " by an IRI, a short form or an OBO style id")
                ),

                responseFields(
               		 fieldWithPath("_embedded").description("The list of terms"),
                        fieldWithPath("_links").description("<<terms-links,Links>> to other terms"),
                        fieldWithPath("page.size").description("The number of terms in this page"),
                        fieldWithPath("page.totalElements").description("The total number of terms"),
                        fieldWithPath("page.totalPages").description("The total number of pages"),
                        fieldWithPath("page.number").description("The page number")
                ),
                
                links(halLinks(),
                        linkWithRel("self").description("Link to this term"))

        );

        this.mockMvc.perform(
       		 get("/ols/api/terms?id={id}", "EFO:0000001")
       		 	.contextPath("/ols")
       		 	.accept(MediaType.APPLICATION_JSON))
                	.andExpect(status().isOk());
   }
    
    @Test
    public void termsByIriPathAndIsDefiningOntology() throws Exception {
    	 this.document.snippets(
                 pathParameters(
                         parameterWithName("id").description("The double UTF-8 encoded IRI of a term")
                 ),

                 responseFields(
                		 fieldWithPath("_embedded").description("The list of terms"),
                         fieldWithPath("_links").description("<<terms-links,Links>> to other terms"),
                         fieldWithPath("page.size").description("The number of terms in this page"),
                         fieldWithPath("page.totalElements").description("The total number of terms"),
                         fieldWithPath("page.totalPages").description("The total number of pages"),
                         fieldWithPath("page.number").description("The page number")
                 ),
                 
                 links(halLinks(),
                         linkWithRel("self").description("Link to this term"))

         );

         this.mockMvc.perform(
        		 get("/ols/api/terms/findByIdAndIsDefiningOntology/{id}", 
        				 URLEncoder.encode("http://www.ebi.ac.uk/efo/EFO_0000001", "UTF-8"))
        		 	.contextPath("/ols")
        		 	.accept(MediaType.APPLICATION_JSON))
                 	.andExpect(status().isOk());
    }


    @Test
    public void termsByIriParamAndIsDefiningOntology() throws Exception {
    	 this.document.snippets(
                 pathParameters(
                         parameterWithName("iri").description("The IRI of the term to find")
                 ),

                 responseFields(
                		 fieldWithPath("_embedded").description("The list of terms"),
                         fieldWithPath("_links").description("<<terms-links,Links>> to other terms"),
                         fieldWithPath("page.size").description("The number of terms in this page"),
                         fieldWithPath("page.totalElements").description("The total number of terms"),
                         fieldWithPath("page.totalPages").description("The total number of pages"),
                         fieldWithPath("page.number").description("The page number")
                 ),
                 
                 links(halLinks(),
                         linkWithRel("self").description("Link to this term"))

         );

         this.mockMvc.perform(
        		 get("/ols/api/terms/findByIdAndIsDefiningOntology?iri={iri}", 
        				 "http://www.ebi.ac.uk/efo/EFO_0000001")
        		 	.contextPath("/ols")
        		 	.accept(MediaType.APPLICATION_JSON))
                 	.andExpect(status().isOk());
    }    
   
    @Test
    public void termsByShortFormParamAndIsDefiningOntology() throws Exception {
    	 this.document.snippets(
                 pathParameters(
                         parameterWithName("short_form").description("This typically refers to the "
                         		+ "last part of an IRI. They are not necessarily unique, e.g. "
                         		+ "GO_0098743")
                 ),

                 responseFields(
                		 fieldWithPath("_embedded").description("The list of terms"),
                         fieldWithPath("_links").description("<<terms-links,Links>> to other terms"),
                         fieldWithPath("page.size").description("The number of terms in this page"),
                         fieldWithPath("page.totalElements").description("The total number of terms"),
                         fieldWithPath("page.totalPages").description("The total number of pages"),
                         fieldWithPath("page.number").description("The page number")
                 ),
                 
                 links(halLinks(),
                         linkWithRel("self").description("Link to this term"))

         );

         this.mockMvc.perform(
        		 get("/ols/api/terms/findByIdAndIsDefiningOntology?short_form={short_form}", 
        				 "EFO_0000001")
        		 	.contextPath("/ols")
        		 	.accept(MediaType.APPLICATION_JSON))
                 	.andExpect(status().isOk());
    } 

    @Test
    public void termsByOboIdParamAndIsDefiningOntology() throws Exception {
    	 this.document.snippets(
                 pathParameters(
                         parameterWithName("obo_id").description("The OBO id of the term to find. "
                         		+ "This is the OBO style id that is not guaranteed to be unique "
                         		+ "within a given ontology")
                 ),

                 responseFields(
                		 fieldWithPath("_embedded").description("The list of terms"),
                         fieldWithPath("_links").description("<<terms-links,Links>> to other terms"),
                         fieldWithPath("page.size").description("The number of terms in this page"),
                         fieldWithPath("page.totalElements").description("The total number of terms"),
                         fieldWithPath("page.totalPages").description("The total number of pages"),
                         fieldWithPath("page.number").description("The page number")
                 ),
                 
                 links(halLinks(),
                         linkWithRel("self").description("Link to this term"))

         );

         this.mockMvc.perform(
        		 get("/ols/api/terms/findByIdAndIsDefiningOntology?obo_id={obo_id}", "EFO:0000001")
        		 	.contextPath("/ols")
        		 	.accept(MediaType.APPLICATION_JSON))
                 	.andExpect(status().isOk());
    }    

    @Test
    public void termsByIdParamAndIsDefiningOntology() throws Exception {
   	 this.document.snippets(
                pathParameters(
                        parameterWithName("id").description("Id here refers to a term identified either"
                        		+ " by an IRI, a short form or an OBO style id")
                ),

                responseFields(
               		 fieldWithPath("_embedded").description("The list of terms"),
                        fieldWithPath("_links").description("<<terms-links,Links>> to other terms"),
                        fieldWithPath("page.size").description("The number of terms in this page"),
                        fieldWithPath("page.totalElements").description("The total number of terms"),
                        fieldWithPath("page.totalPages").description("The total number of pages"),
                        fieldWithPath("page.number").description("The page number")
                ),
                
                links(halLinks(),
                        linkWithRel("self").description("Link to this term"))

        );

        this.mockMvc.perform(
       		 get("/ols/api/terms/findByIdAndIsDefiningOntology?id={id}", "EFO:0000001")
       		 	.contextPath("/ols")
       		 	.accept(MediaType.APPLICATION_JSON))
                	.andExpect(status().isOk());
   }
    
    
    @Test
    public void propertiesByIriPath() throws Exception {
    	 this.document.snippets(
                 pathParameters(
                         parameterWithName("id").description("The double UTF-8 encoded IRI of a property")
                 ),

                 responseFields(
                		 fieldWithPath("_embedded").description("The list of properties"),
                         fieldWithPath("_links").description("<<terms-links,Links>> to other properties"),
                         fieldWithPath("page.size").description("The number of properties in this page"),
                         fieldWithPath("page.totalElements").description("The total number of properties"),
                         fieldWithPath("page.totalPages").description("The total number of pages"),
                         fieldWithPath("page.number").description("The page number")
                 ),
                 
                 links(halLinks(),
                         linkWithRel("self").description("Link to this property"))

         );

         this.mockMvc.perform(
        		 get("/ols/api/properties/{id}", 
        				 URLEncoder.encode("http://www.ebi.ac.uk/efo/EFO_0000784", "UTF-8"))
        		 	.contextPath("/ols")
        		 	.accept(MediaType.APPLICATION_JSON))
                 	.andExpect(status().isOk());
    }
    
    @Test
    public void propertiesByIriParam() throws Exception {
    	 this.document.snippets(
                 pathParameters(
                         parameterWithName("iri").description("The IRI of the property to find")
                 ),

                 responseFields(
                		 fieldWithPath("_embedded").description("The list of properties"),
                         fieldWithPath("_links").description("<<terms-links,Links>> to other properties"),
                         fieldWithPath("page.size").description("The number of properties in this page"),
                         fieldWithPath("page.totalElements").description("The total number of properties"),
                         fieldWithPath("page.totalPages").description("The total number of pages"),
                         fieldWithPath("page.number").description("The page number")
                 ),
                 
                 links(halLinks(),
                         linkWithRel("self").description("Link to this property"))

         );

         this.mockMvc.perform(
        		 get("/ols/api/properties?iri={iri}", "http://www.ebi.ac.uk/efo/EFO_0000784")
        		 	.contextPath("/ols")
        		 	.accept(MediaType.APPLICATION_JSON))
                 	.andExpect(status().isOk());
    }
    
    
    @Test
    public void propertiesByShortFormParam() throws Exception {
         this.mockMvc.perform(
        		 get("/ols/api/properties?short_form={short_form}", "EFO_0000784")
        		 	.contextPath("/ols")
        		 	.accept(MediaType.APPLICATION_JSON))
                 	.andExpect(status().isOk());
    }   
    
    @Test
    public void propertiesByOboIdParam() throws Exception {
         this.mockMvc.perform(
        		 get("/ols/api/properties?obo_id={obo_id}", "EFO:0000784")
        		 	.contextPath("/ols")
        		 	.accept(MediaType.APPLICATION_JSON))
                 	.andExpect(status().isOk());
    }
    
    @Test
    public void propertiesByIdParam() throws Exception {
         this.mockMvc.perform(
        		 get("/ols/api/properties?id={id}", "EFO:0000784")
        		 	.contextPath("/ols")
        		 	.accept(MediaType.APPLICATION_JSON))
                 	.andExpect(status().isOk());
    }    
   
    @Test
    public void propertiesByIriPathAndIsDefiningOntology() throws Exception {
   	 this.document.snippets(
             pathParameters(
                     parameterWithName("id").description("The double UTF-8 encoded IRI of a property")
             ),

             responseFields(
            		 fieldWithPath("_embedded").description("The list of properties"),
                     fieldWithPath("_links").description("<<terms-links,Links>> to other properties"),
                     fieldWithPath("page.size").description("The number of properties in this page"),
                     fieldWithPath("page.totalElements").description("The total number of properties"),
                     fieldWithPath("page.totalPages").description("The total number of pages"),
                     fieldWithPath("page.number").description("The page number")
             ),
             
             links(halLinks(),
                     linkWithRel("self").description("Link to this property"))

     );

         this.mockMvc.perform(
        		 get("/ols/api/properties/findByIdAndIsDefiningOntology/{id}", 
        				 URLEncoder.encode("http://www.ebi.ac.uk/efo/EFO_0000784", "UTF-8"))
        		 	.contextPath("/ols")
        		 	.accept(MediaType.APPLICATION_JSON))
                 	.andExpect(status().isOk());
    } 
    
    @Test
    public void propertiesByIriParamAndIsDefiningOntology() throws Exception {
	   	 this.document.snippets(
	             pathParameters(
	                     parameterWithName("iri").description("The IRI of the property to find")
	             ),
	
	             responseFields(
	            		 fieldWithPath("_embedded").description("The list of properties"),
	                     fieldWithPath("_links").description("<<terms-links,Links>> to other properties"),
	                     fieldWithPath("page.size").description("The number of properties in this page"),
	                     fieldWithPath("page.totalElements").description("The total number of properties"),
	                     fieldWithPath("page.totalPages").description("The total number of pages"),
	                     fieldWithPath("page.number").description("The page number")
	             ),
	             
	             links(halLinks(),
	                     linkWithRel("self").description("Link to this property"))
	
	     );

         this.mockMvc.perform(
        		 get("/ols/api/properties/findByIdAndIsDefiningOntology?iri={iri}", 
        				 "http://www.ebi.ac.uk/efo/EFO_0000784")
        		 	.contextPath("/ols")
        		 	.accept(MediaType.APPLICATION_JSON))
                 	.andExpect(status().isOk());
    } 
    
    @Test
    public void propertiesByShortFormParamAndIsDefiningOntology() throws Exception {
         this.mockMvc.perform(
        		 get("/ols/api/properties/findByIdAndIsDefiningOntology?short_form={short_form}", 
        				 "EFO_0000784")
        		 	.contextPath("/ols")
        		 	.accept(MediaType.APPLICATION_JSON))
                 	.andExpect(status().isOk());
    }   
    
    @Test
    public void propertiesByOboIdParamAndIsDefiningOntology() throws Exception {
         this.mockMvc.perform(
        		 get("/ols/api/properties/findByIdAndIsDefiningOntology?obo_id={obo_id}", 
        				 "EFO:0000784")
        		 	.contextPath("/ols")
        		 	.accept(MediaType.APPLICATION_JSON))
                 	.andExpect(status().isOk());
    }
    
    @Test
    public void propertiesByIdParamAndIsDefiningOntology() throws Exception {
         this.mockMvc.perform(
        		 get("/ols/api/properties/findByIdAndIsDefiningOntology?id={id}", "EFO:0000784")
        		 	.contextPath("/ols")
        		 	.accept(MediaType.APPLICATION_JSON))
                 	.andExpect(status().isOk());
    }   
    
    
    
////
    
    @Test
    public void individualsByIriPath() throws Exception {
    	 this.document.snippets(
                 pathParameters(
                         parameterWithName("id").description("The double UTF-8 encoded IRI of an individual")
                 ),

                 responseFields(
                		 fieldWithPath("_embedded").description("The list of individuals"),
                         fieldWithPath("_links").description("<<terms-links,Links>> to other individuals"),
                         fieldWithPath("page.size").description("The number of individuals in this page"),
                         fieldWithPath("page.totalElements").description("The total number of individuals"),
                         fieldWithPath("page.totalPages").description("The total number of pages"),
                         fieldWithPath("page.number").description("The page number")
                 ),
                 
                 links(halLinks(),
                         linkWithRel("self").description("Link to this individual"))

         );

         this.mockMvc.perform(
        		 get("/ols/api/individuals/{id}", 
        				 URLEncoder.encode("http://purl.obolibrary.org/obo/IAO_0000125", "UTF-8"))
        		 	.contextPath("/ols")
        		 	.accept(MediaType.APPLICATION_JSON))
                 	.andExpect(status().isOk());
    }
    
    @Test
    public void individualsByIriParam() throws Exception {
    	 this.document.snippets(
                 pathParameters(
                         parameterWithName("iri").description("The IRI of the individual to find")
                 ),

                 responseFields(
                		 fieldWithPath("_embedded").description("The list of individuals"),
                         fieldWithPath("_links").description("<<terms-links,Links>> to other individuals"),
                         fieldWithPath("page.size").description("The number of individuals in this page"),
                         fieldWithPath("page.totalElements").description("The total number of individuals"),
                         fieldWithPath("page.totalPages").description("The total number of pages"),
                         fieldWithPath("page.number").description("The page number")
                 ),
                 
                 links(halLinks(),
                         linkWithRel("self").description("Link to this individual"))

         );

         this.mockMvc.perform(
        		 get("/ols/api/individuals?iri={iri}", "http://purl.obolibrary.org/obo/IAO_0000125")
        		 	.contextPath("/ols")
        		 	.accept(MediaType.APPLICATION_JSON))
                 	.andExpect(status().isOk());
    }
    
    
    @Test
    public void individualsByShortFormParam() throws Exception {
         this.mockMvc.perform(
        		 get("/ols/api/individuals?short_form={short_form}", "IAO_0000125")
        		 	.contextPath("/ols")
        		 	.accept(MediaType.APPLICATION_JSON))
                 	.andExpect(status().isOk());
    }   
    
    @Test
    public void individualsByOboIdParam() throws Exception {
         this.mockMvc.perform(
        		 get("/ols/api/individuals?obo_id={obo_id}", "IAO:0000125")
        		 	.contextPath("/ols")
        		 	.accept(MediaType.APPLICATION_JSON))
                 	.andExpect(status().isOk());
    }
    
    @Test
    public void individualsByIdParam() throws Exception {
         this.mockMvc.perform(
        		 get("/ols/api/individuals?id={id}", "IAO:0000125")
        		 	.contextPath("/ols")
        		 	.accept(MediaType.APPLICATION_JSON))
                 	.andExpect(status().isOk());
    }    
   
    @Test
    public void individualsByIriPathAndIsDefiningOntology() throws Exception {
   	 this.document.snippets(
             pathParameters(
                     parameterWithName("id").description("The double UTF-8 encoded IRI of a individual")
             ),

             responseFields(
            		 fieldWithPath("_embedded").description("The list of individuals"),
                     fieldWithPath("_links").description("<<terms-links,Links>> to other individuals"),
                     fieldWithPath("page.size").description("The number of individuals in this page"),
                     fieldWithPath("page.totalElements").description("The total number of individuals"),
                     fieldWithPath("page.totalPages").description("The total number of pages"),
                     fieldWithPath("page.number").description("The page number")
             ),
             
             links(halLinks(),
                     linkWithRel("self").description("Link to this individual"))

     );

         this.mockMvc.perform(
        		 get("/ols/api/individuals/findByIdAndIsDefiningOntology/{id}", 
        				 URLEncoder.encode("http://purl.obolibrary.org/obo/RO_0001901", "UTF-8"))
        		 	.contextPath("/ols")
        		 	.accept(MediaType.APPLICATION_JSON))
                 	.andExpect(status().isOk());
    } 
    
    @Test
    public void individualsByIriParamAndIsDefiningOntology() throws Exception {
	   	 this.document.snippets(
	             pathParameters(
	                     parameterWithName("iri").description("The IRI of the individual to find")
	             ),
	
	             responseFields(
	            		 fieldWithPath("_embedded").description("The list of individuals"),
	                     fieldWithPath("_links").description("<<terms-links,Links>> to other individuals"),
	                     fieldWithPath("page.size").description("The number of individuals in this page"),
	                     fieldWithPath("page.totalElements").description("The total number of individuals"),
	                     fieldWithPath("page.totalPages").description("The total number of pages"),
	                     fieldWithPath("page.number").description("The page number")
	             ),
	             
	             links(halLinks(),
	                     linkWithRel("self").description("Link to this individual"))
	
	     );

         this.mockMvc.perform(
        		 get("/ols/api/individuals/findByIdAndIsDefiningOntology?iri={iri}", 
        				 "http://purl.obolibrary.org/obo/RO_0001901")
        		 	.contextPath("/ols")
        		 	.accept(MediaType.APPLICATION_JSON))
                 	.andExpect(status().isOk());
    } 
    
    @Test
    public void individualsByShortFormParamAndIsDefiningOntology() throws Exception {
         this.mockMvc.perform(
        		 get("/ols/api/individuals/findByIdAndIsDefiningOntology?short_form={short_form}", 
        				 "IAO_0000125")
        		 	.contextPath("/ols")
        		 	.accept(MediaType.APPLICATION_JSON))
                 	.andExpect(status().isOk());
    }   
    
    @Test
    public void individualsByOboIdParamAndIsDefiningOntology() throws Exception {
         this.mockMvc.perform(
        		 get("/ols/api/individuals/findByIdAndIsDefiningOntology?obo_id={obo_id}", 
        				 "RO_0001901")
        		 	.contextPath("/ols")
        		 	.accept(MediaType.APPLICATION_JSON))
                 	.andExpect(status().isOk());
    }
    
    @Test
    public void individualsByIdParamAndIsDefiningOntology() throws Exception {
         this.mockMvc.perform(
        		 get("/ols/api/individuals/findByIdAndIsDefiningOntology?id={id}", "RO_0001901")
        		 	.contextPath("/ols")
        		 	.accept(MediaType.APPLICATION_JSON))
                 	.andExpect(status().isOk());
    }    
    
}
