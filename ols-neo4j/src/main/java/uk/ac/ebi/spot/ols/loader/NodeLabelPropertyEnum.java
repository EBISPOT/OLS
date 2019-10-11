package uk.ac.ebi.spot.ols.loader;

/**
 * 
 * @author Henriette Harmse
 * @date 2019-05-13
 *
 * Samples, Phenotypes and Ontologies Team 
 * EMBL-EBI
 */
public enum NodeLabelPropertyEnum {

	OLS_ID("olsId"),
	IRI("iri"),
	SHORT_FORM("short_form"),
	OBO_ID("obo_id"),
	ONTOLOGY_NAME("ontology_name");
	
	private final String propertyName;
	
	NodeLabelPropertyEnum(String propertyName) {
		this.propertyName = propertyName;
	}
	
	public String getPropertyName() {
		return propertyName;
	}
}
