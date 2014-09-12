<?php
    include 'dbconnect.php';
    
    $phonenumber = isset($_GET['phonenumber']) ? $_GET['phonenumber'] : '';
    $sessionid   = isset($_GET['sessionid']) ? $_GET['sessionid'] : '0';
    
    $stmt = $pdo->prepare('CALL prcDeleteRoute(:sessionID, :phoneNumber)');     
    $stmt->execute(array(':sessionID' => $sessionid, ':phoneNumber' => $phonenumber));

?>
