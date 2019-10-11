package uk.ac.ebi.spot.ols.loader;

import org.neo4j.unsafe.batchinsert.BatchInserter;

/**
 * 
 * @author Henriette Harmse
 * @date 2019-05-13
 *
 * Samples, Phenotypes and Ontologies Team 
 * EMBL-EBI
 */
class OLSBatchIndexerCreatorTestHelper extends OLSBatchIndexerCreator {

	private OLSBatchIndexerCreatorTestHelper() {
	}
	
	static BatchInserter createBatchInserterForTesting(String neo4jDirectory) {
		return OLSBatchIndexerCreator.createBatchInserter(null, neo4jDirectory);
	}
}
