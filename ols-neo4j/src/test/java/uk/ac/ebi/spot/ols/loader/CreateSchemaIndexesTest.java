package uk.ac.ebi.spot.ols.loader;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Disabled;
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
	private static final String NEO4J_DIR = "./" + TEST_NAME + "/neo4j";
	
	public CreateSchemaIndexesTest() {
	}
	
//	@Disabled
	@Test
	void testCreateSchemaIndexes() {
		BatchInserter batchInserter = OLSBatchIndexerCreatorHelper.createBatchInserter(
				FileSystems.getDefault().getPath(NEO4J_DIR).toString());
		
		OLSBatchIndexerCreator.createSchemaIndexes(batchInserter);
	}	
	
	
	@AfterAll
	static void tearDownAll() {
		File neo4jDirectoryAsFile = new File(FileSystems.getDefault().getPath(NEO4J_DIR).toString());
		try {
			FileUtils.deleteDirectory(neo4jDirectoryAsFile);
		} catch (IOException e) {
			logger.debug(neo4jDirectoryAsFile.getAbsolutePath() + " directory could not be deleted", e);
		}			
	}	
}
