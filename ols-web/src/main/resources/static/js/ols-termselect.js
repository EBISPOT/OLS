
$(document).ready(function() {

    $( "input[data-olswidget='select']" ).each(function() {

        $(this).devbridgeAutocomplete({
            serviceUrl: '/api/select',
            minChars: 3,
            dataType : 'json',
            paramName: 'q',
            onSelect : function (suggestion)  {
                var type = getUrlType(suggestion.data.type);
                var encoded = encodeURIComponent(suggestion.data.iri);
                window.location.href = 'ontology/' + suggestion.data.ontology + "/" + type + '?iri=' + encoded;
            },
            transformResult: function(response) {
                return {
                    suggestions: $.map(response.response.docs, function(dataItem) {
                        var id =   dataItem.id;

                        var label = dataItem.label;
                        var synonym = "";
                        var cantHighlight = true;
                        if (response.highlighting[id].label_autosuggest != undefined) {
                            label = response.highlighting[id].label_autosuggest[0];
                            cantHighlight = false;
                        }
                        else if (response.highlighting[id].label != undefined) {
                            label = response.highlighting[id].label[0];
                            cantHighlight = false;
                        }

                        if (cantHighlight) {
                            if (response.highlighting[id].synonym_autosuggest != undefined) {
                                synonym = response.highlighting[id].synonym_autosuggest[0];
                            }
                            else if (response.highlighting[id].synonym != undefined) {
                                synonym = response.highlighting[id].synonym[0];
                            }
                        }

                        return { value: dataItem.label, data: {ontology: dataItem.ontology_name, iri : dataItem.uri, label: label, synonym: synonym, type: dataItem.type}};
                    })
                };
            },
            formatResult: function (suggestion, currentValue) {

                var label = suggestion.data.label ;
                var extra = "";
                if (suggestion.data.synonym != "") {
                    label =  suggestion.data.synonym;
                    extra = "<div class='sub-text'>synonym for " + suggestion.value + "</div>"
                }

                return "<div><div class='ontology-source'>" + suggestion.data.ontology + "</div><div class='ontology-suggest''><div class='suggestion-value'>" + label + "</div>" + extra + "</div></div>";
            }
        });

    });
});