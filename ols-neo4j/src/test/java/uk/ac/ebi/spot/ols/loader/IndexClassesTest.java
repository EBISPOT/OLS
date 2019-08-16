package uk.ac.ebi.spot.ols.loader;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.ac.ebi.spot.ols.config.OntologyLoadingConfiguration.DEFAULT_PREFERRED_ROOT_TERM_ANNOTATION_PROPERTY;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.index.lucene.unsafe.batchinsert.LuceneBatchInserterIndexProvider;
import org.neo4j.tooling.GlobalGraphOperations;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserterIndex;
import org.neo4j.unsafe.batchinsert.BatchInserterIndexProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

import uk.ac.ebi.spot.ols.config.OntologyLoadingConfiguration;
import uk.ac.ebi.spot.ols.config.OntologyResourceConfig;
import uk.ac.ebi.spot.ols.exception.OntologyLoadingException;

@Tag("integrationTest")
@TestMethodOrder(OrderAnnotation.class)
@TestInstance(Lifecycle.PER_CLASS)
public class IndexClassesTest {
	private static final Logger logger = LoggerFactory.getLogger(IndexClassesTest.class);
	
	private static final String NEO4J_DIR = "/neo4j";
	private static final String INDEX_CLASSES_TEST = "IndexClassesTest";
	private static final String INDEX_CLASSES_TEST_DEPRECATED = "IndexClassesTestDeprecated";
	private static final String INDEX_CLASSES_TEST_ROOT_DIR = "./" + INDEX_CLASSES_TEST;
	private static final String INDEX_CLASSES_TEST_DEPRECATED_ROOT_DIR = 
			"./" + INDEX_CLASSES_TEST_DEPRECATED;
	private static final String INDEX_CLASSES_TEST_NEO4J_DIR = INDEX_CLASSES_TEST_ROOT_DIR + NEO4J_DIR;
	private static final String INDEX_CLASSES_TEST_DEPRECATED_NEO4J_DIR = 
			INDEX_CLASSES_TEST_DEPRECATED + NEO4J_DIR;
	private static final String BASE_DIR_FOR_TEST_RESOURCES = "./src/test/resources/";
	
	
	public IndexClassesTest() {
	}

	@Disabled
	@Order(1)
	@ParameterizedTest
	@MethodSource("provideOntologies")
	void testIndexClasses(String ontologyIRI, String title, String namespace, String ontologyToIndex, 
			String baseUri, String neo4JDir) {
		
		BatchInserter batchInserter = OLSBatchIndexerCreatorTestHelper
				.createBatchInserter(null, INDEX_CLASSES_TEST_NEO4J_DIR);
		
        OntologyResourceConfig.OntologyResourceConfigBuilder builder =
                new OntologyResourceConfig.OntologyResourceConfigBuilder(ontologyIRI, title, 
                		namespace, (new File(BASE_DIR_FOR_TEST_RESOURCES + ontologyToIndex).toURI()));
        
        builder.setBaseUris(Collections.singleton(baseUri));

        OntologyResourceConfig config = builder.build();
        OntologyLoadingConfiguration ontologyLoadingConfiguration = new 
        		OntologyLoadingConfiguration(DEFAULT_PREFERRED_ROOT_TERM_ANNOTATION_PROPERTY);
        
        OntologyLoader ontologyLoader = null;
        try {
            ontologyLoader = new HermitOWLOntologyLoader(config, null, ontologyLoadingConfiguration);
        } catch (OntologyLoadingException e) {
            logger.error(e.getMessage(), e);
        }
        
        BatchInserterIndexProvider batchInserterIndexProvider =
                    new LuceneBatchInserterIndexProvider(batchInserter);
        BatchInserterIndex batchInserterIndex = OLSBatchIndexerCreatorTestHelper
        		.createBatchInserterIndex(batchInserterIndexProvider);
        
        BatchNeo4JIndexer batchNeo4JIndexer = new BatchNeo4JIndexerHelper(
        		ontologyLoader.getOntologyName(), batchInserterIndex, batchInserterIndexProvider, 
        		batchInserter, neo4JDir);
        
        Map<String, Long> classNodeMap = new HashMap<>();
        Map<String, Long> mergedNodeMap = new HashMap<>();
        
        batchNeo4JIndexer.indexClasses(batchInserter, ontologyLoader, classNodeMap, mergedNodeMap);
        
        batchInserterIndexProvider.shutdown();
        batchInserter.shutdown();
	}

	@Disabled
	@Order(2)
	@ParameterizedTest
	@MethodSource("provideOntologies")
	void testIndexClassesDeprecated(String ontologyIRI, String title, String namespace, String ontologyToIndex, 
			String baseUri, String neo4JDir) {
		
		BatchInserter batchInserter = OLSBatchIndexerCreatorTestHelper
				.createBatchInserter(null, INDEX_CLASSES_TEST_DEPRECATED_NEO4J_DIR);
		
        OntologyResourceConfig.OntologyResourceConfigBuilder builder =
                new OntologyResourceConfig.OntologyResourceConfigBuilder(ontologyIRI, title, 
                		namespace, (new File(BASE_DIR_FOR_TEST_RESOURCES + ontologyToIndex).toURI()));
        
        builder.setBaseUris(Collections.singleton(baseUri));

        OntologyResourceConfig config = builder.build();
        
        OntologyLoader ontologyLoader = null;
        try {
            ontologyLoader = new HermitOWLOntologyLoader(config);
        } catch (OntologyLoadingException e) {
            logger.error(e.getMessage(), e);
        }
        
        BatchInserterIndexProvider batchInserterIndexProvider =
                    new LuceneBatchInserterIndexProvider(batchInserter);
        BatchInserterIndex batchInserterIndex = OLSBatchIndexerCreatorTestHelper
        		.createBatchInserterIndex(batchInserterIndexProvider);
        
        BatchNeo4JIndexer batchNeo4JIndexer = new BatchNeo4JIndexerHelper(
        		ontologyLoader.getOntologyName(), batchInserterIndex, batchInserterIndexProvider,
        		batchInserter, neo4JDir);
        
        Map<String, Long> classNodeMap = new HashMap<>();
        Map<String, Long> mergedNodeMap = new HashMap<>();
        
        batchNeo4JIndexer.indexClassesDeprecated(batchInserter, ontologyLoader, classNodeMap, 
        		mergedNodeMap);
        
        
        batchInserterIndexProvider.shutdown();
        batchInserter.shutdown();
	}
	
	
	/**
	 * For now we just do a few naive checks.
	 * 
	 */
	@Disabled
	@Order(3)
	@Test
	void compareIndexClassesToDeprecatedVersion() {
		GraphDatabaseService indexClassesDB =
				new GraphDatabaseFactory().newEmbeddedDatabase(INDEX_CLASSES_TEST_NEO4J_DIR);
		GraphDatabaseService indexClassesDeprecatedDB =
				new GraphDatabaseFactory().newEmbeddedDatabase(INDEX_CLASSES_TEST_DEPRECATED_NEO4J_DIR);
		
		Transaction indexClassesTransaction = indexClassesDB.beginTx();
		Transaction indexClassesDeprecatedTransaction = indexClassesDeprecatedDB.beginTx();
		
		GlobalGraphOperations indexClassesGlobalGraphOperations = 
				GlobalGraphOperations.at(indexClassesDB);		
		GlobalGraphOperations indexClassesDeprecatedGlobalGraphOperations = 
				GlobalGraphOperations.at(indexClassesDeprecatedDB);	
		
		ResourceIterable<Label> indexClassesAllLabels = 
				indexClassesGlobalGraphOperations.getAllLabels();
		ResourceIterable<Label> indexClassesDepracatedAllLabels = 
				indexClassesDeprecatedGlobalGraphOperations.getAllLabels();
		assertTrue(compare(indexClassesAllLabels.iterator(), 
				indexClassesDepracatedAllLabels.iterator())==0);
		

		ResourceIterable<Node> indexClassesAllNodes = 
				indexClassesGlobalGraphOperations.getAllNodes();
		ResourceIterable<Node> indexClassesDepracatedAllNodes = 
				indexClassesDeprecatedGlobalGraphOperations.getAllNodes();
		assertTrue(compareResourceIteratorsByNode(indexClassesAllNodes.iterator(), 
				indexClassesDepracatedAllNodes.iterator())==0);		
		
		indexClassesTransaction.success();
		indexClassesDeprecatedTransaction.success();
		
		indexClassesTransaction.close();
		indexClassesDeprecatedTransaction.close();		
	}
	
	
	private int compare(ResourceIterator<Label> a, ResourceIterator<Label> b) {
	    while (a.hasNext() && b.hasNext()) {
	        int comparison = a.next().name().compareTo(b.next().name());
	        if (comparison != 0) {
	            return comparison;
	        }
	    }
	    if (a.hasNext())
	        return 1;
	    if (b.hasNext())
	        return -1;
	    return 0;
	}

	private int compareResourceIteratorsByNode(ResourceIterator<Node> a, ResourceIterator<Node> b) {
	    while (a.hasNext() && b.hasNext()) {
	    	if (a.next().getId() == b.next().getId())
	    		return 0;
	    	else if (a.next().getId() < b.next().getId())
	    		return -1;
	    	else
	    		return 1;
	    }
	    if (a.hasNext())
	        return 1;
	    if (b.hasNext())
	        return -1;	
	    return 0;
	}	
	
	private static Stream<Arguments> provideOntologies() {
	    return Stream.of(
	      Arguments.of("http://purl.obolibrary.org/obo/duo", "Data Use Ontology", "DUO", 
	    		  "duo-preferred-roots.owl", "http://purl.obolibrary.org/obo/DUO_", 
	    		  INDEX_CLASSES_TEST_NEO4J_DIR)	
	    );
	}
	
	
	@AfterAll
	void tearDownAll() {
		TestUtils.deleteTestDirectory(INDEX_CLASSES_TEST_ROOT_DIR);		
//		TestUtils.deleteTestDirectory(INDEX_CLASSES_TEST_DEPRECATED_ROOT_DIR);		
	}
	
}
