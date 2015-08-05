
$(document).ready(function() {

    $( "div[data-olswidget='tree']" ).each(function() {

        // get param
        var ontologyName = $(this).data("olsontology");
        var termType = getUrlType($(this).data("ols-termtype"));
        var termIri = $(this).data("ols-iri");

        console.log("Init tree for: " + ontologyName + " - " + termIri + " - " + termType);
        // show errors if above not defined

        // show loading

        // build tree
        $.jstree.defaults.core.data = true;
        $.jstree.defaults.core.expand_selected_onload = true;

        var baseUrl = '/api/ontology/' + ontologyName + '/' + termType + '/';
        var url = baseUrl + encodeURIComponent(encodeURIComponent(termIri)) + '/jstree' ;
        console.log("tree url " + url)
        var treeDiv = $('<div></div>')
            .jstree({
                'core' : {
                    'data':function (node, cb) {

                        if (node.id === '#') {
                            $.getJSON (url, function(data) {
                                cb(data)
                            });
                        }
                        else {

                            var requestIri = node.original.iri;
                            // get all children
                            var childUrl = baseUrl + encodeURIComponent(encodeURIComponent(requestIri)) + '/children?size=1000' ;

                            $.getJSON (childUrl, function(data) {


                                var newData = [];
                                var parentId = node.id;
                                var counter = 1;
                                $.each(data._embedded.terms, function(index, term) {
                                    newData.push(
                                        {
                                            "id" : parentId + "_" + counter,
                                            "parent" : parentId,
                                            "iri" : term.iri,
                                            "ontologyName" : term.ontologyName,
                                            "text" : term.label,
                                            "leaf" : term.leafNode,
                                            "children" : !term.leafNode
                                        }
                                    );
                                    counter++;
                                });
                                cb(newData)
                            });

                        }
                    },
                    "themes" : { "dots": true
                        , "icons": false }


                }
            }).bind("dblclick.jstree", function (event) {

                var tree = $(this).jstree();
                var node = tree.get_node(event.target);
                console.log(node);
                // Do my action
                window.open(termType + '?iri=' + encodeURIComponent(node.original.iri));
            });
        ;


        $(this).append(treeDiv);

    });
});