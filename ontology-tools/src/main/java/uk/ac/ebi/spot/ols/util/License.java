package uk.ac.ebi.spot.ols.util;

public class License {
	
	String url;
	String logo;
	String label;
	
	public License() {}
	
	public License(String url, String logo, String label) {
		super();
		this.url = url;
		this.logo = logo;
		this.label = label;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public String toString() {
		return "License [url=" + url + ", logo=" + logo + ", label=" + label + "]";
	}

	
	
}
