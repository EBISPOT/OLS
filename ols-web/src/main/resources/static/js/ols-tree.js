
$(document).ready(function() {
    var div=$("#term-tree");
    var ontologyName = $("#term-tree").data("olsontology");
    var termType = getUrlType($("#term-tree").data("ols-termtype"));
    var termIri = $("#term-tree").data("ols-iri");
    var relativePath = $("#term-tree").data("selectpath") ? $("#term-tree").data("selectpath") : '';

    var app = require("ols-treeview");
    var instance = new app();
    instance.draw(div, false, ontologyName, termType, termIri, relativePath, {});
});
