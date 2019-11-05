	package uk.ac.ebi.spot.ols.loader;

import java.nio.file.FileSystems;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Henriette Harmse
 * @date 2019-05-13
 *
 * Samples, Phenotypes and Ontologies Team 
 * EMBL-EBI
 */
public class CreateSchemaIndexesTest {
	private static final Logger logger = LoggerFactory.getLogger(CreateSchemaIndexesTest.class);
	
	private static final String TEST_NAME = "CreateSchemaIndexesTest";
	private static final String TEST_ROOT_DIR = "./" + TEST_NAME;
	private static final String NEO4J_DIR = TEST_ROOT_DIR + "/neo4j";
	
	public CreateSchemaIndexesTest() {
	}
	
	@Disabled
	@Tag("integrationTest")
	@Test
	void testCreateSchemaIndexes() {
		BatchInserter batchInserter = OLSBatchIndexerCreatorTestHelper.createBatchInserterForTesting(
				FileSystems.getDefault().getPath(NEO4J_DIR).toString());
		
		OLSBatchIndexerCreator.createSchemaIndexes(batchInserter);
	}	
	
	
	@AfterAll
	static void tearDownAll() {
		TestUtils.deleteTestDirectory(FileSystems.getDefault().getPath(TEST_ROOT_DIR).toString());
	}	
}
