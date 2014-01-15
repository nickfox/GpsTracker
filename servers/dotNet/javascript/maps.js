function loadRoutes(data, responseCode) {
    if (data.length == 0) {
        showMessage('There are no routes available to view.');
        map.innerHTML = '';
    }
    else {
        // get list of routes
        var xml = GXml.parse(data);

        var routes = xml.getElementsByTagName("route");

        // create the first option of the dropdown box
        var option = document.createElement('option');
        option.setAttribute('value', '0');
        option.innerHTML = 'Select Route...';
        routeSelect.appendChild(option);

        // iterate through the routes and load them into the dropdwon box. 
        for (i = 0; i < routes.length; i++) {
            var option = document.createElement('option');
            option.setAttribute('value', '?sessionID=' + routes[i].getAttribute("sessionID")
			                    + '&phoneNumber=' + routes[i].getAttribute("phoneNumber"));
            option.innerHTML = routes[i].getAttribute("phoneNumber") + "  " + routes[i].getAttribute("times");
            routeSelect.appendChild(option);
        }

        // need to reset this for firefox
        routeSelect.selectedIndex = 0;

        hideWait();
        showMessage('Please select a route below.');
    }

}

// this will get the map and route, the route is selected from the dropdown box
function getRouteForMap() {
    if (hasMap()) {
        showWait('Getting map...');
        var url = 'GetRouteForMap.aspx' + routeSelect.options[routeSelect.selectedIndex].value;
        GDownloadUrl(url, loadGPSLocations);
    }
    else {
        alert("Please select a route before trying to refresh map.");
    }
}

// check to see if we have a map loaded, don't want to autorefresh or delete without it
function hasMap() {
    if (routeSelect.selectedIndex == 0) { // means no map
        return false;
    }
    else {
        return true;
    }
}

function loadGPSLocations(data, responseCode) {
    if (data.length == 0) {
        showMessage('There is no tracking data to view.');
        map.innerHTML = '';
    }
    else {
        if (GBrowserIsCompatible()) {

            // create list of GPS data locations from our XML
            var xml = GXml.parse(data);

            // markers that we will display on Google map
            var markers = xml.getElementsByTagName("locations");

            // get rid of the wait gif
            hideWait();

            // create new map and add zoom control and type of map control
            var map = new GMap2(document.getElementById("map"));
            map.addControl(new GSmallMapControl());
            map.addControl(new GMapTypeControl());

            var length = markers.length;

            // center map on last marker so we can see progress during refreshes
            map.setCenter(new GLatLng(parseFloat(markers[length - 1].getAttribute("latitude")),
	                                  parseFloat(markers[length - 1].getAttribute("longitude"))), zoomLevel);

            // interate through all our GPS data, create markers and add them to map
            for (var i = 0; i < length; i++) {
                var point = new GLatLng(parseFloat(markers[i].getAttribute("latitude")),
	                                    parseFloat(markers[i].getAttribute("longitude")));

                var marker = createMarker(i, length, point,
		                     markers[i].getAttribute("speed"),
		                     markers[i].getAttribute("direction"),
		                     markers[i].getAttribute("distance"),
		                     markers[i].getAttribute("locationMethod"),
		                     markers[i].getAttribute("gpsTime"),
		                     markers[i].getAttribute("phoneNumber"),
		                     markers[i].getAttribute("sessionID"),
		                     markers[i].getAttribute("accuracy"),
		                     markers[i].getAttribute("eventType"),
		                     markers[i].getAttribute("extraInfo"));

                // add markers to map
                map.addOverlay(marker);
            }
        }

        // show route name
        showMessage(routeSelect.options[routeSelect.selectedIndex].innerHTML);
    }
}

function createMarker(i, length, point, speed, direction, distance, locationMethod, gpsTime,
                      phoneNumber, sessionID, accuracy, eventType, extraInfo) {
    var icon = new GIcon();

    // make the most current marker red
    if (i == length - 1) {
        icon.image = "images/coolred_small.png";
    }
    else {
        icon.image = "images/coolblue_small.png";
    }

    icon.shadow = "images/coolshadow_small.png";
    icon.iconSize = new GSize(12, 20);
    icon.shadowSize = new GSize(22, 20);
    icon.iconAnchor = new GPoint(6, 20);
    icon.infoWindowAnchor = new GPoint(5, 1);

    var marker = new GMarker(point, icon);

    // this describes how we got our location data, either by satellite or by cell phone tower
    var lm = "";
    if (locationMethod == "8") {
        lm = "Cell Tower";
    } else if (locationMethod == "327681") {
        lm = "Satellite";
    } else {
        lm = locationMethod;
    }

    var str = "</td></tr>";

    // when a user clicks on last marker, let them know it's final one
    if (i == length - 1) {
        str = "</td></tr><tr><td align=left>&nbsp;</td><td><b>Final location</b></td></tr>";
    }

    // this creates the pop up bubble that displays info when a user clicks on a marker
    GEvent.addListener(marker, "click", function () {
        marker.openInfoWindowHtml(
        "<table border=0 style=\"font-size:95%;font-family:arial,helvetica,sans-serif;\">"
        + "<tr><td align=right>&nbsp;</td><td>&nbsp;</td><td rowspan=2 align=right>"
        + "<img src=images/" + getCompassImage(direction) + ".jpg alt= />"
        + str
        + "<tr><td align=right>Speed:</td><td>" + speed + " mph</td></tr>"
        + "<tr><td align=right>Distance:</td><td>" + distance + " mi</td><td>&nbsp;</td></tr>"
        + "<tr><td align=right>Time:</td><td colspan=2>" + gpsTime + "</td></tr>"
        + "<tr><td align=right>Method:</td><td>" + lm + "</td><td>&nbsp;</td></tr>"
        + "<tr><td align=right>Phone #:</td><td>" + phoneNumber + "</td><td>&nbsp;</td></tr>"
        + "<tr><td align=right>Session ID:</td><td>" + sessionID + "</td><td>&nbsp;</td></tr>"
        + "<tr><td align=right>Accuracy:</td><td>" + accuracy + " ft</td><td>&nbsp;</td></tr>"
        + "<tr><td align=right>Event Type:</td><td>" + eventType + "</td><td>&nbsp;</td></tr>"
        + "<tr><td align=right>Extra Info:</td><td>" + extraInfo + "</td><td>&nbsp;</td></tr>"

        + "</table>"
        );
    });

    return marker;
}

// this chooses the proper image for our litte compass in the popup window
function getCompassImage(azimuth) {
    if ((azimuth >= 337 && azimuth <= 360) || (azimuth >= 0 && azimuth < 23))
        return "compassN";
    if (azimuth >= 23 && azimuth < 68)
        return "compassNE";
    if (azimuth >= 68 && azimuth < 113)
        return "compassE";
    if (azimuth >= 113 && azimuth < 158)
        return "compassSE";
    if (azimuth >= 158 && azimuth < 203)
        return "compassS";
    if (azimuth >= 203 && azimuth < 248)
        return "compassSW";
    if (azimuth >= 248 && azimuth < 293)
        return "compassW";
    if (azimuth >= 293 && azimuth < 337)
        return "compassNW";

    return "";
}

function deleteRoute() {
    if (hasMap()) {
        var answer = confirm("This will permanently delete this route\n from the database. Do you want to delete?")
        if (answer) {
            showWait('Deleting route...');
            var url = 'DeleteRoute.aspx' + routeSelect.options[routeSelect.selectedIndex].value;
            GDownloadUrl(url, deleteRouteResponse);
        }
        else {
            return false;
        }
    }
    else {
        alert("Please select a route before trying to delete.");
    }
}

function deleteRouteResponse(data, responseCode) {
    map.innerHTML = '';
    routeSelect.length = 0;
    GDownloadUrl('GetRoutes.aspx', loadRoutes);
}

// auto refresh the map. there are 3 transitions (shown below). transitions happen when a user
// selects an option in the auto refresh dropdown box. an interval is an amount of time in between
// refreshes of the map. for instance, auto refresh once a minute. in the method below, the 3 numbers
// in the code show where the 3 transitions are handled. setInterval turns on a timer that calls 
// the getRouteForMap() method every so many seconds based on the value of newInterval. 
// clearInterval turns off the timer. if newInterval is 5, then the value passed to setInterval is 
// 5000 milliseconds or 5 seconds. 
function autoRefresh() {
    /*
        1) going from off to any interval
        2) going from any interval to off
        3) going from one interval to another
    */

    if (hasMap()) {
        newInterval = refreshSelect.options[refreshSelect.selectedIndex].value;

        if (currentInterval > 0) { // currently running at an interval

            if (newInterval > 0) { // moving to another interval (3)
                clearInterval(intervalID);
                intervalID = setInterval("getRouteForMap();", newInterval * 1000);
                currentInterval = newInterval;
            }
            else { // we are turning off (2)
                clearInterval(intervalID);
                newInterval = 0;
                currentInterval = 0;
            }
        }
        else { // off and going to an interval (1)
            intervalID = setInterval("getRouteForMap();", newInterval * 1000);
            currentInterval = newInterval;
        }

        // show what auto refresh action was taken and after 5 seconds, display the route name again
        showMessage(refreshSelect.options[refreshSelect.selectedIndex].innerHTML);
        setTimeout('showRouteName();', 5000);
    }
    else {
        alert("Please select a route before trying to refresh map.");
        refreshSelect.selectedIndex = 0;
    }
}

function changeZoomLevel() {
    if (hasMap()) {
        zoomLevel = zoomLevelSelect.selectedIndex + 1;

        getRouteForMap();

        // show what zoom level action was taken and after 5 seconds, display the route name again
        showMessage(zoomLevelSelect.options[zoomLevelSelect.selectedIndex].innerHTML);
        setTimeout('showRouteName();', 5000);
    }
    else {
        alert("Please select a route before selecting zoom level.");
        zoomLevelSelect.selectedIndex = zoomLevel - 1;
    }
}

function showMessage(message) {
    messages.innerHTML = 'Gps Tracker: <b>' + message + '</b>';
}

function showRouteName() {
    showMessage(routeSelect.options[routeSelect.selectedIndex].innerHTML);
}

function showWait(theMessage) {
    map.innerHTML = '<img src="images/ajax-loader.gif"' +
                    'style="position:absolute;top:225px;left:325px;">';
    showMessage(theMessage);
}

function hideWait() {
    map.innerHTML = '';
    messages.innerHTML = 'Gps Tracker';
}

