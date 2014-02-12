
function loadRoutes(xml) {
    if (xml.length == 0) {
        showMessage('There are no routes available to view.');
        map.innerHTML = '';
    }
    else {
        // create the first option of the dropdown box
		var option = document.createElement('option');
		option.setAttribute('value', '0');
		option.innerHTML = 'Select Route...';
		routeSelect.appendChild(option);

		// iterate through the routes and load them into the dropdwon box.
		$(xml).find('route').each(function(){
			var option = document.createElement('option');
			option.setAttribute('value', '?sessionID=' + $(this).attr('sessionID')
		                    + '&phoneNumber=' + $(this).attr('phoneNumber'));
							
			var shortSessionID = $(this).attr('sessionID').substring(0,5);				
			option.innerHTML = $(this).attr('phoneNumber') + "-" + shortSessionID + "  " + $(this).attr('times');
			routeSelect.appendChild(option);
		});
		
        // need to reset this for firefox
        routeSelect.selectedIndex = 0;

        hideWait();
        showMessage('<span style="color:#F00;">Please select a route below.</span>');
    }
}

// this will get the map and route, the route is selected from the dropdown box
function getRouteForMap() {
    if (hasMap()) {
        showWait('Getting map...');
	    var url = 'getgpslocations.php' + routeSelect.options[routeSelect.selectedIndex].value;
		
		//alert("testing route: " + routeSelect.options[routeSelect.selectedIndex].value);

	    $.ajax({
	           url: url,
	           type: 'GET',
	           dataType: 'xml',
	           success: function(data) {
	              loadGPSLocations(data);
	           }
	       });
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

function loadGPSLocations(xml) {
    if (xml.length == 0) {
        showMessage('There is no tracking data to view.');
        map.innerHTML = '';
    }
    else {
        // get rid of the wait gif
        hideWait();
			
		var map = new google.maps.Map(
			      document.getElementById('map'), {
			        zoom: zoomLevel,
			        mapTypeId: google.maps.MapTypeId.ROADMAP
				  });

      		// note: replace this adsense publisher ID and channel with your own.
      		var publisherID = 'pub-7095775186404141';
			var channel = '6961715451';		
		    var adUnitDiv = document.createElement('div');
		    var adUnitOptions = {
		      format: google.maps.adsense.AdFormat.HALF_BANNER,
		      position: google.maps.ControlPosition.TOP_CENTER,
		      backgroundColor: '#c4d4f3',
		      borderColor: '#e5ecf9',
		      titleColor: '#0000cc',
		      textColor: '#000000',
		      urlColor: '#009900',
		      publisherId: publisherID,
			  channelNumber: channel,
		      map: map,
		      visible: true
		    };
		    var adUnit = new google.maps.adsense.AdUnit(adUnitDiv, adUnitOptions);			

		   var finalLocation = false;

			// iterate through the locations and create map markers for each location
			$(xml).find('locations').each(function(){
				
				// want to set the map center on the last location
				if ($(this).is(':last-child')) {
					map.setCenter(new google.maps.LatLng($(this).attr('latitude'),$(this).attr('longitude')));
					finalLocation = true;
				}

		        var marker = createMarker(
					$(this).attr('latitude'),
					$(this).attr('longitude'),
		            $(this).attr('speed'),
		            $(this).attr('direction'),
		            $(this).attr('distance'),
		            $(this).attr('locationMethod'),
		            $(this).attr('gpsTime'),
		            $(this).attr('phoneNumber'),
		            $(this).attr('sessionID'),
		            $(this).attr('accuracy'),
		            $(this).attr('extraInfo'),
					map, finalLocation);
			});
        }

        // display route name above map
        showMessage(routeSelect.options[routeSelect.selectedIndex].innerHTML);
}

function createMarker(latitude, longitude, speed, direction, distance, locationMethod, gpsTime,
                      phoneNumber, sessionID, accuracy, extraInfo, map, finalLocation) {			  			 						  	
	var iconUrl;					  

    if (finalLocation) {
    	iconUrl = 'images/coolred_small.png';
    } else {
    	iconUrl = 'images/coolblue_small.png';
    }
						  
	var markerImage = {
	    url: iconUrl,
	    size: new google.maps.Size(12, 20),
	    origin: new google.maps.Point(0,0),
	    anchor: new google.maps.Point(6, 30)
	  };
	
	var marker = new google.maps.Marker({
	    position: new google.maps.LatLng(latitude, longitude),
	    map: map,
	    icon: markerImage
	});

    var lastMarker = "</td></tr>";

    // when a user clicks on last marker, let them know it's final one
    if (finalLocation) {
        lastMarker = "</td></tr><tr><td align=left>&nbsp;</td><td><b>Final location</b></td></tr>";
    }
	
	// convert from meters to feet
	accuracy = parseInt(accuracy * 3.28);
	
	var contentString = "<table border=0 style=\"font-size:95%;font-family:arial,helvetica,sans-serif;\">"
        + "<tr><td align=right>&nbsp;</td><td>&nbsp;</td><td rowspan=2 align=right>"
        + "<img src=images/" + getCompassImage(direction) + ".jpg alt= />" + lastMarker
        + "<tr><td align=right>Speed:</td><td>" + speed +  " mph</td></tr>"
        + "<tr><td align=right>Distance:</td><td>" + distance +  " mi</td><td>&nbsp;</td></tr>"
        + "<tr><td align=right>Time:</td><td colspan=2>" + gpsTime +  "</td></tr>"
        + "<tr><td align=right>Method:</td><td>" + locationMethod + "</td><td>&nbsp;</td></tr>"
        + "<tr><td align=right>Phone #:</td><td>" + phoneNumber + "</td><td>&nbsp;</td></tr>"
        + "<tr><td align=right>Session ID:</td><td>" + sessionID + "</td><td>&nbsp;</td></tr>"
        + "<tr><td align=right>Accuracy:</td><td>" + accuracy + " ft</td><td>&nbsp;</td></tr>"
        + "<tr><td align=right>Extra Info:</td><td>" + extraInfo + "</td><td>&nbsp;</td></tr></table>";
		
    var infowindow = new google.maps.InfoWindow({
	    content: contentString
	});
				
	google.maps.event.addListener(marker, 'click', function() {
	    infowindow.open(map, marker);
	});
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
	    if (answer){
            showWait('Deleting route...');
            var url = 'deleteroute.php' + routeSelect.options[routeSelect.selectedIndex].value;

		    $.ajax({
		           url: url,
		           type: 'GET',
		           success: function() {
		              deleteRouteResponse();
		           }
		       });	
	    }
	    else {
		    return false;
	    }
	}
	else {
	    alert("Please select a route before trying to delete.");
	}
}

function deleteRouteResponse() {
    map.innerHTML = '';
    routeSelect.length = 0;

    $.ajax({
           url: 'getroutes.php',
           type: 'GET',
           success: function(data) {
              loadRoutes(data);
           }
       });
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
     messages.innerHTML = 'GpsTracker: <strong>' + message + '</strong>';
}

function showRouteName() {
    showMessage(routeSelect.options[routeSelect.selectedIndex].innerHTML);
}

function showWait(theMessage) {
    map.innerHTML = '<img src="images/ajax-loader.gif" style="position:absolute;top:225px;left:325px;">';
    showMessage(theMessage);
}

function hideWait() {
    map.innerHTML = '';
    messages.innerHTML = 'GpsTracker';
}

