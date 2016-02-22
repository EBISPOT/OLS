var dateBefore;
var dateAfter;
var ontologyName;
var name='';
var status='pie';
var date='';
var URL='';


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

    //Harccoded URL for development
    var tmpURL="http://snarf.ebi.ac.uk:9080/changes-api/changes/search/findByOntologyNameAndChangeSubjectUri?ontologyName=go&subject=http://purl.obolibrary.org/obo/GO_1902944"



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


          for (i=0;i<obj.length;i++)
          {
            htmlString+='<table>'
            htmlString+='<tr><td>changeName: </td><td>'+obj[i]["changeName"]+'</td></tr>'
            htmlString+='<tr><td>changeDate: </td><td>'+obj[i]["changeDate"]+'</td></tr>'
            htmlString+='<tr><td>changeIRI: </td><td>'+obj[i]["changeSubjectUri"]+'</td></tr>'

            /*
            $("#diachron-wrapper").append("changeName: "+obj[i]["changeName"]+"<br>")
            $("#diachron-wrapper").append("changeDate: "+obj[i]["changeDate"]+"<br>")
            $("#diachron-wrapper").append("changeIRI: "+obj[i]["changeSubjectUri"]+"<br>")
            */


          var keys=_.keys(obj[i].changeProperties)
          if (obj[i].changeProperties!==null)
          {
          var f;
          //Add line for every property in key
          for (f=0;f<keys.length;f++)
            {
              htmlString+='<tr><td>'+keys[f]+'</td><td>'+obj[i].changeProperties[keys[f]]+'</td></tr>'
            }
          }
          //htmlString+='<tr><td>&nbsp;</td><td>&nbsp;</td></tr>'
          htmlString+='</table>'
        }

        $("#diachron-wrapper").html(htmlString)

        sortTable(obj)

        $("#diachron-wrapper").append("<div id='graph'></div>");
        var data=delieverBubbleChartData(obj)
        console.log(data);
        bubble('graph', data)
      }


    })

})

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
          tmpDate=tmpDate.getFullYear()*/

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
  }









var tmpdata={
  "_embedded" : {
    "changes" : [ {
      "changeDate" : "2016-02-09T01:00:00.000+0000",
      "ontologyName" : "go",
      "changeName" : "ADD CLASS",
      "changeSubjectUri" : "http://purl.obolibrary.org/obo/GO_1904945",
      "changeProperties" : null,
      "_links" : {
        "self" : {
          "href" : "http://snarf.ebi.ac.uk:9080/changes-api/changes/56bb4889eea9a7219a2151fd"
        },
        "change" : {
          "href" : "http://snarf.ebi.ac.uk:9080/changes-api/changes/56bb4889eea9a7219a2151fd"
        }
      }
    },
    {
      "changeDate" : "2016-01-09T01:00:00.000+0000",
      "ontologyName" : "go",
      "changeName" : "ADD CLASS",
      "changeSubjectUri" : "http://purl.obolibrary.org/obo/GO_1904945",
      "changeProperties" : null,
      "_links" : {
        "self" : {
          "href" : "http://snarf.ebi.ac.uk:9080/changes-api/changes/56bb4889eea9a7219a2151fd"
        },
        "change" : {
          "href" : "http://snarf.ebi.ac.uk:9080/changes-api/changes/56bb4889eea9a7219a2151fd"
        }
      }
    },
    {
      "changeDate" : "2016-01-19T01:00:00.000+0000",
      "ontologyName" : "go",
      "changeName" : "ADD CLASS",
      "changeSubjectUri" : "http://purl.obolibrary.org/obo/GO_1904945",
      "changeProperties" : null,
      "_links" : {
        "self" : {
          "href" : "http://snarf.ebi.ac.uk:9080/changes-api/changes/56bb4889eea9a7219a2151fd"
        },
        "change" : {
          "href" : "http://snarf.ebi.ac.uk:9080/changes-api/changes/56bb4889eea9a7219a2151fd"
        }
      }
    },
    {
      "changeDate" : "2016-01-29T01:00:00.000+0000",
      "ontologyName" : "go",
      "changeName" : "DELETE CLASS",
      "changeSubjectUri" : "http://purl.obolibrary.org/obo/GO_1904945",
      "changeProperties" : null,
      "_links" : {
        "self" : {
          "href" : "http://snarf.ebi.ac.uk:9080/changes-api/changes/56bb4889eea9a7219a2151fd"
        },
        "change" : {
          "href" : "http://snarf.ebi.ac.uk:9080/changes-api/changes/56bb4889eea9a7219a2151fd"
        }
      }
    },
     {
      "changeDate" : "2016-02-09T01:00:00.000+0000",
      "ontologyName" : "go",
      "changeName" : "Mark as Obsolete",
      "changeSubjectUri" : "http://purl.obolibrary.org/obo/GO_1904945",
      "changeProperties" : null,
      "_links" : {
        "self" : {
          "href" : "http://snarf.ebi.ac.uk:9080/changes-api/changes/56bb4889eea9a7219a215200"
        },
        "change" : {
          "href" : "http://snarf.ebi.ac.uk:9080/changes-api/changes/56bb4889eea9a7219a215200"
        }
      }
    } ]
  },
  "_links" : {
    "self" : {
      "href" : "http://snarf.ebi.ac.uk:9080/changes-api/changes/search/findByOntologyNameAndChangeSubjectUri?ontologyName=go&subject=http://purl.obolibrary.org/obo/GO_1904945"
    }
  },
  "page" : {
    "size" : 20,
    "totalElements" : 2,
    "totalPages" : 1,
    "number" : 0
  }
}
