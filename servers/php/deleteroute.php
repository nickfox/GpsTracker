<?php
    include 'dbconnect.php';
    
    $stmt = $pdo->prepare('CALL prcDeleteRoute(:sessionID, :phoneNumber)');     
    $stmt->execute(array(':sessionID' => $_GET['sessionID'], ':phoneNumber' => $_GET['phoneNumber']));

?>
