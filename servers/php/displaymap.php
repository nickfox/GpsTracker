<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Gps Tracker</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    
    <script src="//code.jquery.com/jquery-1.11.1.min.js"></script>
    <script src="//maps.google.com/maps/api/js?v=3&sensor=false&libraries=adsense"></script>
    <script src="js/maps.js"></script>
    <script src="js/leaflet-0.7.3/leaflet.js"></script>
    <script src="js/leaflet-plugins/google.js"></script>
    <script src="js/leaflet-plugins/bing.js"></script>
    <link href="css/light.css" rel="stylesheet" type="text/css" />
    <link href="js/leaflet-0.7.3/leaflet.css" rel="stylesheet" type="text/css" />
    <link rel="stylesheet" href="//maxcdn.bootstrapcdn.com/bootstrap/3.2.0/css/bootstrap.min.css">

</head>
<body>
    <div class="container">
        <div class="row">
            <div class="col-sm-4" id="toplogo">
                <img src="images/gpstracker-man-blue.png" alt="hal" id="halimage" >GpsTracker
            </div>
            <div class="col-sm-8 paddingright15" id="messages"></div>
        </div>
        <div class="row">
            <div class="col-sm-12" id="mapdiv">
                <div id="map-canvas"></div>
            </div>
        </div>
        <div class="row">
            <div class="col-sm-12 paddingright15" id="selectdiv">
                <select id="routeSelect" tabindex="1"></select>
            </div>
        </div>
        <div class="row">
            <div class="col-sm-3 deletediv">
                <input type="button" id="delete" value="Delete" tabindex="2">
            </div>
            <div class="col-sm-3 autorefreshdiv">
                <input type="button" id="autorefresh" value="Auto Refresh - Off" tabindex="3">
            </div>
            <div class="col-sm-3 refreshdiv">
                <input type="button" id="refresh" value="Refresh" tabindex="4">
            </div>
            <div class="col-sm-3 viewalldiv">
                <input type="button" id="viewall" value="View All" tabindex="5">
            </div>
        </div>
    </div>       
</body>
</html>
    