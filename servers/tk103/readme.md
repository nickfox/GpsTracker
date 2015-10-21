Please note that I have included 2 PDF manuals within this directory. The first, gps306a_b-user manual, is the actual manual that came with the device we were testing. The second, coban GPRS protocol, is NOT the manual for the device but it is for a very similar device. The reason I have included it is because it explains the gprs protocol and messages very well and is useful knowledge.

tk103-server.php is the actual server. I will explain in a tutorial how it works.

socket-client.php can be used to test the server but is not required once the server is setup and running properly.

The gps-tracker directory has the wordpress plugin that is used to view the gps locations from the client. Please note that this is a specialized version of the wordpress plugin that ONLY works with the tk103-server and tk103 clients. This version of the plugin will NOT work with the wordpress android client.

this is a log of 3 gps data points received from the ODB gps device we were testing.

root@gps-tracker:~/gps-tracker# php tk103-server.php

new connection: 72.143.228.226:51380

total clients: 1

data: ##,imei:359710049095095,A;

sent LOAD to client

data: 359710049095095;

sent ON to client

data: imei:359710049095095,tracker,151022031050,,F,191050.000,A,5105.9833,N,11404.9573,W,0.01,0.00,,0,0,,,;

data: 359710049095095;

sent ON to client

data: imei:359710049095095,tracker,151022031150,,F,191150.000,A,5105.9832,N,11404.9571,W,0.02,0.00,,0,0,,,;

data: 359710049095095;

sent ON to client

data: imei:359710049095095,tracker,151022031250,,F,191250.000,A,5105.9832,N,11404.9571,W,0.02,0.00,,0,0,,,;

