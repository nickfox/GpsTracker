<?php
	include 'dbconnect.php';

	isset($_GET['sessionID']) ? $sessionID = $_GET['sessionID'] : $sessionID = '0';
	isset($_GET['phoneNumber']) ? $phoneNumber = $_GET['phoneNumber'] : $phoneNumber = '0';

	$query = 'CALL prcDeleteRoute(\'' . $sessionID . '\',\''  . $phoneNumber  . '\')';

	// execute query
	if (!$mysqli->multi_query($query)) {
		die('$mysqli->multi_query: '  . $mysqli->error);
	}

	$mysqli->close();
?>
