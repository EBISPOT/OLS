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
    console.log(serviceURL);
    console.log(ontologyName);


    URL=constructURL(document.URL)


    date=new Date();
    dateBefore=date.getFullYear()+'-'+(date.getMonth()+1)+'-'+date.getDate();
    //replace with webservice call for last 10 dates!
    dateAfter="2015-01-09"


  /*   THIS is how we can get DATES later on, when there are MORE changes
    var DateURL=URL+"/changes/changesummaries/search/dates?size=10&ontologyName="+ontologyName
    $.getJSON(tmpURL, function(obj){})
      .fail(function(){console.log("Failed to do webservice call!"); return null})
      .done(function(obj){}*/



    //Append general structur of window
    $("#diachron-wrapper").append("<div id='datepicker'><div>")
    $("#diachron-wrapper").append("<div id='graphpart'><div>")
    $("#diachron-wrapper").append("<div id='optionfield'><div>")

    //Append datepicker
    $("#datepicker").html('<br>Display data from - to<div><input type="text" id="dateAfter" value="'+dateAfter+'"><input type="text" id="dateBefore" value="'+dateBefore+'"> <input id="changeDateSubmit" type="submit" value="Update Data!"></div><br>')

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
      console.log( $("#dateAfter").val() );
      console.log( $("#dateBefore").val() );

      dateBefore=$("#dateBefore").val()
      dateAfter=$("#dateAfter").val()
      update();
    })

    $("#pieChart").on('click', function() {status="pie"; pieChartData();})
    $("#overTime").on('click', function() {status="line"; lineChartData() })
    update();
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
           exporting: { enabled: false },
           credits:{enabled:false},
           title: {
               text: title
           },
           series:[{
              name:"Total per type",
              data: data,
              point:{events: {click: function(e){name=this.name;BarChart(divname, this.name) }}}
          }],
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




function BarChart(divname, name){
  $("#"+divname).highcharts().destroy();
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
  })
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
                 date=this.category;
                detailview(divname, this.series.name, this.category)
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


function detailview(divname, changeName, date){
  console.log("Show information for "+changeName+" on the date of "+date);
  status="detail"

  $("#"+divname).highcharts().destroy();
  $("#"+divname).html("<h3>Changes for <strong>"+changeName+"</strong> on the <strong>"+date+"</strong></h3>")

  //Transform Date to two dates (before and after day)
  var x=new Date(date);
  var tmpdateafter=x.getFullYear()+'-'+(x.getMonth()+1)+'-'+(x.getDate()-1);
  var tmpdatebefore=x.getFullYear()+'-'+(x.getMonth()+1)+'-'+(x.getDate()+1);

  //Using between
  tmpURL=URL+"changes/search/findByOntologyNameAndChangeNameAndChangeDateBetween"
  tmpURL=tmpURL+"?ontologyName="+ontologyName+"&changeName="+changeName+"&after="+tmpdateafter+"&before="+tmpdatebefore


  //  New version with new webservice
  //var tmpURL=URL+"changes/search/findByOntologyNameAndChangeNameAndChangeDate"
  //tmpURL=tmpURL+"?ontologyName="+ontologyName+"&changeName="+changeName+"&date="+date


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

      //Link to OLS -     base + ontologies/go/terms?iri= + changeSubjectUri
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

//useless!
//function formatDate(date){
//  var tmpDate=new Date(date)
//  tmpDate=tmpDate.getFullYear()+"-"+(tmpDate.getMonth()+1)+"-"+tmpDate.getDay()
//  return tmpDate
//}



/*
function addtable(data, after){
  var now=new Date();
  console.log(now);
  console.log(now.getMonth()+1);
  console.log(now.getFullYear());
  console.log(now.getDate());

  now=now.getFullYear()+'-'+(now.getMonth()+1)+'-'+now.getDate();

  var table='<br>Data from<input type="text" name="FirstName" value="'+after+'"> to <input type="text" name="TO" value="'+now+'"><input id="changeDateSubmit" type="submit" value="Get Data!"></form>';
  table=table+'<table style="width:100%"><tr><td>?</td><td>?</td><td>?</td></tr><tr><td>?</td><td>?</td><td>?</td></tr><tr><td>?</td><td>?</td><td>?</td></tr><tr><td>?</td><td>?</td><td>?</td></tr></table> '
  $("#diachron-table").append(table);
  $("#changeDateSubmit").on('click', function(){alert("You wish that this would work, wouldn't you?")})
}*/
