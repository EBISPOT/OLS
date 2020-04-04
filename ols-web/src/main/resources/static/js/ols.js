


function getUrlType (type) {
    var urlType = 'terms';
    if (type == 'property') {
        urlType = 'properties';
    }
    else if (type == 'individual') {
        urlType= 'individuals';
    }
    else if (type == 'ontology') {
        urlType= 'ontology';
    }
    return urlType;
}

function goTo (url) {
    window.location.href =  url;
}

$(function() {
    $('p.annotation-value').each(function(i, el) {
        $(el).html($(el).html().replace(/((http|https|ftp):\/\/[^\s,]+)/g, "<a href=\"$1\">$1</a>"))
    })
})
