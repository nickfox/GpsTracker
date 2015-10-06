<?php

// open a server on port 7331
$server = stream_socket_server("tcp://0.0.0.0:7331", $errno, $errorMessage);

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

        echo "data: " . $data . "\n";

        if (!$data) {
            unset($client_socks[ array_search($sock, $client_socks) ]);
            @fclose($sock);
            echo "client disconnected. total clients: ". count($client_socks) . "\n";
            continue;
        }

        //send the message back to client
        $response = "ok\n";
        fwrite($sock, $response);
    }
}