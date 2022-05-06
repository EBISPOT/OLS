package uk.ac.ebi.spot.ols.neo4j.service;


import org.springframework.stereotype.Component;

@Component
public class PropertyJsTreeBuilder extends AbstractJsTreeBuilder {
    @Override
    String getJsTreeParentQuery(String lang) {
        StringBuilder query = new StringBuilder();

        query.append("MATCH path = (n:Property)-[r:SUBPROPERTYOF*]->(parent)\n");
        query.append("USING INDEX n:Property(iri)\n");
        query.append("WHERE n.ontology_name = {0} AND n.iri = {1}\n");
        query.append("UNWIND rels(path) as r1\n");
        query.append("RETURN distinct id(startNode(r1)) as startId, startNode(r1).iri as startIri, ");
        query.append("startNode(r1).label as startLabel, startNode(r1).has_children as hasChildren, r1.label as relation,");
        query.append("collect( distinct id(endNode(r1)) ) as parents");

        return query.toString();
    }

    @Override
    String getJsTreeParentQuery(String lang, ViewMode viewMode) {
        return getJsTreeParentQuery(lang);
    }

    @Override
    String getJsTreeParentSiblingQuery(String lang) {
        StringBuilder query = new StringBuilder();

        query.append("MATCH path = (n:Property)-[r:SUBPROPERTYOF*]->(parent)<-[r2:SUBPROPERTYOF]-(n1:Property)\n");
        query.append("USING INDEX n:Property(iri)\n");
        query.append("WHERE n.ontology_name = {0} AND n.iri = {1}\n");
        query.append("UNWIND rels(path) as r1\n");
        query.append("WITH r1\n");
        query.append("WHERE startNode(r1).is_obsolete=false\n");
        query.append("RETURN distinct id(startNode(r1)) as startId, startNode(r1).iri as startIri, ");
        query.append("startNode(r1).label as startLabel, startNode(r1).has_children as hasChildren, r1.label as relation,");
        query.append("collect( distinct id(endNode(r1)) ) as parents");

        return query.toString();
    }

    @Override
    String getJsTreeParentSiblingQuery(String lang ,ViewMode viewMode) {
        return getJsTreeParentSiblingQuery(lang);
    }

    @Override
    String getJsTreeChildrenQuery(String lang) {
        StringBuilder query = new StringBuilder();

        query.append("MATCH path = (child)-[r:SUBPROPERTYOF]->(n:Property)\n");
        query.append("USING INDEX n:Property(iri)\n");
        query.append("WHERE n.ontology_name = {0} AND n.iri = {1}\n");
        query.append("UNWIND rels(path) as r1\n");
        query.append("RETURN distinct id(startNode(r1)) as startId, startNode(r1).iri as startIri,");
        query.append("startNode(r1).label as startLabel, startNode(r1).has_children as hasChildren, r1.label as relation");

        return query.toString();
    }

    @Override
    String getRootName() {
        return "TopObjectProperty";
    }

    @Override
    String getJsTreeRoots(String lang, ViewMode viewMode) {
        throw new UnsupportedOperationException("Implementation not necessary.");
    }
}
