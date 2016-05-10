package uk.ac.ebi.spot.ols.config;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.config.Neo4jConfiguration;
import uk.ac.ebi.spot.ols.util.OLSEnv;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.File;
import java.nio.file.Files;

/**
 * @author Simon Jupp
 * @date 05/08/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@Configuration
public class OlsNeo4jConfiguration extends Neo4jConfiguration {

    public OlsNeo4jConfiguration() {
        setBasePackage("uk.ac.ebi.spot.ols");
    }

    @Bean (destroyMethod = "shutdown")
    static GraphDatabaseService graphDatabaseService() {

        GraphDatabaseService service =  new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(getNeo4JPath())
//                .setConfig(GraphDatabaseSettings.read_only, "true")
                .setConfig( GraphDatabaseSettings.dump_configuration, "true" )
                .setConfig( GraphDatabaseSettings.keep_logical_logs, "false" )
                .newGraphDatabase();

        registerShutdownHook(service);
        return service;
    }

    private static void registerShutdownHook( final GraphDatabaseService graphDb )
    {
        // Registers a shutdown hook for the Neo4j instance so that it
        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
        // running application).
        Runtime.getRuntime().addShutdownHook( new Thread()
        {
            @Override
            public void run()
            {
                graphDb.shutdown();
            }
        } );
    }

    public static String getNeo4JPath () {
        String neo4jPath = System.getProperty("ols.neo4j.filedir");
        if (neo4jPath != null) {
            return neo4jPath;
        }
        return OLSEnv.getOLSHome() + File.separator + "neo4j";
    }
}
