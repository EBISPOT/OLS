package uk.ac.ebi.spot.ols.config;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.api.exceptions.index.ExceptionDuringFlipKernelException;
import org.neo4j.ogm.session.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.transaction.Neo4jTransactionManager;
import uk.ac.ebi.spot.ols.util.OLSEnv;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.File;
import java.nio.file.Files;

/**
 * @author Simon Jupp
 * @date 05/08/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */

import org.neo4j.dbms.api.DatabaseManagementService;

import static org.neo4j.configuration.GraphDatabaseSettings.DEFAULT_DATABASE_NAME;

@Configuration
// @EnableNeo4jRepositories(basePackages = "uk.ac.ebi.spot.ols.neo4j.repository")
//@EnableTransactionManagement
public class OlsNeo4jConfiguration {

    private static Logger log = LoggerFactory.getLogger(OlsNeo4jConfiguration.class);
    public Logger getLog() {
        return log;
    }

    @Bean
    public SessionFactory sessionFactory() {
        // with domain entity base package(s)
        return new SessionFactory("uk.ac.ebi.spot.ols");
    }

    @Bean
    public Neo4jTransactionManager transactionManager() {
        return new Neo4jTransactionManager(sessionFactory());
    }

    @Bean // (destroyMethod = "shutdown")
    static DatabaseManagementService databaseManagementService() {

        DatabaseManagementService managementService = new DatabaseManagementServiceBuilder(new File(getNeo4JPath())).build();
        registerShutdownHook( managementService );

        return managementService;
    }

    @Bean // (destroyMethod = "shutdown")
    static GraphDatabaseService graphDatabaseService() {

        GraphDatabaseService service = databaseManagementService().database( DEFAULT_DATABASE_NAME );

                //                 .setConfig( GraphDatabaseSettings.dump_configuration, "true" )
                //.setConfig( GraphDatabaseSettings.keep_logical_logs, "false" )

        return service;
    }

    private static void registerShutdownHook( final DatabaseManagementService managementService )
    {
        // Registers a shutdown hook for the Neo4j instance so that it
        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
        // running application).
        Runtime.getRuntime().addShutdownHook( new Thread()
        {
            @Override
            public void run()
            {
                managementService.shutdown();
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
