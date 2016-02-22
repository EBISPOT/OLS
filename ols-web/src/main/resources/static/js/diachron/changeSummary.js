var dateBefore;
var dateAfter;
var ontologyName;
var name='';
var status='line';
var date='';
var URL='';
var semaphore=false;

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
        $("#diachron-wrapper").append("<div id='datepicker'><div>")
        $("#diachron-wrapper").append("<div id='graphpart'><div>")
        $("#diachron-wrapper").append("<div id='optionfield'><div>")

        //Append datepicker
        $("#datepicker").html('<br>Display data from - to<div><input type="text" id="dateAfter" value="'+dateAfter+'"> <input type="text" id="dateBefore" value="'+dateBefore+'"> <input id="changeDateSubmit" type="submit" value="Update Data!"></div><br>')
        $("#optionfield").html('<br><button id="pieChart" type="button">Show Pie Chart Summary</button> <button id="overTime" type="button">Show data over Timeframe</button>');

        $("#optionfield").html('<br><button id="pieChart" type="button">Show Pie Chart Summary</button> <button id="overTime" type="button">Show data over Timeframe</button>');

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

        $("#pieChart").on('click', function() {status="pie"; pieChartData();})
        $("#overTime").on('click', function() {status="line"; lineChartData() })
        update();

    })
  })


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
          {     data[i]={"name":tmp[i], "y":value[i]}    }

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
      htmlString+="<table id='testTable' class='tablesorter'>"
      htmlString+="<thead><tr><th>Change Name</th><th>Total</th><th>Percentage</th></tr></thead>"
      htmlString+="<tbody>"
      for (i=0;i<data.length;i++)
      {
        htmlString+="<tr><td>"+data[i].name+"</td><td>"+data[i].y+"</td><td>"+Math.round(data[i].y/sum*100)+"</td></tr>"
      }
      htmlString+="</tbody></table>"
      console.log(htmlString);

      $("#"+divname).append(htmlString)
      $("#testTable").tablesorter({sortList:[[1],[1]]})
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
                 drawBarTable(divname, data)
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


function drawBarTable(divname, data){
  console.log("In drawBarTable", data);
  var htmlString="";

  htmlString+="<hr>Detailview for '"+ontologyName+"' between "+dateAfter+" and "+dateBefore+"</h3>";
  htmlString+="<table id='testTable' class='tablesorter'>"
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
  $("#testTable").tablesorter({sortList:[[0],[1]]})
}



function parseResult(obj){
  var categories=[];
  var tmpdata=[];
  var data=[];

  /* Preparing the data */
  for (var i=0;obj.length>i;i++)
  {
      console.log(obj[i].changeName);
      console.log(obj[i].changeDate)
      console.log(obj[i].count)
      var tmp=obj[i].changeDate;
      //tmp=formatDate(tmp)
      console.log(tmp);

      if (! _.contains(categories, tmp))
        {
          categories.push(tmp)
        }
      if (!_.findWhere(tmpdata, {"name":obj[i].changeName}))
        {
          tmpdata.push({"name": obj[i].changeName, "data" : [obj[i].count]})
          console.log("Pushed to tmpdata "+obj[i].changeName+" "+obj[i].count);
        }
      else {
        console.log("Term already found so I have to push to data");
        console.log(_.findWhere(tmpdata, {"name":obj[i].changeName}));
        _.findWhere(tmpdata, {"name":obj[i].changeName})["data"].push(obj[i].count)
      }
  }

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
    var title= "Sum of all changes per type between "+ dateAfter+" and "+dateBefore;
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
                 if (semaphore===false)
                    { sempahore=true;

                      //detailDateView(divname,this.category, stats) /*Add stats to the method*/
                      detailDateView(divname,this.category)
                      /*ADD stats*/
                    }

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


          /*  Fking asynchronious calling * /
          var OLSurl="http://www.ebi.ac.uk/ols/beta/api/ontologies/"+ontologyName+"/terms?iri="+obj[i].changeSubjectUri
          $.getJSON(OLSurl, function(olsdata){})
          .fail(function(){console.log("Failed to do webservice call! Please try again later or make sure the "+OLSurl+" exists!"); return null})
          .done(function(olsdata){

          console.log("Run through number " ,i);
          console.log(obj[i]);
          console.log(obj[i].changeName);

          olsdata=olsdata["_embedded"]["terms"]
          var label=olsdata.label;

          //console.log(obj[i]);
          //console.log(label);
          masterdata.push({"changeName": obj[i].changeName, "changeSubjectURI": obj[i].changeSubjectUri, "label": label,"changeProperties": obj[i].changeProperties})
        })*/



        //no label here!
        masterdata.push({"changeName": obj[i].changeName, "changeSubjectURI": obj[i].changeSubjectUri,"changeProperties": obj[i].changeProperties})

      }

      token.resolve();

  })

  return token;
}


function detailDateView(divname, date){
  masterdata=[];
  console.log("Let's do this for a certain date");
  var htmlString='';
  $("#"+divname).highcharts().destroy();
  htmlString+="<h3>Changes for <strong>"+date+"</strong></h3>";

  //Transform Date to two dates (before and after day)
  var x=new Date(date);
  var tmpdateafter=x.getFullYear()+'-'+(x.getMonth()+1)+'-'+(x.getDate()-1);
  var tmpdatebefore=x.getFullYear()+'-'+(x.getMonth()+1)+'-'+(x.getDate()+1);

  tmpURL=URL+"changes/search/findByOntologyNameAndChangeDateBetween"
  tmpURL=tmpURL+"?ontologyName="+ontologyName+"&after="+tmpdateafter+"&before="+tmpdatebefore

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


    $.when.apply($,tokenArray).done(function() {
          console.log("Collected all data! So let's Test the results");
          console.log("Total Elements ",totalElements);
          console.log("Length masterdata", masterdata.length);

          var tableData=ConstructDataTable(masterdata)


          /*******   OLD try   ********
          var htmlString='';

          htmlString+='<table class="table table-condensed" style="border-collapse:collapse;">'
          htmlString+='<thead><tr><th>id</th><th>Label</th><th>Number of changes</th></tr></thead>'
          htmlString+='<tbody>'


          for (var i=0;i<tableData.length;i++)
          {
              htmlString+='<tr data-toggle="collapse" class="accordion-toggle" data-target="#test'+i+'"><td>'+tableData[i].id+'</td><td>'+tableData[i].Label+'</td><td>'+tableData[i].changes.length+'</td></tr>'
              htmlString+='<tr><td colspan="6" class="hiddenRow"><div id="test'+i+'" class="accordian-body collapse">Demo2</div></td></tr>'
          }

          htmlString+='</tbody></table>'
          $("#"+divname).html(htmlString);
           END OLD TRY ********/


          var htmlString2='<div id="stats">Total number of terms wich changes found: '+tableData.length
          htmlString2+='<br>'
          /* Other stats */
          htmlString2+='</div><br>'

          htmlString2+='<table id="test" class="display" cellspacing="0" width="100%">'
          htmlString2+='<thead><tr><th>id</th><th>Label</th><th>Number of changes</th></tr></thead>'
          htmlString2+='<tbody>'

          for (var i=0;i<tableData.length;i++)
          {
              htmlString2+='<tr id="'+i+'" class="mainrow"><td>'+tableData[i].id+'</td><td>'+tableData[i].Label+'</td><td>'+tableData[i].changes.length+'</td></tr>'
          }



          htmlString2+='</tbody></table>'


          $("#"+divname).append(htmlString2);
          var table=$("#test").DataTable();

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
                childHTML+='<table><tr><td>Change Name</td><td>'+tmpchanges[f].changeName+'</td></tr>'


                var keys=_.keys(tmpchanges[f].changeProperties)

                if (tmpchanges[f].changeProperties!==null)
                {
                var counter;
                //Add line for every property in key
                for (counter=0;counter<keys.length;counter++)
                {
                      if (!keys[counter].startsWith("predicate"))
                          childHTML+='<tr><td>'+keys[counter]+'</td><td>'+tmpchanges[f].changeProperties[keys[counter]]+'</td><tr>' }
                }

                childHTML+='</table>'



              }

              row.child(childHTML).show();
              tr.addClass('shown');
            }


          })

      })

  })
}



/* Construction the Data for the data table */
function ConstructDataTable(masterdata){
  console.log("In construct Data Table");
  //console.log(masterdata);
  var uniqueTermURIList=[];
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







    console.log(masterdata.length);
    console.log(dataObject.length);
    console.log(tmpcounter);
    console.log(dataObject.length+tmpcounter);
    console.log(dataObject);

   return dataObject;
}



/* Detail view for CHANGE NAME at a certain DATE  */
function detailview(divname, changeName, date){
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

    obj=obj["_embedded"]["changes"]

    var i=0;
    var htmlString='';
    var baseUrl=document.URL;
    baseUrl+="/terms?iri=";

    for (i=0 ; i<obj.length ; i++)
    {
      var keys=_.keys(obj[i].changeProperties)

      htmlString='<table><tr><td>Change Name</td><td>'+obj[i].changeName+'</td><tr>'
      htmlString+='<tr><td>ChangeSubjectUri</td><td><a href="'+baseUrl+encodeURIComponent(obj[i].changeSubjectUri)+'">'+obj[i].changeSubjectUri+'</a></td><tr>'

      //Link to OLS -   base + ontologies/go/terms?iri= + changeSubjectUri
      console.log(obj[i].changeProperties);
      console.log(keys);
      console.log(keys.length);

      if (obj[i].changeProperties!==null)
      {
        var f;
        //Add line for every property in key
        for (f=0;f<keys.length;f++)
          {      htmlString+='<tr><td>'+keys[f]+'</td><td>'+obj[i].changeProperties[keys[f]]+'</td><tr>' }
      }

      htmlString+='</table><br>'
      $("#"+divname).append(htmlString)
    }

  })

}
