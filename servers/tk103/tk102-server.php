<?php

$ip_address = "0.0.0.0";
$port = "7331";

// open a server on port 7331
$server = stream_socket_server("tcp://$ip_address:$port", $errno, $errorMessage);

if ($server === false) {
    die("stream_socket_server error: $errorMessage");
}

$client_socks = array();

while (true) {
    // prepare readable sockets
    $read_socks = $client_socks;
    $read_socks[] = $server;

    // start reading and use a large timeout
    if(!stream_select($read_socks, $write, $except, 300000)) {
        die('stream_select error.');
    }

    // new client
    if(in_array($server, $read_socks)) {
        $new_client = stream_socket_accept($server);

        if ($new_client) {
            //print remote client information, ip and port number
            echo 'new connection: ' . stream_socket_get_name($new_client, true) . "\n";

          $client_socks[] = $new_client;
            echo "total clients: ". count($client_socks) . "\n";

            // $output = "hello new client.\n";
            // fwrite($new_client, $output);
        }

        //delete the server socket from the read sockets
        unset($read_socks[ array_search($server, $read_socks) ]);
    }

    // message from existing client
    foreach ($read_socks as $sock) {
        $data = fread($sock, 128);
        
        // imei:359710049095095,tracker,151006012336,,F,172337.000,A,5105.9792,N,11404.9599,W,0.01,322.56,,0,0,,,

        echo "data: " . $data . "\n";

        if (!$data) {
            unset($client_socks[ array_search($sock, $client_socks) ]);
            @fclose($sock);
            echo "client disconnected. total clients: ". count($client_socks) . "\n";
            continue;
        }

        // imei alone is a heartbeat message and may expect "ON" returned
        // in order to keep gprs connection
        // if you see a string starting with ##, must respond with "LOAD"
        

        //send the message back to client
        $response = "ON";
        fwrite($sock, $response);
    }
}