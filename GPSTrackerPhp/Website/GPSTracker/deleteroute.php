<?php
// Please leave the link below with the source code, thank you.
// http://www.websmithing.com/portal/Programming/tabid/55/articleType/ArticleView/articleId/6/Google-Map-GPS-Cell-Phone-Tracker-Version-2.aspx

	include 'dbconnect2.php';

	$query = 'CALL prcDeleteRoute(\'' . $_GET["sessionID"] . '\',\''  . $_GET["phoneNumber"]  . '\')';

	// execute query
	if (!$mysqli->multi_query($query)) {
		die('$mysqli->multi_query: '  . $mysqli->error);
	}

	$mysqli->close();
?>
