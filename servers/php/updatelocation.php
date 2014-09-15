<?php
    include 'dbconnect.php';
    
    $latitude       = isset($_GET['latitude']) ? $_GET['latitude'] : '0.0';
    $longitude      = isset($_GET['longitude']) ? $_GET['longitude'] : '0.0';
    $speed          = isset($_GET['speed']) ? $_GET['speed'] : '0';
    $direction      = isset($_GET['direction']) ? $_GET['direction'] : '0';
    $distance       = isset($_GET['distance']) ? $_GET['distance'] : '0';
    $date           = isset($_GET['date']) ? $_GET['date'] : '0000-00-00 00:00:00';
    $date           = urldecode($date);
    $locationmethod = isset($_GET['locationmethod']) ? $_GET['locationmethod'] : '';
    $locationmethod = urldecode($locationmethod);
    $username       = isset($_GET['username']) ? $_GET['username'] : '';
    $phonenumber    = isset($_GET['phonenumber']) ? $_GET['phonenumber'] : '';
    $sessionid      = isset($_GET['sessionid']) ? $_GET['sessionid'] : '0';
    $accuracy       = isset($_GET['accuracy']) ? $_GET['accuracy'] : '0';
    $extrainfo      = isset($_GET['extrainfo']) ? $_GET['extrainfo'] : '';
    $eventtype      = isset($_GET['eventtype']) ? $_GET['eventtype'] : '';

    // from the phone
    $params = array(':latitude'        => $latitude,
                    ':longitude'       => $longitude,
                    ':speed'           => $speed,
                    ':direction'       => $direction,
                    ':distance'        => $distance,
                    ':date'            => $date,
                    ':locationmethod'  => $locationmethod,
                    ':username'        => $username,
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
                          :username, 
                          :phonenumber, 
                          :sessionid, 
                          :accuracy, 
                          :extrainfo, 
                          :eventtype);'
                      );
      
    $stmt->execute($params);
    $timestamp = $stmt->fetchColumn();
    echo $timestamp;    
?>
