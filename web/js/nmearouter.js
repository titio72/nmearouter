loadJavascript("js/jquery.min.js");
loadJavascript("js/angular.min.js");
loadJavascript("js/angular-sanitize.min.js");
loadJavascript("js/bootbox.min.js");
loadJavascript("js/bootstrap.min.js");
loadJavascript("js/moment.min.js");
loadJavascript("js/Chart.min.js");

loadStylesheet("css/bootstrap.min.css");
loadStylesheet("css/bootstrap-datepicker.min.css");

var app = angular.module("nmearouter", ['ngSanitize'])
.filter('numberFixedLen', function () {
    return function (n, zeroes, digits) {
        var num = parseFloat(n, 10);
        dig = parseInt(digits, 10);
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

function httpGetAgents() {
	var xmlHttp = new XMLHttpRequest();
	xmlHttp.open("GET", "http://" + window.location.hostname + ":1112/agentsj",
			false);
	xmlHttp.setRequestHeader('Content-Type', 'text/plain');
	xmlHttp.send(null);
	return JSON.parse(xmlHttp.responseText).agents;
}

function httpBackup() {
	var xmlHttp = new XMLHttpRequest();
	xmlHttp.open("GET", "http://" + window.location.hostname + ":1112/backup", false);
	xmlHttp.setRequestHeader('Content-Type', 'text/plain');
	xmlHttp.send(null);
	var json = JSON.parse(xmlHttp.responseText);
	return json;
}

function httpLoadSpeedDateRange(dt0, dt1) {
	var xmlHttp = new XMLHttpRequest();
	xmlHttp.open("GET", "http://" + window.location.hostname + ":1112/speed?date=" + dt0 + "&dateTo=" + dt1, false);
	xmlHttp.setRequestHeader('Content-Type', 'text/plain');
	xmlHttp.send(null);
	var json = JSON.parse(xmlHttp.responseText);
	return getDataset("Speed", json.serie, 1, 1, 1);
}

function httpLoadMeteoDateRange(tp, all, dt0, dt1) {
	var xmlHttp = new XMLHttpRequest();
	xmlHttp.open("GET", "http://" + window.location.hostname + ":1112/meteo?date=" + dt0 + "&dateTo=" + dt1 + "&type=" + tp, false);
	xmlHttp.setRequestHeader('Content-Type', 'text/plain');
	xmlHttp.send(null);
	var json = JSON.parse(xmlHttp.responseText);
	return getDataset(tp, json.serie, all, 1, all);
}

function getDataset(caption, sr, min, avg, max) {
	var data = new Object();
	data.datasets = [];
	if (min>0) data.datasets.push(fillDataset(caption + "Min", 	sr, "vMin", "#00FF00", "#22FF22"));
	if (avg>0) data.datasets.push(fillDataset(caption, 			sr, "v", 	"#555555", "#222222"));
	if (max>0) data.datasets.push(fillDataset(caption + "Max", 	sr, "vMax", "#FF0000", "#FF2222"));
	return data;
}

function fillDataset(caption, sr, attr, color, borderColor) {
	var dataset = new Object();
	dataset.label = caption;
	dataset.data = [];
	for (i = 0; i<sr.length; i++) {
		var item = sr[i];
		var datapoint = new Object();
		datapoint.x = Date.parse(item['time']);
		datapoint.y = parseFloat(item[attr]);
		dataset.data.push(datapoint);
	}
    dataset.pointBackgroundColor = color;"#FF0000",
    dataset.pointBorderColor = borderColor;
	return dataset;
}

function info() {
	bootbox.alert({
		message: 
		"<p>NMEA Router v1.0</p>" +			
		"<p>Andrea Boni</p>"
	});
}

function backup() {
	var res = httpBackup();
	if (res.result=="Ok") {
		window.open("http://" + window.location.hostname + ":1112/" + res.file);
	} else {
		
	}
}

function tripInfo(trip) {
	var xmlHttp = new XMLHttpRequest();
	xmlHttp.open("GET", "http://" + window.location.hostname + 
			":1112/tripinfo?trip=" + trip.name, false);
	xmlHttp.setRequestHeader('Content-Type', 'text/plain');
	xmlHttp.send(null);
	var json = JSON.parse(xmlHttp.responseText);
	var duration = json.duration;
	var d = Math.floor(duration / 60 / 60 / 24);
	var h = Math.floor(duration / 60 / 60) % 24;
	var m = Math.round(duration / 60) % 60;
	bootbox.alert({
		message: 
		"<p>" + json.start + " - " + json.end + " UTC</p>" +			
		"<p>Distance <b>" + Math.round(json.dist * 100)/100 + "NM</b> in <b>" + d + "d " + h + "h " + m + "m</b></p>" +
		"<p>Max Speed <b>" + Math.round(json.maxspeed * 100) / 100 + "Kn</b></p>" +
		"<p>Max 30s Avg Speed <b>" + Math.round(json.maxspeed30 * 100) / 100 + "Kn</b></p>"
	});
	
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
	var ss = d.name.split(".");
	xmlHttp.open("GET", "http://" + window.location.hostname + ":1112/createtrip?trip=" + ss[0] + "&date=" + ss[1], false);
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
	var caption;
	var xmlHttp = new XMLHttpRequest();
	xmlHttp.open("GET", "http://" + window.location.hostname + ":1112/cruisingdays", false);
	xmlHttp.setRequestHeader('Content-Type', 'text/plain');
	xmlHttp.send(null);
	var json = JSON.parse(xmlHttp.responseText);
	return json;
}

function httpGetTrack(dtF, dtT) {
	var caption;
	var xmlHttp = new XMLHttpRequest();
	xmlHttp.open("GET", "http://" + window.location.hostname + ":1112/track?format=json&dateFrom=" + dtF +
			"&dateTo=" + dtT, false);
	xmlHttp.setRequestHeader('Content-Type', 'text/plain');
	xmlHttp.send(null);
	var json = JSON.parse(xmlHttp.responseText);
	return json.track.path;
}

function loadJavascript( url ) {
    var ajax = new XMLHttpRequest();
    ajax.open( 'GET', url, false ); // <-- the 'false' makes it synchronous
    ajax.onreadystatechange = function () {
        var script = ajax.response || ajax.responseText;
        if (ajax.readyState === 4) {
            switch( ajax.status) {
                case 200:
                    eval.apply( window, [script] );
                    console.log("script loaded: ", url);
                    break;
                default:
                    console.log("ERROR: script not loaded: ", url);
            }
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