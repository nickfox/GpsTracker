<?php

    include 'dbconnect.php';
    
    $stmt = $pdo->prepare('CALL prcGetAllRoutesForMap();');
    $stmt->execute();

    $json = '{ "locations": [';

    foreach ($stmt as $row) {
        $json .= $row['json'];
        $json .= ',';
    }

    $json = rtrim($json, ",");
    $json .= '] }';

    header('Content-Type: application/json');
    echo $json;

?>