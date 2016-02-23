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
  console.log(urlToProcess);
  var tmp=urlToProcess.slice(0,urlToProcess.indexOf("uk:"))
  console.log(tmp);
  tmp+="uk:9080/changes-api/"
  console.log(tmp);
  return tmp;
}



$(document).ready(function() {
    ontologyName = $("#diachron-tab").data("olsontology");
    var serviceURL= $("#diachron-tab").data("selectpath");


    console.log("construction of the URL");
    console.log(document.URL);


    ontologyName =   $( "div[data-olswidget='tree']" ).data("olsontology");
    var termType = getUrlType(  $( "div[data-olswidget='tree']" ).data("ols-termtype"));
    var termIri =   $( "div[data-olswidget='tree']" ).data("ols-iri");


    console.log("Init tree for: " + ontologyName + " - " + termIri + " - " + termType);

    URL=constructURL(document.URL)


    //URL for static dev
    //var tmpURL="http://www.ebi.ac.uk/ols/beta/api/ontologies?page=1&size=1"

    //URL for later on
    var tmpURL=URL+"changes/search/findByOntologyNameAndChangeSubjectUri?ontologyName="+ontologyName+"&subject="+termIri



    $.getJSON(tmpURL, function(obj){})
    .fail(function(){   console.log("Failed to do webservice call!"); console.log(tmpURL); return null })
    .done(function(obj){
      console.log(obj);

      obj=obj["_embedded"]["changes"]


      //Only need for static dev
      //obj=tmpdata["_embedded"]["changes"] //tmp development

      if (obj.length===0)
      {
            $("#diachron-wrapper").html("<h3>There is no history for this term to display</h3>")
      }
      else
        {
          var i;
          var htmlString='';

          htmlString+='<table id="termtable" class="display" cellspacing="0" width="100%">'
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

        //console.log(htmlString);
        $("#diachron-wrapper").html(htmlString)
        $("#termtable").DataTable({
          "aoColumns" :[
            false,
            false,
            false,
            {"bSortable":false}
          ]

        });


        /*All the things below might go away - maybe I can do something with the chart, maybe not*/
        /*sortTable(obj)
        $("#diachron-wrapper").append("<div id='graph'></div>");
        var data=delieverBubbleChartData(obj)
        console.log(data);
        bubble('graph', data)*/

      }


    })

})

/*
function sortTable(obj)
{

  var htmlString="<table id='testTable' class='tablesorter'><thead><tr>"
  htmlString+="<th>changeDate</th><th>changeName</th></tr></thead>"
  htmlString+="<tbody>"
  for (i=0;i<obj.length;i++)
  {
  htmlString+="<tr><td>"+obj[i]["changeDate"]+"</td>"
  htmlString+="<td>"+obj[i]["changeName"]+"</td>"


  htmlString+="</tr>"
  }

  htmlString+="</tbody></table>"

  $("#diachron-wrapper").append(htmlString);
  $("#testTable").tablesorter()
}




/*
function delieverBubbleChartData(obj)
    //{ x: 95, y: 95, z: 13.8, name: 'BE', country: 'Belgium' }
    {
      console.log("in deliever Bubble Chart");
      console.log(obj);

      var returndata=[];
      for (var i=0;obj.length>i;i++)
      {

        var tmpDate=new Date(obj[i].changeDate)
        console.log(obj[i].changeDate);
        console.log(tmpDate);

        /*
        if (option==="day")
          tmpDate=tmpDate.getDay()
        if (option==="month")
          tmpDate=tmpDate.getMonth()
        if (option==="year")
          tmpDate=tmpDate.getFullYear()* /

        returndata.push({"x": tmpDate, "y":0, "z": 3, "name":obj[i].changeName, "info":obj[i].changeSubjectUri })
      }

      console.log("After processing data");
      console.log(returndata);
      return returndata;
    }




   function bubble(divname, Rdata){

        //Static dev
        //var Rdata=delieverBubbleChartData(tmpdata["_embedded"]["changes"], "month");

        console.log("Data before drawing");
        console.log(Rdata);
        var chartoptions = {
          chart:{
            type: 'bubble',
            zoomtype: 'xy'
          },
          title: {text: 'Bubble for changesummaryjson'},
          plotOptions: {
            bubble: {
                dataLabels: {
                    enabled: true,
                    format: '{point.name}'
                }
            }
        },
        tooltip:{  pointFormat:'{point.info}'},
    series: [{"data":Rdata}]
        };


        Highcharts.chart(divname, chartoptions);
  }*/
