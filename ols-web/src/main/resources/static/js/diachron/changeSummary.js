var dateBefore;
var dateAfter;
var ontologyName;
var name='';
var status='line';
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
var tmp=urlToProcess.slice(0, urlToProcess.indexOf("ols"))
tmp+="spot/dino/changes-api/"
return tmp;
}


$(document).ready(function() {
//Register onclicks for other tab links to hide the Legend when leaving ontology history
$("#tree-link").on('click', hideLegend)
$("#property-link").on('click', hideLegend)
$("#meta-link").on('click', hideLegend)

$("#diachron-link").on('click', function(){

    ontologyName = $("#diachron-tab").data("olsontology");
    var serviceURL= $("#diachron-tab").data("selectpath");

    URL=constructURL(document.URL)
    //hardcoded
    //URL=constructURL("http://www.ebi.ac.uk/ols/beta")

    date=new Date();
    dateBefore=date.getFullYear()+'-'+(date.getMonth()+1)+'-'+date.getDate();
    //Default date if webservice call is not successful
    dateAfter="2015-01-01"

  /*  THIS is how we can get DATES later on, when there are MORE changes*/
    var dateSize=10;
    var DateURL=URL+"changesummaries/search/dates?size="+dateSize+"&ontologyName="+ontologyName
    $.getJSON(DateURL, function(obj){})
      .fail(function(){console.log("Failed to do webservice call! Please try again later or make sure the "+DateURL+" exists!");
      $("#searching").hide();
      $("#diachron-wrapper").html("<h3>Sorry, failed to call webservice. </h3>Maybe the server is down. Check the console for additional information.")
       return null
     })
      .done(function(obj){
      if (obj.length!==0)
        dateAfter=obj[obj.length-1]["changeDate"]

        //Append general structur of window
        $("#diachron-wrapper").append("<div id='graphpart'><div>")
        $("#diachron-wrapper").append("<div id='buttonBox' style='text-align:center; margin-top:20px;'><div>")
        //Append datepicker

          lineChartData();

          if ($(document).find("#LegendDiv").length === 0)
          {    buildLegend(); }

          $("#ontology_info_box").fadeOut(200, function(){
          $("#LegendDiv").fadeIn();
          });

    })
  })
})


function hideLegend(){
    //$("#LegendDiv").hide();
    $("#LegendDiv").fadeOut(200, function() {
    $("#ontology_info_box").fadeIn();
    });
}

function buildLegend(){
  var keys=_.keys(colorObject)
  var htmlString='<div id="LegendDiv" class="panel panel-primary"><div class="panel-heading"><h3 class="panel-title">Legend</h3></div><div id="LegendBody" class="panel-body">'
  htmlString+="<table>";

  for (var i=0; keys.length>i; i++)
  {
    htmlString+='<tr><td>'+keys[i]+'</td><td bgcolor="'+colorObject[keys[i]]+'"></td></tr>'
  }
  htmlString+="</table></div></div>"
  $("#right_info_box").append(htmlString);
  $("#LegendDiv").hide();
}


function createpiechart(divname, date, data){
  var title="Changes for "+date

  var chartoptions={
      chart: {
         type: 'pie',
     },
     credits:{enabled:false},
     title: {
         text: title
     },
     series:[{
        name:"Total per type",
        data: data,
        point:{events: {click: function(e){
          $("#buttonBox").empty(); //Try to get rid of it
          detailDateView(divname, date, this.name)
        }}}
    }],

    exporting:{
        buttons:{
          customButton:{
            x:0,
            y:0,
            onclick : function(){
                $("#"+divname).highcharts().destroy();
                drawPieTable(divname, data, date)
            },
              text: "Show datatable",
              _titleKey : "tooltip",
            },
            contextButton: {
                enabled: true,
                x:0,
                y:26
              }
}},
    lang: { noData: "No data to display", tooltip: "Show the data of the piechart as table"},
    noData: {
      style: {
         fontWeight: 'bold',
         fontSize: '15px',
         color: '#303030'
        }
      }
   };

   Highcharts.chart(divname, chartoptions)

   var htmlString="<div style='text-align:center; margin-top:25px;'><button id='back' class='primary'> Back </button></div>"
   $("#buttonBox").html(htmlString)
   $("#back").on('click', function(){lineChartData()})
}



function piechartview(divname, date){

  //Transform Date to two dates (before and after day)
  var x=new Date(date);
  var tmpdateafter=x.getFullYear()+'-'+(x.getMonth()+1)+'-'+(x.getDate());
  var tmpdatebefore=x.getFullYear()+'-'+(x.getMonth()+1)+'-'+(x.getDate());

  /*
  var momentdate=moment(tmpdateafter);
  XXtmpdateafter=momentdate.subtract(1,'days');
  XXtmpdatebefore=momentdate.add(1,'days');

  console.log("Datestuff: ");
  console.log(tmpdateafter);
  console.log(tmpdatebefore);
  console.log(XXtmpdateafter);
  console.log(XXtmpdatebefore);
  console.log(XXtmpdateafter.format('YYYY-MM-DD'));
  console.log(XXtmpdatebefore.format('YYYY-MM-DD'));*/


  //Using between
  var tmpURL=URL+"changesummaries/search/findByOntologyNameAndChangeDateBetween";
  tmpURL=tmpURL+"?ontologyName="+ontologyName+"&after="+tmpdateafter+"&before="+tmpdatebefore

  //Using the exact endpoint



  $.getJSON(tmpURL, function(obj){})
  .fail(function(){   console.log("Failed to do webservice call!"); console.log(tmpURL); return null })
  .done(function(obj){
    var tmp=[];
    var value=[];

    for (var i=0; obj.length>i; i++)
    {
        if (! _.contains(tmp, obj[i].changeName))
          {
            tmp.push(obj[i].changeName)
            value.push(obj[i].count)

          }
        else {
            var index=_.indexOf(tmp,obj[i].changeName);
            value[index]=value[index]+obj[i].count;
        }
    }

    var data=[]
    for (var i=0;tmp.length>i ;i++)
        {

          data[i]={"name":tmp[i], "y":value[i], "color": colorObject[tmp[i]] }    }

    createpiechart(divname, date, data)
    })
}



function drawPieTable(divname, data, date)
{
      var i;
      var htmlString='';
      // Count the sum quickly
      var sum=0;
      for (i=0;i<data.length;i++)
      {
        sum+=data[i].y
      }

      htmlString+="<h3>Summary for '"+date+"</h3>"
      htmlString+='<table id="testTable" class="display" cellspacing="0" width="100%">'
      htmlString+="<thead><tr><th>Change Name</th><th>Total</th><th>Percentage</th></tr></thead>"
      htmlString+="<tbody>"
      for (i=0;i<data.length;i++)
      {
        htmlString+="<tr><td>"+data[i].name+"</td><td>"+data[i].y+"</td><td>"+Math.round(data[i].y/sum*100)+"</td></tr>"
      }
      htmlString+="</tbody></table>"

      $("#"+divname).append(htmlString)

      htmlString="<div style='text-align:center'><button id='back' class='primary'> Back </button></div>"
      $("#buttonBox").html(htmlString)
      $("#back").on('click', function(){piechartview(divname,date)})
      $("#testTable").DataTable({"order" : [[2, "desc"]]})
}


function drawLineTable(divname, obj)
{
  var htmlString='';
  htmlString+='<h3>Summary of all changes from</h3>';
  htmlString+='<table id="testTable" class="display" cellspacing="0" width="100%">'
  htmlString+='<thead><tr><th>ChangeType</th><th>Date</th><th>Number of changes</th></tr></thead>'
  htmlString+='<tbody>'

  for (var j=0;obj.series.length>j;j++)
    {
    for (var i=0; obj.categories.length>i;i++){
    htmlString+="<tr><td>"+obj.series[j].name+"</td><td>"+obj.categories[i]+"</td><td>"+obj.series[j].data[i]+"</td></tr>"
    }
  }

  htmlString+="</tbody></table>"

  $("#"+divname).html(htmlString)

  htmlString="<div style='text-align:center'><button id='back' class='primary'> Back </button></div>"
  $("#buttonBox").html(htmlString)
  $("#back").on('click', function(){linechart(divname, obj)})

  $("#testTable").DataTable({"order" : [[1, "desc"]]})
}


function parseResult(obj){
  var categories=[];
  var tmpdata=[];
  var data=[];

  var keys=_.keys(colorObject)

  //Fill the categories array with all potential dates we can find in the object
  for (var i=0;i<obj.length;i++)
  {
      var tmp=obj[i].changeDate;
      if (! _.contains(categories, tmp))
        {
          categories.push(tmp)
        }
  }

  //Create an empty data opject with the length of categories, filled with 0s
  var data=[];
  for (var i=0;i<categories.length;i++)
  {      data.push(0) }


  //Get all the keys as they represent all potential names in the object
  for (var i=0;i<keys.length;i++)
  {      tmpdata.push({"name": keys[i], "color": colorObject[keys[i]], "data":data.slice(0)})  }  //every tmpdata is supposed to get his own data array filled with 0

  //Go through the object, and save count at the position of the array corresponding to the position of changeDate in the categories array
  for (var i=0;i<obj.length;i++)
  {
        var index=stringCompare(categories, obj[i].changeDate)    //Find the index of the object data in the categories
        var tmpentry=_.findWhere(tmpdata, {"name":obj[i].changeName}) //Find the object with the right name
        if (tmpentry!=undefined && index!=-1)
          {
            tmpentry.data[index]=obj[i].count;    //save the value/count at the right position in the data array of the object
          }
 }


//Check if a series is empty and if so, remove it (=don't add it to the controll[])
  var control=[];

  for (var i=0; i<tmpdata.length; i++)
  {
    var sum=0;
    for (var j=0;j<tmpdata[i].data.length;j++)
      {
        sum+=tmpdata[i].data[j]
      }

      //If the summ after the for loop is not 0, we add the series to the control[]
      if (sum!==0)
      {
        control.push(tmpdata[i])
      }
  }
  var returndata={"categories": categories, "series": control}
  return returndata
}


//Look for the index of b in the array a, return the index once found - this function is a helper function for the parse function
function stringCompare(a,b){
  for (var i=0;i<a.length;i++)
    {
        if (a[i]===b)
          {
            return i
          }
    }
    return -1
}


function lineChartData(){
    var tmpURL=URL+"changesummaries/search/findByOntologyNameAndChangeDateBetween";

    //Construction the URL dynamically
    tmpURL=tmpURL+"?ontologyName="+ontologyName+"&before="+dateBefore+"&after="+dateAfter;

    $.getJSON(tmpURL, function(obj){})
    .fail(function(){console.log("Failed to do webservice call!"); return null})
    .done(function(obj){

      var returndata=parseResult(obj);
      linechart("graphpart",returndata);
    })
}

linechart = function(divname, returndata)
{
    $("#buttonBox").empty()
    var title= "All changes between "+ dateAfter+" and "+dateBefore;
    /* Now designing the chart */
    var chartoptions={
      chart: {
         type: 'line'
     },
     exporting: { enabled: false },
     credits:{enabled:false},
     title: {
         text: title
     },
     xAxis: {
         categories: returndata.categories,
         labels: {
           useHTML: true,
           formatter: function(){
             return '<p class="link">'+this.value+'<p>';
           }
         }
     },
      yAxis: {
          title: {
              text: 'Number of changes'
           }
       },
       plotOptions:{
         series:{
           cursor: 'pointer',
        point: {
             events:{
               click: function(){
                    date=this.category;
                    $("#datepicker").empty();
                    detailDateView(divname,this.category, this.series.name)
              }
          }}}
        },
     series:returndata.series,
     exporting: {
      buttons:{
      customButton:{
        x:0,
        y:0,
        onclick : function (){

          $("#"+divname).highcharts().destroy();
          drawLineTable(divname, returndata)
        },
        text: "Show datatable",
        _titleKey : "tooltip",
      },
      contextButton: {
          enabled: true,
          x:0,
          y:26
        }
      }},
     lang: { noData: "No data to display",  tooltip: "Show the data of the linechart as table"},
     noData: {
       style: {
          fontWeight: 'bold',
          fontSize: '15px',
          color: '#303030'
         }
       }
    };

       Highcharts.chart(divname, chartoptions)

       /* Register onclick event for the xAxis*/
       $('.link').on('click', function () {
         $("#datepicker").empty();
         piechartview(divname, $(this).text())
       });

       $('.highcharts-legend-item').on('click', function(){
        //IF A Legend Item is clicked, we have to re-register the onclick event
         $('.link').on('click', function () {
           $("#datepicker").empty();
           piechartview(divname, $(this).text())
          })
        })
        /*End of registering onclick events for XAxis elements*/

       //Append datepicker to the linechartview
       $("#"+divname).append("<div id='datepicker' style='text-align:center;'></div>")
       $("#datepicker").html('<hr style="width:30%; margin-left:35%">Show data from <input type="text" size="9" id="dateAfter" value="'+dateAfter+'"> to <input type="text" size="9" id="dateBefore" value="'+dateBefore+'"> <input id="changeDateSubmit" class="btn  btn-default" type="submit" value="Update Data!" title="Adjust the dates on the left to the timeframe you want to compare and hit this update button to display the accurant data">')

       var pickerAfter = new Pikaday(
       {
           field: document.getElementById('dateAfter'),
           format: 'YYYY-MM-DD',
           firstDay: 1,
           minDate: new Date(2000, 0, 1),
           maxDate: new Date(2020, 12, 31),
           yearRange: [2000, 2020],
           bound: true,
           container: document.getElementById('container'),
       });

       var pickerBefore = new Pikaday(
       {
           field: document.getElementById('dateBefore'),
           format: 'YYYY-MM-DD',
           firstDay: 1,
           minDate: new Date(2000, 0, 1),
           maxDate: new Date(2020, 12, 31),
           yearRange: [2000, 2020],
           bound: true,
           container: document.getElementById('container'),
       });


       //Onclick event for the getDataButton
       $("#changeDateSubmit").on('click', function(e){
         e.preventDefault();
         dateBefore=$("#dateBefore").val()
         dateAfter=$("#dateAfter").val()
         //update();
         lineChartData()
       })

}


var masterdata=[];
var OLSterms=[];
function callWebserviceForDateView(inputURL){
  var token=$.Deferred();

  $.getJSON(inputURL, function(obj){})
  .fail(function(){console.log("Failed to do webservice call!"); return null})
  .done(function(obj){

    var i;
    obj=obj["_embedded"]["changes"]

      for (i=0;i<obj.length;i++){
        masterdata.push({"changeName": obj[i].changeName, "changeSubjectURI": obj[i].changeSubjectUri,"changeProperties": obj[i].changeProperties})
      }

      token.resolve();
  })
  return token;
}


var tableData=[];
function detailDateView(divname, date, className){
  var searchbar='<div style="text-align: center;" id="searching"><img th:src="@{../img/loading1.gif}" src="../img/loading1.gif" alt="Search loading..."/><span> Loading, please wait... </span></div>'
  $("#"+divname).html(searchbar);

  masterdata=[];
  var htmlString='';
  if ($("#"+divname).highcharts()!==undefined)
    {$("#"+divname).highcharts().destroy();}

  //No between
  var tmpURL;
  var view='';

  //if no className is provided, then look for data by date
  if (className==="")
  {
    tmpURL=URL+"changes/search/findByOntologyNameAndChangeDate"
    tmpURL=tmpURL+"?ontologyName="+ontologyName+"&date="+date
    htmlString+="<h3 class='dataTableHeadline'>All changes for <strong>"+date+"</strong></h3>";
  }

  // if there is a className, than only fetch data for this classname at this date
else {
    tmpURL=URL+"changes/search/findByOntologyNameAndChangeNameAndChangeDate"
    tmpURL=tmpURL+"?ontologyName="+ontologyName+"&date="+date+"&changeName="+className
    htmlString+="<h3 class='dataTableHeadline'>Changes for <strong>"+className+"</strong> on the <strong>"+date+"</strong></h3>";
}

  var tokenArray=[];

  $.getJSON(tmpURL, function(obj){})
  .fail(function(){console.log("Failed to do webservice call!"); return null})
  .done(function(obj){

    var totalElements=obj["page"].totalElements;
    var totalPages=obj["page"].totalPages;

    var i;
    for (i=0;i<totalPages;i++)
    {
      pagingURL=tmpURL+"&page="+i;
      tokenArray.push(callWebserviceForDateView(pagingURL));
    }


//This is exectued after all token are resolved - which means the diachron webservice calls are finished
    $.when.apply($,tokenArray).done(function() {
          tableData=ConstructDataTable(masterdata)
          /*This part is for the second asynchronious webservice call round!*/
          var tokenArrayTwo=[]
          for (var i=0;i<tableData.length;i++)
          {
          tokenArrayTwo.push(callOLSforLabel(tableData[i].id))
          }

          //This is executed after all OLS calls for our results from diachron are finshed - now I finally can build a table with all information that I need
          $.when.apply($,tokenArrayTwo).done(function(){
                      var htmlString2=''

                      htmlString2+='<table id="testTable" class="display" cellspacing="0" width="100%">'
                      htmlString2+='<thead><tr><th>id</th><th>Label</th><th># and types of changes</th><th></th></tr></thead>'
                      htmlString2+='<tbody>'

                      var baseUrl=document.URL;
                      baseUrl+="/terms?iri=";

                      //Go through every entry within the tableData
                      for (var i=0;i<tableData.length;i++)
                      {
                        var tmpchangetypes=[];
                        var tmpchangefield=tableData[i].changes

                        //Go through all changes of every object and save the changename, so it can be displayed in the main table (for search)
                          for (var tcounter=0; tmpchangefield.length>tcounter; tcounter++)
                            {

                                // Only push the changeName if it's not already in the array, we want unique terms
                              if (! _.contains(tmpchangetypes, tmpchangefield[tcounter].changeName))
                              {
                                tmpchangetypes.push(tmpchangefield[tcounter].changeName)
                              }
                            }


                        // Usually, the first case will be true and be executed. This adds a row in the datatable
                          if (tableData[i].Label !== "Deleted Class")
                          {
                          htmlString2+='<tr id="'+i+'" class="mainrow"><td><small><a href="'+baseUrl+encodeURIComponent(tableData[i].id)+'">'+tableData[i].id+'</a></small></td><td>'+tableData[i].Label+'</td><td>'+tableData[i].changes.length+' - <small>'+tmpchangetypes+'</small></td><td style="cursor:pointer;"><img src="../img/eye.png" alt="Click me" title="Click on a row to see more results!"/></td></tr>'
                          }
                        // The entry is about a 'Deleted Class' - that is why we might want to replace the label in the future and remove the link (because it does no exist no more!)
                        else {
                          htmlString2+='<tr id="'+i+'" class="mainrow"><td><small>'+tableData[i].id+'</small></td><td>'+tableData[i].Label+'</td><td>'+tableData[i].changes.length+' - <small>'+tmpchangetypes+'</small></td><td style="cursor:pointer;"><img src="../img/eye.png" alt="Click me" title="Click on a row to see more results!"/></td></tr>'
                        }

                      }



                      htmlString2+='</tbody></table>'



                      $("#searching").hide();
                      $("#"+divname).html(htmlString);

                      //In case we are in a partial view, we offer a button to 'Show all data' in the datatable
                      if (className!=="")
                      {
                      htmlString2+='<div style="text-align:left; margin-bottom:20px;"><button id="allData" type="button"> Show all changes </button></div>'
                      $("#"+divname).append(htmlString2);
                      $('#allData').on('click', function(){
                        $("#buttonBox").empty();
                        detailDateView(divname, date, "")
                      })

                      }
                      else {
                        $("#"+divname).append(htmlString2);
                      }



                      /* Add Button Box and event listener */
                      var button="<div style='text-align:center; margin-top:25px;'><button id='back' class='primary'> Back </button></div>"
                      $("#buttonBox").html(button)
                      $("#back").on('click', function(){lineChartData()})
                      /* Add Button BACK to the line chart in every case (Should we change that)*/


                      var table=$("#testTable").DataTable({
                        "aoColumns" : [
                          false,
                          false,
                          false,
                          {"bSortable":false}
                        ]
                      });

                      $('#testTable tbody').on('click', 'tr.mainrow', function() {
                        var tr=$(this).closest('tr')
                        var row=table.row(tr)



                        if (row.child.isShown())
                        {
                          row.child.hide();
                          tr.removeClass('shown');
                        }
                        else
                        {

                          var rowid=tr.attr('id');
                          var tmpchanges=tableData[rowid].changes;
                          var childHTML='';
                          for (var f=0;f<tmpchanges.length;f++)
                          {
                            childHTML+='<table><tr><td width="80px">Change Name</td><td>'+tmpchanges[f].changeName+'</td><td bgcolor="'+colorObject[tmpchanges[f].changeName]+'" width="20px"></td></tr>'
                            var keys=_.keys(tmpchanges[f].changeProperties)

                            if (tmpchanges[f].changeProperties!==null)
                            {
                            var counter;
                            //Add line for every property in key
                            for (counter=0;counter<keys.length;counter++)
                            {
                                  if (!keys[counter].startsWith("predicate"))
                                    {
                                      var tmpPropList=tmpchanges[f].changeProperties[keys[counter]]

                                      //If there are multiple values for a key, go through the values and print the whole array

                                      if (tmpPropList.length>1)
                                      {
                                      childHTML+='<tr><td>'+keys[counter]+'</td><td>'

                                      for (var tmpi=0;tmpi<tmpPropList.length;tmpi++)
                                        {
                                            childHTML+='- '+tmpPropList[tmpi]+'<br>'
                                        }
                                        childHTML+='</td><tr>'
                                      }

                                    //If there is only ONE Value for the key, print this value
                                    if (tmpPropList.length===1){
                                        childHTML+='<tr><td>'+keys[counter]+'</td><td>'+tmpchanges[f].changeProperties[keys[counter]]+'</td><tr>';
                                      }

                                    }

                            }

                            }

                            childHTML+='</table>'



                          }

                          row.child(childHTML).show();
                          tr.addClass('shown');
                        }


                      })
          })
          /* END of the second asynchronious calling round */
      })

  })
}




/*Calling for OLS Label*/
function callOLSforLabel(iri){

    var token=$.Deferred();

    var OLSurl=document.URL.slice(0, document.URL.indexOf("ontologies"))
    OLSurl=OLSurl+"api/ontologies/"+ontologyName+"/terms?iri="+iri

    //hardcoded
    //var OLSurl="http://www.ebi.ac.uk/ols/beta/"+"api/ontologies/"+ontologyName+"/terms?iri="+iri

  $.getJSON(OLSurl, function(olsdata){})
    .fail(function(event, jqxhr, exception){

      //console.log("Failed with these params");
      //console.log(event);
      //console.log(event.status);

    /*If calling Ols for the label and getting 404 back, means that we are looking for a deleted Class
    Since we won't find a delete class in OLS, we need to catch this case and work around it. */
   if (event.status === 404){
      var tmp=_.findWhere(tableData, {"id":iri})
      tmp.Label="Deleted Class";
      }
    /* In case that we have another error that 404, we still display the error message*/
    else{
      console.log("Failed to do webservice call! Please try again later or make sure the "+OLSurl+" exists!");
      $("#searching").hide();
      $("#diachron-wrapper").html("<h3>Sorry, failed to call webservice. </h3>Maybe the server is down. Check the console for additional information.")
    }
      return token.resolve();
    })
    .done(function(olsdata){

      olsdata=olsdata["_embedded"]["terms"]
      var label=olsdata[0].label;
      var tmp=_.findWhere(tableData, {"id":iri})
      tmp.Label=label


    return token.resolve();
  })

return token;
}
/**/


/* Construction the Data for the data table */
function ConstructDataTable(masterdata){

  var dataObject=[];

  var i;
  var tmpcounter=0;
  for (i=0;i<masterdata.length;i++){

    var tmp=masterdata[i].changeSubjectURI

    var index=_.findIndex(dataObject, {"id":tmp})
    var tmpChanges;

    if (index=== -1){
              tmpChanges={"changeName": masterdata[i].changeName, "changeProperties": masterdata[i].changeProperties}
              dataObject.push({"id":tmp, "Label": "-", "changes":[tmpChanges]} );
      }

    else {
        tmpcounter++;

        var tmpProps=dataObject[index].changes;
        tmpChanges={"changeName": masterdata[i].changeName, "changeProperties": masterdata[i].changeProperties}

        tmpProps.push(tmpChanges);
        dataObject[index].changes=tmpProps;
      }
    }

   return dataObject;
}
