<?php
    include 'dbconnect.php';
    
    $sessionid   = isset($_GET['sessionid']) ? $_GET['sessionid'] : '0';

    switch ($dbType) {
        case DB_MYSQL:
            $stmt = $pdo->prepare($sqlFunctionCallMethod.'prcDeleteRoute(:sessionID)');     
            break;
        case DB_POSTGRESQL:
        case DB_SQLITE3:
            $stmt = $pdo->prepare('DELETE FROM gpslocations WHERE sessionID = :sessionID');     
            break;
    }

    $stmt->execute(array(':sessionID' => $sessionid));

?>
