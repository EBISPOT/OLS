package uk.ac.ebi.spot.ols.neo4j.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

/**
 * @author Simon Jupp
 * @date 18/08/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 *
 * Build a JSTree (https://www.jstree.com) JSON serialisation of the tree
 *
 * Viewing graphs (or DAGs) as trees where terms may have multiple parents means we need to
 * use the alternate json format for jstree where you list each node and specify the parent node.
 * JsTree can't handle terms with mutliple parents, so we need to create a unique id for the multi-parent nodes.
 */
@Component
public class JsTreeBuilder {

    @Autowired(required = false)
    GraphDatabaseService graphDatabaseService;

    private String rootName = "Thing";

    public JsTreeBuilder() {

    }

    public void setRootName(String rootName) {
        this.rootName = rootName;
    }

    public Object getIndividualJsTree(String lang, String ontologyName, String iri) {
        Map<String, Object> paramt = new HashMap<>();
        paramt.put("0", ontologyName);
        paramt.put("1", iri);
        Result res = graphDatabaseService.execute(JsTreeQueries.individualParentTreeQuery(lang), paramt);

        setRootName("Thing");
        return getJsTreeObject(lang, ontologyName, iri, res);
    }

    public Object getPropertyJsTree(String lang, String ontologyName, String iri, boolean siblings) {
        Map<String, Object> paramt = new HashMap<>();
        paramt.put("0", ontologyName);
        paramt.put("1", iri);
        String query = siblings ? JsTreeQueries.propertyParentSiblingTreeQuery(lang) : JsTreeQueries.propertyParentTreeQuery(lang);
        Result res = graphDatabaseService.execute(query, paramt);

        setRootName("TopObjectProperty");
        return getJsTreeObject(lang, ontologyName, iri, res);
    }


    public Object getClassJsTree(String lang, String ontologyName, String iri, boolean siblings) {
        Map<String, Object> paramt = new HashMap<>();
        paramt.put("0", ontologyName);
        paramt.put("1", iri);
        String query = siblings ? JsTreeQueries.parentSiblingTreeQuery(lang) : JsTreeQueries.parentTreeQuery(lang);
        Result res = graphDatabaseService.execute(query, paramt);

        setRootName("Thing");
        return getJsTreeObject(lang, ontologyName, iri, res);
    }

    public Object getJsTreeClassChildren(String lang, String ontologyName, String iri, String parentNodeId) {
        return getJsTreeChildren(lang, "term", ontologyName, iri, parentNodeId);
    }

    public Object getJsTreePropertyChildren(String lang, String ontologyName, String iri, String parentNodeId) {
        return getJsTreeChildren(lang, "property", ontologyName, iri, parentNodeId);
    }

    private Object getJsTreeChildren(String lang, String type,String ontologyName, String iri, String parentNodeId) {
        Map<String, Object> paramt = new HashMap<>();
        paramt.put("0", ontologyName);
        paramt.put("1", iri);
        String query = JsTreeQueries.getJsTreeClassChildren(lang);
        if (type.equals("property")) {
            query = JsTreeQueries.getJsTreePropertyChildren(lang);
        }
        Result res = graphDatabaseService.execute(query, paramt);

        List<JsTreeObject> treeObjects = new ArrayList<>();

        int counter = 1;
        while (res.hasNext()) {
            Map<String, Object> row = res.next();
            String nodeId = row.get("startId").toString();
            String startIri = row.get("startIri").toString();
            String startLabel = row.get("startLabel").toString();
            String startLabelLocalized = row.get("startLabelLocalized") != null ? row.get("startLabelLocalized").toString() : null;
            String relation = row.get("relation").toString().replaceAll(" ", "_");
            boolean hasChildren = Boolean.parseBoolean(row.get("hasChildren").toString());

            String startNode = nodeId + "_child_" + counter;


            JsTreeObject jsTreeObject = new JsTreeObject(
                    startNode,
                    startIri,
                    ontologyName,
                    startLabelLocalized != null ? startLabelLocalized : startLabel,
                    relation,
                    hasChildren,
                    parentNodeId
            );

            if (jsTreeObject.isHasChildren()) {
                jsTreeObject.setChildren(true);
                jsTreeObject.getState().put("opened", false);
            }
            treeObjects.add(jsTreeObject);

            counter++;
        }

        return treeObjects;
    }

    private Object getJsTreeObject(String lang, String ontologyName, String iri, Result res) {

        // create a map of the start node to the rows in the results
        Map<String, List<Map<String, Object>>> resultsMap = new HashMap<>();
        while (res.hasNext()) {
            Map<String, Object> r = res.next();
            String nodeId = r.get("startId").toString();
            if (!resultsMap.containsKey(nodeId)) {
                resultsMap.put(nodeId, new ArrayList<>());
            }
            resultsMap.get(nodeId).add(r);
        }

        Map<String, Collection<JsTreeObject>> jsTreeObjectMap = new HashMap<>();
        Collection<String> parentIds = new HashSet<>();

        for (String id : resultsMap.keySet()) {
            generateJsTreeObject(lang, id, ontologyName, jsTreeObjectMap, resultsMap, parentIds);
        }

        // find all the nodes that are parents (i.e. should be expanded)
        // any nodes that aren't parents are leaves in the tree, we need to check these have child nodes to see if they have further children

        Collection<JsTreeObject> jsTreeObjects = new HashSet<>();
        for (String key : jsTreeObjectMap.keySet()) {
            for (JsTreeObject jsTreeObject : jsTreeObjectMap.get(key)) {

                if (jsTreeObject.getIri().equals(iri)) {
                    jsTreeObject.getState().put("selected", true);
                }

                if (!parentIds.contains(jsTreeObject.getId())) {
                    // this is a leaf node

                    if (jsTreeObject.isHasChildren()) {
                        jsTreeObject.setChildren(true);
                        jsTreeObject.getState().put("opened", false);
                    }
                }
            }

            jsTreeObjects.addAll(jsTreeObjectMap.get(key));
        }

        return jsTreeObjects;
    }

    /**
     *
     * This method walks up graph and splits parent nodes when a term has more than one.
     *
     * @param nodeId starting node
     * @param ontologyName the active ontology
     * @param jsTreeObjectMap store a map of already created jsTree Objects
     * @param resultsMap a map of the start nodes to the rows in the results table
     * @param parentIdCollector collect parent Ids that we've had to create
     */
    private void generateJsTreeObject(String lang, String nodeId, String ontologyName, Map<String, Collection<JsTreeObject>> jsTreeObjectMap, Map<String, List<Map<String, Object>>> resultsMap, Collection<String> parentIdCollector) {
        try {
            Collection<JsTreeObject> objectVersions = getJsObjectTree(lang, nodeId, ontologyName, resultsMap, jsTreeObjectMap, parentIdCollector);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Collection<JsTreeObject> getJsObjectTree(String lang, String nodeId, String ontologyName, Map<String, List<Map<String, Object>>> resultsMap, Map<String, Collection<JsTreeObject>> jsTreeObjectMap, Collection<String> parentIdCollector) throws IOException {

        // return the object if we have seen it before
        if (jsTreeObjectMap.containsKey(nodeId)) {
            return jsTreeObjectMap.get(nodeId);
        }

        // if no key, then we are at a root
        if (!resultsMap.containsKey(nodeId)) {
            return Collections.singleton(new JsTreeObject("#", "#", ontologyName, "", rootName, false, "#"));
        }

        int x = 1;

        for (Map<String, Object> row : resultsMap.get(nodeId) ) {
            List<Integer> parentIds = new ObjectMapper().readValue(row.get("parents").toString(), List.class);

            for (Integer pid : parentIds) {

                String parentId = pid.toString();

                for (JsTreeObject parentObject : getJsObjectTree(lang, parentId, ontologyName, resultsMap, jsTreeObjectMap, parentIdCollector)) {

                    String startIri = row.get("startIri").toString();
                    String startLabel = row.get("startLabel").toString();
		    String startLabelLocalized = row.get("startLabelLocalized") != null ? row.get("startLabelLocalized").toString() : null;
                    String relation = row.get("relation").toString().replaceAll(" ", "_");
                    boolean hasChildren = Boolean.parseBoolean(row.get("hasChildren").toString());

                    String startNode = nodeId + "_" + x;


                    JsTreeObject jsTreeObject = new JsTreeObject(
                            startNode,
                            startIri,
                            ontologyName,
                            startLabelLocalized != null ? startLabelLocalized : startLabel,
                            relation,
                            hasChildren,
                            parentObject.getId()
                    );

                    if (!jsTreeObjectMap.containsKey(nodeId)) {
                        jsTreeObjectMap.put(nodeId, new HashSet<>());
                    }
                    jsTreeObjectMap.get(nodeId).add(jsTreeObject);
                    parentIdCollector.add(parentObject.getId());
                    x++;
                }
            }
        }


        return jsTreeObjectMap.get(nodeId);
    }

}
