package uk.ac.ebi.spot.ols.neo4j.service;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.kernel.GraphDatabaseDependencies;

import java.util.HashMap;
import java.util.Map;

public class ClassJsTreeBuilderHelper extends ClassJsTreeBuilder {

    public ClassJsTreeBuilderHelper(String neo4jDir) {
        graphDatabaseService = createGraphDatabaseService(neo4jDir);
    }

    private static GraphDatabaseService createGraphDatabaseService(String neo4JDir) {
        Map<String, String> config = new HashMap<>();
        config.put("dump_configuration", "true");
        config.put("keep_logical_logs", "false");
        GraphDatabaseService graphDatabaseService =  new EmbeddedGraphDatabase(neo4JDir,
                config, GraphDatabaseDependencies.newDependencies());

        return graphDatabaseService;
    }
}
