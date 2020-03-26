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

function httpGetTripAnalyticsByDate(ts0, ts1) {
  var xmlHttp = new XMLHttpRequest();
  xmlHttp.open("GET", "http://" + window.location.hostname + ":1112/trackanalytics?from=" + ts0 + "&to=" + ts1,
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
  xmlHttp.open("GET", "http://" + window.location.hostname + ":1112/speed?from=" + dt0 + "&to=" + dt1, true);
  xmlHttp.onreadystatechange = function() {
    if (xmlHttp.status==200 && xmlHttp.readyState==4) {
      var json = JSON.parse(xmlHttp.responseText);
      cback(json);
    }
  };
  xmlHttp.setRequestHeader('Content-Type', 'text/plain');
  xmlHttp.send(null);
}

function httpLoadSpeedById(id, cback) {
  var xmlHttp = new XMLHttpRequest();
  xmlHttp.open("GET", "http://" + window.location.hostname + ":1112/speed?trip=" + id, true);
  xmlHttp.onreadystatechange = function() {
    if (xmlHttp.status==200 && xmlHttp.readyState==4) {
      var json = JSON.parse(xmlHttp.responseText);
      cback(json);
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
  xmlHttp.open("GET", "http://" + window.location.hostname + ":1112/meteo?from=" + dt0 + "&to=" + dt1, true);
  xmlHttp.setRequestHeader('Content-Type', 'text/plain');
  xmlHttp.send(null);
}

function httpLoadAllMeteoById(id, cback) {
  var xmlHttp = new XMLHttpRequest();
  xmlHttp.onreadystatechange = function() {
    if (xmlHttp.status==200 && xmlHttp.readyState==4) {
      var json = JSON.parse(xmlHttp.responseText);
      cback(json);
    }
  }
  xmlHttp.open("GET", "http://" + window.location.hostname + ":1112/meteo?trip=" + id, true);
  xmlHttp.setRequestHeader('Content-Type', 'text/plain');
  xmlHttp.send(null);
}

function getDataset(caption, sr, min, avg, max) {
  var data = new Object();
  data.datasets = [];
  if (min>0) data.datasets.push(fillDataset(caption + "Min", 	sr, "vMin", "gray", "darkgray"));
  if (avg>0) data.datasets.push(fillDataset(caption, 			    sr, "v", 	  "blue", "darkblue"));
  if (max>0) data.datasets.push(fillDataset(caption + "Max", 	sr, "vMax", "red", "darkred"));
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

function getGKey() {
  var xmlHttp = new XMLHttpRequest();
  xmlHttp.open("GET", "gmap.key", false);
  xmlHttp.setRequestHeader('Content-Type', 'text/plain');
  xmlHttp.send(null);
  return xmlHttp.responseText;
}

function dropTrip(id, cback) {
  var xmlHttp = new XMLHttpRequest();
  xmlHttp.open("GET", "http://" + window.location.hostname + ":1112/droptrip?trip=" + id);
  xmlHttp.onreadystatechange = function() {
    if (xmlHttp.readyState==4 && xmlHttp.status==200) {
      cback();
    }
  };
  xmlHttp.setRequestHeader('Content-Type', 'text/plain');
  xmlHttp.send(null);
}

function changeName(trip, name, cback) {
  var xmlHttp = new XMLHttpRequest();
  xmlHttp.open("GET", "http://" + window.location.hostname + 
      ":1112/changetripdesc?trip=" + trip + "&desc=" + encodeURIComponent(name));
  xmlHttp.onreadystatechange = function() {
    if (xmlHttp.readyState==4 && xmlHttp.status==200) {
      cback();
    }
  };
  xmlHttp.setRequestHeader('Content-Type', 'text/plain');
  xmlHttp.send(null);
}

function httpGetTrips() {
  var xmlHttp = new XMLHttpRequest();
  xmlHttp.open("GET", "http://" + window.location.hostname + ":1112/trips?year=0", false);
  xmlHttp.setRequestHeader('Content-Type', 'text/plain');
  xmlHttp.send(null);
  var json = JSON.parse(xmlHttp.responseText);
  return json;
}

function httpGetTrackByDate(dtF, dtT, cback) {
  var xmlHttp = new XMLHttpRequest();
  var url = "http://" + window.location.hostname + ":1112/track?format=json&from=" + dtF + 
    "&to=" + dtT;
  xmlHttp.open("GET", url, true);
  xmlHttp.onreadystatechange = function() {
    if (xmlHttp.readyState==4 && xmlHttp.status==200) {
      var json = JSON.parse(xmlHttp.responseText);
      cback(json.track.path);
    }
  };
  xmlHttp.setRequestHeader('Content-Type', 'text/plain');
  xmlHttp.send(null);
}

function httpGetTrackById(trip, cback) {
  var xmlHttp = new XMLHttpRequest();
  xmlHttp.open("GET", "http://" + window.location.hostname + ":1112/track?format=json&trip=" + trip, true);
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
