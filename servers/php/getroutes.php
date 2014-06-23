<?php
    include 'dbconnect.php';

    $query = 'CALL prcGetRoutes();';
    
    $json = '{ "routes": [';

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
