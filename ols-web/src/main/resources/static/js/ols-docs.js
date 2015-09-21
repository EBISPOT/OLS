$(document).ready(function() {

    var url_header = "api-guide.html #header";
    var url_footer = "api-guide.html #footer";
    var url = "api-guide.html #content";
    console.log("Documentation should be loaded from " + url + "...");

    $("#docs-header").load (url_header);
    $("#docs-content").load (url);
    $("#docs-footer").load (url_footer);

});
