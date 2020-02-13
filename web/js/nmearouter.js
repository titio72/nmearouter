loadJavascript("js/hammer.min.js");
loadJavascript("js/jquery.min.js");
loadJavascript("js/angular.min.js");
loadJavascript("js/angular-sanitize.min.js");
loadJavascript("js/bootbox.min.js");
loadJavascript("js/bootstrap.min.js");
loadJavascript("js/moment-with-locales.min.js");
loadJavascript("js/Chart.min.js");
loadJavascript("js/hammer.min.js");
loadJavascript("js/chartjs-plugin-zoom.min.js");

loadStylesheet("css/bootstrap.min.css");
loadStylesheet("css/bootstrap-datepicker.min.css");

var app = angular.module("nmearouter", ['ngSanitize'])
.filter('numberFixedLen', function () {
    return function (n, zeroes, digits) {
        var num = parseFloat(n, 10);
        zeroes = parseInt(zeroes, 10);
        if (isNaN(num) || isNaN(digits) || isNaN(zeroes)) {
            return n;
        }
        num = num.toFixed(digits);
        num = ''+num;
        while (num.length < zeroes) {
            num = '0'+num;
        }
        return num;
    };
});

function calcTrueWind(speed, appWindDeg, appWindSpeed) {
  var v = speed;
  var w = appWindSpeed;
  var wA = appWindDeg / 180.0 * Math.PI;

  var wAx = w * Math.sin(wA);
  var wAy = w * Math.cos(wA);

  var wTx = wAx;
  var wTy = wAy - v;

  var r = {
    angle: 180.0 * ((Math.PI / 2) - Math.atan2(wTy, wTx)) / Math.PI,
    speed: Math.sqrt(wTx * wTx + wTy * wTy)
  }

  r.angle = r.angle % 360;
  if (r.angle<0) r.angle = 360 + r.angle; 

  return r;
}


function httpGetShutdown() {
  bootbox.confirm({
    message: "Do you really want to shutdown?",
    buttons: {
      confirm: {
        label: 'Yes',
        className: 'btn-success'
      },
      cancel: {
        label: 'No',
        className: 'btn-danger'
      }
    },
    callback: function (result) {
      if (result) {
          var xmlHttp = new XMLHttpRequest();
          xmlHttp.open( "GET", "http://" + window.location.hostname + ":1112/shutdown", false);
          xmlHttp.setRequestHeader('Content-Type', 'text/plain');
          xmlHttp.send( null );
      }
    }
  });
}

function httpGetTripAnalytics(trip) {
  var xmlHttp = new XMLHttpRequest();
  xmlHttp.open("GET", "http://" + window.location.hostname + ":1112/trackanalytics?trip=" + trip,
      false);
  xmlHttp.setRequestHeader('Content-Type', 'text/plain');
  xmlHttp.send(null);
  return JSON.parse(xmlHttp.responseText);
}


function httpGetAgents() {
  var xmlHttp = new XMLHttpRequest();
  xmlHttp.open("GET", "http://" + window.location.hostname + ":1112/agentsj",
      false);
  xmlHttp.setRequestHeader('Content-Type', 'text/plain');
  xmlHttp.send(null);
  return JSON.parse(xmlHttp.responseText).agents;
}

function httpLoadSpeedDateRange(dt0, dt1, cback) {
  var xmlHttp = new XMLHttpRequest();
  xmlHttp.open("GET", "http://" + window.location.hostname + ":1112/speed?date=" + dt0 + "&dateTo=" + dt1, true);
  xmlHttp.onreadystatechange = function() {
    if (xmlHttp.status==200 && xmlHttp.readyState==4) {
      var json = JSON.parse(xmlHttp.responseText);
      cback(json);
    }
  };
  xmlHttp.setRequestHeader('Content-Type', 'text/plain');
  xmlHttp.send(null);
}

function httpLoadSpeedAnalysisDateRange(dt0, dt1, cback) {
  var xmlHttp = new XMLHttpRequest();
  xmlHttp.open("GET", "http://" + window.location.hostname + ":1112/speedanalysis?date=" + dt0 + "&dateTo=" + dt1, true);
  xmlHttp.onreadystatechange = function() {
    if (xmlHttp.status==200 && xmlHttp.readyState==4) {
      var json = JSON.parse(xmlHttp.responseText);
      
      var dataset = new Object();
      
      dataset.label = "Distance";
      dataset.backgroundColor = "#FF0000";
      dataset.data = [];
      
      var sr = json.serie;
      var i;
      for (i = 0; i<sr.length; i++) {
        var item = sr[i];
        dataset.data.push(item.distance);
      }
      
      cback(dataset);
    }
  };
  xmlHttp.setRequestHeader('Content-Type', 'text/plain');
  xmlHttp.send(null);
}

function httpLoadAllMeteoDateRange(dt0, dt1, cback) {
  var xmlHttp = new XMLHttpRequest();
  xmlHttp.onreadystatechange = function() {
    if (xmlHttp.status==200 && xmlHttp.readyState==4) {
      var json = JSON.parse(xmlHttp.responseText);
      cback(json);
    }
  }
  xmlHttp.open("GET", "http://" + window.location.hostname + ":1112/meteo?date=" + dt0 + "&dateTo=" + dt1, true);
  xmlHttp.setRequestHeader('Content-Type', 'text/plain');
  xmlHttp.send(null);
}

/*
function httpLoadMeteoDateRangeA(tp, all, dt0, dt1, cback) {
  var xmlHttp = new XMLHttpRequest();
  xmlHttp.onreadystatechange = function() {
    if (xmlHttp.status==200 && xmlHttp.readyState==4) {
      var json = JSON.parse(xmlHttp.responseText);
      var res = getDataset(tp, json.serie, all, 1, all);
      cback(res);
    }
  }
  xmlHttp.open("GET", "http://" + window.location.hostname + ":1112/meteo?date=" + dt0 + "&dateTo=" + dt1 + "&type=" + tp, true);
  xmlHttp.setRequestHeader('Content-Type', 'text/plain');
  xmlHttp.send(null);
}
*/

/*
function httpLoadMeteoDateRangeA(tp, all, dt0, dt1, cback) {
	var xmlHttp = new XMLHttpRequest();
	xmlHttp.open("GET", "http://" + window.location.hostname + ":1112/meteo?date=" + dt0 + "&dateTo=" + dt1 + "&type=" + tp, true);
	xmlHttp.onreadystatechange = function() {
		if (this.readyState==4 && this.status==200) {
			var json = JSON.parse(xmlHttp.responseText);
			cback(getDataset(tp, json.serie, (all & 1)!=0, (all & 2)!=0, (all & 4)!=0));
		}
	}
	xmlHttp.setRequestHeader('Content-Type', 'text/plain');
	xmlHttp.send(null);
}
*/

function onhttpresult(cback) {

}

function getDataset(caption, sr, min, avg, max) {
  var data = new Object();
  data.datasets = [];
  if (min>0) data.datasets.push(fillDataset(caption + "Min", 	sr, "vMin", "#555555", "#777777"));
  if (avg>0) data.datasets.push(fillDataset(caption, 			sr, "v", 	"#555555", "#22FF22"));
  if (max>0) data.datasets.push(fillDataset(caption + "Max", 	sr, "vMax", "#FF0000", "#FF2222"));
  return data;
}

function info() {
  bootbox.alert({
    message: 
    "<p>NMEA Router v1.0</p>" +			
    "<p>Andrea Boni</p>"
  });
}

function backup() {
  var xmlHttp = new XMLHttpRequest();
  xmlHttp.onload = function() {
    if (this.status == 200) {
      var json = JSON.parse(xmlHttp.responseText);
      window.open("http://" + window.location.hostname + ":1112/" + json.file);
    } else {
      bootbox.alert({
        title: "Backup",
        message: "Sorry, it didn't work..."
      });
    }
  };
  xmlHttp.open("GET", "http://" + window.location.hostname + ":1112/backup", true);
  xmlHttp.setRequestHeader('Content-Type', 'text/plain');
  xmlHttp.send(null);
}

function tripInfo(tripString) {
  var ss = tripString.split("|");
  var trip = ss[1];
  var json = getTrip(trip);

  bootbox.alert({
    title: json.name,
    message: "<p>" + json.start + " - " + json.end + " UTC</p>" +			
      "<p>Distance <b>" + Math.round(json.dist * 100)/100 + "NM</b> in <b>" + json.totalTime + "</b></p>" +
      "<p>Sail time <b>" + json.sailTime + "</b></p>" +
      "<p>Max Speed <b>" + Math.round(json.maxspeed * 100) / 100 + "Kn</b></p>" +
      "<p>Max 30s Avg Speed <b>" + Math.round(json.maxspeed30 * 100) / 100 + "Kn</b></p>" +
      "<p>Avg Speed <b>" + Math.round(json.avgspeed * 100) / 100 + "Kn</b></p>"
  
  });
  
}

function getGKey() {
  var xmlHttp = new XMLHttpRequest();
  xmlHttp.open("GET", "gmap.key", false);
  xmlHttp.setRequestHeader('Content-Type', 'text/plain');
  xmlHttp.send(null);
  return xmlHttp.responseText;
}

function getInfoA(dateFrom, dateTo, cback) {
  var xmlHttp = new XMLHttpRequest();
  xmlHttp.open("GET", "http://" + window.location.hostname + 
      ":1112/dayinfo?from=" + dateFrom + "&to=" + dateTo, true);
  xmlHttp.onreadystatechange = function() {
    if (xmlHttp.readyState==4 && xmlHttp.status==200) {
      cback(readInfo(xmlHttp.responseText));
    }
  }
  xmlHttp.setRequestHeader('Content-Type', 'text/plain');
  xmlHttp.send(null);
}

function getInfo(dateFrom, dateTo) {
  var xmlHttp = new XMLHttpRequest();
  xmlHttp.open("GET", "http://" + window.location.hostname + 
      ":1112/dayinfo?from=" + dateFrom + "&to=" + dateTo, false);
  xmlHttp.setRequestHeader('Content-Type', 'text/plain');
  xmlHttp.send(null);
  return readInfo(xmlHttp.responseText);
}

function readInfo(resText) {
  var json = JSON.parse(resText);

  var sailtime = json.sailtime;
  var dS = Math.floor(sailtime / 60 / 60 / 24);
  var hS = Math.floor(sailtime / 60 / 60) % 24;
  var mS = Math.round(sailtime / 60) % 60;

  var res = {
    name: "",
    id: 0,
    start: json.start,
    end: json.end,
    navigationTime: dS + "d " + hS + "h " + mS + "m",
    maxspeed: json.maxspeed,
    maxspeed30: json.maxspeed30,
    avgspeed: json.avgspeed,
    dist: json.dist,

    maxspeed1NM: json.speed_1NM,
    maxspeed1N_time_0: (json.t0_1NM!=null)?moment.unix(json.t0_1NM / 1000).format('hh:mm:ss'):'',
    maxspeed1N_time_1: (json.t1_1NM!=null)?moment.unix(json.t1_1NM / 1000).format('hh:mm:ss'):'',

    maxspeed5NM: json.speed_5NM,
    maxspeed5N_time_0: (json.t0_5NM!=null)?moment.unix(json.t0_5NM / 1000).format('hh:mm:ss'):'',
    maxspeed5N_time_1: (json.t1_5NM!=null)?moment.unix(json.t1_5NM / 1000).format('hh:mm:ss'):'',

    maxspeed10NM: json.speed_10NM,
    maxspeed10N_time_0: (json.t0_10NM!=null)?moment.unix(json.t0_10NM / 1000).format('hh:mm:ss'):'',
    maxspeed10N_time_1: (json.t1_10NM!=null)?moment.unix(json.t1_10NM / 1000).format('hh:mm:ss'):'',

    distSailing: json.engineOff,
    distEngine: json.engineOn,
    distUnknown: json.engineUnknown,
  };
  return res;
}

function getTripA(trip, cback) {
  var xmlHttp = new XMLHttpRequest();
  xmlHttp.open("GET", "http://" + window.location.hostname + 
        ":1112/tripinfo?trip=" + trip, true);
  xmlHttp.onreadystatechange = function() {
    if (xmlHttp.readyState==4 && xmlHttp.status==200) {
      cback(readTrip(trip, xmlHttp.responseText));
    }
  }
  xmlHttp.setRequestHeader('Content-Type', 'text/plain');
  xmlHttp.send(null);
}

function getTrip(trip) {
  var xmlHttp = new XMLHttpRequest();
  xmlHttp.open("GET", "http://" + window.location.hostname + 
        ":1112/tripinfo?trip=" + trip, false);
  xmlHttp.setRequestHeader('Content-Type', 'text/plain');
  xmlHttp.send(null);
  return readTrip(trip, xmlHttp.responseText);
}

function readTrip(trip, resText) {
  var json = JSON.parse(resText);
  var duration = json.duration;
  var d = Math.floor(duration / 60 / 60 / 24);
  var h = Math.floor(duration / 60 / 60) % 24;
  var m = Math.round(duration / 60) % 60;

  var sailtime = json.sailtime;
  var dS = Math.floor(sailtime / 60 / 60 / 24);
  var hS = Math.floor(sailtime / 60 / 60) % 24;
  var mS = Math.round(sailtime / 60) % 60;

  var res = {
    name: json.name,
    id: trip,
    start: json.start,
    end: json.end,
    dist: json.dist,
    navigationTime: dS + "d " + hS + "h " + mS + "m",
    totalTime: d + "d " + h + "h " + m + "m",
    maxspeed: json.maxspeed,
    maxspeed30: json.maxspeed30,
    avgspeed: json.avgspeed
  };
  return res;
}

function deleteDay(d) {
  var xmlHttp = new XMLHttpRequest();
  xmlHttp.open("GET", "http://" + window.location.hostname + ":1112/dropcruisingday?date=" + d.name, false);
  xmlHttp.setRequestHeader('Content-Type', 'text/plain');
  xmlHttp.send(null);
  var controllerElement = document.getElementById('TripsPanel');
  var controllerScope = angular.element(controllerElement).scope();
  var days = httpGetCruisingDays();
  controllerScope.days = days;
  controllerScope.$evalAsync();
  return xmlHttp.responseText;
}

function tripIt(d) {
  var xmlHttp = new XMLHttpRequest();
  var ss = d.split("|");
  xmlHttp.open("GET", "http://" + window.location.hostname + ":1112/createtrip?trip=" + ss[1] + "&date=" + ss[2], false);
  xmlHttp.setRequestHeader('Content-Type', 'text/plain');
  xmlHttp.send(null);
  var controllerElement = document.getElementById('TripsPanel');
  var controllerScope = angular.element(controllerElement).scope();
  var days = httpGetCruisingDays();
  controllerScope.days = days;
  controllerScope.$evalAsync();
  return xmlHttp.responseText;
}

function changeName(trip, name) {
  var xmlHttp = new XMLHttpRequest();
  xmlHttp.open("GET", "http://" + window.location.hostname + 
      ":1112/changetripdesc?trip=" + trip + "&desc=" + encodeURIComponent(name), false);
  xmlHttp.setRequestHeader('Content-Type', 'text/plain');
  xmlHttp.send(null);
  var controllerElement = document.getElementById('TripsPanel');
  var controllerScope = angular.element(controllerElement).scope();
  var days = httpGetCruisingDays();
  controllerScope.days = days;
  controllerScope.$evalAsync();
  return xmlHttp.responseText;
}

function httpGetCruisingDays() {
  var xmlHttp = new XMLHttpRequest();
  xmlHttp.open("GET", "http://" + window.location.hostname + ":1112/cruisingdays", false);
  xmlHttp.setRequestHeader('Content-Type', 'text/plain');
  xmlHttp.send(null);
  var json = JSON.parse(xmlHttp.responseText);
  return json;
}

function httpGetTrack(dtF, dtT, cback) {
  var xmlHttp = new XMLHttpRequest();
  xmlHttp.open("GET", "http://" + window.location.hostname + ":1112/track?format=json&dateFrom=" + dtF +
      "&dateTo=" + dtT, true);
  xmlHttp.onreadystatechange = function() {
    if (xmlHttp.readyState==4 && xmlHttp.status==200) {
      var json = JSON.parse(xmlHttp.responseText);
      cback(json.track.path);
    }
  };
  xmlHttp.setRequestHeader('Content-Type', 'text/plain');
  xmlHttp.send(null);
}

function loadJavascript( url ) {
    var ajax = new XMLHttpRequest();
    ajax.open( 'GET', url, false ); // <-- the 'false' makes it synchronous
    ajax.onreadystatechange = function () {
        var script = ajax.response || ajax.responseText;
        if (ajax.readyState === 4 && ajax.status==200) {
            eval.apply( window, [script] );
            console.log("script loaded: ", url);
        } else {
            console.log("ERROR: script not loaded: ", url);
        }
    };
    ajax.send(null);
}

function loadStylesheet( url ) {
  var link = document.createElement( "link" );
  link.href = url;
  link.type = "text/css";
  link.rel = "stylesheet";
  link.media = "screen,print";
  document.getElementsByTagName( "head" )[0].appendChild( link );
}

function direction( a ) {
  if (a<0) a += 180;
  a = Math.round(a/22.5);
  switch (a) {
  case 0: return "N";
  case 1: return "NNE";
  case 2: return "NE";
  case 3: return "ENE";
  case 4: return "E";
  case 5: return "ESE";
  case 6: return "SE";
  case 7: return "SSE";
  case 8: return "S";
  case 9: return "SWW";
  case 10: return "SW";
  case 11: return "WSW";
  case 12: return "W";
  case 13: return "WNW";
  case 14: return "NW";
  case 15: return "NNW";
  case 16: return "N";
  }
  return "";
}
