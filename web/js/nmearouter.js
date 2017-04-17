function httpLoadMeteo(tp, all) {
	var xmlHttp = new XMLHttpRequest();
	xmlHttp.open("GET", "http://" + window.location.hostname + ":1112/meteo?type=" + tp, false);
	xmlHttp.setRequestHeader('Content-Type', 'text/plain');
	xmlHttp.send(null);
	var json = JSON.parse(xmlHttp.responseText);
	if (all==1)
		return getMeteoDataA(tp, json.serie)
	else
		return getMeteoData(tp, json.serie)
}

function httpLoadMeteoByDate(tp, all, dt) {
	var xmlHttp = new XMLHttpRequest();
	xmlHttp.open("GET", "http://" + window.location.hostname + ":1112/meteo?date=" + dt + "&type=" + tp, false);
	xmlHttp.setRequestHeader('Content-Type', 'text/plain');
	xmlHttp.send(null);
	var json = JSON.parse(xmlHttp.responseText);
	if (all==1) 
		return getMeteoDataA(tp, json.serie)
	else
		return getMeteoData(tp, json.serie)
}

function httpLoadMeteoDateRange(tp, all, dt0, dt1) {
	var xmlHttp = new XMLHttpRequest();
	xmlHttp.open("GET", "http://" + window.location.hostname + ":1112/meteo?date=" + dt0 + "&dateTo=" + dt1 + "&type=" + tp, false);
	xmlHttp.setRequestHeader('Content-Type', 'text/plain');
	xmlHttp.send(null);
	var json = JSON.parse(xmlHttp.responseText);
	if (all==1) 
		return getMeteoDataA(tp, json.serie)
	else
		return getMeteoData(tp, json.serie)
}

function getMeteoData(caption, sr) {
	var data = new Object();
	data.datasets = [];
	
	var dataset = new Object();
	dataset.label = caption;
	dataset.data = [];
	for (i = 0; i<sr.length; i++) {
		var item = sr[i];
		var datapoint = new Object();
		datapoint.x = Date.parse(item.time);
		datapoint.y = parseFloat(item.v);
		dataset.data.push(datapoint);
	}
	data.datasets.push(dataset);
	return data;
}
		
function getMeteoDataA(caption, sr) {
	var data = new Object();
	data.datasets = [];
	
	var dataset = new Object();
	dataset.label = caption;
	dataset.data = [];
	for (i = 0; i<sr.length; i++) {
		var item = sr[i];
		var datapoint = new Object();
		datapoint.x = Date.parse(item.time);
		datapoint.y = parseFloat(item.v);
		dataset.data.push(datapoint);
	}
	data.datasets.push(dataset);

	var datasetMin = new Object();
	datasetMin.label = caption + "Min";
	datasetMin.data = [];
	for (i = 0; i<sr.length; i++) {
		var item = sr[i];
		var datapoint = new Object();
		datapoint.x = Date.parse(item.time);
		datapoint.y = parseFloat(item.vMin);
		datasetMin.data.push(datapoint);
	}
    datasetMin.pointBackgroundColor = "#00FF00",
    datasetMin.pointBorderColor = "#22FF22",
	data.datasets.push(datasetMin);

	var datasetMax = new Object();
	datasetMax.label = caption + "Max";
	datasetMax.data = [];
	for (i = 0; i<sr.length; i++) {
		var item = sr[i];
		var datapoint = new Object();
		datapoint.x = Date.parse(item.time);
		datapoint.y = parseFloat(item.vMax);
		datasetMax.data.push(datapoint);
	}
    datasetMax.pointBackgroundColor = "#FF0000",
    datasetMax.pointBorderColor = "#FF2222",
	data.datasets.push(datasetMax);
	
	return data;
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
	xmlHttp.open("GET", "http://" + window.location.hostname + ":1112/track?format=json&from=" + dtF +
			"&to=" + dtT, false);
	xmlHttp.setRequestHeader('Content-Type', 'text/plain');
	xmlHttp.send(null);
	var json = JSON.parse(xmlHttp.responseText);
	return json.track.path;
}