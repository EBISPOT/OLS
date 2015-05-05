// Uses javascript from there : http://code.stephenmorley.org/javascript/collapsible-lists/#usage but modified.

jQuery(document).ready(function () {
    jQuery.noConflict();
    jQuery('.term-relational-tree').injectRelationalTree();


});

var element_to_inject;
var json_path;
var label;
var ontology_name;

//make it a jquery plugin that can be attached to any element.
(function ($) {


    //function getDataAndInject(){
    $.fn.injectRelationalTree = function () {

        element_to_inject = this;

        //Get the <term-relational-tree> tag label attribute
        //var label = $(".term-relational-tree").attr("data-label");
        var label = this.attr("data-label");
        //Get the <term-relational-tree> tag ontology-name attribute
        //var ontology_name= $(".term-relational-tree").attr("data-ontology-name");
        ontology_name = this.attr("data-ontology-name");

        //Get the <term-relational-tree> tag json-path attribute
        // The json_path the url pointing to the solr server, or the server which given a label parameter returns the expected json.
        // The label part of the url should contain the following string "LABEL_TO_REPLACE" so that the widget can easily replace
        // this with the given label.

        // If the json_path was not given use the ebi default url.
        //var json_path = $(".term-relational-tree").attr("data-json-path")
        json_path = this.attr("data-json-path");
        if (typeof json_path == 'undefined' || json_path.indexOf("LABEL_TO_REPLACE") == -1) {
            json_path = "http://localhost:8983/solr/ontology/select?q=label%3ALABEL_TO_REPLACE&rows=1&wt=json&indent=true";
        }
        //If the json_path does not contain a LABEL_TO_REPLACE string then send an error message.
        else if (json_path.indexOf("LABEL_TO_REPLACE") == -1) {
            html_to_inject = '<div class="msgError">BAD USE OF THE &#60;term-relational-tree&#62; tag, you should provide a json_path parameter which is a url' +
            'pointing to your solr server with a LABEL_TO_REPLACE string where the widget need to replace with the given label<br>' +
            'ex : &#60;term-relational-tree  label="lung" ontology-name="EFO" json-path="http://localhost:8983/solr/ontology/select?q=label%3ALABEL_TO_REPLACE+AND+ontology_name%3AONTOLOGY_NAME_TO_REPLACE&rows=1&wt=json&indent=true"&#62;&#60;/term-relational-tree&#62;</div>';

            //$('.term-relational-tree').append(html_to_inject);
            this.append(html_to_inject);
        }
        //If the json_path does not contain a LABEL_TO_REPLACE string then send an error message.
        else if (json_path.indexOf("ONTOLOGY_NAME_TO_REPLACE") == -1) {
            html_to_inject = '<div class="msgError">BAD USE OF THE &#60;term-relational-tree&#62; tag, you should provide a json_path parameter which is a url' +
            'pointing to your solr server with a ONTOLOGY_NAME_TO_REPLACE string where the widget need to replace with the given ontology-name<br>' +
            'ex : &#60;term-relational-tree  label="lung" ontology-name="EFO" json-path="http://localhost:8983/solr/ontology/select?q=label%3ALABEL_TO_REPLACE+AND+ontology_name%3AONTOLOGY_NAME_TO_REPLACE&rows=1&wt=json&indent=true"&#62;&#60;/term-relational-tree&#62;</div>';

            //$('.term-relational-tree').append(html_to_inject);
            this.append(html_to_inject);
        }
        //If the user didn't provide the label parameter then send an error message.
        else if (typeof label == 'undefined') {
            html_to_inject = '<div class="msgError">BAD USE OF THE &#60;term-relational-tree&#62; tag, you should at least provide a label parameter<br>' +
            'ex : &#60;term-relational-tree label="lung"&#62;&#60;/term-relational-tree&#62;</div>';
            //$('.term-relational-tree').append(html_to_inject);
            this.append(html_to_inject);
        }
        else if (typeof ontology_name == 'undefined') {
            html_to_inject = '<div class="msgError">BAD USE OF THE &#60;term-relational-tree&#62; tag, you should at least provide an ontology-name parameter<br>' +
            'ex : &#60;term-relational-tree label="lung" ontology-name="EFO"&#62;&#60;/term-relational-tree&#62;</div>';
            //$('.term-relational-tree').append(html_to_inject);
            this.append(html_to_inject);
        }


        //Finally if the user has provided everything then parse the json, build the term overview and inject it in the <term-relational-tree> tag.
        else {

            var full_path = json_path.replace("LABEL_TO_REPLACE", label);
            full_path = full_path.replace("ONTOLOGY_NAME_TO_REPLACE", ontology_name);
            //because the url contains the string json.wrf=on_data, then javascript knows that when
            // $.getJSON(full_path);
            // is called it should build the json object from the given full_path parameter and send it to the on_data function.
            full_path = full_path + "&wt=json&json.wrf=on_data&callback=?"
            console.log("FULL_PAHT = " + full_path);
            $.getJSON(full_path);//,function(json){
        }
    }

}(jQuery));

function solr_has_children(json){
    var hasChildren = json.response.docs[0].has_children;
    if(hasChildren){
        var label = json.response.docs[0].label;
        var uri = json.response.docs[0].uri;
        var bbop_sibling_graph_json_string = json.response.docs[0].bbop_direct_children_json;
        console.log("label = " + label);
        jQuery('#' + label).addClass('solrCollapsibleListClosed');
        jQuery('#' + label).unbind('click').click(function(event) {
            console.log("BOnjour\n\n");
            console.log("jQuery('#' + label).attr(class) = " + jQuery('#' + label).attr("class") + "\n\n");

            event.stopPropagation();

            //jQuery('#' + label).attr("class", "solrCollapsibleListOpen");
            //jQuery('#' + label).addClass('solrCollapsibleListOpen').removeClass('solrCollapsibleListClosed');
            var elmtClass = jQuery(this).attr("class");
            console.log("elmtClass = " + elmtClass+ " index of : " +  elmtClass.indexOf("solrCollapsibleListClosed") + "_\n");
            if (elmtClass.indexOf("solrCollapsibleListClosed") == 0){
                jQuery(this).removeClass("solrCollapsibleListClosed");
                jQuery(this).addClass(' solrCollapsibleListOpen');
                console.log("id = " + jQuery(this).attr('id'));
                var toInject = buildAndInjectTree(json, uri, true);
                toInject = toInject.substring(toInject.indexOf("</font>") + 7, toInject.lastIndexOf("</li>"));
                jQuery(this).append(toInject);
            }else if (elmtClass.indexOf("solrCollapsibleListOpen") == 0){
                jQuery(this).find( "ul").empty();
                jQuery(this).removeClass("solrCollapsibleListOpen");
                jQuery(this).addClass("solrCollapsibleListClosed");
            }

        });
        console.log("\n\n\n");


    }
}

function on_data(json) {


    //Get the html description of the tree
    var collapsibleHtml =  "<ul class=\"collapsibleList\">"
        + buildAndInjectTree(json, undefined, false);
        +"</ul>";

    //Inject the html description of the tree in the element with id "myid"
    var element = document.getElementById("myid");
    element.innerHTML = collapsibleHtml ;

    // make ul element with class="collapsibleList" collapsible
    CollapsibleLists.apply();
    console.log("\n\n");
}


function buildAndInjectTree(json,root,childrenRequired){

    var bbop_graph_json_string;
    if(childrenRequired){
        var bbop_graph_json_string = json.response.docs[0].bbop_direct_children_json;

    }else {
        var bbop_graph_json_string = json.response.docs[0].bbop_sibling_graph_json;
    }
    console.log("bbop_graph_json_string = " + bbop_graph_json_string);
    var bbop_graph_json = JSON.parse(bbop_graph_json_string);

    var requestedTermLabel = json.response.docs[0].label;
    console.log("requestedTermLabel = " + requestedTermLabel);

    var treeData = new Object();

    var all_children;
    var all_parent = [];

    // The bbop_sibling_graph_json which is stored in the solR index is composed of 2 part :
    //   the node part where all the node composing the tree are described
    //   the edge part where the tree is described
    // From the node part we get all the term IRI and label and build and iri2label array associating
    // the iri with their label as follow :
    //      IRI[\t]label
    // This array will be used later on when building the tree to display the the node label in the nodes rather then
    // the node IRI.
    var iri2label = [];
    for (var i = 0; i<bbop_graph_json.nodes.length; i++){
        var iri =bbop_graph_json.nodes[i].IRI;
        var label = bbop_graph_json.nodes[i].LABEL;
        console.log(iri + " = " + label + "\n");
        iri2label[iri2label.length] = iri + "\t" + label;
    }


    //Walking through the bbob_sibling_graph_json.edges description of the tree to build the treeData object.
    //There I have used the javascript object treeData as a 'dictionnary', 'hash' or 'map' to associate a nodes with
    // an array of their children.
    // treeData[http://ebi.ac.uk/efo/EFO_000001] woulr return and array containing the IRI of the direct children of that
    // node.
    for (var i = 0; i < bbop_graph_json.edges.length; i++) {


        var obj = bbop_graph_json.edges[i].obj;
        var sub = bbop_graph_json.edges[i].sub;

        all_children = all_children + "\t" + obj;
        all_parent[all_parent.length] = sub;


        var children_array = treeData[sub];
        if (typeof children_array == 'undefined') {
            children_array = [];
        }
        children_array[children_array.length] = obj;
        treeData[sub] = children_array;


    }

    //Search for the tree root (i.e. : the one element which has no parent).
    if(!root) {
        for (var i = 0; i < all_parent.length; i++) {
            var parent = all_parent[i];
            if (all_children.indexOf(parent) == -1) {
                root = parent;
            }
        }
    }
    console.log("ROOT = " + root + "\n");

    return readTreeData(treeData, root, iri2label, requestedTermLabel,childrenRequired);

}


//Returns true if the iri term has children, false if it doesn't.
//treeData : an object which associates parents with and array of direct children
//          ex : { http://www.ebi.ac.uk/efo/EFO_0000001 : ["http://www.ifomis.org/bfo/1.1/snap#SpecificallyDependentContinuant","http://www.ifomis.org/bfo/1.1/snap#MaterialEntity","http://www.ifomis.org/bfo/1.1/snap#Site","http://purl.obolibrary.org/obo/IAO_0000030", "http://www.ifomis.org/bfo/1.1/span#ProcessualEntity"],
//                 http://www.ifomis.org/bfo/1.1/snap#SpecificallyDependentContinuant : ["http://www.ifomis.org/bfo/1.1/snap#Disposition", "http://www.ifomis.org/bfo/1.1/snap#Function", "http://www.ifomis.org/bfo/1.1/snap#Role", "http://www.ifomis.org/bfo/1.1/snap#Quality"]
//               }
//
function hasChildren(treeData, iri){
    var rootChildren = treeData[iri];
    if(typeof  rootChildren !== 'undefined' ) {

        if(rootChildren.length !== 0) {
            return true;
        }else{
            return false;
        }
    }else{
        return false;
    }

}

//
// Recursive function which build the tree.
// treeData : an object which associates parents with and array of direct children
//          ex : { http://www.ebi.ac.uk/efo/EFO_0000001 : ["http://www.ifomis.org/bfo/1.1/snap#SpecificallyDependentContinuant","http://www.ifomis.org/bfo/1.1/snap#MaterialEntity","http://www.ifomis.org/bfo/1.1/snap#Site","http://purl.obolibrary.org/obo/IAO_0000030", "http://www.ifomis.org/bfo/1.1/span#ProcessualEntity"],
//                 http://www.ifomis.org/bfo/1.1/snap#SpecificallyDependentContinuant : ["http://www.ifomis.org/bfo/1.1/snap#Disposition", "http://www.ifomis.org/bfo/1.1/snap#Function", "http://www.ifomis.org/bfo/1.1/snap#Role", "http://www.ifomis.org/bfo/1.1/snap#Quality"]
//               }
// root : the root of the tree
//       ex :http://www.ebi.ac.uk/efo/EFO_0000001
// iri2label : an array of cells containing the iri of a term followed by its label
//            ex : http://www.ebi.ac.uk/efo/EFO_0000001 experimental factor
// requestedTermLabel : the term for which we're building the tree ( this is so that it can be highlighted when displayed (if not given, nothing will be highlighted).
//
// Return : an html string representing the tree.
//          ex :    <ul class="collapsibleList">
//                  <li class=" collapsibleListOpen"><font color="#4D4D4D"> experimental factor</font>
//                      <ul class="collapsibleList collapsibleList">
//                      <li class=" collapsibleListOpen"><font color="#4D4D4D"> material property</font>
//                          <ul class="collapsibleList collapsibleList collapsibleList">
//                          <li class=" collapsibleListOpen"><font color="#4D4D4D"> disposition</font>
//                              <ul class="collapsibleList collapsibleList collapsibleList collapsibleList">
//                              <li class=" collapsibleListOpen"><font color="#4D4D4D"> disease</font>
//                                  <ul class="collapsibleList collapsibleList collapsibleList collapsibleList collapsibleList">
//                                  <li class=" collapsibleListOpen"><font color="#4D4D4D"> brain disease</font>
//                                      <ul class="collapsibleList collapsibleList collapsibleList collapsibleList collapsibleList collapsibleList">
//                                      <li><font color="#FFC36E"> insomnia</font></li><li><font color="#4D4D4D"> Genetic neurodegenerative disease</font></li>
//                                      <li><font color="#4D4D4D"> narcolepsy with cataplexy</font></li><li><font color="#4D4D4D"> mental retardation</font></li>
//                                      <li><font color="#4D4D4D"> Genetic dementia</font></li>
//                                      <li><font color="#4D4D4D"> memory impairment</font></li>
//                                      <li><font color="#4D4D4D"> Pick disease</font></li>
//                                      <li><font color="#4D4D4D"> central nervous system cyst</font></li>
//                                      <li><font color="#4D4D4D"> dementia</font></li>
//                                      </ul>
//                                  </li>
//                              <li><font color="#4D4D4D"> mode of inheritance</font></li>
//                              <li><font color="#4D4D4D"> normal</font></li>
//                              <li><font color="#4D4D4D"> mental health</font></li>
//                              </ul>
//                          </li>
//                          <li><font color="#4D4D4D"> function</font></li>
//                          <li><font color="#4D4D4D"> role</font></li>
//                          <li><font color="#4D4D4D"> quality</font></li>
//                          </ul>
//                      </li>
//                      <li><font color="#4D4D4D"> material entity</font></li>
//                      <li><font color="#4D4D4D"> site</font></li>
//                      <li><font color="#4D4D4D"> information entity</font></li>
//                      <li><font color="#4D4D4D"> process</font></li>
//                      </ul>
//                  </li>
//                  </ul>
//
function readTreeData(treeData, root, iri2label, requestedTermLabel, childrenRequired){

    //console.log("json path = " + json_path + "\n");
    //short_form
    var string = "";

    //Getting the short form of the IRI to search in solr with short_form.
    //
    // ex : IRI = http://purl.obolibrary.org/obo/IAO_0000030
    //      short_form = IAO_0000030
    //
    //      IRI = http://www.ifomis.org/bfo/1.1/span#ProcessualEntity
    //      short_form = ProcessualEntity
    //
    //      IRI = http://www.ebi.ac.uk/efo/EFO_0000684
    //      short_form = EFO_0000684
    //
    short_form = root.substr(root.lastIndexOf("/")+1, root.length).replace("span#", "").replace("snap#","");
    console.log("short_form = " + short_form);


    if(! treeData[root]) {
        var subTerm_solrPath = json_path;
        subTerm_solrPath = subTerm_solrPath.replace("LABEL_TO_REPLACE", short_form);
        subTerm_solrPath = subTerm_solrPath.replace("ONTOLOGY_NAME_TO_REPLACE", ontology_name);
        subTerm_solrPath = subTerm_solrPath.replace("label", "short_form");
        subTerm_solrPath = subTerm_solrPath + "&wt=json&json.wrf=solr_has_children&callback=?"

        jQuery.getJSON(subTerm_solrPath);//,function(json){


        console.log("subTerm_solrPath =  " + subTerm_solrPath);
    }

    var label;
    for(var k=0;k<iri2label.length; k++){
        var value = iri2label[k];
        if(value.indexOf(root) != -1){
            var splitValue = value.split("\t");
            label = splitValue[1];
        }
    }

    //Right the node label in orange if the node is the node for which the user requested the tree or in  dark grey
    // otherwise.
    var color = "#4D4D4D"; //dark grey
    console.log( "Bip " + requestedTermLabel + ", " + root + ", " + label + "\n");
    if(requestedTermLabel == label && !childrenRequired){
        color = "#FFC36E";//orange
    }

    string = string + "<li id=\"" + label+ "\">";
    string = string + "<font color=" +color + "> " + label + "</font>";
    if(hasChildren(treeData, root)) {
        if(childrenRequired){
            string = string + "<ul class=\"solrCollapsibleList\">";

        }else {
            string = string + "<ul class=\"collapsibleList\">";
        }
    }

    var rootChildren = treeData[root];
    if(typeof  rootChildren !== 'undefined' ) {

        if(rootChildren.length !== 0) {

            for (var i = 0; i < rootChildren.length; i++) {

                string = string + readTreeData(treeData, rootChildren[i], iri2label,requestedTermLabel,childrenRequired);

            }

        }
    }
    if(hasChildren(treeData, root)) {
        string = string +  "</ul>";
    }
    string = string + "</li>";

    return string;


}




