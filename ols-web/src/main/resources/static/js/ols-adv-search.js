$(document).ready(function() {
    $('.ontology-select').select2({placeholder: "Filter by ontology"})
        .on('select2:select', function (e) {
            $("#ontology-id").append($('<option/>', {
                value: e.target.value.toLowerCase(),
                text : e.target.value.toLowerCase(),
                selected : 'selected'
            }));

            //always restart start when faceting
            $('#start').val(0);
            $("#filter_form").submit();
        })
        .on('select2:unselecting', function (e) {

            $("#ontology-id").find("[value=\"" + e.target.value.toLowerCase() + "\"]").remove();
            //always restart start when faceting
            $('#start').val(0);
            $("#filter_form").submit();
        });

    $('.type-select').select2({placeholder: "Filter by type"})
        .on('select2:select', function (e) {
            $("#ontology-type-id").append($('<option/>', {
                value: e.target.value.toLowerCase(),
                text : e.target.value.toLowerCase(),
                selected : 'selected'
            }));

            //always restart start when faceting
            $('#start').val(0);
            $("#filter_form").submit();
        })
        .on('select2:unselecting', function (e) {

            $("#ontology-type-id").find("[value=\"" + e.target.value.toLowerCase() + "\"]").remove();
            //always restart start when faceting
            $('#start').val(0);
            $("#filter_form").submit();
        });

    $('.typeahead').typeahead('val', $('#query-id').val(), false).typeahead('close');

    ontologyList = new Object();
    $('#ontology-select-id option').each(function(){
        ontologyList[this.value]=$(this).attr('data-ontology-title');
    });

    try {
        loadResults();
    } catch (err) {

    }

});

var ontologyList;
//var ontologyTitle;

function clearFilter() {
    $('#ontology-select-id').val('');
    $('#ontology-id').val('');
    $('#ontology-type-id').val('');
    $('#ontology-select-type-id').val('');
    $('#group-id').attr('checked', false);
    $('#exact-id').attr('checked', false);
    $('#obsolete-id').attr('checked', false);
    $('#filter_form').submit();

}

function loadResults() {

    var query =$('#filter_form').serialize()
    //console.log("Loading results for " + query);

    solrSearch(query)

}

function solrSearch(queryTerm) {
    //console.log("Solr search request received for " + queryTerm);
    $.getJSON('api/search?' + queryTerm)
        .done(function (data) {
            processData(data);
        });
}


function clickPrev() {
    if (! $( "#prev-button" ).hasClass( "disabled" )) {
        var start = $('#start-display').text() - 11;
        $('#start').val(start);
        $('#filter_form').submit();
    }


}

function clickNext() {

    if (!$( "#next-button" ).hasClass( "disabled" )) {
        var end = $('#end-display').text();
        $('#start').val(end);
        $('#filter_form').submit();
    }
}

function processData(data) {
    var docs = data.response.docs;

    // render results stats and pagination
    var start = data.response.start;
    var end = data.response.numFound > 10 ? start + 10 : data.response.numFound;
    var total = data.response.numFound;

    if (total == 0) {
        $('.search-results-count').text("No results!")
        // hide spinner
        $('#searching').hide();
        $('#search-results-summary').show();

        return;
    }
    $('#start-display').text(start + 1);
    $('#end-display').text(end);
    $('#total-display').text(total);

    if (start > 0) {
        $('#prev-button').removeClass('disabled');
    }
    if (end < total) {
        $('#next-button').removeClass('disabled');
    }


    // hide spinner
    $('#searching').hide();
    // show summary
    $('#search-results-summary').show();

    // render search results
    var searchResult = $('#search-results');
    $.each(docs, function(index, row) {

        var isOntology = (row.type == 'ontology');
        var link;

        if (isOntology) {
            link = getOntologyLink(row.label, row.ontology_name);
        }
        else {
            link = getTermLink(row.label,row.ontology_name, row.type, row.iri);
        }


        var description = row.description;
        if (description != undefined) {
            description = row.description[0];

            if (description.length > 300) {
                description = description.substr(0, 500) + '…';
            }
        }

        var resultHtml = $('<section></section>');
        resultHtml = resultHtml.append(link);
        resultHtml = resultHtml.append('&nbsp;&nbsp;');


        var shortId = row.obo_id;
        if (shortId == undefined) {
            shortId = row.short_form;
        }

        var termShortId;
        if (isOntology) {
            termShortId = $("<div class='ontology-source'>" + row.ontology_prefix + "</div>");
        }
        else {
            termShortId= $("<div class='term-source'>" + shortId + "</div>");
        }

        resultHtml = resultHtml.append(termShortId);

        resultHtml = resultHtml.append('<br/>');
        resultHtml = resultHtml.append($('<span class="search-results-url"></span>').text(row.iri));
        resultHtml = resultHtml.append('<br/>');
        if (description != undefined) {
            resultHtml = resultHtml.append($('<span class="search-results-description"></span>').text(description));
            resultHtml = resultHtml.append('<br/>');
        }

        if (!isOntology) {
            resultHtml = resultHtml.append('<b>Ontology: </b>');

            var ontologyTitle = ontologyList[row.ontology_name];
            var ontologyLink = $('<a>',{
                class: 'nounderline',
                text: ontologyTitle,
                href: 'ontologies/' + row.ontology_name
            });
            resultHtml = resultHtml.append(ontologyLink);
            resultHtml = resultHtml.append('&nbsp;');
            var ontologies = $("<div class='ontology-source' title='"+ontologyList[row.ontology_name]+"'>" + row.ontology_prefix + "</div>");
            resultHtml = resultHtml.append(ontologies);
            resultHtml = resultHtml.append('<br/>');

            if (data.expanded != undefined) {
                if (data.expanded[row.iri] != undefined) {
                    resultHtml = resultHtml.append('<b>Also in: </b>');

                    $.each (data.expanded[row.iri].docs, function (expandedIndex, expandedRow) {
                        var exLink = getTermLink(expandedRow.ontology_prefix, expandedRow.ontology_name,expandedRow.type, expandedRow.iri )

                        var ontoLink = $("<a title='"+ontologyList[expandedRow.ontology_name]+"' href='" + exLink.attr('href') + "' style='border-bottom-width: 0px;'></a>")
                            .append($("<span class='ontology-source'></span>").text(exLink.text()))
                        resultHtml.append(ontoLink);
                    })

                }
            }
        }

        resultHtml = resultHtml.append('<br/>');
        resultHtml = resultHtml.append('<br/>');

        searchResult.append(resultHtml);
    });


    var facets = data.facet_counts.facet_fields;

    var typeSummary = $('#type-summary');
    var ontologySummary = $('#ontology-summary');

    renderTypesFacetField(facets.type, typeSummary);
    renderOntologyFacetField(facets.ontology_prefix, ontologySummary);
    //renderFacetField(facets.is_defining_ontology, "Defining ontology", searchSummary);
    //renderFacetField(facets.is_obsolete, "Is Obsolete", searchSummary);
    //renderFacetField(facets.subset, "Susbsets", searchSummary);

    // IE hack to force closing of autocomplete after search result drawn
    $('.typeahead').typeahead('close');

}

function getOntologyLink (label, ontology ) {
    return $('<a>',{
        class: 'search-results-label nounderline',
        text: label,
        title: label,
        href: 'ontologies/' + ontology
    });
}


function getTermLink (label, ontology, type, uri ) {
    var encodedUri = encodeURIComponent(uri);
    var _type = getUrlType(type);

    return $('<a>',{
        class: 'search-results-label nounderline',
        text: label,
        title: label,
        href: 'ontologies/' + ontology + "/" + _type + "?iri=" + encodedUri
    });
}

function renderTypesFacetField (facetArray, searchSummary) {
    if (facetArray != undefined) {

        var numberOfFacets = 0;
        var fieldList = $('<div class="list-group"></div>');

        for (var x = 0 ; x < facetArray.length; x = x + 2) {
            var name = facetArray[x];
            var count = facetArray[x + 1];

            if (count > 0) {
                fieldList.append('<button type=\'button\' id="'+name+'" class="type_list list-group-item">'+name+ '<span class="badge">' + count + '</span></button>');
                numberOfFacets++;
            }

        }

        if (numberOfFacets > 0) {
            searchSummary.append(fieldList);
        }

        $(".type_list").on('click', function(e){
            //$('#ontology-select-id').val('');
            $("#ontology-type-id").append($('<option/>', {
                value: e.target.id.toLowerCase(),
                text : e.target.id.toLowerCase(),
                selected : 'selected'
            }));

            //always restart start when faceting
            $('#start').val(0);
            $("#filter_form").submit();
        });
    }
}

function renderOntologyFacetField (facetArray, searchSummary) {

    if (facetArray != undefined) {

        var numberOfFacets = 0;

        var fieldList = $('<div class="list-group"></div>');

        for (var x = 0 ; x < facetArray.length; x = x + 2) {
            var name = facetArray[x];
            var count = facetArray[x + 1];

            if (count > 0) {
                fieldList.append('<button type=\'button\' id="'+name+'" class="onto_list list-group-item" title="'+ontologyList[name.toLowerCase()]+'">'+name+ '<span class="badge">' + count + '</span></button>');
                numberOfFacets++;
            }

        }

        if (numberOfFacets > 0) {
            searchSummary.append(fieldList);
        }

        //Register click event for ontology list
        $(".onto_list").on('click', function(e){
            //$('#ontology-select-id').val('');
            $("#ontology-id").append($('<option/>', {
                value: e.target.id.toLowerCase(),
                text : e.target.id.toLowerCase(),
                selected : 'selected'
            }));

            //always restart start when faceting
            $('#start').val(0);
            $("#filter_form").submit();
        });
    }
}