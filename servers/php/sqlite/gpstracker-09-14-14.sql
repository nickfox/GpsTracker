--------------------------------------------------------------
-- SQL for creating GpsTracker objext in a PostgreSQL database
--------------------------------------------------------------
DROP VIEW  IF EXISTS v_GetAllRoutesForMap;
DROP VIEW  IF EXISTS v_GetRouteForMap;
DROP VIEW  IF EXISTS v_GetRoutes;
DROP INDEX IF EXISTS sessionIDIndex;
DROP INDEX IF EXISTS phoneNumberIndex;
DROP INDEX IF EXISTS userNameIndex;

DROP TABLE IF EXISTS gpslocations;

CREATE TABLE gpslocations (
  GPSLocationID integer PRIMARY KEY autoincrement unique not null,
  lastUpdate timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  latitude double precision NOT NULL DEFAULT '0.0',
  longitude double precision NOT NULL DEFAULT '0.0',
  phoneNumber varchar(50) NOT NULL DEFAULT '',
  userName varchar(50) NOT NULL DEFAULT '',
  sessionID varchar(50) NOT NULL DEFAULT '',
  speed integer  NOT NULL DEFAULT '0',
  direction integer  NOT NULL DEFAULT '0',
  distance double precision NOT NULL DEFAULT '0.0',
  gpsTime timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  locationMethod varchar(50) NOT NULL DEFAULT '',
  accuracy integer  NOT NULL DEFAULT '0',
  extraInfo varchar(255) NOT NULL DEFAULT '',
  eventType varchar(50) NOT NULL DEFAULT ''
);
CREATE INDEX sessionIDIndex   ON gpslocations (sessionID);
CREATE INDEX phoneNumberIndex ON gpslocations (phoneNumber);
CREATE INDEX userNameIndex    ON gpslocations (userName);

-----------------------------------------------
-- Sample data for table gpslocations
-----------------------------------------------

INSERT INTO gpslocations VALUES 
(1,'2007-01-03 19:37:00',47.627327,-122.325691,'gpsTracker3','gpsTracker3','8BA21D90-3F90-407F-BAAE-800B04B1F5EB',0,0,0.0,'2007-01-03 19:37:00','na',137,'na','gpsTracker'),
(2,'2007-01-03 19:38:00',47.607258,-122.330077,'gpsTracker3','gpsTracker3','8BA21D90-3F90-407F-BAAE-800B04B1F5EB',0,0,0.0,'2007-01-03 19:38:00','na',137,'na','gpsTracker'),
(3,'2007-01-03 19:39:00',47.601703,-122.324670,'gpsTracker3','gpsTracker3','8BA21D90-3F90-407F-BAAE-800B04B1F5EB',0,0,0.0,'2007-01-03 19:39:00','na',137,'na','gpsTracker'),
(4,'0000-00-00 00:00:00',47.593757,-122.195074,'gpsTracker2','gpsTracker2','8BA21D90-3F90-407F-BAAE-800B04B1F5EC',0,0,0.0,'2007-01-03 19:40:00','na',137,'na','gpsTracker'),
(5,'2007-01-03 19:41:00',47.601397,-122.190353,'gpsTracker2','gpsTracker2','8BA21D90-3F90-407F-BAAE-800B04B1F5EC',0,0,0.0,'2007-01-03 19:41:00','na',137,'na','gpsTracker'),
(6,'2007-01-03 19:42:00',47.610020,-122.190697,'gpsTracker2','gpsTracker2','8BA21D90-3F90-407F-BAAE-800B04B1F5EC',0,0,0.0,'2007-01-03 19:42:00','na',137,'na','gpsTracker'),
(7,'2007-01-03 19:43:00',47.636631,-122.214558,'gpsTracker1','gpsTracker1','8BA21D90-3F90-407F-BAAE-800B04B1F5ED',0,0,0.0,'2007-01-03 19:43:00','na',137,'na','gpsTracker'),
(8,'2007-01-03 19:44:00',47.637961,-122.201769,'gpsTracker1','gpsTracker1','8BA21D90-3F90-407F-BAAE-800B04B1F5ED',0,0,0.0,'2007-01-03 19:44:00','na',137,'na','gpsTracker'),
(9,'2007-01-03 19:45:00',47.642935,-122.209579,'gpsTracker1','gpsTracker1','8BA21D90-3F90-407F-BAAE-800B04B1F5ED',0,0,0.0,'2007-01-03 19:45:00','na',137,'na','gpsTracker');


-----------------------------------------------
--   v_GetAllRoutesForMap --
-----------------------------------------------
CREATE VIEW v_GetAllRoutesForMap AS
  SELECT 
    gpslocations.sessionid,
    gpslocations.gpstime,
  '{  "latitude":"'  || CAST(latitude AS VARCHAR) || 
  '", "longitude":"' || CAST(longitude AS VARCHAR) ||
  '", "speed":"'     || CAST(speed AS VARCHAR) ||
  '", "direction":"' || CAST(direction AS VARCHAR) ||
  '", "distance":"'  || CAST(distance AS VARCHAR) || 
  '", "locationMethod":"' || locationMethod || 
  '", "gpsTime":"'   || gpsTime ||
  '", "userName":"'  || userName ||
  '", "phoneNumber":"' || phoneNumber || 
  '", "sessionID":"' || CAST(sessionID AS VARCHAR) ||
  '", "accuracy":"'  || CAST(accuracy AS VARCHAR) ||
  '", "extraInfo":"' || extraInfo || '" }'  json
   FROM ( SELECT max(gpslocations_1.gpslocationid) AS id
           FROM gpslocations gpslocations_1
          WHERE gpslocations_1.sessionid != '0' AND 
                length(gpslocations_1.sessionid) != 0 AND 
                gpslocations_1.gpstime != '0000-00-00 00:00:00'
          GROUP BY gpslocations_1.sessionid) maxid
     JOIN gpslocations ON gpslocations.gpslocationid = maxid.id
  ORDER BY gpslocations.gpstime;
 
 
 
-----------------------------------------------
--   v_GetRouteForMap --
-----------------------------------------------
CREATE VIEW v_GetRouteForMap AS
SELECT 
  sessionid, 
  lastupdate,
  '{ "latitude":"'   || CAST(latitude AS VARCHAR) || 
  '", "longitude":"' || CAST(longitude AS VARCHAR) ||
  '", "speed":"'     || CAST(speed AS VARCHAR) ||
  '", "direction":"' || CAST(direction AS VARCHAR) ||
  '", "distance":"'  || CAST(distance AS VARCHAR) || 
  '", "locationMethod":"' || locationMethod || 
  '", "gpsTime":"'   || gpsTime ||
  '", "userName":"'  || userName ||
  '", "phoneNumber":"' || phoneNumber || 
  '", "sessionID":"' || CAST(sessionID AS VARCHAR) ||
  '", "accuracy":"'  || CAST(accuracy AS VARCHAR) ||
  '", "extraInfo":"' || extraInfo || '" }'  json
  FROM gpslocations
  WHERE gpstime != '0000-00-00 00:00:00'
  ORDER BY lastupdate;
 ;

-----------------------------------------------
--   v_GetRoutes --
-----------------------------------------------
CREATE VIEW v_GetRoutes AS
select 
    '{ "sessionID": "'|| CAST(sessionID AS VARCHAR) ||
	'", "userName": "' || userName || 
	'", "times": "(' || startTime || 
	' - ' || endtime || ')" }' json
from (
  select 
    distinct sessionid, userName, 
	MIN(gpsTime) startTime, 
	MAX(gpsTime) endtime
 FROM gpslocations
 group by sessionid,username
 ORDER BY startTime DESC
)
AS routes
;

