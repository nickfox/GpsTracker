<?php

$dbuser = 'gpstracker_user';
$dbpass = 'gpstracker';
$params = array(PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION, 
                PDO::ATTR_EMULATE_PREPARES => false, 
                PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC);

$pdo = new PDO('mysql:host=localhost;dbname=gpstracker;charset=utf8', $dbuser, $dbpass, $params);

if (version_compare(PHP_VERSION, '5.3.6', '<')) {
    $pdo->exec('set names utf8');
}

?>