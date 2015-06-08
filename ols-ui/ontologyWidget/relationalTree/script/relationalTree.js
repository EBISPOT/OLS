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

            $.getJSON(full_path);//,function(json){
        }
    }

}(jQuery));





function getLabel (data){

    label = data.response.docs[0].label;
    short_form = data.response.docs[0].short_form;

    //label = 'fish';
    console.log("getLabel callback function THE LABEL   = " + label);

    var x = document.getElementsByTagName("text");
    var i;
    for (i = 0; i < x.length; i++) {
        var text = x[i].innerHTML;
        var lastIndexOf = text.lastIndexOf('/');
        lastIndexOf++;
        var short_form_from_URI = text.substr(lastIndexOf);
        short_form_from_URI = short_form_from_URI.replace('snap#','');

        if(short_form.indexOf(short_form_from_URI) != -1){
            x[i].innerHTML=label;
        }
        console.log("text = " + text);
    }




}


function on_data(json) {

    var bbop_sibling_graph_json_string = json.response.docs[0].bbop_sibling_graph_json;

    var bbop_sibling_graph_json = JSON.parse(bbop_sibling_graph_json_string);

    var requestedTermLabel = json.response.docs[0].label;

    var treeData = new Object();



    var array_of_parents = [];
    var all_children;
    var all_parent = [];

    var iri2label = [];
    for (var i = 0; i<bbop_sibling_graph_json.nodes.length; i++){
        var iri =bbop_sibling_graph_json.nodes[i].IRI;
        var label = bbop_sibling_graph_json.nodes[i].LABEL;
        console.log(iri + " = " + label + "\n");
        iri2label[iri2label.length] = iri + "\t" + label;
    }

    console.log("iri2label.length = " + iri2label.length + "\n");

    for (var i = 0; i < bbop_sibling_graph_json.edges.length; i++) {


        var obj = bbop_sibling_graph_json.edges[i].obj;
        var sub = bbop_sibling_graph_json.edges[i].sub;

        all_children = all_children + "\t" + obj;
        all_parent[all_parent.length] = sub;


        var children_array = treeData[sub];
        if (typeof children_array == 'undefined') {
            //console.log("children_array is undefined\n");
            children_array = [];
        }else{
            //console.log("children_array is NOT undefined\n");

        }
        children_array[children_array.length] = obj;
        treeData[sub] = children_array;
        //console.log("adding for parent " + sub + " array of children with  " + children_array.length + " children including " + obj+ "\n");

        //console.log("obj = " + obj + "\n");
        //console.log("sub = " + sub + "\n\n");


    }

    //Search for the tree root
    var root;
    for (var i = 0; i < all_parent.length; i++) {
        var parent = all_parent[i];
        if (all_children.indexOf(parent) == -1) {
            root = parent;
        }

    }


    //var treeData =
    //{
    //    "name": "lung",
    //    "children": [
    //        {
    //            "name": "has_parts",
    //            "children":[
    //                {"name": "respiratory system"}
    //            ]
    //        },
    //        {
    //            "name": "develops_from",
    //            "children": [
    //                {"name": "digetive_system"},
    //                {"name":"endoderm"},
    //                {"name" : "primordium"},
    //            ]
    //        }
    //    ]
    //};
    //



    var d3Node = {"name": root} ;
    d3Node = readTreeData(treeData, root, 0, iri2label, requestedTermLabel);


    // Create a svg canvas
    var vis = d3.select("#myid").append("svg:svg")
        .attr("width", 2500)
        .attr("height", 1500)
        .append("svg:g")
        .attr("transform", "translate(400, 0)"); // shift everything to the right

    // Create a tree "canvas"
    var tree = d3.layout.tree()
        .size([1500,1200]);
    //tree.nodeSize(100);
    var diagonal = d3.svg.diagonal()
        // change x and y (for the left to right tree)
        .projection(function(d) { return [d.y, d.x]; });



    // Preparing the data for the tree layout, convert data into an array of nodes
    var nodes = tree.nodes(d3Node);
    // Create an array with all the links
    var links = tree.links(nodes);

    var link = vis.selectAll("pathlink")
        .data(links)
        .enter().append("svg:path")
        .attr("class", "link")
        .attr("d", diagonal)
        .attr("stroke:gray;stroke-width:1;")

    var node = vis.selectAll("g.node")
        .data(nodes)
        .enter().append("svg:g")
        .attr("stroke", "#4D4D4D")
        .attr("transform", function(d) { return "translate(" + d.y + "," + d.x + ")"; })

    //x = d.x;
    //y = d.y;
    //console.log("x = " + x);

    // Add the dot at every node
    node.append("svg:rect")
        .attr("width", function(d) { name = d.name; length = name.length*6 + name.length*6*0.15; return length})
        .attr("height",40)
        .attr("style", function(d) { if(d.type == 'red') {return "fill:#FFEFD8;fill-opacity:1;stroke-width:1;stroke:#FFC36E"} else {return "fill:#DBEEFF;fill-opacity:1;stroke-width:1;stroke:#77BEFE"}})
//      .attr("x",x)
//     .attr("y",y)
        .attr("align-content","center")
        .attr("transform", function(d){
            name = d.name;
            length = name.length*6+name.length*6*0.1;
            if(!d.children){
                string = "translate(0,-20)";
                return string;
            }
            string = "translate(0,-20)";
            return string
        })
        .attr("rx","10")
        .attr("rx","10");

    // place the name atribute left or right depending if children
    node.append("svg:text")
        .attr("dx", function(d) {return 0;})
//return d.children ? -8 : 8; })
        .attr("dy", 3)
       // .attr("text-anchor", function(d) { return d.children ? "end" : "start"; })
        .text(function(d) { return d.name; }).attr("style","font-family:sans-serif;font-weight: 200;font-size:13px")







    console.log("\n\n\n\n\n\n\n");


    //console.log("bbop_sibling_graph_json_string = " + bbop_sibling_graph_json_string);
}

function readTreeData(treeData, root, tabCount, iri2label, requestedTermLabel){

    var solr_path = json_path;

    var label;
    for(var k=0;k<iri2label.length; k++){
        var value = iri2label[k];
        console.log("value = " + value + "/n");
        if(value.indexOf(root) != -1){
            var splitValue = value.split("\t");
            label = splitValue[1];
        }
    }

    //
    //var lastIndexOf = root.lastIndexOf('/');
    //lastIndexOf++;
    //var shortform = root.substr(lastIndexOf);
    //shortform = shortform.replace('snap#','');
    //
    //
    //solr_path = solr_path.replace("LABEL_TO_REPLACE", shortform);
    //solr_path = solr_path.replace("ONTOLOGY_NAME_TO_REPLACE", ontology_name);
    //solr_path = solr_path.replace("label" , 'short_form');
    //solr_path = solr_path + "&wt=json&json.wrf=getLabel&callback=?";
    //
    //
    //jQuery.getJSON(solr_path);//,function(json){


    //var script = document.createElement('script');
    //script.src = solr_path;
    //document.getElementsByTagName('head')[0].appendChild(script);
    ////window.setTimeout(wait(), 1000);

    console.log("first call back, label = " + label + "\n");


    //solr_path = solr_path + "&wt=json&json.wrf=get_label&callback=?"
    //console.log("solr_path = " + solr_path);
    var color = 'black';
    if(requestedTermLabel == label){
        color = 'red';
    }

    var d3Node = {"name":label, "type" : color};



    var tabs = "\t";
    for(i = 0; i<tabCount;i++){
        tabs = tabs + "\t";
    }
    tabCount++;


    //console.log("looking for " + root + " in treeData\n");
    var rootChildren = treeData[root];
    if(typeof  rootChildren !== 'undefined' ) {

        if(rootChildren.length !== 0) {
            var childrenArray = [];

            for (var i = 0; i < rootChildren.length; i++) {


                var children = d3Node['children'];
                if(typeof children == 'undefined'){
                    children = [];
                }

                //console.log(tabs + rootChildren[i] + "\n");
                var child = readTreeData(treeData, rootChildren[i], tabCount,iri2label,requestedTermLabel);

                children[children.length]=child;
                d3Node['children']=children;


            }

        }
    }

    return d3Node;


}




