package uk.ac.ebi.spot.ols.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * The purpose of this class is to define configuration that are specific to the loading of ontologies
 * into OLS that are applicable across ontologies.
 * 
 * 
 * Henriette To do: 
 * 1. It may make sense to move setting of the entityExpansionLimit here.
 * 
 * @author Henriette Harmse
 *
 */
@Component
public class OntologyLoadingConfiguration {
  
	public final static String DEFAULT_PREFERRED_ROOT_TERM_ANNOTATION_PROPERTY = 
			"http://www.ebi.ac.uk/ols/vocabulary/hasPreferredRootTerm";
	
    public OntologyLoadingConfiguration() {
		super();
	}


    
	public OntologyLoadingConfiguration(String preferredRootTermAnnotationProperty) {
		super();
		this.preferredRootTermAnnotationProperty = preferredRootTermAnnotationProperty;
	}

	@Value("${annotationproperty.preferredroot.term:DEFAULT_PREFERRED_ROOT_TERM_ANNOTATION_PROPERTY}")
    private String preferredRootTermAnnotationProperty;

	public String getPreferredRootTermAnnotationProperty() {
		return preferredRootTermAnnotationProperty;
	}
}
