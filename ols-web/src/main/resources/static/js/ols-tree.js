
$(document).ready(function() {

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

        var rootUrl = '/api/ontology/' + ontologyName + '/roots?size=1000';
        var baseUrl = '/api/ontology/' + ontologyName + '/' + termType + '/';
        var url = baseUrl + encodeURIComponent(encodeURIComponent(termIri)) + '/jstree' ;
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
                            var childUrl = baseUrl + encodeURIComponent(encodeURIComponent(requestIri)) + '/children?size=1000';

                            $.getJSON(childUrl, function (data) {
                                var parentId = node.id;
                                var data = _processOlsData(data, parentId, termType);
                                cb(data)

                            });
                        }

                    },
                    "themes": {
                        "dots": true
                        , "icons": false
                    }
                }
            }).bind("dblclick.jstree", function (event) {

                var tree = $(this).jstree();
                var node = tree.get_node(event.target);
                console.log(node);
                // Do my action
                window.location.href = relativePath + "/ontology/" + node.original.ontology_name + "/" + termType + '?iri=' + encodeURIComponent(node.original.iri);
            });


        $(this).append(treeDiv);

    });
});

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