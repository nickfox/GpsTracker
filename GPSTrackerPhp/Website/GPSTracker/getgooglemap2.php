<?php
// Please leave the link below with the source code, thank you.
// http://www.websmithing.com/portal/Programming/tabid/55/articleType/ArticleView/articleId/6/Google-Map-GPS-Cell-Phone-Tracker-Version-2.aspx

	include 'dbconnect2.php';

	// you need to replace this with your own key from Google
	$key = 'ABQIAAAAQ35Hu3xqOoeD50UMgBW0cBQEt3eA6mol2Np5q6SAKw0EDVXpM9hRExX__LZW3RbLXHuLKZlwC0oypOw';
	$url = 'http://maps.google.com/staticmap';

	// this is from the queryString that was sent by the phone
	$height = $_GET['h'];
	$width = $_GET['w'];
	$lat = $_GET['lat'];
	$lng = $_GET['lng'];
	$zoom = $_GET['zm'];
	$mph = $_GET['mph'];
	$direction = $_GET['dir'];
	$distance = $_GET['dis'];
	$date = urldecode($_GET['dt']);
	$locationMethod = urldecode($_GET['lm']);
	$date = getDateFromJavaDate($date);
	$phoneNumber = $_GET['pn'];
	$sessionID = $_GET['sid'];
	$accuracy = $_GET['acc'];
	$locationIsValid = $_GET['iv'];
	$extraInfo = $_GET['info'];

	// save the gps location to the database
	$query = 'CALL prcSaveGPSLocation2(\''
	  . $lat  . '\',\''
	  . $lng  . '\',\''
	  . $mph  . '\',\''
	  . $direction  . '\',\''
	  . $distance  . '\',\''
	  . getDateFromJavaDate($date)  . '\',\''
	  . $locationMethod  . '\',\''
	  . $phoneNumber  . '\',\''
	  . $sessionID  . '\',\''
	  . $accuracy  . '\',\''
	  . $locationIsValid  . '\',\''
	  . $extraInfo . '\')';

	if (!$mysqli->multi_query($query)) {
		die('$mysqli->multi_query: '  . $mysqli->error);
	}

	$mysqli->close();

	// build the Google map url
	$mapUrl = $url
	  . '?markers='
	  . $lat
	  . ','
	  . $lng
	  . ',blueu&zoom='
	  . $zoom
	  . '&size='
	  . $width
	  . 'x'
	  . $height
	  . '&maptype=mobile&key='
	  . $key;

	// get the map image
	$map = imageCreateFromGIF($mapUrl);

	// send the map put as a png, java me is required to handle png images
	header('Content-type: image/png');
	imagePNG($map);
	imageDestroy($map);


	function getDateFromJavaDate($theDate) {
		// need to convert from this: Fri May 19 21:03:48 GMT-08:00 2007
		// to this: 2008-04-17 12:07:02, MySQL DATETIME

		$months = array("1"=>"Jan", "2"=>"Feb", "3"=>"Mar", "4"=>"Apr", "5"=>"May", "6"=>"Jun",
			            "7"=>"Jul" ,"8"=>"Aug", "9"=>"Sep", "10"=>"Oct", "11"=>"Nov", "12"=>"Dec");

        $hour = substr($theDate, 11, 2);
        $minute = substr($theDate, 14, 2);
        $day = substr($theDate, 8, 2);
        $month = substr($theDate, 4, 3);
        $year = substr($theDate, strlen($theDate) - 4, 4);

        $format = '%Y-%m-%d %I:%M:%S';

		return strftime($format, mktime($hour, $minute, 0, array_search($month, $months), $day, $year));
	}

?>
