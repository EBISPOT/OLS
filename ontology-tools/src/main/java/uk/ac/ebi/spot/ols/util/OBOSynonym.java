package uk.ac.ebi.spot.ols.util;

import java.util.Collection;

/**
 * @author Simon Jupp
 * @date 02/03/2016
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
public class OBOSynonym {

    String name;
    String scope;
    String type;
    Collection<OBOXref> Xrefs;

    public OBOSynonym() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Collection<OBOXref> getXrefs() {
        return Xrefs;
    }

    public void setXrefs(Collection<OBOXref> xrefs) {
        Xrefs = xrefs;
    }
}
