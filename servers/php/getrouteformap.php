<?php
    include 'dbconnect.php';
    
    $phonenumber = isset($_GET['phonenumber']) ? $_GET['phonenumber'] : '';
    $sessionid   = isset($_GET['sessionid']) ? $_GET['sessionid'] : '0';
    
    $stmt = $pdo->prepare('CALL prcGetRouteForMap(:sessionID, :phoneNumber)');     
    $stmt->execute(array(':sessionID' => $sessionid, ':phoneNumber' => $phonenumber));
    
    $json = '{ "locations": [';

    foreach ($stmt as $row) {
        $json .= $row['json'];
        $json .= ',';
    }

    $json = rtrim($json, ",");
    $json .= '] }';

    header('Content-Type: application/json');
    echo $json;
    
    // echo '{ "locations": [] }';
?>