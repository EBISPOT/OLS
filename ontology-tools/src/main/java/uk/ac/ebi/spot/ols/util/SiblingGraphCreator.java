package uk.ac.ebi.spot.ols.util;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import org.semanticweb.owlapi.model.IRI;
import uk.ac.ebi.spot.ols.exception.OntologyLoadingException;
import uk.ac.ebi.spot.ols.loader.OntologyLoader;

import java.io.*;
import java.util.Collection;

/**
 * Created by catherineleroy on 16/03/2015.<br>
 *<br>
 * This class contains the utility method to create for term a bbob json graph describing the tree with all the siblings,
 * parents, parent's sibling of this term.<br>
 * To find out more about bbop graph :<br>
 * https://github.com/berkeleybop/bbop-js/wiki/Graph<br>
 *<br>
 * This is an example of a bbpop graph : <br>
 * <br>
 * {<br>
 * "nodes": [<br>
 * {<br>
 * "id": "GO:0043474",<br>
 * "lbl": "pigment metabolic process involved in pigmentation"<br>
 * },<br>
 *  {<br>
 * "id": "GO:0043475",<br>
 * "lbl": "pigment metabolic process involved in pigment accumulation"<br>
 *  }<br>
 * ],<br>
 * "edges": [<br>
 *  {<br>
 * "sub": "GO:0043475",<br>
 * "obj": "GO:0043474",<br>
 * "pred": "is_a"<br>
 * }<br>
 * ]<br>
 * }<br>
 *<br>
 */
public class SiblingGraphCreator  {

    /**
     * Build the bbop graph containing all the parents, sibling, sibling parents, describing how those relate and
     * flush it to the given OutputStream.<br>
     *<br>
     * @param loader - an ontology loader for the ontology your interested in<br>
     * @param classTerm - the IRI of the term for which you want to build the sibbling graph<br>
     * @param out - an OutputStream in which the graph will be saved<br>
     *<br>
     * @throws IOException<br>
     */
    public void buildBpopGraph(OntologyLoader loader, IRI classTerm, OutputStream out) throws IOException {


        JsonGenerator jsonGenerator = new JsonFactory().createGenerator(out);

        //Start the json object
        jsonGenerator.writeStartObject();

        // Building the "nodes" section of the json file in which we describe all the nodes that will be represented
        // in the graph.
        jsonGenerator.writeArrayFieldStart("nodes");
        jsonGenerator = addParentsNodes(classTerm, loader, jsonGenerator);
        jsonGenerator.writeEndArray();

        // Building the "edges" section of the json file in which we describe the relationship between the nodes previously
        // described in the nodes section
        jsonGenerator.writeArrayFieldStart("edges");
        jsonGenerator = addParentsEdges(classTerm, loader, jsonGenerator);
        jsonGenerator.writeEndArray();

        //Close the json object
        jsonGenerator.writeEndObject();

        //Flush and close so that the json is written in the given OutputStream object
        jsonGenerator.flush();
        jsonGenerator.close();

        return;
    }


    public JsonGenerator addChildrenNodes(IRI term, OntologyLoader loader, JsonGenerator jsonGenerator) throws IOException {

        Collection<IRI> directChildren = loader.getDirectChildTerms(term);


        for (IRI directChild : directChildren) {

            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("IRI", directChild.toString());
            jsonGenerator.writeEndObject();

            jsonGenerator = addChildrenNodes(directChild, loader, jsonGenerator);
        }


        return jsonGenerator;

    }


    public JsonGenerator addChildrenEdges(IRI term, OntologyLoader loader, JsonGenerator jsonGenerator) throws IOException {

        Collection<IRI> directChildren = loader.getDirectChildTerms(term);


        for (IRI directChild : directChildren) {


            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("pred", "is_a");
            jsonGenerator.writeStringField("obj", directChild.toString());
            jsonGenerator.writeStringField("sub", term.toString());
            jsonGenerator.writeEndObject();

            jsonGenerator = addChildrenEdges(directChild, loader, jsonGenerator);
        }


        return jsonGenerator;

    }

    /**
     * Gets all the parents, siblings of the given term and using the jsonGenerator it adds a description
     * of the nodes to the json as follow :<br>
     * ex : {"IRI":"http://www.ebi.ac.uk/efo/EFO_0000510"},<br>
     *      {"IRI":"http://www.ebi.ac.uk/efo/EFO_0000506"},<br>
     *      {"IRI":"http://www.ebi.ac.uk/efo/EFO_0004020"},<br>
     *      {"IRI":"http://www.ebi.ac.uk/efo/EFO_0004021"},
     *      {"IRI":"http://www.ebi.ac.uk/efo/EFO_0004293"},<br>
     *      {"IRI":"http://www.ebi.ac.uk/efo/EFO_0004030"}<br>
     *      ... etc<br>
     * This method is recursive so it will call itself on any found parent.<br>
     * @param term - the IRI of the term for which you want to get all the parents and sibling nodes.<br>
     * @param loader - an ontology loader for the ontology your interested in<br>
     * @param jsonGenerator - a jsonGenerator object<br>
     * @return<br>
     * @throws IOException<br>
     */
    public JsonGenerator addParentsNodes(IRI term, OntologyLoader loader, JsonGenerator jsonGenerator) throws IOException {

        Collection<IRI> directParents = loader.getDirectParentTerms(term);

        //For each parent
        for (IRI directParent : directParents) {
            //add the node using the jsonGenerator object.
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("IRI", directParent.toString());
            jsonGenerator.writeEndObject();

            //get the sibbling of the given term and add them using the jsonGenerator object.
            jsonGenerator = addSiblingNodes(directParent, term, loader, jsonGenerator);

            //run recursively this method on the directParent
            jsonGenerator = addParentsNodes(directParent, loader, jsonGenerator);
        }

        return jsonGenerator;

    }

    /**
     * Gets all the parents,siblings of the given term and using the jsonGenerator it adds a description<br>
     * of the nodes as follow to the json indicating the relationship between the nodes :<br>
     * ex : {<br>
     *          "pred":"is_a",<br>
     *          "obj":"http://www.ebi.ac.uk/efo/EFO_0004019",<br>
     *          "sub":"http://www.ebi.ac.uk/efo/EFO_0000510"<br>
     *      },<br>
     *      {<br>
     *          "pred":"is_a",<br>
     *          "obj":"http://www.ebi.ac.uk/efo/EFO_0000506",<br>
     *          "sub":"http://www.ebi.ac.uk/efo/EFO_0000510"<br>
     *      },<br>
     *      {<br>
     *          "pred":"is_a",<br>
     *          "obj":"http://www.ebi.ac.uk/efo/EFO_0004020",<br>
     *          "sub":"http://www.ebi.ac.uk/efo/EFO_0000510"<br>
     *      }<br>
     *      ... etc<br>
     * This method is recursive so it will call itself on any found parent.<br>
     * @param term - the IRI of the term for which you want to get all the parents and sibling nodes.<br>
     * @param loader - an ontology loader for the ontology your interested in<br>
     * @param jsonGenerator - a jsonGenerator object<br>
     * @return the jsonGenerator object
     * @throws IOException
     */
    public JsonGenerator addParentsEdges(IRI term, OntologyLoader loader, JsonGenerator jsonGenerator) throws IOException {

        Collection<IRI> directParents = loader.getDirectParentTerms(term);

        for (IRI directParent : directParents) {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("pred", "is_a");
            jsonGenerator.writeStringField("obj", term.toString());
            jsonGenerator.writeStringField("sub", directParent.toString());
            jsonGenerator.writeEndObject();

            jsonGenerator = addSiblingEdges(directParent, term, loader, jsonGenerator);

            jsonGenerator = addParentsEdges(directParent, loader, jsonGenerator);
        }


        return jsonGenerator;

    }

    public JsonGenerator addSiblingNodes(IRI parent, IRI child, OntologyLoader loader, JsonGenerator jsonGenerator) throws IOException {

        Collection<IRI> directChildren = loader.getDirectChildTerms(parent);


        for (IRI directChild : directChildren) {
            if (!directChild.equals(child)) {
                jsonGenerator.writeStartObject();
                jsonGenerator.writeStringField("IRI", directChild.toString());
                jsonGenerator.writeEndObject();
            }
        }


        return jsonGenerator;
    }

    public JsonGenerator addSiblingEdges(IRI parent, IRI child, OntologyLoader loader, JsonGenerator jsonGenerator) throws IOException {

        Collection<IRI> directChildren = loader.getDirectChildTerms(parent);

        for (IRI directChild : directChildren) {
            if (!directChild.equals(child)) {
                jsonGenerator.writeStartObject();
                jsonGenerator.writeStringField("pred", "is_a");
                jsonGenerator.writeStringField("obj", directChild.toString());
                jsonGenerator.writeStringField("sub", parent.toString());
                jsonGenerator.writeEndObject();
            }
        }


        return jsonGenerator;
    }

}