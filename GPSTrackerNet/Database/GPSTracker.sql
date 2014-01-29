USE [GPSTracker]
GO
/****** Object:  Table [dbo].[gpslocations]    Script Date: 04/19/2008 09:33:23 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[gpslocations](
	[GPSLocationID] [int] IDENTITY(1,1) NOT NULL,
	[LastUpdate] [datetime] NOT NULL CONSTRAINT [DF_gpslocations_LastUpdate]  DEFAULT (getdate()),
	[Latitude] [decimal](10, 6) NOT NULL,
	[Longitude] [decimal](10, 6) NOT NULL,
	[phoneNumber] [varchar](20) NOT NULL,
	[sessionID] [varchar](25) NOT NULL,
	[speed] [int] NOT NULL,
	[direction] [int] NOT NULL,
	[distance] [int] NOT NULL,
	[gpsTime] [datetime] NOT NULL,
	[LocationMethod] [varchar](100) NOT NULL,
	[accuracy] [int] NOT NULL,
	[isLocationValid] [varchar](5) NOT NULL,
	[extraInfo] [varchar](255) NOT NULL,
 CONSTRAINT [PK_gpslocations] PRIMARY KEY CLUSTERED
(
	[GPSLocationID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
SET ANSI_PADDING OFF
GO
/****** Object:  StoredProcedure [dbo].[prcGetRoutes]    Script Date: 04/19/2008 09:33:23 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[prcGetRoutes]
AS

SET NOCOUNT ON

CREATE TABLE #tempRoutes
(
    sessionID VARCHAR(25) NULL,
    phoneNumber VARCHAR(20) NULL,
    startTime DATETIME NULL,
		endTime DATETIME NULL
)

-- get the distinct routes
INSERT #tempRoutes (sessionID, phoneNumber)
SELECT DISTINCT sessionID, phoneNumber
FROM gpslocations

-- get the route start times
UPDATE #tempRoutes
SET startTime = (SELECT MIN(gpsTime) FROM gpslocations gl
WHERE gl.sessionID = tr.sessionID
AND gl.phoneNumber = tr.phoneNumber)
FROM #tempRoutes tr

-- get the route end times
UPDATE #tempRoutes
SET endTime = (SELECT MAX(gpsTime) FROM gpslocations gl
WHERE gl.sessionID = tr.sessionID
AND gl.phoneNumber = tr.phoneNumber)
FROM #tempRoutes tr

-- format dates and then send it out as xml

SELECT sessionID '@sessionID',
phoneNumber '@phoneNumber',
'(' + CONVERT(VARCHAR(25), startTime, 100)
+ ' - ' +
CONVERT(VARCHAR(25), endTime, 100) + ')' '@times'
FROM #tempRoutes
ORDER BY phoneNumber
FOR XML PATH('route'), ROOT('routes')

DROP TABLE #tempRoutes
GO
/****** Object:  StoredProcedure [dbo].[prcDeleteRoute]    Script Date: 04/19/2008 09:33:23 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[prcDeleteRoute]
@sessionID VARCHAR(20),
@phoneNumber VARCHAR(25)
AS

SET NOCOUNT ON

DELETE FROM gpslocations
WHERE sessionID = @sessionID
AND phoneNumber = @phoneNumber
GO
/****** Object:  StoredProcedure [dbo].[prcGetRouteForMap]    Script Date: 04/19/2008 09:33:23 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[prcGetRouteForMap]
@sessionID VARCHAR(20),
@phoneNumber VARCHAR(25)
AS

SET NOCOUNT ON

  SELECT latitude '@latitude', longitude '@longitude',
  speed '@speed', direction '@direction', distance '@distance',
  locationMethod '@locationMethod', CONVERT(VARCHAR(25), gpsTime, 100) '@gpsTime',
	phoneNumber '@phoneNumber',  sessionID '@sessionID', accuracy '@accuracy',
	isLocationValid '@isLocationValid', extraInfo '@extraInfo'

  FROM gpslocations
	WHERE sessionID = @sessionID
	AND phoneNumber = @phoneNumber
	ORDER BY lastupdate
  FOR XML PATH('locations'), ROOT('gps')
GO
/****** Object:  StoredProcedure [dbo].[prcSaveGpsLocation2]    Script Date: 04/19/2008 09:33:23 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE	PROCEDURE [dbo].[prcSaveGpsLocation2]

@lat               VARCHAR(45),
@lng               VARCHAR(45),
@mph               VARCHAR(45),
@direction         VARCHAR(45),
@distance          VARCHAR(45),
@date              VARCHAR(100),
@locationMethod    VARCHAR(100),
@phoneNumber			 VARCHAR(20),
@sessionID				 VARCHAR(50),
@accuracy					 INT,
@locationIsValid	 VARCHAR(5),
@extraInfo				 VARCHAR(255)

AS

SET NOCOUNT ON

  DECLARE @returnValue INT
  SET @returnValue = 0

  INSERT INTO gpslocations (Latitude, Longitude, speed, direction, distance, gpsTime, locationMethod,
                            phoneNumber,  sessionID, accuracy, isLocationValid, extraInfo)
  VALUES (@lat, @lng, @mph, @direction, @distance, @date, @locationMethod,
					@phoneNumber, @sessionID, @accuracy, @locationIsValid, @extraInfo)

  SET @returnValue = IDENT_CURRENT('gpslocations')

  IF @returnValue > 0
    SELECT @returnValue
  ELSE
    SELECT 0
GO
