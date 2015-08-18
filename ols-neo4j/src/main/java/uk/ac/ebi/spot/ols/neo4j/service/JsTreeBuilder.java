package uk.ac.ebi.spot.ols.neo4j.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.neo4j.graphdb.Result;

import java.io.IOException;
import java.util.*;

/**
 * @author Simon Jupp
 * @date 18/08/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
public class JsTreeBuilder {

    private String rootName = "Thing";
    public JsTreeBuilder() {

    }


    public JsTreeBuilder(String rootName) {
        this.rootName =rootName;
    }

    public Object getJsTreeObject(String ontologyName, String iri, Result res) {
        Map<String, Map<String, Object>> resultsMap = new HashMap<>();
        while (res.hasNext()) {
            Map<String, Object> r = res.next();
            String nodeId = r.get("startId").toString();
            resultsMap.put(nodeId, r);
        }

        Map<String, Collection<JsTreeObject>> jsTreeObjectMap = new HashMap<>();
        Collection<String> parentIds = new HashSet<>();

        for (String id : resultsMap.keySet()) {
            generateJsTreeObject(id, ontologyName, jsTreeObjectMap, resultsMap, parentIds);

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

    private void generateJsTreeObject(String nodeId, String ontologyName, Map<String, Collection<JsTreeObject>> jsTreeObjectMap, Map<String, Map<String, Object>> resultsMap, Collection<String> parentIdCollector) {
        try {
            Collection<JsTreeObject> objectVersions = getJsObjectTree(nodeId, ontologyName, resultsMap, jsTreeObjectMap, parentIdCollector);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Collection<JsTreeObject> getJsObjectTree(String nodeId, String ontologyName, Map<String, Map<String, Object>> resultsMap, Map<String, Collection<JsTreeObject>> jsTreeObjectMap, Collection<String> parentIdCollector) throws IOException {
        if (jsTreeObjectMap.containsKey(nodeId)) {
            return jsTreeObjectMap.get(nodeId);
        }

        // if no key, then we are at a root
        if (!resultsMap.containsKey(nodeId)) {
            return Collections.singleton(new JsTreeObject("#", "#", ontologyName, rootName, false, "#"));
        }

        Map<String, Object> row = resultsMap.get(nodeId);

        List<Integer> parentIds = new ObjectMapper().readValue(row.get("parents").toString(), List.class);

        int x = 1;
        for (Integer pid : parentIds) {

            String parentId = pid.toString();

            for (JsTreeObject parentObject : getJsObjectTree(parentId, ontologyName, resultsMap, jsTreeObjectMap, parentIdCollector)) {

                String startIri = row.get("startIri").toString();
                String startLabel = row.get("startLabel").toString();
                boolean hasChildren = Boolean.parseBoolean(row.get("hasChildren").toString());

                String startNode = nodeId + "_" + x;


                JsTreeObject jsTreeObject = new JsTreeObject(
                        startNode,
                        startIri,
                        ontologyName,
                        startLabel,
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
        return jsTreeObjectMap.get(nodeId);
    }

}
