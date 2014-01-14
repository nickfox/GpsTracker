-- MySQL dump 10.13  Distrib 5.5.34, for debian-linux-gnu (i686)
--
-- Host: localhost    Database: gpstracker
-- ------------------------------------------------------
-- Server version	5.5.34-0ubuntu0.12.04.1

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
) ENGINE=InnoDB AUTO_INCREMENT=85 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `gpslocations`
--

LOCK TABLES `gpslocations` WRITE;
/*!40000 ALTER TABLE `gpslocations` DISABLE KEYS */;
INSERT INTO `gpslocations` VALUES (3,'2014-01-08 11:52:25',48.856700,2.350800,'webUser','11137',137,0,25.0,'2007-10-17 18:37:00','0',95,'yes','webUser'),(4,'2014-01-08 11:52:54',48.856700,2.350800,'webUser','11137',137,0,25.0,'2007-10-17 18:37:00','0',95,'yes','webUser'),(5,'2014-01-08 11:53:01',48.856700,2.350800,'webUser','11137',137,0,25.0,'2007-10-17 18:37:00','0',95,'yes','webUser'),(6,'2014-01-08 11:53:09',48.856700,2.350800,'webUser','11137',137,0,25.0,'2007-10-17 18:37:00','0',95,'yes','webUser'),(7,'2014-01-08 12:03:29',48.856700,2.350800,'webUser','11137',137,0,25.0,'2007-10-17 18:37:00','0',95,'yes','webUser');
/*!40000 ALTER TABLE `gpslocations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'gpstracker'
--
/*!50003 DROP PROCEDURE IF EXISTS `prcDeleteRoute` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = latin1 */ ;
/*!50003 SET character_set_results = latin1 */ ;
/*!50003 SET collation_connection  = latin1_swedish_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`gpstracker_user`@`localhost` PROCEDURE `prcDeleteRoute`(
_sessionID VARCHAR(20),
_phoneNumber VARCHAR(25)
)
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
ALTER DATABASE `gpstracker` CHARACTER SET utf8 COLLATE utf8_general_ci ;
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
ALTER DATABASE `gpstracker` CHARACTER SET latin1 COLLATE latin1_swedish_ci ;
/*!50003 DROP PROCEDURE IF EXISTS `prcGetRouteForMap` */;
ALTER DATABASE `gpstracker` CHARACTER SET utf8 COLLATE utf8_general_ci ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`gpstracker_user`@`localhost` PROCEDURE `prcGetRouteForMap`(
_sessionID VARCHAR(50),
_phoneNumber VARCHAR(50))
BEGIN
  SELECT
  CONCAT('<locations latitude="', CAST(latitude AS CHAR),'" longitude="', CAST(longitude AS CHAR),
  '" speed="', CAST(speed AS CHAR), '" direction="', CAST(direction AS CHAR), '" distance="', CAST(distance AS CHAR),
  '" locationMethod="', locationMethod, '" gpsTime="', DATE_FORMAT(gpsTime, '%b %e %Y %h:%i%p'), '" phoneNumber="', phoneNumber,
  '" sessionID="', CAST(sessionID AS CHAR), '" accuracy="', CAST(accuracy AS CHAR), '" extraInfo="', extraInfo, '" />') xml
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
ALTER DATABASE `gpstracker` CHARACTER SET latin1 COLLATE latin1_swedish_ci ;
/*!50003 DROP PROCEDURE IF EXISTS `prcGetRoutes` */;
ALTER DATABASE `gpstracker` CHARACTER SET utf8 COLLATE utf8_general_ci ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`gpstracker_user`@`localhost` PROCEDURE `prcGetRoutes`()
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
  CONCAT('<route sessionID="', CAST(sessionID AS CHAR),  '" phoneNumber="', phoneNumber,
  '" times="(', DATE_FORMAT(startTime, '%b %e %Y %h:%i%p'), ' - ',
  DATE_FORMAT(endTime, '%b %e %Y %h:%i%p'), ')" />')
  FROM tempRoutes
  ORDER BY phoneNumber;

  DROP TABLE tempRoutes;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
ALTER DATABASE `gpstracker` CHARACTER SET latin1 COLLATE latin1_swedish_ci ;
/*!50003 DROP PROCEDURE IF EXISTS `prcGetUUID` */;
ALTER DATABASE `gpstracker` CHARACTER SET utf8 COLLATE utf8_general_ci ;
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
ALTER DATABASE `gpstracker` CHARACTER SET latin1 COLLATE latin1_swedish_ci ;
/*!50003 DROP PROCEDURE IF EXISTS `prcSaveGPSLocation` */;
ALTER DATABASE `gpstracker` CHARACTER SET utf8 COLLATE utf8_general_ci ;
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
ALTER DATABASE `gpstracker` CHARACTER SET latin1 COLLATE latin1_swedish_ci ;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2014-01-14 15:17:52
