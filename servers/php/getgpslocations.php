<?php

    include 'dbconnect.php';
    
    isset($_GET['sessionID']) ? $sessionID = $_GET['sessionID'] : $sessionID = '0';
    isset($_GET['phoneNumber']) ? $phoneNumber = $_GET['phoneNumber'] : $phoneNumber = '0';

    $query = 'CALL prcGetRouteForMap(\'' . $sessionID . '\',\''  . $phoneNumber  . '\')';
    
    $json = '{ "locations": [';

    // execute query
    if ($mysqli->multi_query($query)) {

        do {  // build our json array
            if ($result = $mysqli->store_result()) {
                while ($row = $result->fetch_row()) {
                    $json .= $row[0];
                    $json .= ',';
                }
                $result->close();
            }
        } while ($mysqli->more_results() && $mysqli->next_result());
    }
    else {
        die('error: '  . $mysqli->error);
    }

    $json = rtrim($json, ",");
    $json .= '] }';

    header('Content-Type: application/json');
    echo $json;

    $mysqli->close();
?>
