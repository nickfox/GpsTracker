<?php

$dbuser = 'gpstraker_user';
$dbpass = 'ofi';
$params = array(PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION, 
                PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC);

$pdo = new PDO('mysql:host=localhost;dbname=gpstracker;charset=utf8', $dbuser, $dbpass, $params);

?>
