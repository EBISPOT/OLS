var dateBefore;
var dateAfter;
var ontologyName;
var name='';
var status='line';
var date='';
var URL='';
//var semaphore=false;
//var showDatepicker=true;

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
var tmp=urlToProcess.slice(0,urlToProcess.indexOf("ols")+3)
console.log(tmp);
tmp+="/diachron/changes-api/"
console.log(tmp);
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
    console.log("construction of the URL");
    console.log(document.URL);
    console.log(serviceURL);
    console.log(ontologyName);


    URL=constructURL(document.URL)


    date=new Date();
    dateBefore=date.getFullYear()+'-'+(date.getMonth()+1)+'-'+date.getDate();
    //Default date if webservice call is not successful
    dateAfter="2015-01-01"

  /*  THIS is how we can get DATES later on, when there are MORE changes*/
    var dateSize=10;
    var DateURL=URL+"changesummaries/search/dates?size="+dateSize+"&ontologyName="+ontologyName
    $.getJSON(DateURL, function(obj){})
      .fail(function(){console.log("Failed to do webservice call! Please try again later or make sure the "+DateURL+" exists!"); return null})
      .done(function(obj){
      if (obj.length!==0)
        dateAfter=obj[obj.length-1]["changeDate"]

        //Append general structur of window
        $("#diachron-wrapper").append("<div id='graphpart'><div>")
        $("#diachron-wrapper").append("<div id='datepicker' style='text-align:center;'><div>")
        $("#diachron-wrapper").append("<div id='optionfield' style='text-align:center; margin-top:20px;'><div>")
        //Append datepicker

        $("#datepicker").html('Show data from <input type="text" size="9" id="dateAfter" value="'+dateAfter+'"> to <input type="text" size="9" id="dateBefore" value="'+dateBefore+'"> <input id="changeDateSubmit" type="submit" value="Update Data!">')
        $("#optionfield").html('<hr><button id="overTime" class="primary" type="button">Show data over time as linechart</button> <button id="pieChart" class="primary" type="button">Show summary as pie chart</button>');

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
          update();
        })

        $("#pieChart").on('click', function() {
              status="pie";
              $('#datepicker').show();
              pieChartData();})

        $("#overTime").on('click', function() {
              status="line";
              $('#datepicker').show();
              lineChartData()
        })


          update();

          if ($(document).find("#LegendDiv").length === 0)
          {    buildLegend(); }
          else {              $("#LegendDiv").show();     }


    })
  })
})



function hideLegend(){
    $("#LegendDiv").hide();
}

function update(){
  if (status==="pie")
    pieChartData();
  if (status==="line")
    lineChartData();
  if(status==="bar")
    BarChart("graphpart", name);

  //if(status==="detail")
    //updatedetailview("graphpart", name, date);  // DOES THIS MAKE SENSE? SO FAR, DETAILVIEW IS ONE DATE
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
  console.log(htmlString);
  $("#right_info_box").append(htmlString)
}

function pieChartData(){
    var tmpURL=URL+"changesummaries/search/findByOntologyNameAndChangeDateBetween";
    tmpURL=tmpURL+"?ontologyName="+ontologyName+"&before="+dateBefore+"&after="+dateAfter;

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
            console.log(tmp[i])
            console.log(colorObject[tmp[i]]);
            data[i]={"name":tmp[i], "y":value[i], "color": colorObject[tmp[i]] }    }

          console.log(data);
        piechart("graphpart", data);
    })
  }



function piechart(divname, data){
        var title="Changes between "+dateAfter+" and "+dateBefore

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
              point:{events: {click: function(e){name=this.name;BarChart(divname, this.name) }}}
          }],


          exporting:{
              buttons:{
                customButton:{
                  x:-50,
                  //menuItems: [ {text: "Show this data as table", onclick: function(){alert("123")} } ],
                  onclick : function(){

                      $("#"+divname).highcharts().destroy();
                      drawPieTable(divname, data)
                  },
                    symbol: "menu",
                    _titleKey : "tooltip",
                  },
                  contextButton: {
                      enabled: false
                    }
      }},

          lang: { noData: "No data to display", tooltip: "Now that is a tooltip"},
          noData: {
            style: {
               fontWeight: 'bold',
               fontSize: '15px',
               color: '#303030'
              }
            }
         };

         Highcharts.chart(divname, chartoptions)
}


function drawPieTable(divname, data)
{
    console.log("Imagin this to replace the chart", data);
    //  $("#"+divname).append("Well imagin a table to be here!");

      var i;
      var htmlString='';
      // Count the sum quickly
      var sum=0;
      for (i=0;i<data.length;i++)
      {
        sum+=data[i].y
      }
      console.log("And the sum of it all is "+sum);

      htmlString+="<h3>Change Summary for '"+ontologyName+"' between "+dateAfter+" and "+dateBefore+"</h3>"
      htmlString+='<table id="testTable" class="display" cellspacing="0" width="100%">'
      htmlString+="<thead><tr><th>Change Name</th><th>Total</th><th>Percentage</th></tr></thead>"
      htmlString+="<tbody>"
      for (i=0;i<data.length;i++)
      {
        htmlString+="<tr><td>"+data[i].name+"</td><td>"+data[i].y+"</td><td>"+Math.round(data[i].y/sum*100)+"</td></tr>"
      }
      htmlString+="</tbody></table>"
      console.log(htmlString);

      $("#"+divname).append(htmlString)
      $("#testTable").DataTable({"order" : [[2, "desc"]]})
}


function BarChart(divname, name){
  //$("#"+divname).highcharts().destroy();
  status="bar"

  var tmpURL=URL+"changesummaries/search/findByOntologyNameAndChangeNameAndChangeDateBetween";

  var title="Changes for type "+name+" between "+dateAfter+" and "+dateBefore

  //Construction the URL dynamically
  tmpURL=tmpURL+"?ontologyName="+ontologyName+"&changeName="+name+"&before="+dateBefore+"&after="+dateAfter;
  console.log(tmpURL);
  $.getJSON(tmpURL, function(obj){})
  .fail(function(){console.log("Failed to do webservice call!"); return null})
  .done(function(obj){

    console.log("BarChartData");
    console.log(obj);
    var data=parseResult(obj);

    console.log(data);

    var title="Distribution of "+name

    var chartoptions={
      chart: {
         type: 'column',
     },
     exporting: { enabled: false },
     credits:{enabled:false},
     title: {
         text: title
     },
     xAxis: {
         categories: data.categories
     },
      yAxis: {
          title: {
              text: title
           }
       },
       plotOptions:{
         series:{point: {
             events:{
               click: function(){
                 date=this.category;
                 detailview(divname, name, this.category);
               }
             }
           }
         }
       },
     series:data.series,
     exporting:{
         buttons:{
           customButton:{
             x:-50,
             //menuItems: [ {text: "Show this data as table", onclick: function(){alert("123")} } ],
             onclick : function(){

                 $("#"+divname).highcharts().destroy();
                 drawBarTable(divname, name, data)
             },
               symbol: "menu",
               _titleKey : "tooltip",
             },
             contextButton: {
                 enabled: false
               }
      }},

     lang: { noData: "No data to display", tootltip: "Click this button to display data as table"},
     noData: {
       style: {
          fontWeight: 'bold',
          fontSize: '15px',
          color: '#303030'
         }
       }
    };
       Highcharts.chart(divname, chartoptions)
  })
}




function drawBarTable(divname, changename, data){
  console.log("In drawBarTable", data);
  var htmlString="";
  htmlString+="<h3>Detail view for '"+ontologyName+", "+changename+" between "+dateAfter+" and "+dateBefore+"</h3>";
  htmlString+="<table id='testTable' class='display' cellspacing='0' width='100%'>"
  htmlString+="<thead><tr><th>Date</th><th>Number of changes</th></tr></thead>"
  htmlString+="<tbody>"

  var i;
  for (i=0;i<data.categories.length ;i++){
    console.log(data.categories[i]);
    console.log(data.series[0].data[i]);
  htmlString+="<tr><td>"+data.categories[i]+"</td><td>"+data.series[0].data[i]+"</td></tr>"
  }

  htmlString+="</tbody></table>"
  console.log(htmlString);
  $("#"+divname).append(htmlString)
  $("#testTable").DataTable({"order":[[1,"desc"]]})
}



function parseResult(obj){
  var categories=[];
  var tmpdata=[];
  var data=[];

  /* Preparing the data */
  for (var i=0;obj.length>i;i++)
  {


      var tmp=obj[i].changeDate;
      console.log(tmp);

      if (! _.contains(categories, tmp))
        {
          categories.push(tmp)
        }
      if (!_.findWhere(tmpdata, {"name":obj[i].changeName}))
        {
          tmpdata.push({"name": obj[i].changeName, "data" : [obj[i].count], "color":colorObject[obj[i].changeName]})
          console.log("Pushed to tmpdata "+obj[i].changeName+" "+obj[i].count);
        }
      else {
        console.log("Term already found so I have to push to data");
        console.log(_.findWhere(tmpdata, {"name":obj[i].changeName}));
        _.findWhere(tmpdata, {"name":obj[i].changeName})["data"].push(obj[i].count)
      }
  }


  console.log("parse Results");
  console.log(tmpdata);
  //curation of data
   for (var i=0; tmpdata.length>i; i++)
  {
    tmpdata[i].data=tmpdata[i].data.reverse();
  }
  categories=categories.reverse();

  // - leads to wrong  results - var returndata={"categories": categories.reverse(), "series": tmpdata.reverse()} -- so delete this --

  var returndata={"categories": categories, "series": tmpdata}
  return returndata
}



function lineChartData(){
    //console.log("In lineChartData!");
    var tmpURL=URL+"changesummaries/search/findByOntologyNameAndChangeDateBetween";

    //Construction the URL dynamically
    tmpURL=tmpURL+"?ontologyName="+ontologyName+"&before="+dateBefore+"&after="+dateAfter;
    console.log(tmpURL);

    $.getJSON(tmpURL, function(obj){})
    .fail(function(){console.log("Failed to do webservice call!"); return null})
    .done(function(obj){

      var returndata=parseResult(obj);
      console.log(returndata);
      linechart("graphpart",returndata);
    })
}

linechart = function(divname, returndata)
{
    var title= "All changes per type between "+ dateAfter+" and "+dateBefore;
    console.log("In LINE CHART");
    //var returndata=lineChartData();
    console.log(returndata);
    console.log(returndata.categories);
    console.log(returndata.series);
    /* Now designing the chart */
    var chartoptions={
      chart: {
         type: 'line',
     },
     exporting: { enabled: false },
     credits:{enabled:false},
     title: {
         text: title
     },
     xAxis: {
         categories: returndata.categories
     },
      yAxis: {
          title: {
              text: 'Number of changes'
           }
       },
       plotOptions:{
         series:{point: {
             events:{
               click: function(){
                 //alert("onclick event")
                 date=this.category;
                 //if (semaphore===false)
                    //{ sempahore=true;

                      //detailDateView(divname,this.category, stats) /*Add stats to the method*/
                      detailDateView(divname,this.category)
                      /*ADD stats*/


                    //} - End semaphore - potential target to delete

                //detailview(divname, this.series.name, this.category)
              }}}}},
     series:returndata.series,
     lang: { noData: "No data to display"},
     noData: {
       style: {
          fontWeight: 'bold',
          fontSize: '15px',
          color: '#303030'
         }
       }
    };
       Highcharts.chart(divname, chartoptions)
}



var masterdata=[];
var OLSterms=[];
function callWebserviceForDateView(inputURL){
  console.log("IN callWebserviceForDateView");
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
function detailDateView(divname, date){
  masterdata=[];
  console.log("Let's do this for a certain date");
  var htmlString='';
  $("#"+divname).highcharts().destroy();
  htmlString+="<h3>Changes for <strong>"+date+"</strong></h3>";

  //Transform Date to two dates (before and after day)
  /*var x=new Date(date);
  var tmpdateafter=x.getFullYear()+'-'+(x.getMonth()+1)+'-'+(x.getDate()-1);
  var tmpdatebefore=x.getFullYear()+'-'+(x.getMonth()+1)+'-'+(x.getDate()+1);

  tmpURL=URL+"changes/search/findByOntologyNameAndChangeDateBetween"
  tmpURL=tmpURL+"?ontologyName="+ontologyName+"&after="+tmpdateafter+"&before="+tmpdatebefore
*/

  //No between
  tmpURL=URL+"changes/search/findByOntologyNameAndChangeDate"
  tmpURL=tmpURL+"?ontologyName="+ontologyName+"&date="+date



  console.log(tmpURL);

  var tokenArray=[];

  $.getJSON(tmpURL, function(obj){})
  .fail(function(){console.log("Failed to do webservice call!"); return null})
  .done(function(obj){

    console.log(obj);
    var totalElements=obj["page"].totalElements;
    var totalPages=obj["page"].totalPages;

    var i;
    for (i=0;i<totalPages;i++)
    {
      console.log(tmpURL+"&page="+i);
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
          $.when.apply($,tokenArrayTwo).done(function() {
            console.log("All other webservice Calls finished, finally done!");

                      var htmlString2=''

                      /* Stats for table * /
                      htmlString2+='<div id="stats">Total number of terms wich changes found: '+tableData.length
                      htmlString2+='<br>'
                      htmlString2+='</div><br>'
                      / * */

                      /*htmlString2+='- Click on a row to expand and see the details!<br>'*/

                      htmlString2+='<table id="test" class="display" cellspacing="0" width="100%">'
                      htmlString2+='<thead><tr><th>id</th><th>Label</th><th># and types of changes</th><th></th></tr></thead>'
                      htmlString2+='<tbody>'

                      var baseUrl=document.URL;
                      baseUrl+="/terms?iri=";

                      //Go through every entry within the tableData
                      for (var i=0;i<tableData.length;i++)
                      {
                        var tmpchangetypes=[];
                        var tmpchangefield=tableData[i].changes
                        console.log(tmpchangefield);

                        //Go through all changes of every object and save the changename, so it can be displayed in the main table (for search)
                          for (var tcounter=0; tmpchangefield.length>tcounter; tcounter++)
                            {
                              console.log(tmpchangefield);
                              console.log(tmpchangefield[tcounter].changeName);

                                // Only push the changeName if it's not already in the array, we want unique terms
                              if (! _.contains(tmpchangetypes, tmpchangefield[tcounter].changeName))
                              {
                                tmpchangetypes.push(tmpchangefield[tcounter].changeName)
                              }
                            }
                          htmlString2+='<tr id="'+i+'" class="mainrow"><td><small><a href="'+baseUrl+encodeURIComponent(tableData[i].id)+'">'+tableData[i].id+'</a></small></td><td>'+tableData[i].Label+'</td><td>'+tableData[i].changes.length+' - <small>'+tmpchangetypes+'</small></td><td><img style="cursor:pointer;" src="../img/eye.png" alt="Click me" title="Click on a row to see more results!"/></td></tr>'
                      }



                      htmlString2+='</tbody></table>'


                      $("#"+divname).append(htmlString2);
                      var table=$("#test").DataTable({
                        "aoColumns" : [
                          false,
                          false,
                          false,
                          {"bSortable":false}
                        ]
                      });

                      $('#test tbody').on('click', 'tr.mainrow', function() {
                      //console.log('clicked on ', this, $(this).closest('tr'));
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

                          console.log(rowid);
                          console.log(tableData);
                          console.log(tableData[rowid]);

                          var childHTML='';

                          console.log(tmpchanges);
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
    var OLSurl="http://www.ebi.ac.uk/ols/beta/api/ontologies/"+ontologyName+"/terms?iri="+iri

  $.getJSON(OLSurl, function(olsdata){})
    .fail(function(){console.log("Failed to do webservice call! Please try again later or make sure the "+OLSurl+" exists!"); return null})
    .done(function(olsdata){

      console.log("Doing other webservice calls");
      olsdata=olsdata["_embedded"]["terms"]
      var label=olsdata[0].label;
      console.log(olsdata);
      console.log(label);
      var tmp=_.findWhere(tableData, {"id":iri})
      console.log(tmp);
      tmp.Label=label
      console.log(tmp);

    return token.resolve();
  })

return token;
}
/**/


/* Construction the Data for the data table */
function ConstructDataTable(masterdata){
  console.log("In construct Data Table");
  var dataObject=[];

  var i;
  var tmpcounter=0;
  for (i=0;i<masterdata.length;i++){

    var tmp=masterdata[i].changeSubjectURI

    var index=_.findIndex(dataObject, {"id":tmp})
    var tmpChanges;

    if (index=== -1){
              tmpChanges={"changeName": masterdata[i].changeName, "changeProperties": masterdata[i].changeProperties}
              dataObject.push({"id":tmp, "Label": "Get dynamically from OLS", "changes":[tmpChanges]} );
      }

    else {
        console.log("Finally in ELSE")
        tmpcounter++;

        var tmpProps=dataObject[index].changes;
        tmpChanges={"changeName": masterdata[i].changeName, "changeProperties": masterdata[i].changeProperties}

        tmpProps.push(tmpChanges);
        dataObject[index].changes=tmpProps;
      }
    }

    //console.log(masterdata.length);
    //console.log(dataObject.length);
    //console.log(tmpcounter);
    //console.log(dataObject.length+tmpcounter);
    //console.log(dataObject);

   return dataObject;
}



/* Detail view for CHANGE NAME at a certain DATE  */
function detailview(divname, changeName, date){

  console.log("Let's try to hide the datepicker");
  $('#datepicker').hide();

  console.log("Show information for "+changeName+" on the date of "+date);
  status="detail"

  $("#"+divname).highcharts().destroy();
  $("#"+divname).html("<h3>Changes for <strong>"+changeName+"</strong> on the <strong>"+date+"</strong></h3>")

  //Transform Date to two dates (before and after day)
  //var x=new Date(date);
  //var tmpdateafter=x.getFullYear()+'-'+(x.getMonth()+1)+'-'+(x.getDate()-1);
  //var tmpdatebefore=x.getFullYear()+'-'+(x.getMonth()+1)+'-'+(x.getDate()+1);

  //Using between
  //tmpURL=URL+"changes/search/findByOntologyNameAndChangeNameAndChangeDateBetween"
  //tmpURL=tmpURL+"?ontologyName="+ontologyName+"&changeName="+changeName+"&after="+tmpdateafter+"&before="+tmpdatebefore

  //  New version with new webservice
  var tmpURL=URL+"changes/search/findByOntologyNameAndChangeNameAndChangeDate"
  tmpURL=tmpURL+"?ontologyName="+ontologyName+"&changeName="+changeName+"&date="+date

  console.log("Attention, tmp URL - still hardcoded?");
  console.log(tmpURL);

  $.getJSON(tmpURL, function(obj){})
  .fail(function(){console.log("Failed to do webservice call!"); return null})
  .done(function(obj){
    console.log(obj);

    var baseUrl=document.URL;
    baseUrl+="/terms?iri=";

    obj=obj["_embedded"]["changes"]

    var i=0;
    var htmlString='';

    for (i=0 ; i<obj.length ; i++)
    {
      var keys=_.keys(obj[i].changeProperties)

      console.log(obj[i].changeName);
      console.log(colorObject[obj[i].changeName]);


      htmlString='<table><tr><td>Change Name</td><td>'+obj[i].changeName+'</td><td bgcolor="'+colorObject[obj[i].changeName]+'" width="20px"></td><tr>'
      htmlString+='<tr><td width="80px">ChangeSubjectUri</td><td><a href="'+baseUrl+encodeURIComponent(obj[i].changeSubjectUri)+'">'+obj[i].changeSubjectUri+'</a></td><tr>'


      /* THIS would have to be handled with tokens AGAIN
      var OLSurl="http://www.ebi.ac.uk/ols/beta/api/ontologies/"+ontologyName+"/terms?iri="+obj[i].changeSubjectUri
      $.getJSON(OLSurl, function(olsdata){})
      .fail(function(){console.log("Failed to do webservice call! Please try again later or make sure the "+OLSurl+" exists!"); return null})
      .done(function(olsdata){

      olsdata=olsdata["_embedded"]["terms"]
      var label=olsdata[0].label;


      console.log("Durchlauf Number "+i);
      console.log(label);

    }) END of part that should be handled with tokens*/


    if (obj[i].changeProperties!==null)
    {
      var f;
      //Add line for every property in key
      for (f=0;f<keys.length;f++)
        {
          if (!keys[f].startsWith("predicate"))
            {
              var tmpPropList=obj[i].changeProperties[keys[f]]

              //If there are multiple values for a key, go through the values and print the whole array
              if(tmpPropList.length>1){
                htmlString+='<tr><td>'+keys[f]+'</td><td>'
                for (var tmpi=0;tmpi<tmpPropList.length;tmpi++)
                {
                  htmlString+='- '+tmpPropList[tmpi]+'<br>'
                }

              }

              //If there is only ONE Value for the key, print this value
              if(tmpPropList.length===1){
                htmlString+='<tr><td>'+keys[f]+'</td><td>'+obj[i].changeProperties[keys[f]]+'</td><tr>'
              }
            }


        }
    }

    htmlString+='</table><br>'
    $("#"+divname).append(htmlString)


    }

  })

}
