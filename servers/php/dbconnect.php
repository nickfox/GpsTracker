<?php

$dbuser = 'gpstracker_user';
$dbpass = 'gpstracker';
$params = array(PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION, 
                PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_NUM);

$pdo = new PDO('mysql:host=localhost;dbname=gpstracker;charset=utf8', $dbuser, $dbpass, $params);

?>