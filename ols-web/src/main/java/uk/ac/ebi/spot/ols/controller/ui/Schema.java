package uk.ac.ebi.spot.ols.controller.ui;

import java.util.Collection;
import java.util.HashSet;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

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
	
	public MultiValueMap<String, String> filterBy(String schema, String classification) {
		MultiValueMap<String, String> filter = new LinkedMultiValueMap<String, String>();
		filter.add("schema", schema);
		filter.add("classification", classification);
		return filter;
	}
	
	public String generateURIString(String classification) {
		return org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequest().replaceQueryParams(filterBy(this.getKey(),classification)).toUriString();
	}

}
