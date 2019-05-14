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
class OLSBatchIndexerCreatorHelper extends OLSBatchIndexerCreator {

	private OLSBatchIndexerCreatorHelper() {
	}
	
	static BatchInserter createBatchInserterForTesting(String neo4jDirectory) {
		return OLSBatchIndexerCreator.createBatchInserter(neo4jDirectory);
	}
}
