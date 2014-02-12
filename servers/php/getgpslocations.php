<?php

	include 'dbconnect.php';
	
	isset($_GET['sessionID']) ? $sessionID = $_GET['sessionID'] : $sessionID = '0';
	isset($_GET['phoneNumber']) ? $phoneNumber = $_GET['phoneNumber'] : $phoneNumber = '0';

	$query = 'CALL prcGetRouteForMap(\'' . $sessionID . '\',\''  . $phoneNumber  . '\')';
	
	$xml = '<gps>';

	// execute query
	if ($mysqli->multi_query($query)) {

	    do {
	        if ($result = $mysqli->store_result()) {
	            while ($row = $result->fetch_row()) {
	                $xml .= $row[0];
	            }
	            $result->close();
	        }
	    } while ($mysqli->next_result());
	}
	else {
		die('$mysqli->multi_query: '  . $mysqli->error);
	}

	$xml .= '</gps>';

	header('Content-Type: text/xml');
	echo $xml;

	$mysqli->close();
?>