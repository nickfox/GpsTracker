<?php
/**
 * Handles activation/deactivation of plugin
 *
 * @package    Gps_Tracker
 * @subpackage Classes/Setup
 * @author     Nick Fox <nickfox@websmithing.com>
 * @license    MIT/GPLv2 or later
 * @link       https://www.websmithing.com/gps-tracker
 * @copyright  2014 Nick Fox
 */

// Exit if accessed directly
if ( ! defined( 'ABSPATH' ) ) exit;

/**
 * Gps_Tracker_Setup Class
 *
 * @since 1.0.0
 */
class Gps_Tracker_Setup
{
	/**
	 * Fired when the plugin is activated. Create table for Gps Tracker and two stored procedures.
     * One to get all the routes for display in the drop down box and the other to get a single
     * route in geojson format to create the map and populate the markers.
	 *
	 * @since 1.0.0
     * @global $wpdb
     * @global $charset_collate
     * @return void
	 */
    public static function activate()
    {
        // clear the permalinks©
        flush_rewrite_rules();
        
        if ( ! current_user_can( 'activate_plugins' ) )
            return;
        $plugin = isset($_REQUEST['plugin']) ? $_REQUEST['plugin'] : '';
        check_admin_referer( "activate-plugin_{$plugin}" );

        global $wpdb;
        global $charset_collate;
        require_once( ABSPATH . 'wp-admin/includes/upgrade.php' ); 
        $table_name = $wpdb->prefix . 'gps_locations';
        
        $sql = "DROP TABLE IF EXISTS {$table_name};  
          CREATE TABLE {$table_name} (
          gps_location_id int(10) unsigned NOT NULL AUTO_INCREMENT,
          last_update timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
          latitude decimal(10,7) NOT NULL DEFAULT '0.0000000',
          longitude decimal(10,7) NOT NULL DEFAULT '0.0000000',
          user_name varchar(50) NOT NULL DEFAULT '',
          phone_number varchar(50) NOT NULL DEFAULT '',          
          session_id varchar(50) NOT NULL DEFAULT '',
          speed int(10) unsigned NOT NULL DEFAULT '0',
          direction int(10) unsigned NOT NULL DEFAULT '0',
          distance decimal(10,1) NOT NULL DEFAULT '0.0',
          gps_time timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
          location_method varchar(50) NOT NULL DEFAULT '',
          accuracy int(10) unsigned NOT NULL DEFAULT '0',
          extra_info varchar(255) NOT NULL DEFAULT '',
          event_type varchar(50) NOT NULL DEFAULT '',
          UNIQUE KEY (gps_location_id),
          KEY session_id_index (session_id),
          KEY user_name_index (user_name)
        ) $charset_collate;";

        dbDelta($sql);
        
        $location_row_count = $wpdb->get_var( "SELECT COUNT(*) FROM {$table_name};" );
        
        if ( 0 == $location_row_count ) {
            $sql = "INSERT INTO {$table_name} VALUES (1,'2007-01-03 11:37:00',47.627327,-122.325691,'wordpressUser3','3BA21D90-3F90-407F-BAAE-800B04B1F5EB','8BA21D90-3F90-407F-BAAE-800B04B1F5EB',0,0,0.0,'2007-01-03 11:37:00','na',137,'na','wordpress');"; 
            $wpdb->query($sql);
            $sql = "INSERT INTO {$table_name} VALUES (2,'2007-01-03 11:38:00',47.607258, -122.330077,'wordpressUser3','3BA21D90-3F90-407F-BAAE-800B04B1F5EB','8BA21D90-3F90-407F-BAAE-800B04B1F5EB',0,0,0.0,'2007-01-03 11:38:00','na',137,'na','wordpress');"; 
            $wpdb->query($sql);            
            $sql = "INSERT INTO {$table_name} VALUES (3,'2007-01-03 11:39:00',47.601703, -122.324670,'wordpressUser3','3BA21D90-3F90-407F-BAAE-800B04B1F5EB','8BA21D90-3F90-407F-BAAE-800B04B1F5EB',0,0,0.0,'2007-01-03 11:39:00','na',137,'na','wordpress');"; 
            $wpdb->query($sql);

            $sql = "INSERT INTO {$table_name} VALUES (4,'2007-01-03 11:40:00',47.593757, -122.195074,'wordpressUser2','2BA21D90-3F90-407F-BAAE-800B04B1F5EC','8BA21D90-3F90-407F-BAAE-800B04B1F5EC',0,0,0.0,'2007-01-03 11:40:00','na',137,'na','wordpress');"; 
            $wpdb->query($sql);
            $sql = "INSERT INTO {$table_name} VALUES (5,'2007-01-03 11:41:00',47.601397, -122.190353,'wordpressUser2','2BA21D90-3F90-407F-BAAE-800B04B1F5EC','8BA21D90-3F90-407F-BAAE-800B04B1F5EC',0,0,0.0,'2007-01-03 11:41:00','na',137,'na','wordpress');"; 
            $wpdb->query($sql);            
            $sql = "INSERT INTO {$table_name} VALUES (6,'2007-01-03 11:42:00',47.610020, -122.190697,'wordpressUser2','2BA21D90-3F90-407F-BAAE-800B04B1F5EC','8BA21D90-3F90-407F-BAAE-800B04B1F5EC',0,0,0.0,'2007-01-03 11:42:00','na',137,'na','wordpress');"; 
            $wpdb->query($sql);
            
            $sql = "INSERT INTO {$table_name} VALUES (7,'2007-01-03 11:43:00',47.636631, -122.214558,'wordpressUser1','1BA21D90-3F90-407F-BAAE-800B04B1F5ED','8BA21D90-3F90-407F-BAAE-800B04B1F5ED',0,0,0.0,'2007-01-03 11:43:00','na',137,'na','wordpress');"; 
            $wpdb->query($sql);
            $sql = "INSERT INTO {$table_name} VALUES (8,'2007-01-03 11:44:00',47.637961, -122.201769,'wordpressUser1','1BA21D90-3F90-407F-BAAE-800B04B1F5ED','8BA21D90-3F90-407F-BAAE-800B04B1F5ED',0,0,0.0,'2007-01-03 11:44:00','na',137,'na','wordpress');"; 
            $wpdb->query($sql);            
            $sql = "INSERT INTO {$table_name} VALUES (9,'2007-01-03 11:45:00',47.642935, -122.209579,'wordpressUser1','1BA21D90-3F90-407F-BAAE-800B04B1F5ED','8BA21D90-3F90-407F-BAAE-800B04B1F5ED',0,0,0.0,'2007-01-03 11:45:00','na',137,'na','wordpress');"; 
            $wpdb->query($sql);                                               
        }
        
        $procedure_name =  $wpdb->prefix . "get_routes";
        $wpdb->query( "DROP PROCEDURE IF EXISTS {$procedure_name};" ); 
           
        $sql = "CREATE PROCEDURE {$procedure_name}()
        BEGIN
        CREATE TEMPORARY TABLE temp_routes (
            session_id VARCHAR(50),
            user_name VARCHAR(50),
            start_time DATETIME,
            end_time DATETIME)
            ENGINE = MEMORY;

        INSERT INTO temp_routes (session_id, user_name)
        SELECT DISTINCT session_id, user_name
        FROM {$table_name};

        UPDATE temp_routes tr
        SET start_time = (SELECT MIN(gps_time) FROM {$table_name} gl
        WHERE gl.session_id = tr.session_id
        AND gl.user_name = tr.user_name);

        UPDATE temp_routes tr
        SET end_time = (SELECT MAX(gps_time) FROM {$table_name} gl
        WHERE gl.session_id = tr.session_id
        AND gl.user_name = tr.user_name);

        SELECT
        CONCAT('{ \"session_id\": \"', CAST(session_id AS CHAR),  '\", \"user_name\": \"', user_name, '\", \"times\": \"(', DATE_FORMAT(start_time, '%b %e %Y %h:%i%p'), ' - ', DATE_FORMAT(end_time, '%b %e %Y %h:%i%p'), ')\" }') json
        FROM temp_routes
        ORDER BY start_time DESC;

        DROP TABLE temp_routes;
        END;";                

        $wpdb->query( $sql ); 
        // $wpdb->print_error();
        
        $procedure_name =  $wpdb->prefix . "get_geojson_route";
        $wpdb->query("DROP PROCEDURE IF EXISTS {$procedure_name};");
      
        $sql = "CREATE PROCEDURE {$procedure_name}(
        _session_id VARCHAR(50))
        BEGIN
        SET @counter := 0;
        SELECT
        CONCAT('{\"type\": \"Feature\", \"id\": \"', CAST(session_id AS CHAR), '\", \"properties\": {\"speed\": ', CAST(speed AS CHAR), ', \"direction\": ', CAST(direction AS CHAR), ', \"distance\": ', CAST(distance AS CHAR), ', \"location_method\": \"', CAST(location_method AS CHAR), '\", \"gps_time\": \"', DATE_FORMAT(gps_time, '%b %e %Y %h:%i%p'), '\", \"user_name\": \"', CAST(user_name AS CHAR), '\", \"phone_number\": \"', CAST(phone_number AS CHAR), '\", \"accuracy\": ', CAST(accuracy AS CHAR), ', \"geojson_counter\": ', @counter := @counter + 1, ', \"extra_info\": \"', CAST(extra_info AS CHAR), '\"}, \"geometry\": {\"type\": \"Point\", \"coordinates\": [', CAST(longitude AS CHAR), ', ', CAST(latitude AS CHAR), ']}}') geojson 
        FROM {$table_name}
        WHERE session_id = _session_id
        ORDER BY last_update;
        END;";

        $wpdb->query( $sql );
        
        $procedure_name =  $wpdb->prefix . "get_all_geojson_routes";
        $wpdb->query("DROP PROCEDURE IF EXISTS {$procedure_name};");
      
        $sql = "CREATE PROCEDURE {$procedure_name}()
        BEGIN
        SET @counter := 0;
        SELECT
        session_id,
        gps_time,
        CONCAT('{\"type\": \"Feature\", \"id\": \"', CAST(session_id AS CHAR), '\", \"properties\": {\"speed\": ', CAST(speed AS CHAR), ', \"direction\": ', CAST(direction AS CHAR), ', \"distance\": ', CAST(distance AS CHAR), ', \"location_method\": \"', CAST(location_method AS CHAR), '\", \"gps_time\": \"', DATE_FORMAT(gps_time, '%b %e %Y %h:%i%p'), '\", \"user_name\": \"', CAST(user_name AS CHAR), '\", \"phone_number\": \"', CAST(phone_number AS CHAR), '\", \"accuracy\": ', CAST(accuracy AS CHAR), ', \"geojson_counter\": ', @counter := @counter + 1, ', \"extra_info\": \"', CAST(extra_info AS CHAR), '\"}, \"geometry\": {\"type\": \"Point\", \"coordinates\": [', CAST(longitude AS CHAR), ', ', CAST(latitude AS CHAR), ']}}') geojson 
        FROM (SELECT MAX(gps_location_id) ID
        FROM {$table_name}  
        WHERE session_id != '0' && CHAR_LENGTH(session_id) != 0 && gps_time != '0000-00-00 00:00:00'
        GROUP BY session_id) AS MaxID
        JOIN {$table_name} ON {$table_name}.gps_location_id = MaxID.ID
        ORDER BY gps_time;
        END;";

        $wpdb->query( $sql );
        
        $procedure_name =  $wpdb->prefix . "delete_route";
        $wpdb->query("DROP PROCEDURE IF EXISTS {$procedure_name};");

        $sql = "CREATE PROCEDURE {$procedure_name}(
        _session_id VARCHAR(50))
        BEGIN
        DELETE FROM {$table_name}
        WHERE sessionID = _sessionID;
        END;";
        
	$wpdb->query( $sql );

	$procedure_name =  $wpdb->prefix . "save_gps_location";
	$wpdb->query("DROP PROCEDURE IF EXISTS {$procedure_name};");

	$sql = "CREATE PROCEDURE {$procedure_name}(
	_latitude DECIMAL(10,7),
	_longitude DECIMAL(10,7),
	_user_name VARCHAR(50),
	_phone_number VARCHAR(50),
	_session_id VARCHAR(50),
	_speed INT(10),
	_direction INT(10),
	_distance DECIMAL(10,1),
	_gps_time TIMESTAMP,
	_location_method VARCHAR(50),
	_accuracy INT(10),
	_extra_info VARCHAR(255),
	_event_type VARCHAR(50)
	)
	BEGIN
   	INSERT INTO {$table_name} (latitude, longitude, user_name, phone_number, session_id, speed, direction, distance, gps_time, location_method, accuracy, extra_info, event_type)
   	VALUES (_latitude, _longitude, _user_name, _phone_number, _session_id, _speed, _direction, _distance, _gps_time, _location_method, _accuracy, _extra_info, _event_type);
   	SELECT NOW();
	END;";

        $wpdb->query( $sql ); 
        
        // make sure this is last AFTER the stored procedures or wrong table name gets used
        $table_name = $wpdb->prefix . 'gps_logger';
        $sql = "DROP TABLE IF EXISTS {$table_name};  
          CREATE TABLE {$table_name} (
          gps_logger_id int(10) unsigned NOT NULL AUTO_INCREMENT,
          last_update timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
          gps_action varchar(5) NOT NULL DEFAULT '',
          phone_number varchar(50) NOT NULL DEFAULT '',
          app_id varchar(50) NOT NULL DEFAULT '',          
          session_id varchar(50) NOT NULL DEFAULT '',
          nonce varchar(50) NOT NULL DEFAULT '',
          UNIQUE KEY (gps_logger_id)
        ) $charset_collate;";

        dbDelta( $sql ); 
    }

	/**
	 * Fired when the plugin is deactivated.
	 *
	 * @since 1.0.0
	 *
	 */
    public static function deactivate()
    {
        if ( ! current_user_can( 'activate_plugins' ) )
            return;
        $plugin = isset($_REQUEST['plugin']) ? $_REQUEST['plugin'] : '';
        check_admin_referer( "deactivate-plugin_{$plugin}" );

        // uncomment during development
       
        global $wpdb;
        $table_name = $wpdb->prefix . 'gps_locations';
        $sql = "DROP TABLE IF EXISTS {$table_name};";
        $wpdb->query($sql);
    
        $table_name = $wpdb->prefix . 'gps_logger';
        $sql = "DROP TABLE IF EXISTS {$table_name};";
        $wpdb->query($sql);
    
        $procedure_name =  $wpdb->prefix . "get_routes";
        $wpdb->query("DROP PROCEDURE IF EXISTS {$procedure_name};");
    
        $procedure_name =  $wpdb->prefix . "get_geojson_route";
        $wpdb->query("DROP PROCEDURE IF EXISTS {$procedure_name};");
        
        $procedure_name =  $wpdb->prefix . "get_all_geojson_routes";
        $wpdb->query("DROP PROCEDURE IF EXISTS {$procedure_name};");
        
        $procedure_name =  $wpdb->prefix . "delete_route";
        $wpdb->query("DROP PROCEDURE IF EXISTS {$procedure_name};");
        
        $table_name = $wpdb->prefix . 'gps_logger';
        $sql = "DROP TABLE IF EXISTS {$table_name};";
        $wpdb->query($sql);
        
        delete_option( 'gpstracker_app_id' );

    }
}
