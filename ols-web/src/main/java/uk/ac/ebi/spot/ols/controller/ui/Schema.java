package uk.ac.ebi.spot.ols.controller.ui;

import java.util.Collection;
import java.util.HashSet;

public class Schema {
	
	String key;
	Collection<String> values = new HashSet<>();
	
	public Schema(String key, Collection<String> values) {
		super();
		this.key = key;
		this.values = values;
	}
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public Collection<String> getValues() {
		return values;
	}
	public void setValues(Collection<String> values) {
		this.values = values;
	}	

}
