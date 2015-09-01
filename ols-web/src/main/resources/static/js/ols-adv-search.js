$(document).ready(function() {


    $('.ontology-select').select2();
    $('.type-select').select2();


    $('#local-search').on ('submit', function (e) {
        e.preventDefault();
        var queryTerm = $('#local-searchbox').val();

        if (queryTerm != '') {
            console.log("new search for " + queryTerm)
            $('#query-id').val(queryTerm);
            var query =$('#filter_form').serialize();
            $('#filter_form').submit();
        }

    });

    try {
        loadResults();
    } catch (err) {

    }



});

function clearFilter() {
    $('#ontology-select-id').val('');
    $('#ontology-type-id').val('');
    $('#group-id').attr('checked', false);
    $('#exact-id').attr('checked', false);
    $('#obsolete-id').attr('checked', false);
    $('#local-searchbox').submit();

}

function loadResults() {

    //var query = $("#query-id").text();

    var query =$('#filter_form').serialize()
    console.log("Loading results for " + query);

    solrSearch(query)

}

function solrSearch(queryTerm) {
    console.log("Solr search request received for " + queryTerm);

    $.getJSON('api/search?' + queryTerm)
        .done(function (data) {
            processData(data);
        });
}


function clickPrev() {

    var start = $('#start-display').text() - 11;
    $('#start').val(start);
    $('#filter_form').submit();


}

function clickNext() {
    var end = $('#end-display').text();
    $('#start').val(end);
    $('#filter_form').submit();

}

function processData(data) {

    var docs = data.response.docs;

    // render results stats and pagination
    var start = data.response.start;
    var end = data.response.numFound > 10 ? start + 10 : data.response.numFound;
    var total = data.response.numFound;

    if (total == 0) {
        $('.search-results-count').text("No results!")
        return;
    }
    $('#start-display').text(start + 1);
    $('#end-display').text(end);
    $('#total-display').text(total);

    if (start > 0) {
        $('#prev-button').removeAttr('disabled');
    }
    if (end < total) {
        $('#next-button').removeAttr('disabled');
    }


    // render search results
    var searchResult = $('#search-results');
    $.each(docs, function(index, row) {
        var encodedUri = encodeURIComponent(row.iri);
        var type = getUrlType(row.type);

        var link = $('<a>',{
            class: 'search-results-label',
            text: row.label,
            title: row.label,
            href: 'ontology/' + row.ontology_name + "/" + type + "?iri=" + encodedUri
        });

        var description = row.description;
        if (description != undefined) {
            description = row.description[0];

            if (description.length > 300) {
                description = description.substr(0, 300) + '…';
            }
        }

        if (data.expanded != undefined) {
            if (data.expanded[row.iri] != undefined) {

                $.each (data.expanded[row.iri].docs, function (expandedIndex, expandedRow) {
                    $("<div class='ontology-source'>" + expandedRow.ontology_prefix + "</div>").insertAfter(ontologies);
                })

            }


        }

        var resultHtml = $('<section></section>');
        resultHtml = resultHtml.append(link);
        resultHtml = resultHtml.append('&nbsp;&nbsp;');
        var ontologies = $("<div class='ontology-source'>" + row.ontology_prefix + "</div>");

        resultHtml = resultHtml.append(ontologies);

        if (data.expanded != undefined) {
            if (data.expanded[row.iri] != undefined) {

                $.each (data.expanded[row.iri].docs, function (expandedIndex, expandedRow) {
                    resultHtml.append($("<div class='ontology-source'>" + expandedRow.ontology_prefix + "</div>"));
                })

            }
        }

        resultHtml = resultHtml.append('<br/>');
        resultHtml = resultHtml.append($('<span class="search-results-url"></span>').text(row.iri));
        resultHtml = resultHtml.append('<br/>');
        resultHtml = resultHtml.append($('<span class="search-results-description"></span>').text(description));
        resultHtml = resultHtml.append('<br/><hr/>');

        searchResult.append(resultHtml);
    });

    //

    var facets = data.facet_counts.facet_fields;

    var searchSummary = $('#results-summary');

    renderFacetField(facets.ontology_name, "Ontologies", searchSummary);
    renderFacetField(facets.type, "Type", searchSummary);
    //renderFacetField(facets.is_defining_ontology, "Defining ontology", searchSummary);
    //renderFacetField(facets.is_obsolete, "Is Obsolete", searchSummary);
    //renderFacetField(facets.subset, "Susbsets", searchSummary);
}

function renderFacetField (facetArray, name, searchSummary) {

    if (facetArray != undefined) {

        var numberOfFacets = 0;

        var facet = $('<h3></h3>').text(name);
        var fieldList = $('<ul></ul>');

        for (var x = 0 ; x < facetArray.length; x = x + 2) {
            var name = facetArray[x];
            var count = facetArray[x + 1];
            console.log("facets " + name + " - " + count);

            if (count > 0) {
                fieldList.append($('<li></li>').text(name + " (" + count + ")"));
                numberOfFacets++;
            }

        }

        if (numberOfFacets > 1) {
            $('#summary').attr('visibility', 'visible');
            searchSummary.append(facet);
            searchSummary.append(fieldList);
        }
    }
}