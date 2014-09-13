<?php
    include 'dbconnect.php';
    
    $latitude       = isset($_POST['latitude']) ? $_POST['latitude'] : '0.0';
    $longitude      = isset($_POST['longitude']) ? $_POST['longitude'] : '0.0';
    $speed          = isset($_POST['speed']) ? $_POST['speed'] : '0';
    $direction      = isset($_POST['direction']) ? $_POST['direction'] : '0';
    $distance       = isset($_POST['distance']) ? $_POST['distance'] : '0';
    $date           = isset($_POST['date']) ? $_POST['date'] : '0000-00-00 00:00:00';
    $date           = urldecode($date);
    $locationmethod = isset($_POST['locationmethod']) ? $_POST['locationmethod'] : '';
    $locationmethod = urldecode($locationmethod);
    $phonenumber    = isset($_POST['phonenumber']) ? $_POST['phonenumber'] : '';
    $sessionid      = isset($_POST['sessionid']) ? $_POST['sessionid'] : '0';
    $accuracy       = isset($_POST['accuracy']) ? $_POST['accuracy'] : '0';
    $extrainfo      = isset($_POST['extrainfo']) ? $_POST['extrainfo'] : '';
    $eventtype      = isset($_POST['eventtype']) ? $_POST['eventtype'] : '';

    // from the phone
    $params = array(':latitude'        => $latitude,
                    ':longitude'       => $longitude,
                    ':speed'           => $speed,
                    ':direction'       => $direction,
                    ':distance'        => $distance,
                    ':date'            => $date,
                    ':locationmethod'  => $locationmethod,
                    ':phonenumber'     => $phonenumber,
                    ':sessionid'       => $sessionid,
                    ':accuracy'        => $accuracy,
                    ':extrainfo'       => $extrainfo,
                    ':eventtype'       => $eventtype
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
