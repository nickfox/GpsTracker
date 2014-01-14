<?php
	include 'dbconnect.php';

	$query = 'CALL prcDeleteRoute(\'' . $_GET["sessionID"] . '\',\''  . $_GET["phoneNumber"]  . '\')';

	// execute query
	if (!$mysqli->multi_query($query)) {
		die('$mysqli->multi_query: '  . $mysqli->error);
	}

	$mysqli->close();
?>
