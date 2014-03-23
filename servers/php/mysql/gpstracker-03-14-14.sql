-- MySQL dump 10.13  Distrib 5.5.35, for debian-linux-gnu (i686)
--
-- Host: localhost    Database: gpstracker2
-- ------------------------------------------------------
-- Server version	5.5.35-0ubuntu0.12.04.2

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `gpslocations`
--

DROP TABLE IF EXISTS `gpslocations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `gpslocations` (
  `GPSLocationID` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `LastUpdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `Latitude` decimal(10,6) NOT NULL DEFAULT '0.000000',
  `Longitude` decimal(10,6) NOT NULL DEFAULT '0.000000',
  `phoneNumber` varchar(50) NOT NULL DEFAULT '',
  `sessionID` varchar(50) NOT NULL DEFAULT '',
  `speed` int(10) unsigned NOT NULL DEFAULT '0',
  `direction` int(10) unsigned NOT NULL DEFAULT '0',
  `distance` decimal(10,1) NOT NULL DEFAULT '0.0',
  `gpsTime` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `LocationMethod` varchar(50) NOT NULL DEFAULT '',
  `accuracy` int(10) unsigned NOT NULL DEFAULT '0',
  `extraInfo` varchar(255) NOT NULL DEFAULT '',
  `eventType` varchar(50) NOT NULL DEFAULT '',
  PRIMARY KEY (`GPSLocationID`),
  KEY `sessionIDIndex` (`sessionID`),
  KEY `phoneNumberIndex` (`phoneNumber`)
) ENGINE=InnoDB AUTO_INCREMENT=1186 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `gpslocations`
--

LOCK TABLES `gpslocations` WRITE;
/*!40000 ALTER TABLE `gpslocations` DISABLE KEYS */;
INSERT INTO `gpslocations` VALUES (45,'2014-03-03 13:22:10',47.475931,-122.021119,'iosUser','8BA21D90-3F90-407F-BAAE-800B04B1F5EC',0,0,0.0,'2014-03-03 13:22:08','n/a',65,'altitude: 120m','ios'),(114,'2014-03-06 20:21:22',37.785834,-122.406417,'iosUser','9E260B81-57D5-4C36-AED0-34C5F7676A45',0,0,0.0,'2014-03-07 17:21:14','n/a',5,'altitude: 0m','ios'),(121,'2014-03-07 09:16:19',-41.322089,174.771919,'iosUser','09287DC1-D8B2-43F4-885A-9EB8DB0A7CE9',0,0,0.0,'2014-03-08 06:16:19','n/a',65,'altitude: 31m','ios'),(122,'2014-03-07 09:17:59',-41.322139,174.771739,'iosUser','25DE2DB6-FFCE-462F-A22D-25994B0D901A',0,0,0.0,'2014-03-08 06:18:01','n/a',50,'altitude: 29m','ios'),(135,'2014-03-07 21:54:22',37.818813,22.661872,'androidUser','94e63b1f-aa6f-439a-83d8-b57fabb28355',0,0,0.0,'2014-03-08 07:54:19','fused',2276,'0.0','android'),(136,'2014-03-07 21:54:34',37.818813,22.661872,'androidUser','712db5d9-d495-44d4-b2e7-3bd177ebf169',0,0,0.0,'2014-03-08 07:54:35','fused',2276,'0.0','android'),(179,'2014-03-08 11:24:15',37.818813,22.661872,'User-4427','5e1d94a0-c3fb-47b8-9846-4eedb7f12375',0,0,0.0,'2014-03-08 21:24:15','fused',2276,'0.0','android'),(192,'2014-03-08 12:24:12',51.922687,4.500411,'X-androidUser','2b9a3194-491b-448f-bcef-89cc8299d68b',0,0,0.0,'2014-03-08 21:24:08','fused',25,'0.0','android'),(200,'2014-03-08 12:47:38',51.922655,4.500412,'ELAWONEN','722ab5cc-9cd1-439b-abfa-5f82a7e119a1',0,0,0.0,'2014-03-08 21:47:33','fused',26,'0.0','android'),(212,'2014-03-09 15:44:55',12.968171,77.533864,'androidUser','98464f77-a247-4048-a9b2-3d1e4191cb9c',0,0,0.0,'2014-03-10 04:14:50','fused',25,'0.0','android'),(213,'2014-03-09 15:45:05',12.968171,77.533864,'androidUser','98464f77-a247-4048-a9b2-3d1e4191cb9c',0,0,0.0,'2014-03-10 04:15:02','fused',25,'0.0','android'),(214,'2014-03-09 15:45:17',12.968171,77.533864,'androidUser','98464f77-a247-4048-a9b2-3d1e4191cb9c',0,0,0.0,'2014-03-10 04:15:14','fused',25,'0.0','android'),(215,'2014-03-09 15:45:23',12.968171,77.533864,'androidUser','98464f77-a247-4048-a9b2-3d1e4191cb9c',0,0,0.0,'2014-03-10 04:15:20','fused',25,'0.0','android'),(216,'2014-03-09 15:45:26',12.968104,77.533922,'androidUser','5fe474b4-8c3f-44c9-bf34-ebd517c340ef',0,0,0.0,'2014-03-10 04:15:23','fused',14,'0.0','android'),(217,'2014-03-09 15:45:32',12.968100,77.533911,'androidUser','5fe474b4-8c3f-44c9-bf34-ebd517c340ef',0,0,0.0,'2014-03-10 04:15:29','fused',12,'0.0','android'),(218,'2014-03-09 15:45:44',12.968134,77.533898,'androidUser','5fe474b4-8c3f-44c9-bf34-ebd517c340ef',0,0,0.0,'2014-03-10 04:15:41','fused',10,'0.0','android'),(1131,'2014-03-10 20:36:48',12.967535,77.532635,'andy','677332d9-e137-4767-b3fa-bf5c121509f7',0,0,0.0,'2014-03-11 09:06:43','fused',834,'0.0','android'),(1144,'2014-03-11 18:38:29',12.890422,77.578681,'androidUser','68c85b1c-18e2-49de-9286-0afc4396af2c',0,0,0.0,'2014-03-12 07:08:27','fused',20,'0.0','android'),(1145,'2014-03-11 18:42:04',12.890421,77.578679,'androidUser','31d5e433-fec7-40e6-85ea-9445551079dd',0,0,0.0,'2014-03-12 07:12:02','fused',29,'0.0','android'),(1156,'2014-03-12 16:52:43',12.890422,77.578682,'androidUser','46693e80-7411-4f06-aec4-1edc76585c08',0,0,0.0,'2014-03-13 05:22:41','fused',27,'0.0','android'),(1159,'2014-03-12 17:58:07',12.890422,77.578679,'GalaxyUser989','01606372-4610-41c2-b5c0-d4c7ce638738',0,0,0.0,'2014-03-13 06:28:05','fused',28,'0.0','android'),(1167,'2014-03-13 07:14:42',17.398090,78.571595,'androidUser','09c8fe0a-be70-4dad-9e6a-feb4148aade3',0,0,0.0,'2014-03-13 19:44:38','fused',1073,'0.0','android'),(1168,'2014-03-13 07:27:57',17.398090,78.571595,'androidUser','016c6409-0d7f-46f3-b1aa-4bc16092c43b',0,0,0.0,'2014-03-13 19:57:54','fused',1073,'0.0','android'),(1173,'2014-03-13 08:01:54',17.398090,78.571595,'androidUser','3bbeecb7-c869-48b0-a161-8475c5bc18cf',0,0,0.0,'2014-03-13 20:31:50','fused',1073,'0.0','android'),(1174,'2014-03-13 08:02:54',17.398090,78.571595,'androidUser','3bbeecb7-c869-48b0-a161-8475c5bc18cf',0,0,0.0,'2014-03-13 20:32:50','fused',1073,'0.0','android'),(1175,'2014-03-13 08:04:30',17.398090,78.571595,'androidUser','8ac5faa3-ba48-4e93-a6d0-e8a8b63514d6',0,0,0.0,'2014-03-13 20:34:26','fused',1073,'0.0','android');
/*!40000 ALTER TABLE `gpslocations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'gpstracker2'
--
/*!50003 DROP PROCEDURE IF EXISTS `prcDeleteRoute` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `prcDeleteRoute`(
_sessionID VARCHAR(50),
_phoneNumber VARCHAR(50))
BEGIN
  DELETE FROM gpslocations
  WHERE sessionID = _sessionID
  AND phoneNumber = _phoneNumber
  ORDER BY lastupdate;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `prcGetAllGpsLocations` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`gpstracker_user`@`localhost` PROCEDURE `prcGetAllGpsLocations`()
BEGIN

SELECT * FROM gpslocations;

END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `prcGetRouteForMap` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `prcGetRouteForMap`(
_sessionID VARCHAR(50),
_phoneNumber VARCHAR(50))
BEGIN
  SELECT
  CONCAT('{ "latitude":"', CAST(latitude AS CHAR),'", "longitude":"', CAST(longitude AS CHAR), '", "speed":"', CAST(speed AS CHAR), '", "direction":"', CAST(direction AS CHAR), '", "distance":"', CAST(distance AS CHAR), '", "locationMethod":"', locationMethod, '", "gpsTime":"', DATE_FORMAT(gpsTime, '%b %e %Y %h:%i%p'), '", "phoneNumber":"', phoneNumber, '", "sessionID":"', CAST(sessionID AS CHAR), '", "accuracy":"', CAST(accuracy AS CHAR), '", "extraInfo":"', extraInfo, '" }') json
  FROM gpslocations
  WHERE sessionID = _sessionID
  AND phoneNumber = _phoneNumber
  ORDER BY lastupdate;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `prcGetRoutes` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `prcGetRoutes`()
BEGIN
  CREATE TEMPORARY TABLE tempRoutes (
    sessionID VARCHAR(50),
    phoneNumber VARCHAR(50),
    startTime DATETIME,
    endTime DATETIME)
  ENGINE = MEMORY;

  INSERT INTO tempRoutes (sessionID, phoneNumber)
  SELECT DISTINCT sessionID, phoneNumber
  FROM gpslocations;

  UPDATE tempRoutes tr
  SET startTime = (SELECT MIN(gpsTime) FROM gpslocations gl
  WHERE gl.sessionID = tr.sessionID
  AND gl.phoneNumber = tr.phoneNumber);

  UPDATE tempRoutes tr
  SET endTime = (SELECT MAX(gpsTime) FROM gpslocations gl
  WHERE gl.sessionID = tr.sessionID
  AND gl.phoneNumber = tr.phoneNumber);

  SELECT

  CONCAT('{ "sessionID": "', CAST(sessionID AS CHAR),  '", "phoneNumber": "', phoneNumber, '", "times": "(', DATE_FORMAT(startTime, '%b %e %Y %h:%i%p'), ' - ', DATE_FORMAT(endTime, '%b %e %Y %h:%i%p'), ')" }') json
  FROM tempRoutes
  ORDER BY startTime DESC;

  DROP TABLE tempRoutes;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `prcGetUUID` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`gpstracker_user`@`localhost` PROCEDURE `prcGetUUID`()
BEGIN
  SELECT UUID();
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `prcSaveGPSLocation` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`gpstracker_user`@`localhost` PROCEDURE `prcSaveGPSLocation`(
_lat VARCHAR(45),
_lng VARCHAR(45),
_mph VARCHAR(45),
_direction VARCHAR(45),
_distance VARCHAR(45),
_date VARCHAR(100),
_locationMethod VARCHAR(100),
_phoneNumber VARCHAR(20),
_sessionID VARCHAR(50),
_accuracy VARCHAR(20),
_extraInfo VARCHAR(255),
_eventType VARCHAR(50)
)
BEGIN
  INSERT INTO gpslocations (latitude, longitude, speed, direction, distance, gpsTime, locationMethod, phoneNumber,  sessionID, accuracy, extraInfo, eventType)
  VALUES (_lat, _lng, _mph, _direction, _distance, _date, _locationMethod, _phoneNumber, _sessionID, _accuracy, _extraInfo, _eventType);
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2014-03-13 11:42:37
