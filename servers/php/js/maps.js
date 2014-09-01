jQuery(document).ready(function($) {
    var routeSelect = document.getElementById('routeSelect');
    var messages = document.getElementById('messages');
    var map = document.getElementById('map-canvas');
    var autoRefresh = false;
    var intervalID = 0;
    var zoom = 12;
    var sessionIDArray;
    
    var viewingAllRoutes = false;
    
    getAllRoutesForMap();
    load();
    
    $("#routeSelect").change(function() {
        if (hasMap()) {
            viewingAllRoutes = false;
            
            getRouteForMap();
        } 
    });
       
    $("#refresh").click(function() {
        if (viewingAllRoutes) {
            getAllRoutesForMap(); 
        } else {
            if (hasMap()) {
                getRouteForMap();
            }             
        }
    });
       
    $("#delete").click(function() {
        deleteRoute();
    });       
        
    $('#autorefresh').click(function() { 
        if (autoRefresh) {
            turnOffAutoRefresh();           
        } else {
            turnOnAutoRefresh();                     
        }
    }); 
    
    $("#viewall").click(function() {
        getAllRoutesForMap();
    });
    
    function getAllRoutesForMap() {
        // when the page first loads, get the routes from the DB and load them into the dropdown box.
        
        viewingAllRoutes = true;
        routeSelect.selectedIndex = 0;
        showPermanentMessage('Please select a route below.');
                   
        $.ajax({
            url: 'getallroutesformap.php',
            type: 'GET',
            dataType: 'json',
            success: function(data) {
                // console.log(JSON.stringify(data));
                loadGPSLocations(data);
        },
        error: function (xhr, status, errorThrown) {
            //console.log("responseText in error: " + xhr.responseText);
            console.log("error status: " + xhr.status);
            console.log("errorThrown: " + errorThrown);
        }
        });
    }           
        
    function load() {
        // when the page first loads, get the routes from the DB and load them into the dropdown box.           
        $.ajax({
            url: 'getroutes.php',
            type: 'GET',
            dataType: 'json',
            success: function(data) {
            loadRoutes(data);
        },
        error: function (xhr, status, errorThrown) {
            console.log("responseText: " + xhr.responseText);
            console.log("status: " + xhr.status);
            console.log("errorThrown: " + errorThrown);
        }
        });
    }    
    
    function loadRoutes(json) {
        if (json.length == 0) {
            showPermanentMessage('There are no routes available to view.');
            map.innerHTML = '';
        }
        else {
            // create the first option of the dropdown box
            var option = document.createElement('option');
            option.setAttribute('value', '0');
            option.innerHTML = 'Select Route...';
            routeSelect.appendChild(option);

            // when a user taps on a marker, the position of the sessionID in this array is the position of the route
            // in the dropdown box. it's used below to set the index of the dropdown box when the map is changed
            sessionIDArray = [];
            
            // iterate through the routes and load them into the dropdwon box.
            $(json.routes).each(function(key, value){
                var option = document.createElement('option');
                option.setAttribute('value', '?sessionID=' + $(this).attr('sessionID')
                                + '&phoneNumber=' + $(this).attr('phoneNumber'));

                sessionIDArray.push($(this).attr('sessionID'));

                option.innerHTML = $(this).attr('phoneNumber') + " " + $(this).attr('times');
                routeSelect.appendChild(option);
            });

            // need to reset this for firefox
            routeSelect.selectedIndex = 0;

            showPermanentMessage('Please select a route below.');
        }
    }

    // this will get the map and route, the route is selected from the dropdown box
    function getRouteForMap() {
        if (hasMap()) {
        
            // get selected index
            // console.log($("#routeSelect").prop("selectedIndex"));

            // get selected value
            // use this instead $('#routeSelect').val();
        
           var url = 'getrouteformap.php' + routeSelect.options[routeSelect.selectedIndex].value;
           // console.log("testing route: " + $('#routeSelect').val());

            $.ajax({
                   url: url,
                   type: 'GET',
                   dataType: 'json',
                   success: function(data) {
                      loadGPSLocations(data);
                   },
                   error: function (xhr, status, errorThrown) {
                       console.log("responseText: " + xhr.responseText);
                       console.log("status: " + xhr.status);
                       console.log("errorThrown: " + errorThrown);
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

    function loadGPSLocations(json) {
        if (json.length == 0) {
            showPermanentMessage('There is no tracking data to view.');
            map.innerHTML = '';
        }
        else {
            // make sure we only create map object once
            if (map.id == 'map-canvas') {
                // clear any old map objects
                document.getElementById('map-canvas').outerHTML = "<div id='map-canvas'></div>";
           
                // use leaflet (http://leafletjs.com/) to create our map and map layers
                var gpsTrackerMap = new L.map('map-canvas');
            
                var openStreetMapsURL = ('https:' == document.location.protocol ? 'https://' : 'http://') +
                 '{s}.tile.openstreetmap.org/{z}/{x}/{y}.png';
                var openStreetMapsLayer = new L.TileLayer(openStreetMapsURL,
                {attribution:'&copy;2014 <a href="http://openstreetmap.org">OpenStreetMap</a> contributors'});

                // need to get your own bing maps key, http://www.microsoft.com/maps/create-a-bing-maps-key.aspx
                var bingMapsLayer = new L.BingLayer("AnH1IKGCBwAiBWfYAHMtIfIhMVybHFx2GxsReNP5W0z6P8kRa67_QwhM4PglI9yL");
                var googleMapsLayer = new L.Google('ROADMAP');
            
                // this fixes the zoom buttons from freezing
                // https://github.com/shramov/leaflet-plugins/issues/62
                L.polyline([[0, 0], ]).addTo(gpsTrackerMap);

                // this sets which map layer will first be displayed, go ahead and change it to bingMapsLayer or openStreetMapsLayer to see
                gpsTrackerMap.addLayer(googleMapsLayer);

                // this is the switcher control to switch between map types (upper right hand corner of map)
                gpsTrackerMap.addControl(new L.Control.Layers({
                    'Bing Maps':bingMapsLayer,
                    'Google Maps':googleMapsLayer,
                    'OpenStreetMaps':openStreetMapsLayer
                }, {}));
            }

                var finalLocation = false;
                var counter = 0;
                var locationArray = [];
                
                // iterate through the locations and create map markers for each location
                $(json.locations).each(function(key, value){
                    var latitude =  $(this).attr('latitude');
                    var longitude = $(this).attr('longitude');
                    var sessionID = $(this).attr('sessionID');
                    var tempLocation = new L.LatLng(latitude, longitude);
                    
                    locationArray.push(tempLocation);                    
                    counter++;

                    // want to set the map center on the last location
                    if (counter == $(json.locations).length) {
                        gpsTrackerMap.setView(tempLocation, zoom);
                        finalLocation = true;
                    
                        if (!viewingAllRoutes) {
                            displayCityName(latitude, longitude);
                        }
                    }

                    var marker = createMarker(
                        latitude,
                        longitude,
                        sessionID,
                        $(this).attr('speed'),
                        $(this).attr('direction'),
                        $(this).attr('distance'),
                        $(this).attr('locationMethod'),
                        $(this).attr('gpsTime'),
                        $(this).attr('phoneNumber'),
                        $(this).attr('sessionID'),
                        $(this).attr('accuracy'),
                        $(this).attr('extraInfo'),
                        gpsTrackerMap, finalLocation);
                });
                
                // fit markers within window
                var bounds = new L.LatLngBounds(locationArray);
                gpsTrackerMap.fitBounds(bounds);
            }
    }

    function createMarker(latitude, longitude, sessionID, speed, direction, distance, locationMethod, gpsTime,
                          phoneNumber, sessionID, accuracy, extraInfo, map, finalLocation) {
        var iconUrl;

        if (finalLocation) {
            iconUrl = 'images/coolred_small.png';
        } else {
            iconUrl = 'images/coolgreen2_small.png';
        }

        var markerIcon = new L.Icon({
                iconUrl:      iconUrl,
                shadowUrl:    'images/coolshadow_small.png',
                iconSize:     [12, 20],
                shadowSize:   [22, 20],
                iconAnchor:   [6, 20],
                shadowAnchor: [6, 20],
                popupAnchor:  [-3, -25]
        });

        var lastMarker = "</td></tr>";

        // when a user clicks on last marker, let them know it's final one
        if (finalLocation) {
            lastMarker = "</td></tr><tr><td align=left>&nbsp;</td><td><b>Final location</b></td></tr>";
        }

        // convert from meters to feet
        accuracy = parseInt(accuracy * 3.28);

        var popupWindowText = "<table border=0 style=\"font-size:95%;font-family:arial,helvetica,sans-serif;\">" +
            "<tr><td align=right>&nbsp;</td><td>&nbsp;</td><td rowspan=2 align=right>" +
            "<img src=images/" + getCompassImage(direction) + ".jpg alt= />" + lastMarker +
            "<tr><td align=right>Speed:&nbsp;</td><td>" + speed +  " mph</td></tr>" +
            "<tr><td align=right>Distance:&nbsp;</td><td>" + distance +  " mi</td><td>&nbsp;</td></tr>" +
            "<tr><td align=right>Time:&nbsp;</td><td colspan=2>" + gpsTime +  "</td></tr>" +
            "<tr><td align=right>User Name:&nbsp;</td><td>" + phoneNumber + "</td><td>&nbsp;</td></tr>" +
            "<tr><td align=right>Accuracy:&nbsp;</td><td>" + accuracy + " ft</td><td>&nbsp;</td></tr></table>";


        var gpstrackerMarker;
        var title = phoneNumber + " - " + gpsTime

        // make sure the final red marker always displays on top 
        if (finalLocation) {
            gpstrackerMarker = new L.marker(new L.LatLng(latitude, longitude), {title: title, icon: markerIcon, zIndexOffset: 999}).bindPopup(popupWindowText).addTo(map);
        } else {
            gpstrackerMarker = new L.marker(new L.LatLng(latitude, longitude), {title: title, icon: markerIcon}).bindPopup(popupWindowText).addTo(map);
        }
        
        // if we are viewing all routes, we want to go to a route when a user taps on a marker instead of displaying popupWindow
        if (viewingAllRoutes) {
            gpstrackerMarker.unbindPopup();
            
            gpstrackerMarker.on("click", function() {        
                var url = 'getrouteformap.php?sessionID=' + sessionID + "&phoneNumber=" + phoneNumber;

                viewingAllRoutes = false;
 
                var indexOfRouteInRouteSelectDropdwon = sessionIDArray.indexOf(sessionID) + 1;
                routeSelect.selectedIndex = indexOfRouteInRouteSelectDropdwon;

                $.ajax({
                    url: url,
                    type: 'GET',
                    dataType: 'json',
                    success: function(data) {
                        loadGPSLocations(data);
                    },
                    error: function (xhr, status, errorThrown) {
                        console.log("responseText: " + xhr.responseText);
                        console.log("status: " + xhr.status);
                        console.log("errorThrown: " + errorThrown);
                    }
                 });
            }); // on click
        } 
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

    function displayCityName(latitude, longitude) {
        var lat = parseFloat(latitude);
        var lng = parseFloat(longitude);
        var latlng = new google.maps.LatLng(lat, lng);
        reverseGeocoder = new google.maps.Geocoder();
        reverseGeocoder.geocode({'latLng': latlng}, function(results, status) {
    
        if (status == google.maps.GeocoderStatus.OK) {
              // results[0] is full address
              if (results[1]) {
                  reverseGeocoderResult = results[1].formatted_address; 
                  showPermanentMessage(reverseGeocoderResult);
              } else {
                  console.log('No results found');
              }
            } else {
                console.log('Geocoder failed due to: ' + status);
            }
        });
    }

    function turnOffAutoRefresh() {
        console.log('turnOffAutoRefresh');
        showMessage('Auto Refresh Off.');
        $('#autorefresh').val('Auto Refresh - Off');
    
        autoRefresh = false;
        clearInterval(intervalID);         
    }

    function turnOnAutoRefresh() {
        console.log('turnOnAutoRefresh');
    
       // if (hasMap()) {
            showMessage('Auto Refresh On (1 min).'); 
            $('#autorefresh').val('Auto Refresh - On');
            autoRefresh = true;
    
            clearInterval(intervalID);
            
            if (viewingAllRoutes) {
                intervalID = setInterval(getAllRoutesForMap, 60 * 1000); // one minute 
            } else {
                intervalID = setInterval(getRouteForMap, 60 * 1000);          
            } 
      //  } else {
      //      showMessage('Please select a route first.');            
      //  }           
    }

    function deleteRoute() {
        if (hasMap()) {
		
    		// comment out these two lines to get delete working
    		var answer = confirm("Disabled here on test website, this works fine.");
    		return false;
		
            var answer = confirm("This will permanently delete this route\n from the database. Do you want to delete?");
            if (answer){
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
        routeSelect.length = 0;

        document.getElementById('map-canvas').outerHTML = "<div id='map-canvas'></div>";

        $.ajax({
               url: 'getroutes.php',
               type: 'GET',
               success: function(data) {
                  loadRoutes(data);
               }
           });
    }

    // message visible for 7 seconds
    function showMessage(message) {
        // if we show a message like start auto refresh, we want to put back our current address afterwards
        var tempMessage =  $('#messages').html();
        
        $('#messages').html(message);
        setTimeout(function() {
            $('#messages').html(tempMessage);
        }, 7 * 1000); // 7 seconds
    }

    function showPermanentMessage(message) {
        $('#messages').html(message);
    }

    // for debugging, console.log(objectToString(map));
    function objectToString (obj) {
        var str = '';
        for (var p in obj) {
            if (obj.hasOwnProperty(p)) {
                str += p + ': ' + obj[p] + '\n';
            }
        }
        return str;
    }
});

