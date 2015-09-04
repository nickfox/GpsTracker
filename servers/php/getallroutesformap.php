<?php

    include 'dbconnect.php';

    switch ($dbType) {
        case DB_MYSQL:
            $stmt = $pdo->prepare('CALL prcGetAllRoutesForMap();');
            break;
        case DB_POSTGRESQL:
        case DB_SQLITE3:
            $stmt = $pdo->prepare('select * from v_GetAllRoutesForMap;');
            break;
    }

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