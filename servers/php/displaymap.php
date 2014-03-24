<!DOCTYPE html>
<html>
<head>
    <title>Google Map GPS Cell Phone Tracker</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta charset="utf-8">
 
	<script src="javascript/custom-google-loader-1.0.0.min.js"></script>
    <script src="javascript/maps.js"></script>
    <script src="javascript/leaflet-0.7.2/leaflet.js"></script>
    <script src="javascript/leaflet-plugins/google.js"></script>
    <script src="javascript/leaflet-plugins/bing.js"></script>
    <link href="javascript/leaflet-0.7.2/leaflet.css" rel="stylesheet" type="text/css" />
    <link href="styles/styles.css" rel="stylesheet" type="text/css" />
	
    <script type="text/javascript">
    //<![CDATA[
	
		google.load('jquery', '1.11.0');
	
        var routeSelect;
        var refreshSelect;
        var messages;
        var map;
        var intervalID;
        var newInterval;
        var currentInterval;
        var zoomLevelSelect;
        var zoomLevel;

        function load() {
            // the code to process the data is in the javascript/maps.js file

            routeSelect = document.getElementById('selectRoute');
            refreshSelect = document.getElementById('selectRefresh');
            zoomLevelSelect = document.getElementById('selectZoomLevel');
            messages = document.getElementById('messages');
            map = document.getElementById('map');

            intervalID = 0;
            newInterval = 0;
            currentInterval = 0;
            zoomLevel = 12;

            zoomLevelSelect.selectedIndex = 11;
            refreshSelect.selectedIndex = 0;
            showWaitImage('Loading routes...');
            var i = 0;

            // when the page first loads, get the routes from the DB and load them into the dropdown box.           
            $.ajax({
                url: 'getroutes.php',
                type: 'GET',
                dataType: 'json',
                success: function(data) {
                //console.log("success loadRoutes"); 
                loadRoutes(data);
            },
            error: function (xhr, status, errorThrown) {
                console.log("responseText: " + xhr.responseText);
                console.log("status: " + xhr.status);
                console.log("errorThrown: " + errorThrown);
            }
          });
        }
     //]]>
     </script>

</head>
<body  onload="load()">    
    <div id="messages">GpsTracker</div>
    <div id="map"></div>

    <select id="selectRoute" onchange="getRouteForMap();" tabindex="1"></select>

    <select id="selectRefresh" onchange="autoRefresh();" tabindex="2">
        <option value ="0">Auto Refresh - Off</option>
        <option value ="60">Auto Refresh - 1 minute</option>
        <option value ="120">Auto Refresh - 2 minutes</option>
        <option value ="180">Auto Refresh - 3 minutes</option>
        <option value ="300">Auto Refresh - 5 minutes</option>
        <option value ="600">Auto Refresh - 10 minutes</option>
    </select>

    <select id="selectZoomLevel" onchange="changeZoomLevel();" tabindex="3">
        <option value ="1">Zoom Level - 1</option>
        <option value ="2">Zoom Level - 2</option>
        <option value ="3">Zoom Level - 3</option>
        <option value ="4">Zoom Level - 4</option>
        <option value ="5">Zoom Level - 5</option>
        <option value ="6">Zoom Level - 6</option>
        <option value ="7">Zoom Level - 7</option>
        <option value ="8">Zoom Level - 8</option>
        <option value ="9">Zoom Level - 9</option>
        <option value ="10">Zoom Level - 10</option>
        <option value ="11">Zoom Level - 11</option>
        <option value ="12">Zoom Level - 12</option>
        <option value ="13">Zoom Level - 13</option>
        <option value ="14">Zoom Level - 14</option>
        <option value ="15">Zoom Level - 15</option>
        <option value ="16">Zoom Level - 16</option>
        <option value ="17">Zoom Level - 17</option>
    </select>

    <input type="button" id="delete" value="Delete" onclick="deleteRoute()" tabindex="4">
    <input type="button" id="refresh" value="Refresh" onclick="getRouteForMap()" tabindex="5">

     <div id="test">
		<p><== now supports different map providers, Google, Bing or OpenStreetMaps. Select a route below the map
		and then click on the selector just to the left.</p>
	    <br>&nbsp;<br>
		<p>Please note that routes in the dropdown box are a concatenation of phoneNumber (ie. androidUser) and the first five characters of the sessionID. Start times and end times for the routes are in parentheses
   	 <br>&nbsp;<br><!-- i know you love my adept handling of whitespace, don't lie... :o) -->
   	 The routes in the dropdown box are sorted in descending order by startTime so your route should be near the top.	
   	 <br>&nbsp;<br>
   	 Also note that you must use <strong>https</strong> now in this string:
   	  <br>&nbsp;<br>
   	 <span style="color:#f00;">https</span>://www.websmithing.com/gpstracker/updatelocation.php	
   	 <br>&nbsp;<br>
   	 <strong>If you do not change this now in the client phone app you will probably not see your route here.</strong>	 	 
    </div>
	
	<script>
	  (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
	  (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
	  m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
	  })(window,document,'script','//www.google-analytics.com/analytics.js','ga');

	  ga('create', 'UA-3068771-1', 'websmithing.com');
	  ga('send', 'pageview');
	</script>
</body>
</html>





