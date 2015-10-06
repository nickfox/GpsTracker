/tk102 data from Brent's device. note that data received is not always complete.

new connection: 24.114.27.177:23299
total clients: 1
data: ##,imei:359710049095095,A;
data: imei:359710049095095,tracker,151006010036,,F,170036.000,A,5105.9768,N,11404.9571,W,0.00,322.56,,0,0,,,;
data: imei:359710049095095,tracker,151006010106,,F,170107.000,A,5105.9767,N,11404.9572,W,0.02,322.56,,0,0,,,;
data: 359710049095095;
data: imei:359710049095095,tracker,151006010136,,F,170136.000,A,5105.9760,N,11404.9565,W,0.01,322.56,,0,0,,,;
data: imei:359710049095095,tracker,151006010206,,F,170206.000,A,5105.9747,N,11404.9572,W,0.01,322.56,,0,0,,,;
data: 359710049095095;
data: imei:359710049095095,tracker,151006010236,,F,170236.000,A,5105.9736,N,11404.9581,W,0.01,322.56,,0,0,,,;
data: imei:359710049095095,tracker,151006010306,,F,170306.000,A,5105.9736,N,11404.9581,W,0.00,322.56,,0,0,,,;
data: 359710049095095;

// tk102 data from other device
1203292316,0031698765432,GPRMC,211657.000,A,5213.0247,N,00516.7757,E,0.00,273.30,290312,,,A*62,F,imei:123456789012345,123

The message sent by tracker to IP address is as following:
Serial number + authorized number + GPRMC + GPS signal indicator + command + IMEI number + CRC16 checksum.
* 090723164830 = Serial #(year,month,date,hour,minute,second)
* +13616959853 = Authorized #
* GPRMC,214830.000,A,3017.2558,N,09749.4888,W,26.9,108.8,230709,,,A*61 = standard GPRMC sentences.
* F = GPS signal is full, if it indicate "L", means GPS signal is low
* Help me = It is the SOS message, and this section is blank for tracking message
* IMEI = Identifying # of the tracker
* 05 = Means you get 5 GPS fix (from 3 to 10)
* 264.5 = Altitude
* F:3.79V = Battery voltage
* 0= Means the tracker is NOT charged. 1 means the tracker is charged
* 122= The length of the GPRS string
* 13990 = Checksum
* 310 = MCC Mobile Country Code
* 01 = MNC Mobile Network Code
* 0AB0 =LAC Location areacode
* 345A = CellID