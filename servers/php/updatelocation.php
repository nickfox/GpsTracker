<?php
	include 'dbconnect.php';
    
	// use this to return form variables to phone
	//var_dump($_POST);
	//die();

	// from the phone
	isset($_POST['latitude']) ? $latitude = $_POST['latitude'] : $latitude = '0';
	isset($_POST['longitude']) ? $longitude = $_POST['longitude'] : $longitude = '0';
	isset($_POST['speed']) ? $speed = $_POST['speed'] : $speed = '0';
	isset($_POST['direction']) ? $direction = $_POST['direction'] : $direction = '0';
	isset($_POST['distance']) ? $distance = $_POST['distance'] : $distance = '0';
	isset($_POST['date']) ? $date = $_POST['date'] : $date = $_POST['date'];
	$date = urldecode($date);
	isset($_POST['locationmethod']) ? $locationMethod = $_POST['locationmethod'] : $locationMethod = '0';
	$locationMethod = urldecode($locationMethod);
	isset($_POST['phonenumber']) ? $phoneNumber = $_POST['phonenumber'] : $phoneNumber = '0';
	isset($_POST['sessionid']) ? $sessionID = $_POST['sessionid'] : $sessionID = '0';
	isset($_POST['accuracy']) ? $accuracy = $_POST['accuracy'] : $accuracy = '0';
	isset($_POST['extrainfo']) ? $extraInfo = $_POST['extrainfo'] : $extraInfo = '0';
	isset($_POST['eventtype']) ? $eventType = $_POST['eventtype'] : $eventType = '0';

	// save the gps location to the database
	// i'm not to worried about sql injection here since i'm calling a stored procedure here
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
