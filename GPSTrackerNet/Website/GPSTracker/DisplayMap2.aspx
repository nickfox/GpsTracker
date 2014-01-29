<%@ Page Language="C#" AutoEventWireup="true" CodeFile="DisplayMap2.aspx.cs" Inherits="DisplayMap2" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" >
<head>
    <title>Google Map GPS Cell Phone Tracker</title>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8"/>
    <!--
        Please leave the link below with the source code, thank you.
        http://www.websmithing.com/portal/Programming/tabid/55/articleType/ArticleView/articleId/6/Google-Map-GPS-Cell-Phone-Tracker-Version-2.aspx
    -->

	<link href="styles/styles.css" 			rel="stylesheet" type="text/css" />
   	<script src="javascript/maps2.js" 	    type="text/javascript"></script>

    <script type="text/javascript">
  	//<![CDATA[
        
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
		    // the code to process the data is in the javascript/maps2.js file
		  
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
		    showWait('Loading routes...');
		    
		    // when the page first loads, get the routes from the DB and load them into the dropdown box.
		    GDownloadUrl('GetRoutes.aspx', loadRoutes);
		}

	 //]]>
     </script>

</head>
<body  onload="load()" onunload="GUnload()">
<!-- this form is needed for RegisterClientScriptBlock to run correctly. -->
<form id="form1" runat="server"></form>
    <div id="messages">GPS Tracker</div>
    <div id="map"></div>
    
	<select id="selectRoute" onchange="getRouteForMap();" tabindex="1"></select>
	
	<select id="selectRefresh" onchange="autoRefresh();" tabindex="2">
        <option value ="0">Auto Refresh - Off</option>
        <option value ="60">Auto Refresh - 1 minute</option>
        <option value ="120">Auto Refresh - 2 minutes</option>
        <option value ="180">Auto Refresh - 3 minutes</option>
        <option value ="300">Auto Refresh - 5 minutes</option>
        <option value ="6000">Auto Refresh - 10 minutes</option>
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

    <!-- preload images to speed up loading in IE -->
    <img src="images/coolred_small.png" alt="" style="display:none" />
    <img src="images/coolblue_small.png" alt="" style="display:none" />
    <img src="images/coolshadow_small.png" alt="" style="display:none" />
</body>
</html>

   



