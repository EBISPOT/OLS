package uk.ac.ebi.spot.ols.loader;

import static uk.ac.ebi.spot.ols.loader.Neo4JIndexerConstants.instanceLabel;
import static uk.ac.ebi.spot.ols.loader.Neo4JIndexerConstants.mergedClassLabel;
import static uk.ac.ebi.spot.ols.loader.Neo4JIndexerConstants.nodeLabel;
import static uk.ac.ebi.spot.ols.loader.Neo4JIndexerConstants.relationLabel;

import java.io.File;

import org.neo4j.graphdb.ConstraintViolationException;
import org.neo4j.graphdb.Label;
import org.neo4j.helpers.collection.MapUtil;
import org.neo4j.kernel.DefaultFileSystemAbstraction;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserterIndex;
import org.neo4j.unsafe.batchinsert.BatchInserterIndexProvider;
import org.neo4j.unsafe.batchinsert.BatchInserters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.spot.ols.config.OlsNeo4jConfiguration;

/**
 * 
 * @author Henriette Harmse
 * @date 2019-05-11
 *
 * Samples, Phenotypes and Ontologies Team 
 * EMBL-EBI
 */
class OLSBatchIndexerCreator {
	
	private static final Logger logger = LoggerFactory.getLogger(OLSBatchIndexerCreator.class);

	protected OLSBatchIndexerCreator() {
	}

	protected static BatchInserter createBatchInserter(BatchInserter inserter, String neo4jDirectory) {
//		if (inserter == null ) {
			File file = new File(neo4jDirectory);
		
			inserter = BatchInserters.inserter(
                file.getAbsolutePath(),
                new DefaultFileSystemAbstraction());
//		}
		
		return inserter;
	}
	
	static BatchInserter createBatchInserter() {
		return createBatchInserter(null, OlsNeo4jConfiguration.getNeo4JPath());
	}
	
	static BatchInserterIndex createBatchInserterIndex(BatchInserterIndexProvider indexProvider) {
		BatchInserterIndex batchInserterIndex = null;
		try {
			batchInserterIndex = indexProvider.nodeIndex("Resource", MapUtil.stringMap("type", "exact"));
			batchInserterIndex.setCacheCapacity("iri", 1000000);
		} catch (Throwable t) {
			logger.error(t.getMessage(), t);
		}
        return batchInserterIndex;
	}
	
    static void createSchemaIndexes(BatchInserter inserter) {
        createSchemaIndexIfNotExists(inserter, mergedClassLabel, 
        		NodeLabelPropertyEnum.IRI.getPropertyName());
        
        
        for (NodeLabelPropertyEnum nodeLabelPropertyEnum : NodeLabelPropertyEnum.values()) {
        	createSchemaIndexIfNotExists(inserter, nodeLabel, 
        			nodeLabelPropertyEnum.getPropertyName());
        	createSchemaIndexIfNotExists(inserter, relationLabel, 
        			nodeLabelPropertyEnum.getPropertyName());
        	createSchemaIndexIfNotExists(inserter, instanceLabel, 
        			nodeLabelPropertyEnum.getPropertyName());
		}
    }	
    
    
    private static boolean createSchemaIndexIfNotExists(BatchInserter inserter, Label label, 
    		String propertyName) {
        try {
            inserter.createDeferredSchemaIndex( label ).on(propertyName).create();
            return true;
        } catch (ConstraintViolationException e) {
			logger.debug("Couldn't create index for label '" + label.name() + "' with property '" +
					propertyName + "' as it already exists, continuing...");
        }
        return false;
    }    
}
