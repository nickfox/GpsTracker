<?php
	include 'dbconnect.php';
    
	// use this to return form variables to phone
	//var_dump($_POST);
	//die();

	// from the phone
	$latitude = $_POST['latitude'];
	$longitude = $_POST['longitude'];
	$speed = $_POST['speed'];
	$direction = $_POST['direction'];
	$distance = $_POST['distance'];
	$date = urldecode($_POST['date']);
	$locationMethod = urldecode($_POST['locationmethod']);
	$phoneNumber = $_POST['phonenumber'];
	$sessionID = $_POST['sessionid'];
	$accuracy = $_POST['accuracy'];
	$extraInfo = $_POST['extrainfo'];
	$eventType = $_POST['eventtype'];

	// save the gps location to the database
	$query = 'CALL prcSaveGPSLocation(\''
	  . $latitude  . '\',\''
	  . $longitude  . '\',\''
	  . $speed  . '\',\''
	  . $direction  . '\',\''
	  . $distance  . '\',\''
	  . $date  . '\',\''
	  . $locationMethod  . '\',\''
	  . $phoneNumber  . '\',\''
	  . $sessionID  . '\',\''
	  . $accuracy  . '\',\''
	  . $extraInfo . '\',\''
	  . $eventType . '\')';
	  
	if (!$mysqli->multi_query($query)) {
		die('$mysqli->multi_query: '  . $mysqli->error);
	}

	$mysqli->close();

	echo '0';
	
?>
