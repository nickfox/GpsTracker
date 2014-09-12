<?php
    include 'dbconnect.php';

    $stmt = $pdo->prepare('CALL prcGetRoutes();');
    $stmt->execute();

    $json = '{ "routes": [';

    foreach ($stmt as $row) {
        $json .= $row[0];
        $json .= ',';
    }
   
    $json = rtrim($json, ",");
    $json .= '] }';

    header('Content-Type: application/json');
    echo $json;
?>
