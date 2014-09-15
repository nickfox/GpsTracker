<?php

$dbuser = 'gpstracker_user';
$dbpass = 'gpstracker';
$params = array(PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION, 
                PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC);

$pdo = new PDO('mysql:host=localhost;dbname=gpstracker2;charset=utf8', $dbuser, $dbpass, $params);

?>