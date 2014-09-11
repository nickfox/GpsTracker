<?php
    include 'dbconnect.php';
    
    $stmt = $pdo->prepare('CALL prcGetRouteForMap(:sessionID, :phoneNumber)');     
    $stmt->execute(array(':sessionID' => $_GET['sessionID'], ':phoneNumber' => $_GET['phoneNumber']));
    
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