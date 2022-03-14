


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
        $(el).html($(el).html().replace(/((http|https|ftp):\/\/[^\s,\[\]]+)/g, function(url) {
		console.log("Replacing URL with link: " + url);
		return "<a href=\"$1\">" + escapeHtml(url) + "</a>";
	}))
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






$(function() {

	if(!window['ontologyLanguages']) {
		console.log('multilang: no languages available')
		return
	}

	var picker = $('<select/>')
	
	for(var l of ontologyLanguages) {
		picker.append(
			$('<option/>').val(l).text(l).prop('selected', lang === l)
		)
	}

	$('body').append(picker)
	
	picker.css('position', 'fixed')
	picker.css('right', '20px')
	picker.css('top', '40px')
	picker.css('width', '100px')

	picker.change(function(e) {
		var searchParams = new URLSearchParams(window.location.search);
		searchParams.set("lang", e.target.value)
		window.location.search = searchParams.toString();
	})
})

function escapeHtml(unsafe)
{
    return unsafe
         .replace(/&/g, "&amp;")
         .replace(/</g, "&lt;")
         .replace(/>/g, "&gt;")
         .replace(/"/g, "&quot;")
         .replace(/'/g, "&#039;");
 }
