


function getUrlType (type) {
    var urlType = 'terms';
    if (type == 'property') {
        urlType = 'properties';
    }
    else if (type == 'individual') {
        urlType= 'individuals';
    }
    return urlType;
}

function goTo (url) {
    window.location.href =  url;
}