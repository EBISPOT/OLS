package uk.ac.ebi.spot.ols.controller.ui;

import java.util.Collection;
import java.util.HashSet;

/**
 * @author Simon Jupp
 * @date 13/07/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
public class AdvancedSearchOptions {


    private String query;
    private Collection<String> queryField = new HashSet<>();
    private Collection<String> ontologies = new HashSet<>();
    private Collection<String> types = new HashSet<>();
    private Collection<String> slims = new HashSet<>();
    private boolean queryObsoletes = false;
    private boolean isLocal = false;
    private boolean exact = false;
    private Collection<String> childrenOf = new HashSet<>();
    private Integer rows = 10;
    private Integer start = 0;
    private String groupField;

    public AdvancedSearchOptions(String query, boolean queryObsoletes, boolean exact, boolean isLocal, Integer rows, Integer start) {
        this.query = query;
        this.queryObsoletes = queryObsoletes;
        this.isLocal = isLocal;
        this.exact = exact;
        this.rows = rows;
        this.start = start;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Collection<String> getQueryField() {
        return queryField;
    }

    public void setQueryField(Collection<String> queryField) {
        this.queryField = queryField;
    }

    public Collection<String> getOntologies() {
        return ontologies;
    }

    public void setOntologies(Collection<String> ontologies) {
        this.ontologies = ontologies;
    }

    public Collection<String> getTypes() {
        return types;
    }

    public void setTypes(Collection<String> types) {
        this.types = types;
    }

    public Collection<String> getSlims() {
        return slims;
    }

    public void setSlims(Collection<String> slims) {
        this.slims = slims;
    }

    public boolean isQueryObsoletes() {
        return queryObsoletes;
    }

    public void setQueryObsoletes(boolean queryObsoletes) {
        this.queryObsoletes = queryObsoletes;
    }

    public boolean isLocal() {
        return isLocal;
    }

    public void setIsLocal(boolean isLocal) {
        this.isLocal = isLocal;
    }

    public Collection<String> getChildrenOf() {
        return childrenOf;
    }

    public void setChildrenOf(Collection<String> childrenOf) {
        this.childrenOf = childrenOf;
    }

    public Integer getRows() {
        return rows;
    }

    public void setRows(Integer rows) {
        this.rows = rows;
    }

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public boolean isExact() {
        return exact;
    }

    public String getGroupField() {
        return groupField;
    }

    public void setGroupField(String groupField) {
        this.groupField = groupField;
    }
}
