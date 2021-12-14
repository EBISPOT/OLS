package uk.ac.ebi.spot.ols.neo4j.service;

public class JsTreeQueries {
	

	public static String getJsTreeClassChildren(String lang) {
		return "MATCH path = (child)-[r:SUBCLASSOF|RelatedTree]->(n:Class)\n"+
            "USING INDEX n:Class(iri)\n" +
            "WHERE n.ontology_name = {0} AND n.iri = {1}\n"+
            "UNWIND rels(path) as r1\n" +
            "RETURN distinct id(startNode(r1)) as startId, startNode(r1).iri as startIri, " + 
              "startNode(r1).label as startLabel, startNode(r1).`localizedLabels-" + lang + "`[0] as startLabelLocalized, startNode(r1).has_children as hasChildren, r1.label as relation";
	}

    public static String parentTreeQuery(String lang) {
	    return "MATCH path = (n:Class)-[r:SUBCLASSOF|RelatedTree*]->(parent)\n"+
            "USING INDEX n:Class(iri)\n" +
            "WHERE n.ontology_name = {0} AND n.iri = {1}\n"+
            "UNWIND rels(path) as r1\n" +
            "RETURN distinct id(startNode(r1)) as startId, startNode(r1).iri as startIri," + 
              "startNode(r1).label as startLabel, startNode(r1).`localizedLabels-" + lang + "`[0] as startLabelLocalized,  startNode(r1).has_children as hasChildren, " + 
              "r1.label as relation, collect( distinct id(endNode(r1)) ) as parents";
    }

    public static String parentSiblingTreeQuery(String lang) {
	    return "MATCH path = (n:Class)-[r:SUBCLASSOF|RelatedTree*]" + 
            "->(parent)<-[r2:SUBCLASSOF|RelatedTree]-(n1:Class)\n"+
            "USING INDEX n:Class(iri)\n" +
            "WHERE n.ontology_name = {0} AND n.iri = {1}\n"+
            "UNWIND rels(path) as r1\n" +
            "WITH r1\n" +
            "WHERE startNode(r1).is_obsolete=false\n"+
            "RETURN distinct id(startNode(r1)) as startId, startNode(r1).iri as startIri, " + 
              "startNode(r1).label as startLabel, startNode(r1).`localizedLabels-" + lang + "`[0] as startLabelLocalized,  startNode(r1).has_children as hasChildren, " + 
              "r1.label as relation, collect( distinct id(endNode(r1)) ) as parents";
    }


    // Property queries

    public static String getJsTreePropertyChildren(String lang) {
	    return "MATCH path = (child)-[r:SUBPROPERTYOF]->(n:Property)\n"+
            "USING INDEX n:Property(iri)\n" +
            "WHERE n.ontology_name = {0} AND n.iri = {1}\n"+
            "UNWIND rels(path) as r1\n" +
            "RETURN distinct id(startNode(r1)) as startId, startNode(r1).iri as startIri, startNode(r1).label as startLabel, startNode(r1).`localizedLabels-" + lang + "`[0] as startLabelLocalized,  startNode(r1).has_children as hasChildren, r1.label as relation";
    }

    public static String propertyParentTreeQuery(String lang) {
	    return "MATCH path = (n:Property)-[r:SUBPROPERTYOF*]->(parent)\n"+
            "USING INDEX n:Property(iri)\n" +
            "WHERE n.ontology_name = {0} AND n.iri = {1}\n"+
            "UNWIND rels(path) as r1\n" +
            "RETURN distinct id(startNode(r1)) as startId, startNode(r1).iri as startIri, startNode(r1).label as startLabel, startNode(r1).`localizedLabels-" + lang + "`[0] as startLabelLocalized,  startNode(r1).has_children as hasChildren, r1.label as relation, collect( distinct id(endNode(r1)) ) as parents";
    }

    public static String propertyParentSiblingTreeQuery(String lang) {
	    return "MATCH path = (n:Property)-[r:SUBPROPERTYOF*]->(parent)<-[r2:SUBPROPERTYOF]-(n1:Property)\n"+
            "USING INDEX n:Property(iri)\n" +
            "WHERE n.ontology_name = {0} AND n.iri = {1}\n"+
            "UNWIND rels(path) as r1\n" +
            "WITH r1\n" +
            "WHERE startNode(r1).is_obsolete=false\n"+
            "RETURN distinct id(startNode(r1)) as startId, startNode(r1).iri as startIri, startNode(r1).label as startLabel, startNode(r1).`localizedLabels-" + lang + "`[0] as startLabelLocalized,  startNode(r1).has_children as hasChildren, r1.label as relation, collect( distinct id(endNode(r1)) ) as parents";
    }

    // individual tree query
    public static String individualParentTreeQuery(String lang) {
	    return "MATCH path = (n:Individual)-[r:INSTANCEOF|SUBCLASSOF*]->(parent)\n"+
            "USING INDEX n:Individual(iri)\n" +
            "WHERE n.ontology_name = {0} AND n.iri = {1}\n"+
            "UNWIND rels(path) as r1\n" +
            "RETURN distinct id(startNode(r1)) as startId, startNode(r1).iri as startIri, startNode(r1).label as startLabel, startNode(r1).`localizedLabels-" + lang + "`[0] as startLabelLocalized,  startNode(r1).has_children as hasChildren, r1.label as relation,collect( distinct id(endNode(r1)) ) as parents";
    }

    public static String individualParentSiblingTreeQuery(String lang) {
	    return "MATCH path = (n:Individual)-[r:INSTANCEOF|SUBCLASSOF*]->(parent)<-[r2:SUBCLASSOF]-(n1:Individual)\n"+
            "USING INDEX n:Individual(iri)\n" +
            "WHERE n.ontology_name = {0} AND n.iri = {1}\n"+
            "UNWIND rels(path) as r1\n" +
            "WITH r1\n" +
            "WHERE startNode(r1).is_obsolete=false\n"+
            "RETURN distinct id(startNode(r1)) as startId, startNode(r1).iri as startIri, startNode(r1).label as startLabel, startNode(r1).`localizedLabels-" + lang + "`[0] as startLabelLocalized, startNode(r1).has_children as hasChildren, r1.label as relation, collect( distinct id(endNode(r1)) ) as parents";
    }



}