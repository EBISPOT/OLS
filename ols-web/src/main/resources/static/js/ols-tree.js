
$(document).ready(function() {
     showTree(false);

});

function toggleSiblings(elm) {

    var buttonValue = $(elm).val() == 'true';

    if (buttonValue) {
        $(elm).text("Hide siblings");
        $(elm).val(false);
    }
    else {
        $(elm).text("Show siblings");
        $(elm).val(true);

    }
    showTree(buttonValue)
    console.log(buttonValue);

}

function showTree(siblings) {


    $( "div[data-olswidget='tree']").empty();

    $( "div[data-olswidget='tree']" ).each(function() {

        // get param
        var ontologyName = $(this).data("olsontology");
        var termType = getUrlType($(this).data("ols-termtype"));
        var termIri = $(this).data("ols-iri");
        var relativePath = $(this).data("selectpath") ? $(this).data("selectpath") : '';

        console.log("Init tree for: " + ontologyName + " - " + termIri + " - " + termType);
        // show errors if above not defined

        // build tree
        $.jstree.defaults.core.data = true;
        $.jstree.defaults.core.expand_selected_onload = true;

        var rootUrl = relativePath + '/api/ontologies/' + ontologyName + '/' + termType + '/roots?size=1000';

        var baseUrl = relativePath + '/api/ontologies/' + ontologyName + '/' + termType + '/';
        var url = baseUrl + encodeURIComponent(encodeURIComponent(termIri)) + '/jstree' ;
        if (siblings) {
            url += '?siblings=true';
            console.log("rebuild tree iwth siblings " + url)
        }
        console.log("tree url " + url)
        var treeDiv = $('<div></div>')
            .jstree({
                'core' : {
                    'data': function (node, cb) {
                        console.log("node id: " + node.id + " term " + termIri);

                        if (node.id === '#' && termIri != '') {
                            $.getJSON(url, function (data) {
                                cb(data)
                            });
                        }
                        else if (node.id === '#' && termIri === '') {

                            // show roots
                            $.getJSON(rootUrl, function (data) {
                                var data = _processOlsData(data, '#', termType);
                                cb(data)
                            });
                        }
                        else {
                            var requestIri = node.original.iri;
                            // get all children
                            var childUrl = baseUrl + encodeURIComponent(encodeURIComponent(requestIri)) + '/jstree/children/'+ node.id;

                            $.getJSON(childUrl, function (data) {
                                //var parentId = node.id;
                                //var data = _processOlsData(data, parentId, termType);
                                cb(data)

                            });
                        }

                    },
                    "themes": {
                        "dots": true
                        , "icons": false,
                        "name" : "proton"
                        //"responsive" : true
                    }
                },
                plugins: ["sort"]
            }).bind("select_node.jstree", function (node, selected, event) {

                //var tree = $(this).jstree();
                //node = tree.get_node(event.target);
                //console.log(node);
                //console.log(selected);
                //console.log(event);
                // Do my action

                var type = termType;
                if (type == 'individual' && termIri != selected.node.original.iri) {
                    type = getUrlType('terms');
                }
                window.location.href = relativePath + "ontologies/" + selected.node.original.ontology_name + "/" + type + '?iri=' + encodeURIComponent(selected.node.original.iri);
            });


        $(this).append(treeDiv);

    });
}

function _processOlsData (data, parentId, termType) {

    var newData = [];
    var counter = 1;

    var results;
    if (termType == "properties") {
        console.log("getting term type:" + termType);
        results = data._embedded.properties;
    }
    else if (termType == "individuals") {
        results = data._embedded.individuals;
    }
    else {
        results = data._embedded.terms;
    }
    $.each(results, function(index, term) {
        var id = parentId + "_" + counter;
        var parent = parentId;
        if (parentId === '#') {
            id = counter;
            parent = parentId;
        }

        newData.push(
            {
                "id" : id,
                "parent" : parent,
                "iri" : term.iri,
                "ontology_name" : term.ontology_name,
                "text" : term.label,
                "leaf" : !term.has_children,
                "children" : term.has_children
            }
        );
        counter++;
    });
    return newData;

}