<?php

	include 'dbconnect.php';

	$query = 'CALL prcGetRouteForMap(\'' . $_GET["sessionID"] . '\',\''  . $_GET["phoneNumber"]  . '\')';
	
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