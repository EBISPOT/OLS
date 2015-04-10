package uk.ac.ebi.spot.ols.model;

/**
 * Created by dwelter on 19/01/15.
 */
public class SearchResult {

//    public SearchResult(String query){
//
//    }

    private String query;
    private String facet;
    private String filter;

    public String getQuery(){
        return query;
    }

    public void setQuery(String query){
        this.query = query;
    }

    public String getFacet() {
        return facet;
    }

    public void setFacet(String facet) {
        this.facet = facet;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }
}
