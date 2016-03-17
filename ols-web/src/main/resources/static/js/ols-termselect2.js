

$(document).ready(function() {

  //var states = new Bloodhound({
  //  datumTokenizer: Bloodhound.tokenizers.whitespace,
  //  queryTokenizer: Bloodhound.tokenizers.whitespace,
  //  // `states` is an array of state names defined in "The Basics"
  //  local: ['test1', 'test2', 'test3']
  //});
  //

  var relativePath = $(this).data("selectpath") ? $(this).data("selectpath") : '';
  var ontology =   $(this).data("olsontology") ? $(this).data("olsontology") : '';

  var olsSuggestData = new Bloodhound({
    datumTokenizer: Bloodhound.tokenizers.whitespace,
    queryTokenizer: Bloodhound.tokenizers.whitespace,
    remote: {
      url: relativePath + 'api/suggest?q=%QUERY',
      wildcard: "%QUERY",
      transform: function (response) {
        // Map the remote source JSON array to a JavaScript object array
        return $.map(response.response.docs, function (dataItem) {
          return {
            value: dataItem.autosuggest
          };
        });
      }
    }
  });

  var olsSelectData = new Bloodhound({
    datumTokenizer: Bloodhound.tokenizers.whitespace,
    queryTokenizer: Bloodhound.tokenizers.whitespace,
    identify: function(obj) { return obj.id; },
    remote: {
      url: relativePath + 'api/select?q=%QUERY',
      wildcard: "%QUERY",
      transform: function (response) {
        // Map the remote source JSON array to a JavaScript object array
        var query = response.responseHeader.params.q;
        return $.map(response.response.docs, function (dataItem) {

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

          var shortId = dataItem.obo_id;
          if (shortId == undefined) {
            shortId = dataItem.short_form;
          }
          return {
            id: id,
            value: dataItem.label,
            data: {ontology: dataItem.ontology_name, prefix: dataItem.ontology_prefix, iri : dataItem.iri, label: label,synonym: synonym, shortForm: shortId, type: dataItem.type},
            query: query
          };
        });
      }
    }
  });

  $('.typeahead').bind('typeahead:select', function(ev, suggestion) {

        if (suggestion.data != undefined) {
          var type = getUrlType(suggestion.data.type);
          if (type=='ontology') {
            window.location.href = relativePath + 'ontologies/' + suggestion.data.ontology;
          }
          else {
            var encoded = encodeURIComponent(suggestion.data.iri);
            window.location.href = relativePath + 'ontologies/' + suggestion.data.ontology + "/" + type + '?iri=' + encoded;
          }
        }
        else {
          ev.target.form.submit();
        }
      })
      .typeahead({
            hint: false,
            highlight: true,
            minLength: 3,
            limit: 4,
            async: true,

          },
          {
            name: 'suggestion',
            source: olsSuggestData,
            display: 'value'
          },
          {
            name: 'selection',
            source: olsSelectData,
            display: 'value',
            templates: {
              header: '<hr/><h5 style="text-align: center">Jump to</h5>',
              suggestion: function(suggestion) {

                var label = suggestion.data.label ;

                var extra = "";
                if (suggestion.data.synonym != "") {
                  label =  suggestion.data.synonym;
                  extra = "<div class='sub-text'>synonym for " + suggestion.value + "</div>"
                }

                var objectTypeHtml= "<div class='term-source'>" + suggestion.data.shortForm + "</div>";
                var type = getUrlType(suggestion.data.type);

                if (type == 'ontology') {
                  objectTypeHtml = "<div class='ontology-source'>" + suggestion.data.prefix + "</div>"
                }

                return "<div style='width: 100%; display: table;'> <div style='display: table-row'><div  style='display: table-cell;' class='ontology-suggest'><div class='suggestion-value'>" + label + "</div>" + extra + "</div><div style='vertical-align:middle; text-align: right; width:60px; display: table-cell;'>" + objectTypeHtml + "</div></div></div>";

                //Handlebars.compile('<div><strong>{{value}}</strong> – {{data.ontology}}</div>');

              },
              footer:  Handlebars.compile('<hr/><div style="text-align: right;" class="tt-suggestion tt-selectable">Search OLS for <b>{{query}}</b></div>')

            }
          }
      ).focus()
});

// constructs the suggestion engine
