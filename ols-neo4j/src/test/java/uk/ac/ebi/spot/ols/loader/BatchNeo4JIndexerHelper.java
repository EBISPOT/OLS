package uk.ac.ebi.spot.ols.loader;

import org.neo4j.graphdb.GraphDatabaseService;

public class BatchNeo4JIndexerHelper extends BatchNeo4JIndexer {

	private String neo4JPath;
	
//	public BatchNeo4JIndexerHelper(String ontologyName, BatchInserterIndex batchInserterIndex,
//			BatchInserterIndexProvider batchInserterIndexProvider, BatchInserter batchInserter,
//			String neo4JPath){
//		super(ontologyName, batchInserterIndex, batchInserterIndexProvider, batchInserter);
//		this.neo4JPath = neo4JPath;
//	}
//
//	@Override
//    protected GraphDatabaseService getGraphDatabase () {
//   		return new GraphDatabaseFactory().newEmbeddedDatabase(neo4JPath);
//    }
}
