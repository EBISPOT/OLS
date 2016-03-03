package uk.ac.ebi.spot.ols.util;

import java.util.Collection;

/**
 * @author Simon Jupp
 * @date 02/03/2016
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
public class OBODefinitionCitation {

    private String definition;
    private Collection<OBOXref> oboXrefs;

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public Collection<OBOXref> getOboXrefs() {
        return oboXrefs;
    }

    public void setOboXrefs(Collection<OBOXref> oboXrefs) {
        this.oboXrefs = oboXrefs;
    }

    public OBODefinitionCitation() {

    }
}
