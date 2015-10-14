<?php

    $data = "imei:359710049095095,tracker,151006012336,,F,172337.000,A,5105.9792,N,11404.9599,W,0.01,322.56,,0,0,,,";
    
    $data1 = "359710049095095"; // heartbeat requires "ON" response
    
    $data2 = "##,imei:359710049095095,A"; // requires "LOAD" response
    
    // there is also a third message that needs to be sent out based on "help me" message type, line 112, https://github.com/tananaev/traccar/blob/master/src/org/traccar/protocol/Gps103ProtocolDecoder.java

    list($imei, $alarm, $date_time, , $gps_strength, $dont_know, $validity, $latitude, $latitude_direction, $longitude, $longitude_direction, $speed_in_knots, $bearing, , $switch1, $switch2, , , ) = explode(",", $data);
        
    print_r(substr($imei,5) . "\n");
    print_r(degree_to_decimal($latitude,$latitude_direction) . "\n"); // DDMM.MMMM
    print_r(degree_to_decimal($longitude,$longitude_direction) . "\n");

    date_default_timezone_set('UTC');

    print_r(nmea_to_mysql_time($date_time) . "\n"); // YYMMDDHHMMSS
    
    
    function nmea_to_mysql_time($date_time){
		$year = substr($date_time,0,2);
		$month = substr($date_time,2,2);
		$day = substr($date_time,4,2);
        $hour = substr($date_time,6,2);
    	$minute = substr($date_time,8,2);
    	$second = substr($date_time,10,2);
		
    	return date("Y-m-d H:i:s", mktime($hour,$minute,$second,$month,$day,$year));
    	}
           
    function degree_to_decimal($coordinates_in_degrees, $direction){
    		$degrees = (int)($coordinates_in_degrees / 100); 
    		$minutes = $coordinates_in_degrees - ($degrees * 100);
    		$seconds = $minutes / 60;
    		$coordinates_in_decimal = $degrees + $seconds;
    		
            if (($direction == "S") || ($direction == "W")) {
        		$coordinates_in_decimal = $coordinates_in_decimal * (-1);
    		}
            
        	return number_format($coordinates_in_decimal, 6,'.','');
    	}