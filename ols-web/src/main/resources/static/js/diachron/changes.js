var dateBefore;
var dateAfter;
var ontologyName;
var name='';
var date='';
var URL='';


var colorObject={
"ADD CLASS": "#66bd63",
"ADD LABEL":"#a6d96a",
"Add Synonym":"#d9ef8b",
"Add Definition":"#ffffbf",
"Mark as Obsolete":"#fee08b",
"Delete Definition":"#fdae61",
"Delete Synonym":"#f46d43",
"DELETE LABEL":"#d73027",
"DELETE CLASS":"#a50026"
}


//tmp function!
function constructURL(urlToProcess){
  var tmp=urlToProcess.slice(0,urlToProcess.indexOf("ols"))
  tmp+="spot/dino/changes-api/"
  return tmp;
}

function hideLegend(){

  $("#LegendDiv").fadeOut(200, function() {
  $("#term_info_box").fadeIn();
  $("#term_relation_box").fadeIn();
  });


  //  $("#term_info_box").show()
  //  $("#term_relation_box").show()
  //  $("#LegendDiv").hide();
}


$(document).ready(function() {

  $("#tree-link").on('click', hideLegend)
  //$("#meta-link").on('click', hideLegend) // not existing anymore

    ontologyName = $("#diachron-tab").data("olsontology");
    var serviceURL= $("#diachron-tab").data("selectpath");

    ontologyName =   $( "div[data-olswidget='tree']" ).data("olsontology");
    var termType = getUrlType(  $( "div[data-olswidget='tree']" ).data("ols-termtype"));
    var termIri =   $( "div[data-olswidget='tree']" ).data("ols-iri");

    URL=constructURL(document.URL)

    //URL for later on
    var tmpURL=URL+"changes/search/findByOntologyNameAndChangeSubjectUri?ontologyName="+ontologyName+"&subject="+termIri

    //TMP DEV
    var searchbar='<div style="text-align: center;" id="searching"><img th:src="@{../../img/loading1.gif}" src="../../img/loading1.gif" alt="Search loading..."/><span> Loading, please wait... </span></div>'
    $("#diachron-wrapper").html(searchbar);


   //Load data when the diachron-link is clicked (lazy loading)
    $("#diachron-link").on('click', function(){

    $.getJSON(tmpURL, function(obj){})
    .fail(function(){
      console.log("Failed to do webservice call!"); console.log(tmpURL);
      $("#searching").hide();
      $("#diachron-wrapper").html("<h3>Sorry, failed to call webservice. </h3>Maybe the server is down. Check the console for additional information.")
      return null
  })
    .done(function(obj){

      obj=obj["_embedded"]["changes"]


      //$("#term_info_box").fadeOut()
      //$("#term_relation_box").fadeOut()
      //$("#term_info_box").hide()
      //$("#term_relation_box").hide()


      if ($(document).find("#LegendDiv").length === 0)
        {
            buildLegend(); }

         $("#term_info_box").fadeOut()
         $("#term_relation_box").fadeOut(200, function(){
         $("#LegendDiv").fadeIn()
         })


      if (obj.length===0)
      {
            $("#diachron-wrapper").html("<h3>There is no history for this term to display</h3>")
      }
      else
        {
          var i;
          var htmlString='';

          htmlString+='<table id="testTable" class="display" cellspacing="0" width="100%">'
          htmlString+='<thead><tr><th>date</th><th>change</th><th>Info</th><th></th></tr></thead>'

          for (i=0;i<obj.length;i++)
          {

            var date=moment(obj[i]["changeDate"]).format('YYYY-MM-DD')
            htmlString+='<tr><td>'+date+'</td><td>'+obj[i]["changeName"]+'</td>'

          var keys=_.keys(obj[i].changeProperties)
          if (obj[i].changeProperties!==null)
          {
          var f;
          //Add line for every property in key
          htmlString+='<td>'

          for (f=0;f<keys.length;f++)
            {
              if (!keys[f].startsWith("predicate"))
                {
                  var tmpPropList=obj[i].changeProperties[keys[f]]

                  if (tmpPropList.length>1){
                    htmlString+='<strong>'+keys[f]+':</strong><br>'
                    for (tmpi=0;tmpi<tmpPropList.length;tmpi++)
                      {
                        htmlString+='- '+tmpPropList[tmpi]+'<br>'
                      }
                  }


                  if (tmpPropList.length===1){
                    htmlString+='<strong>'+keys[f]+':</strong> '+obj[i].changeProperties[keys[f]]+'<br>'
                  }
                }
          }

            htmlString+='</td><td bgcolor="'+colorObject[obj[i].changeName]+'"></td></tr>'
          }

          else
          {
            htmlString+='<td></td></tr>'
          }

        }
          htmlString+='</table>'

        $("#searching").hide();
        $("#diachron-wrapper").html(htmlString)
        $("#testTable").DataTable({
          "aoColumns" :[
            false,
            false,
            false,
            {"bSortable":false}
          ]

        });
      }


    })

  })

})


function buildLegend(){
  var keys=_.keys(colorObject)
  var htmlString='<div id="LegendDiv" class="panel panel-primary"><div class="panel-heading"><h3 class="panel-title">Legend</h3></div><div id="LegendBody" class="panel-body">'
  htmlString+="<table>";

  for (var i=0; keys.length>i; i++)
  {
    htmlString+='<tr><td>'+keys[i]+'</td><td bgcolor="'+colorObject[keys[i]]+'"></td></tr>'
  }
  htmlString+="</table></div></div>"
  $("#right_info_box").append(htmlString)
  $("#LegendDiv").hide();
}
