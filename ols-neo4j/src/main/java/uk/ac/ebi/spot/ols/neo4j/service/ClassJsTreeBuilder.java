package uk.ac.ebi.spot.ols.neo4j.service;

import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class ClassJsTreeBuilder extends AbstractJsTreeBuilder {

    private static Logger logger = LoggerFactory.getLogger(ClassJsTreeBuilder.class);

    @Override
    String getJsTreeParentQuery() {

        StringBuilder query = new StringBuilder();

        query.append("MATCH path = (n:Class)-[r:SUBCLASSOF|RelatedTree*]->(parent)\n");
        query.append("USING INDEX n:Class(iri)\n");
        query.append("WHERE n.ontology_name = {0} AND n.iri = {1}\n");
        query.append("UNWIND rels(path) as r1\n");
        query.append("RETURN distinct id(startNode(r1)) as startId, startNode(r1).iri as startIri,");
        query.append("startNode(r1).label as startLabel, startNode(r1).has_children as hasChildren, ");
        query.append("r1.label as relation, collect( distinct id(endNode(r1)) ) as parents");

        return query.toString();
    }

    private static String getNodeQueryString(String alias, ViewMode viewMode) {
        switch (viewMode) {
            case PREFERRED_ROOTS: {
                StringBuilder query = new StringBuilder(alias);
                query.append(":");
                query.append(viewMode.getNeo4jLabel());
                return query.toString();
            }
            default: {
                logger.warn("Unexpected view mode detected. Defaulting to preferred roots.");
                StringBuilder query = new StringBuilder(alias);
                query.append(":");
                query.append(ViewMode.PREFERRED_ROOTS.getNeo4jLabel());
                return query.toString();
            }
        }
    }

//    @Override
//    String getJsTreeParentQuery(ViewMode viewMode) {
//
//        switch (viewMode) {
//            case ALL:
//                return getJsTreeParentQuery();
//            default: {
//                StringBuilder query = new StringBuilder();
//
//                query.append("MATCH path = (n:Class)-[r:SUBCLASSOF|RelatedTree*]->(");
//                query.append(getNodeQueryString("parent", viewMode));
//                query.append(")\n");
//                query.append("USING INDEX n:Class(iri)\n");
//                query.append("WHERE n.ontology_name = {0} AND n.iri = {1}\n");
//                query.append("UNWIND rels(path) as r1\n");
//                query.append("RETURN distinct id(startNode(r1)) as startId, startNode(r1).iri as startIri,\n");
//                query.append("startNode(r1).label as startLabel, startNode(r1).has_children as hasChildren,\n");
//                query.append("r1.label as relation, collect( distinct id(endNode(r1)) ) as parents\n");
//                query.append("UNION\n");
//                query.append("MATCH path = (n:Class)-[r:SUBCLASSOF|RelatedTree*]->(");
//                query.append(getNodeQueryString("parent", viewMode));
//                query.append(")\n");
//                query.append("USING INDEX n:Class(iri)\n");
//                query.append("WHERE n.ontology_name = {0} AND n.iri = {1}\n");
//                query.append("WITH COLLECT(path) AS paths, MAX(length(path)) AS maxLength \n");
//                query.append("WITH FILTER(p IN paths WHERE length(p)= maxLength) AS longestPaths\n");
//                query.append("WITH EXTRACT(p IN longestPaths | LAST(NODES(p))) AS last\n");
//                query.append("WITH last[0] AS lastnode\n");
//                query.append("RETURN id(lastnode) as startId, lastnode.iri as startIri,\n");
//                query.append("lastnode.label as startLabel, lastnode.has_children as hasChildren,\n");
//                query.append("'is a' as relation, ['PreferredRoot'] as parents\n");
//
//                return query.toString();
//            }
//        }
//    }

    @Override
    String getJsTreeParentQuery(ViewMode viewMode) {

        switch (viewMode) {
            case ALL:
                return getJsTreeParentQuery();
            default: {
                StringBuilder query = new StringBuilder();

                query.append("MATCH path = (n:Class)-[r:SUBCLASSOF|RelatedTree*]->(");
                query.append(getNodeQueryString("parent", viewMode));
                query.append(")\n");
                query.append("USING INDEX n:Class(iri)\n");
                query.append("WHERE n.ontology_name = {0} AND n.iri = {1}\n");
                query.append("UNWIND rels(path) as r1\n");
                query.append("RETURN distinct id(startNode(r1)) as startId, startNode(r1).iri as startIri,\n");
                query.append("startNode(r1).label as startLabel, startNode(r1).has_children as hasChildren,\n");
                query.append("r1.label as relation, collect( distinct id(endNode(r1)) ) as parents\n");
                query.append("UNION\n");
                query.append("MATCH path = (n:Class)-[r:SUBCLASSOF|RelatedTree*]->(");
                query.append(getNodeQueryString("parent", viewMode));
                query.append(")\n");
                query.append("USING INDEX n:Class(iri)\n");
                query.append("WHERE n.ontology_name = {0} AND n.iri = {1}\n");

                query.append("WITH COLLECT(path) AS paths, MAX(length(path)) AS maxLength\n");
                query.append("WITH FILTER(p IN paths WHERE length(p)= maxLength) AS longestPaths\n");
                query.append("WITH EXTRACT(p IN longestPaths | LAST(NODES(p))) AS last\n");
                query.append("WITH last[0] AS lastnode\n");
                query.append("with id(lastnode) as startId, lastnode.iri as startIri,\n");
                query.append("lastnode.label as startLabel, lastnode.has_children as hasChildren\n");

                query.append("MATCH (");
                query.append(getNodeQueryString("n1", viewMode));
                query.append(")\n");
                query.append("WHERE n1.ontology_name = {0} AND n1.iri = {1}\n");

                query.append("RETURN\n");
                query.append("CASE WHEN startId is null THEN id(n1)\n");
                query.append("ELSE startId END as startId,\n");
                query.append("CASE WHEN startIri is null THEN n1.iri\n");
                query.append("ELSE startIri END as startIri,\n");
                query.append("CASE WHEN startLabel is null THEN n1.label\n");
                query.append("ELSE startLabel END as startLabel,\n");
                query.append("CASE WHEN hasChildren is null THEN n1.has_children\n");
                query.append("ELSE hasChildren END as hasChildren,\n");
                query.append("'is a' as relation, ['PreferredRoot'] as parents\n");

                return query.toString();
            }
        }
    }

    @Override
    String getJsTreeParentSiblingQuery() {
        StringBuilder query = new StringBuilder();

        query.append("MATCH path = (n:Class)-[r:SUBCLASSOF|RelatedTree*]");
        query.append("->(parent)<-[r2:SUBCLASSOF|RelatedTree]-(n1:Class)\n");
        query.append("USING INDEX n:Class(iri)\n");
        query.append("WHERE n.ontology_name = {0} AND n.iri = {1}\n");
        query.append("UNWIND rels(path) as r1\n");
        query.append("WITH r1\n");
        query.append("WHERE startNode(r1).is_obsolete=false\n");
        query.append("RETURN distinct id(startNode(r1)) as startId, startNode(r1).iri as startIri, ");
        query.append("startNode(r1).label as startLabel, startNode(r1).has_children as hasChildren, ");
        query.append("r1.label as relation, collect( distinct id(endNode(r1)) ) as parents");

        return query.toString();
    }

//    @Override
//    String getJsTreeParentSiblingQuery(ViewMode viewMode) {
//        switch (viewMode) {
//            case ALL:
//                return getJsTreeParentSiblingQuery();
//            default: {
//                StringBuilder query = new StringBuilder();
//
//                query.append("MATCH path = (n:Class)-[r:SUBCLASSOF|RelatedTree*]->(");
//                query.append(getNodeQueryString("parent", viewMode));
//                query.append(")<-[r2:SUBCLASSOF|RelatedTree]-(n1:Class)\n");
//                query.append("USING INDEX n:Class(iri)\n");
//                query.append("WHERE n.ontology_name = {0} AND n.iri = {1}\n");
//                query.append("UNWIND rels(path) as r1\n");
//                query.append("WITH r1\n");
//                query.append("WHERE startNode(r1).is_obsolete=false\n");
//                query.append("RETURN distinct id(startNode(r1)) as startId, startNode(r1).iri as startIri, ");
//                query.append("startNode(r1).label as startLabel, startNode(r1).has_children as hasChildren, ");
//                query.append("r1.label as relation, collect( distinct id(endNode(r1)) ) as parents");
//
//                return query.toString();
//            }
//        }
//    }


    @Override
    String getJsTreeParentSiblingQuery(ViewMode viewMode) {
        switch (viewMode) {
            case ALL:
                return getJsTreeParentSiblingQuery();
            default: {
                StringBuilder query = new StringBuilder();

                query.append("MATCH path1 = (");
                query.append(getNodeQueryString("parent", viewMode));
                query.append(")<-[r2:SUBCLASSOF|RelatedTree*]-(n2:Class)\n");
                query.append("UNWIND rels(path1) as pathRels1\n");
                query.append("RETURN distinct id(startNode(pathRels1)) as startId, startNode(pathRels1).iri as startIri,");
                query.append("startNode(pathRels1).label as startLabel, startNode(pathRels1).has_children as hasChildren, ");
                query.append("pathRels1.label as relation, collect( distinct id(endNode(pathRels1)) ) as parents");

                return query.toString();
            }
        }
    }
    @Override
    String getJsTreeChildrenQuery() {
        StringBuilder query = new StringBuilder();

        query.append("MATCH path = (child)-[r:SUBCLASSOF|RelatedTree]->(n:Class)\n");
        query.append("USING INDEX n:Class(iri)\n");
        query.append("WHERE n.ontology_name = {0} AND n.iri = {1}\n");
        query.append("UNWIND rels(path) as r1\n");
        query.append("RETURN distinct id(startNode(r1)) as startId, startNode(r1).iri as startIri, ");
        query.append("startNode(r1).label as startLabel, startNode(r1).has_children as hasChildren, r1.label as relation");

        return query.toString();
    }

    @Override
    String getRootName() {
        return "Thing";
    }
}
