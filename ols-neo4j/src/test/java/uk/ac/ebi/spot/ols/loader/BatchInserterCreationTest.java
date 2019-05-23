package uk.ac.ebi.spot.ols.loader;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.spot.ols.config.OlsNeo4jConfiguration;

/**
 * 
 * @author Henriette Harmse
 * @date 2019-05-13
 *
 * Samples, Phenotypes and Ontologies Team 
 * EMBL-EBI
 */
public class BatchInserterCreationTest {

	private static final Logger logger = LoggerFactory.getLogger(BatchInserterCreationTest.class);
	
	private static final String TEST_NAME = "BatchInserterCreationTest";
	private static final String TEST_ROOT_DIR = "./" + TEST_NAME; 
	private static final String NEO4J_DIR = TEST_ROOT_DIR + "/neo4j";
	
	public BatchInserterCreationTest() {
	}
	
	@Disabled
	@ParameterizedTest
	@MethodSource("provideNeo4jDirectories")
	void testCreateBatchInserter(String neo4jDirectory) {
		logger.debug("neo4jDirectory = " + neo4jDirectory);
		BatchInserter batchInserter = OLSBatchIndexerCreatorTestHelper
				.createBatchInserterForTesting(neo4jDirectory);
		assertNotNull(batchInserter);
	}
	
	
	private static Stream<Arguments> provideNeo4jDirectories() {
	    return Stream.of(
//	      Arguments.of(OlsNeo4jConfiguration.getNeo4JPath()),
	      Arguments.of(FileSystems.getDefault().getPath(NEO4J_DIR).toString())
	    );
	}
	
	@AfterAll
	static void tearDownAll() {
//		deleteTestDirectory(OlsNeo4jConfiguration.getNeo4JPath());
		TestUtils.deleteTestDirectory(FileSystems.getDefault().getPath(TEST_NAME).toString());
	}
}
