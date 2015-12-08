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

    ontologyList = new Object();
    $('#ontology-select-id option').each(function(){
        ontologyList[this.value]=this.text;
     });

    try {
        loadResults();
    } catch (err) {

    }
});

var ontologyList;

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
        // hide spinner
        $('#searching').hide();
        $('#search-results-summary').show();

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


    // hide spinner
    $('#searching').hide();
    // show summary
    $('#search-results-summary').show();

    // render search results
    var searchResult = $('#search-results');
    $.each(docs, function(index, row) {
        var encodedUri = encodeURIComponent(row.iri);
        var type = getUrlType(row.type);

        var link = $('<a>',{
            class: 'search-results-label',
            text: row.label,
            title: row.label,
            href: 'ontologies/' + row.ontology_name + "/" + type + "?iri=" + encodedUri
        });

        var description = row.description;
        if (description != undefined) {
            description = row.description[0];

            if (description.length > 300) {
                description = description.substr(0, 300) + '…';
            }
        }

        /*IS this ever executed? DO we need this*/
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

        var ontologies = $("<div class='ontology-source' title='"+ontologyList[row.ontology_name]+"'>" + row.ontology_prefix + "</div>");
        resultHtml = resultHtml.append(ontologies);




        /*IS this ever executed? Do we need this*/
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




    var facets = data.facet_counts.facet_fields;

    var searchSummary = $('#results-summary');

    renderFacetField(facets.ontology_prefix, "Ontologies", searchSummary);
    renderFacetField(facets.type, "Type", searchSummary);
    //renderFacetField(facets.is_defining_ontology, "Defining ontology", searchSummary);
    //renderFacetField(facets.is_obsolete, "Is Obsolete", searchSummary);
    //renderFacetField(facets.subset, "Susbsets", searchSummary);

}

function renderFacetField (facetArray, inputName, searchSummary) {

    if (facetArray != undefined) {

        var numberOfFacets = 0;

        var facet = $('<h3></h3>').text(inputName);
        var fieldList = $('<ul></ul>');

        for (var x = 0 ; x < facetArray.length; x = x + 2) {
            var name = facetArray[x];
            var count = facetArray[x + 1];

            if (count > 0) {

               if (inputName==="Ontologies")
                        {   fieldList.append('<li><a href="#" id="'+name+'" class="onto_list" title="'+ontologyList[name.toLowerCase()]+'">'+name+ '</a> (' + count + ')</li>');         }
                else
                        {   fieldList.append('<li><a href="#" id="'+name+'" class="type_list">'+name+ '</a> (' + count + ')</li>');         }
                    //    {   fieldList.append(  $('<li></li>').text(name+ " (" + count + ")"));  }

                numberOfFacets++;
            }

        }

        if (numberOfFacets > 1) {
            $('#summary').attr('visibility', 'visible');
            searchSummary.append(facet);
            searchSummary.append(fieldList);
        }

        //Register click event for ontology list
        $(".onto_list").on('click', function(e){
            $('#ontology-select-id').val('');
            $("#ontology-select-id option[value='"+e.target.id.toLowerCase()+"']").prop('selected', true);
            $("#filter_form").submit();
        });

        $(".type_list").on('click', function(e){
            $('#ontology-type-id').val('');
            $('#ontology-type-id option[value="'+e.target.id.toLowerCase()+'"]').prop('selected', true);;
            $("#filter_form").submit();
        });


    }
}