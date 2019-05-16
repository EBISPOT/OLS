package uk.ac.ebi.spot.ols.loader;

import org.neo4j.unsafe.batchinsert.BatchInserterIndex;

public class BatchNeo4JIndexerHelper extends BatchNeo4JIndexer {

	public BatchNeo4JIndexerHelper(String ontologyName, BatchInserterIndex batchInserterIndex){
		super(ontologyName, batchInserterIndex);
	}

}
