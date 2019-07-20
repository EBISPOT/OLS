package uk.ac.ebi.spot.ols.neo4j.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;


public abstract class AbstractJsTreeBuilder {

    @Autowired(required = false)
    GraphDatabaseService graphDatabaseService;

    private String rootName = "Thing";

    private static Logger logger = LoggerFactory.getLogger(AbstractJsTreeBuilder.class);

    public AbstractJsTreeBuilder() {

    }

    public void setRootName(String rootName) {
        this.rootName = rootName;
    }


    abstract String getJsTreeParentQuery();
    abstract String getJsTreeParentQuery(ViewMode viewMode);
    abstract String getJsTreeParentSiblingQuery();
    abstract String getJsTreeParentSiblingQuery(ViewMode viewMode);
    abstract String getJsTreeChildrenQuery();
    abstract String getRootName();


    public Object getJsTree(String ontologyName, String iri, boolean sibling) {
        Map<String, Object> paramt = new HashMap<>();
        paramt.put("0", ontologyName);
        paramt.put("1", iri);

        String query = (sibling) ? getJsTreeParentSiblingQuery() : getJsTreeParentQuery();
        Result res = graphDatabaseService.execute(query, paramt);

        setRootName(getRootName());
        return getJsTreeObject(ontologyName, iri, res);
    }

    public Object getJsTree(String ontologyName, String iri, boolean sibling, ViewMode viewMode) {
        Map<String, Object> paramt = new HashMap<>();
        paramt.put("0", ontologyName);
        paramt.put("1", iri);

        String query = (sibling) ? getJsTreeParentSiblingQuery(viewMode) : getJsTreeParentQuery(viewMode);

        logger.debug("ontologyName = " + ontologyName);
        logger.debug("iri = " + iri);
        logger.debug("query = " + query);

        Result res = graphDatabaseService.execute(query, paramt);

        setRootName(getRootName());
        return getJsTreeObject(ontologyName, iri, res);
    }

    public Object getJsTreeChildren(String ontologyName, String iri, String parentNodeId) {
        Map<String, Object> paramt = new HashMap<>();
        paramt.put("0", ontologyName);
        paramt.put("1", iri);
        String query = getJsTreeChildrenQuery();

        Result res = graphDatabaseService.execute(query, paramt);

        List<JsTreeObject> treeObjects = new ArrayList<>();

        int counter = 1;
        while (res.hasNext()) {
            Map<String, Object> row = res.next();
            String nodeId = row.get("startId").toString();
            String startIri = row.get("startIri").toString();
            String startLabel = row.get("startLabel").toString();
            String relation = row.get("relation").toString().replaceAll(" ", "_");
            boolean hasChildren = Boolean.parseBoolean(row.get("hasChildren").toString());

            String startNode = nodeId + "_child_" + counter;


            JsTreeObject jsTreeObject = new JsTreeObject(
                    startNode,
                    startIri,
                    ontologyName,
                    startLabel,
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

    private Object getJsTreeObject(String ontologyName, String iri, Result res) {

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
            generateJsTreeObject(id, ontologyName, jsTreeObjectMap, resultsMap, parentIds);
        }

        // find all the nodes that are parents (i.e. should be expanded)
        // any nodes that aren't parents are leaves in the tree, we need to check these have child nodes to see if they
        // have further children

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
    private void generateJsTreeObject(String nodeId, String ontologyName,
                                      Map<String, Collection<JsTreeObject>> jsTreeObjectMap,
                                      Map<String, List<Map<String, Object>>> resultsMap,
                                      Collection<String> parentIdCollector) {
        try {
            Collection<JsTreeObject> objectVersions = getJsObjectTree(nodeId, ontologyName, resultsMap, jsTreeObjectMap,
                    parentIdCollector);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Collection<JsTreeObject> getJsObjectTree(String nodeId, String ontologyName, Map<String, List<Map<String, Object>>> resultsMap, Map<String, Collection<JsTreeObject>> jsTreeObjectMap, Collection<String> parentIdCollector) throws IOException {

        // return the object if we have seen it before
        if (jsTreeObjectMap.containsKey(nodeId)) {
            return jsTreeObjectMap.get(nodeId);
        }

        // if no key, then we are at a root
        if (!resultsMap.containsKey(nodeId)) {
            return Collections.singleton(new JsTreeObject("#", "#", ontologyName, "",rootName, false, "#"));
        }

        int x = 1;

        for (Map<String, Object> row : resultsMap.get(nodeId) ) {
            List<Integer> parentIds = new ObjectMapper().readValue(row.get("parents").toString(), List.class);

            for (Integer pid : parentIds) {

                String parentId = pid.toString();

                for (JsTreeObject parentObject : getJsObjectTree(parentId, ontologyName, resultsMap, jsTreeObjectMap,
                        parentIdCollector)) {

                    String startIri = row.get("startIri").toString();
                    String startLabel = row.get("startLabel").toString();
                    String relation = row.get("relation").toString().replaceAll(" ", "_");
                    boolean hasChildren = Boolean.parseBoolean(row.get("hasChildren").toString());

                    String startNode = nodeId + "_" + x;

                    JsTreeObject jsTreeObject = new JsTreeObject(
                            startNode,
                            startIri,
                            ontologyName,
                            startLabel,
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
