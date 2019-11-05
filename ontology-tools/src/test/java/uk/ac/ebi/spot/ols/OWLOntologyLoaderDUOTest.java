package uk.ac.ebi.spot.ols;

import java.io.File;
import java.util.Collection;
import java.util.Collections;

import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.spot.ols.config.OntologyLoadingConfiguration;
import uk.ac.ebi.spot.ols.config.OntologyResourceConfig;
import uk.ac.ebi.spot.ols.exception.OntologyLoadingException;
import uk.ac.ebi.spot.ols.loader.HermitOWLOntologyLoader;
import uk.ac.ebi.spot.ols.loader.OntologyLoader;
import static uk.ac.ebi.spot.ols.config.OntologyLoadingConfiguration.*;
/**
 * This is an integration test since the ontology is loaded from the actual web site. However, to 
 * be a proper repeatable unit test, is should rather read the ontology from the local /resources
 * directory.  
 * 
 * Henriette To do: Split integration tests from unit tests.
 * 
 * @author Henriette Harmse
 * @date 2019-05-07
 *
 * Samples, Phenotypes and Ontologies Team 
 * EMBL-EBI
 */

public class OWLOntologyLoaderDUOTest {
	private static final Logger logger = LoggerFactory.getLogger(OWLOntologyLoaderDUOTest.class);
	
    public static void main(String[] args) {

    	System.out.println("Classpath = " + System.getProperty("java.class.path"));
    	System.out.println("logger.ROOT_LOGGER_NAME = " + logger.ROOT_LOGGER_NAME);
        System.setProperty("entityExpansionLimit", "10000000");
        
        
        
        logger.debug("Work!");
        
        OntologyResourceConfig.OntologyResourceConfigBuilder builder =
                new OntologyResourceConfig.OntologyResourceConfigBuilder(
                        "http://purl.obolibrary.org/obo/duo",
                        "Data Use Ontology",
                        "DUO",
                        (new File("./src/test/resources/duo-preferred-roots.owl").toURI())

                );
        builder.setBaseUris(Collections.singleton("http://purl.obolibrary.org/obo/DUO_"));

        OntologyResourceConfig config= builder.build();

        OntologyLoadingConfiguration ontologyLoadingConfiguration = new 
        		OntologyLoadingConfiguration(DEFAULT_PREFERRED_ROOT_TERM_ANNOTATION_PROPERTY);
        OntologyLoader loader = null;
        try {
            loader = new HermitOWLOntologyLoader(config, null, ontologyLoadingConfiguration);
        } catch (OntologyLoadingException e) {
            e.printStackTrace();
            System.exit(0);
        }
        Collection<IRI> terms = loader.getAllClasses();
        
//        Collection<IRI> annotationPropertyIRIs = loader.getAllAnnotationPropertyIRIs();

//        logger.debug("Annotation properties:");
//        for (IRI iri: annotationPropertyIRIs) {
//        	logger.debug(iri.toString());
//        }
        
        
        logger.debug("Preferred Root Terms");
        for (IRI iri: loader.getPreferredRootTerms()) {
        	logger.debug(iri.toString());
        }        
    }

}
