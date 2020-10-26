package uk.ac.ebi.spot.ols;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;
import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.spot.ols.config.OntologyLoadingConfiguration;
import uk.ac.ebi.spot.ols.config.OntologyResourceConfig;
import uk.ac.ebi.spot.ols.exception.OntologyLoadingException;
import uk.ac.ebi.spot.ols.loader.NoReasonerOWLOntologyLoader;
import uk.ac.ebi.spot.ols.loader.OntologyLoader;

import java.io.File;
import java.util.Collection;
import java.util.Collections;

public class NoReasonerLoaderTest {
    private static final Logger logger = LoggerFactory.getLogger(NoReasonerLoaderTest.class);


    private static void testSimpleHierarchy() {
        OntologyResourceConfig.OntologyResourceConfigBuilder builder =
                new OntologyResourceConfig.OntologyResourceConfigBuilder(
                        "http://www.ebi.ac.uk/test2",
                        "test2",
                        "test2",
                        (new File("ontology-tools/src/test/resources/test2.owl").toURI())

                );
        builder.setBaseUris(Collections.singleton("http://www.ebi.ac.uk/test/Test2"));

//        OntologyResourceConfig.OntologyResourceConfigBuilder builder =
//                new OntologyResourceConfig.OntologyResourceConfigBuilder(
//                        "http://purl.obolibrary.org/obo/duo",
//                        "Data Use Ontology",
//                        "DUO",
//                        (new File("ontology-tools/src/test/resources/duo-preferred-roots.owl").toURI())
//
//                );
//        builder.setBaseUris(Collections.singleton("http://purl.obolibrary.org/obo/DUO_"));
        OntologyResourceConfig config= builder.build();

        OntologyLoadingConfiguration ontologyLoadingConfiguration = new
                OntologyLoadingConfiguration();
        OntologyLoader loader = null;
        try {
            loader = new NoReasonerOWLOntologyLoader(config, null, ontologyLoadingConfiguration);
        } catch (OntologyLoadingException e) {
            e.printStackTrace();
            System.exit(0);
        }
        Collection<IRI> terms = loader.getAllClasses();
        Collection<IRI> properties = loader.getAllObjectPropertyIRIs();

    }

    private static void testDUO() {
        OntologyResourceConfig.OntologyResourceConfigBuilder builder =
                new OntologyResourceConfig.OntologyResourceConfigBuilder(
                        "http://purl.obolibrary.org/obo/duo",
                        "Data Use Ontology",
                        "DUO",
                        (new File("ontology-tools/src/test/resources/duo-preferred-roots.owl").toURI())

                );
        builder.setBaseUris(Collections.singleton("http://purl.obolibrary.org/obo/DUO_"));
        OntologyResourceConfig config= builder.build();

        OntologyLoadingConfiguration ontologyLoadingConfiguration = new
                OntologyLoadingConfiguration();
        OntologyLoader loader = null;
        try {
            loader = new NoReasonerOWLOntologyLoader(config, null, ontologyLoadingConfiguration);
        } catch (OntologyLoadingException e) {
            e.printStackTrace();
            System.exit(0);
        }
        Collection<IRI> terms = loader.getAllClasses();
        Collection<IRI> properties = loader.getAllObjectPropertyIRIs();

    }

    private static void testMondoOWL() {
        OntologyResourceConfig.OntologyResourceConfigBuilder builder =
                new OntologyResourceConfig.OntologyResourceConfigBuilder(
                        "http://purl.obolibrary.org/obo/mondo",
                        "Mondo",
                        "mondo",
                        (new File("ontology-tools/src/test/resources/mondo.owl").toURI())

                );
        builder.setBaseUris(Collections.singleton("http://purl.obolibrary.org/obo/mondo"));
        OntologyResourceConfig config= builder.build();

        OntologyLoadingConfiguration ontologyLoadingConfiguration = new
                OntologyLoadingConfiguration();
        OntologyLoader loader = null;
        try {
            loader = new NoReasonerOWLOntologyLoader(config, null, ontologyLoadingConfiguration);
        } catch (OntologyLoadingException e) {
            e.printStackTrace();
            System.exit(0);
        }
        Collection<IRI> terms = loader.getAllClasses();
        Collection<IRI> properties = loader.getAllObjectPropertyIRIs();

    }

    private static void testMondoOBO() {
        OntologyResourceConfig.OntologyResourceConfigBuilder builder =
                new OntologyResourceConfig.OntologyResourceConfigBuilder(
                        "http://purl.obolibrary.org/obo/mondo",
                        "Mondo",
                        "mondo",
                        (new File("ontology-tools/src/test/resources/mondo.obo").toURI())

                );
        builder.setBaseUris(Collections.singleton("http://purl.obolibrary.org/obo/mondo"));
        OntologyResourceConfig config= builder.build();

        OntologyLoadingConfiguration ontologyLoadingConfiguration = new
                OntologyLoadingConfiguration();
        OntologyLoader loader = null;
        try {
            loader = new NoReasonerOWLOntologyLoader(config, null, ontologyLoadingConfiguration);
        } catch (OntologyLoadingException e) {
            e.printStackTrace();
            System.exit(0);
        }
        Collection<IRI> terms = loader.getAllClasses();
        Collection<IRI> properties = loader.getAllObjectPropertyIRIs();

    }

    public static void main(String[] args) {
        System.out.println("logback-test.xml = " +
                NoReasonerLoaderTest.class.getClassLoader().getResource("logback-test.xml"));
        System.out.println("Classpath = " + System.getProperty("java.class.path"));
        System.out.println("logger.name = " + logger.getName());
        System.setProperty("entityExpansionLimit", "10000000");

        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();

        StatusPrinter.print(lc);
//        testSimpleHierarchy();
        testDUO();
//        testMondoOBO();
//        testMondoOWL();
    }
}
