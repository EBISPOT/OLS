


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

    $('.panel-heading').each(function(i, heading) {
        var open = true
        $(heading).children('h4').append(
            $('<div/>').text('-').addClass('toggle').click(function() {
                if(open) {
                    $(heading).siblings('.panel-body').hide()
                    open = false
                    $(this).text('+')
                } else {
                    $(heading).siblings('.panel-body').show()
                    open = true
                    $(this).text('-')
                }
            })
        )
    })

    var treeButtonsOpen = true
    $('.ols-tree-buttons').prepend(
        $('<div/>').text('-').addClass('toggle').click(function() {
            if(treeButtonsOpen) {
                $(this).text('+').siblings().hide()
                treeButtonsOpen = false
            } else {
                $(this).text('-').siblings().show()
                treeButtonsOpen = true
            }
        })
    )
})
