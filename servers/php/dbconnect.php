<?php

$dbhost = 'localhost';
$dbuser = 'gpstracker_user';
$dbpass = 'gpstracker';
$dbname = 'gpstracker';

$mysqli = new mysqli($dbhost, $dbuser, $dbpass, $dbname);

if (mysqli_connect_errno()) {
	echo "Connection failed: " . mysqli_connect_error();
	exit();
}

?>
