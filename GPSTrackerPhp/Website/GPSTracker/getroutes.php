<?php
// Please leave the link below with the source code, thank you.
// http://www.websmithing.com/portal/Programming/tabid/55/articleType/ArticleView/articleId/6/Google-Map-GPS-Cell-Phone-Tracker-Version-2.aspx

	include 'dbconnect2.php';

	$query = 'CALL prcGetRoutes()';

	$xml = '<routes>';

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

	$xml .= '</routes>';

	header('Content-Type: text/xml');
	echo $xml;

	$mysqli->close();
?>
