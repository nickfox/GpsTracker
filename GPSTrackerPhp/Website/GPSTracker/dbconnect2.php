<?php
// Please leave the link below with the source code, thank you.
// http://www.websmithing.com/portal/Programming/tabid/55/articleType/ArticleView/articleId/6/Google-Map-GPS-Cell-Phone-Tracker-Version-2.aspx

$dbhost = 'localhost';
$dbuser = 'user';
$dbpass = 'password';
$dbname = 'GPSTracker2';

$mysqli = new mysqli($dbhost, $dbuser, $dbpass, $dbname);

if (mysqli_connect_errno()) {
    echo "Connection failed: " . mysqli_connect_error();
    exit();
}

?>