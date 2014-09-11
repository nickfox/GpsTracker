<?php
    include 'dbconnect.php';

    // from the phone
    $params = array(':latitude'        => $_POST['latitude'],
                    ':longitude'       => $_POST['longitude'],
                    ':speed'           => $_POST['speed'],
                    ':direction'       => $_POST['direction'],
                    ':distance'        => $_POST['distance'],
                    ':date'            => urldecode($_POST['date']),
                    ':locationmethod'  => urldecode($_POST['locationmethod']),
                    ':phonenumber'     => $_POST['phonenumber'],
                    ':sessionid'       => $_POST['sessionid'],
                    ':accuracy'        => $_POST['accuracy'],
                    ':extrainfo'       => $_POST['extrainfo'],
                    ':eventtype'       => $_POST['eventtype']
                );

    $stmt = $pdo->prepare('CALL prcSaveGPSLocation(
                          :latitude, 
                          :longitude, 
                          :speed, 
                          :direction, 
                          :distance, 
                          :date, 
                          :locationmethod, 
                          :phonenumber, 
                          :sessionid, 
                          :accuracy, 
                          :extrainfo, 
                          :eventtype);'
                      );
      
    $stmt->execute($params);
    echo '0';    
?>
