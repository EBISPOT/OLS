package uk.ac.ebi.spot.ols.loader;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.ac.ebi.spot.ols.config.OntologyLoadingConfiguration.DEFAULT_PREFERRED_ROOT_TERM_ANNOTATION_PROPERTY;
import static uk.ac.ebi.spot.ols.loader.Neo4JIndexerConstants._nodeLabel;
import static uk.ac.ebi.spot.ols.loader.Neo4JIndexerConstants.nodeLabel;
import static uk.ac.ebi.spot.ols.neo4j.model.Neo4JNodePropertyNameConstants.IS_PREFERRED_ROOT;
import static uk.ac.ebi.spot.ols.neo4j.model.Neo4JNodePropertyNameConstants.IRI;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.index.lucene.unsafe.batchinsert.LuceneBatchInserterIndexProvider;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserterIndex;
import org.neo4j.unsafe.batchinsert.BatchInserterIndexProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.spot.ols.config.OntologyLoadingConfiguration;
import uk.ac.ebi.spot.ols.config.OntologyResourceConfig;
import uk.ac.ebi.spot.ols.exception.OntologyLoadingException;

@TestInstance(Lifecycle.PER_CLASS)
@Tag("integrationTest")
public class PreferredRootTermsNeo4JIndexTest {
	
	private static final Logger logger = LoggerFactory.getLogger(PreferredRootTermsNeo4JIndexTest.class);
	private static final String NEO4J_DIR = "/neo4j";
	private static final String PREFERRED_ROOT_TERMS_TEST = "PreferredRootTermsNeo4JIndexTest";
	private static final String PREFERRED_ROOT_TERMS_TEST_ROOT_DIR = "./" + PREFERRED_ROOT_TERMS_TEST;

	private static final String PREFERRED_ROOT_TERMS_TEST_NEO4J_DIR = PREFERRED_ROOT_TERMS_TEST + 
			NEO4J_DIR;	

	public PreferredRootTermsNeo4JIndexTest() {
	}

//	@Disabled
	@ParameterizedTest
	@MethodSource("providePreferredRootTermsTestArguments")	
	void testPreferredRootTermsCreateNeo4JIndex(OntologyLoader ontologyLoader, String neo4JDir,
			Collection<String> expectedPreferredRootTerms) {
		
		GraphDatabaseService graphDatabaseService = null;
		Transaction transaction = null;
		
		try {
			BatchInserter batchInserter = OLSBatchIndexerCreatorTestHelper
					.createBatchInserter(null, neo4JDir);	
			BatchInserterIndexProvider batchInserterIndexProvider =
	                new LuceneBatchInserterIndexProvider(batchInserter);
			BatchInserterIndex batchInserterIndex = OLSBatchIndexerCreatorTestHelper
	    		.createBatchInserterIndex(batchInserterIndexProvider);	        
	        
			BatchNeo4JIndexer neo4jIndexer = new BatchNeo4JIndexerHelper(ontologyLoader.getOntologyName(),
					batchInserterIndex, batchInserterIndexProvider, batchInserter, neo4JDir);
			
			neo4jIndexer.createIndex(ontologyLoader);
			
			graphDatabaseService = createGraphDatabaseService(neo4JDir);
			
			transaction = graphDatabaseService.beginTx();
			ResourceIterator<Node> preferredRootNodesIterator = graphDatabaseService
					.findNodes(Neo4JIndexerConstants.preferredRootTermLabel);
		
			Collection<String> actualPreferredRootTerms = new LinkedList<String>();
			for (; preferredRootNodesIterator.hasNext();) {
				Node preferredRootNode = preferredRootNodesIterator.next();
				String actualPreferredRootTermIRI = (String)preferredRootNode.getProperty(IRI);
				actualPreferredRootTerms.add(actualPreferredRootTermIRI);
				
				
				assertTrue(((Boolean)preferredRootNode.getProperty(IS_PREFERRED_ROOT)));
				assertTrue(expectedPreferredRootTerms.contains(actualPreferredRootTermIRI));	
			}
			assertTrue(expectedPreferredRootTerms.size() == actualPreferredRootTerms.size());
			transaction.success();
			transaction.close();
			graphDatabaseService.shutdown();
			
		} catch(Throwable t) {
			logger.debug(t.getMessage(), t);
			if (transaction != null) {
				transaction.failure();
				transaction.close();
				graphDatabaseService.shutdown();
			}
		}
	}

	private GraphDatabaseService createGraphDatabaseService(String neo4JDir) {
		GraphDatabaseService graphDatabaseService =  new GraphDatabaseFactory()
				.newEmbeddedDatabaseBuilder(neo4JDir)
				.setConfig(GraphDatabaseSettings.dump_configuration, "true" )
				.setConfig(GraphDatabaseSettings.keep_logical_logs, "false" )
				.newGraphDatabase();
		Runtime.getRuntime().addShutdownHook(new Thread(){
		    @Override
		    public void run()
		    {
		    	graphDatabaseService.shutdown();
		    }
		} );
		return graphDatabaseService;
	}
	
	
	private static OntologyLoader createOntologyLoader(String id, String title, String namespace, 
			String fileLocation, String baseURI) {

		OntologyResourceConfig.OntologyResourceConfigBuilder builder =
                new OntologyResourceConfig.OntologyResourceConfigBuilder(id, title, namespace,
                		new File(fileLocation).toURI());
        
        builder.setBaseUris(Collections.singleton(baseURI));		
        OntologyResourceConfig config = builder.build();

        OntologyLoadingConfiguration ontologyLoadingConfiguration = new 
        		OntologyLoadingConfiguration(DEFAULT_PREFERRED_ROOT_TERM_ANNOTATION_PROPERTY);
        
        OntologyLoader loader = null;
        try {
            loader = new HermitOWLOntologyLoader(config, null, ontologyLoadingConfiguration);
        } catch (OntologyLoadingException e) {
            logger.debug(e.getMessage());
            System.exit(0);
        }		
        return loader;
	}
	
	private static Stream<Arguments> providePreferredRootTermsTestArguments() {	
		Collection<String> expectedPreferredRootTermIRIs = new LinkedList<String>(
				Arrays.asList("http://purl.obolibrary.org/obo/DUO_0000001",
						"http://purl.obolibrary.org/obo/DUO_0000017",
						"http://purl.obolibrary.org/obo/OBI_0000066"));		
		
	    return Stream.of(
	      Arguments.of(createOntologyLoader("http://purl.obolibrary.org/obo/duo", "Data Use Ontology", 
	    		  "DUO", "./src/test/resources/duo-preferred-roots.owl", 
	    		  "http://purl.obolibrary.org/obo/DUO_"), PREFERRED_ROOT_TERMS_TEST_NEO4J_DIR, 
	    		  expectedPreferredRootTermIRIs));
	}	
	
	@AfterAll
	void tearDownAll() {
		TestUtils.deleteTestDirectory(PREFERRED_ROOT_TERMS_TEST_ROOT_DIR);		
	}
	
}
