<?php
/**
 * Handles GET requests from phone by setting up and endpoint /gpstracker/
 * There are two calls to the endpoint, nonce and location. Nonce creates a wordpress nonce based
 * on the session ID sent from the phone and location updates the database with location data
 * from the phone. A valid nonce is required to update the database.
 *
 * @package    Gps_Tracker
 * @subpackage Classes/Endpoint
 * @author     Nick Fox <nickfox@websmithing.com>
 * @license    MIT/GPLv2 or later
 * @link       https://www.websmithing.com/gps-tracker
 * @copyright  2014 Nick Fox
 */

// Exit if accessed directly
if ( ! defined( 'ABSPATH' ) ) exit;

/**
 * Gps_Tracker_Endpoint Class
 *
 * @since 1.0.0
 */
class Gps_Tracker_Endpoint {
        
	/**
	 * Set up the Gps Tracker Endpoint Class
	 *
	 * @since 1.0.0
	 */        
    public function __construct() {            
	    add_action( 'init',              array( $this, 'add_gpstracker_endpoint' ) );
		add_action( 'template_redirect', array( $this, 'process_gpstracker_query' ), -1 );
		add_filter( 'query_vars',        array( $this, 'gpstracker_query_vars' ) );
    }

	/**
	 * Registers a new rewrite endpoint for accessing the API
	 *
	 * @access public
	 * @param array $rewrite_rules WordPress Rewrite Rules
	 * @since 1.0.0
	 */
    public function add_gpstracker_endpoint( $rewrite_rules ) {
        add_rewrite_endpoint( 'gpstracker', EP_ROOT );
    }

	/**
	 * Listens for the GET requests and then processes the request
	 *
	 * @access public
	 * @global $wp_query
	 * @since 1.0.0
	 * @return void
	 */ 
    public function process_gpstracker_query() {
        global $wp_query;

        if ( ! isset($wp_query->query_vars['gpstracker'] ) ) {
            return;
        }
        
        switch ( $wp_query->query_vars['gpstracker'] ) {
            case 'nonce':
                $session_id = isset($wp_query->query_vars['sessionid']) ? $wp_query->query_vars['sessionid'] : '0';
                $session_id_pattern = '/^[0-9a-fA-F]{8}(?:-[0-9a-fA-F]{4}){3}-[0-9a-fA-F]{12}$/';    
      
                if ( preg_match($session_id_pattern, $session_id) ) {
                    echo wp_create_nonce($session_id);
                } else {
                    echo '0';
                }
                
                break;
            case 'location':
                $session_id = isset($wp_query->query_vars['sessionid']) ? $wp_query->query_vars['sessionid'] : '0';
                $wpnonce = isset($wp_query->query_vars['wpnonce']) ? $wp_query->query_vars['wpnonce'] : '1';
            
                if ( ! wp_verify_nonce($wpnonce, $session_id) ) {
                    echo '0';
                    exit;
                }
        
                $latitude = isset($wp_query->query_vars['latitude']) ? $wp_query->query_vars['latitude'] : '0.0';
                $latitude = (float)str_replace(",", ".", $latitude); // to handle European locale decimals
                $longitude = isset($wp_query->query_vars['longitude']) ? $wp_query->query_vars['longitude'] : '0.0';
                $longitude = (float)str_replace(",", ".", $longitude);
                $user_name = isset($wp_query->query_vars['username']) ? $wp_query->query_vars['username'] : 'wordpressUser';
                $phone_number = isset($wp_query->query_vars['phonenumber']) ? $wp_query->query_vars['phonenumber'] : '867-5309';
                $speed = isset($wp_query->query_vars['speed']) ? $wp_query->query_vars['speed'] : '0';
                $direction = isset($wp_query->query_vars['direction']) ? $wp_query->query_vars['direction'] : '0';
                $distance = isset($wp_query->query_vars['distance']) ? $wp_query->query_vars['distance'] : '0';
                $distance = (float)str_replace(",", ".", $distance);
                $gps_time = isset($wp_query->query_vars['gpstime']) ? urldecode($wp_query->query_vars['gpstime']) : '0000-00-00 00:00:00';
                $location_method = isset($wp_query->query_vars['locationmethod']) ? $wp_query->query_vars['locationmethod'] : '0';
                $accuracy = isset($wp_query->query_vars['accuracy']) ? $wp_query->query_vars['accuracy'] : '0';
                $extra_info = isset($wp_query->query_vars['extrainfo']) ? urldecode($wp_query->query_vars['extrainfo']) : '';
                $event_type = isset($wp_query->query_vars['eventtype']) ? $wp_query->query_vars['eventtype'] : 'wordpress';

                global $wpdb;
                $table_name = $wpdb->prefix . 'gps_locations';

                $wpdb->insert( 
            	$table_name, 
            	array( 
                    'latitude'          => $latitude, 
                    'longitude'         => $longitude,
                    'user_name'         => $user_name,
                    'phone_number'      => $phone_number,
                    'session_id'        => $session_id,
                    'speed'             => $speed,
                    'direction'         => $direction,
                    'distance'          => $distance,
                    'gps_time'          => $gps_time,
                    'location_method'   => $location_method,
                    'accuracy'          => $accuracy,
                    'extra_info'        => $extra_info,
                    'event_type'        => $event_type
            	), 
            	array( 
            		'%f', '%f', '%s', '%s', '%s', '%d', '%d', '%f', '%s', '%s', '%d', '%s', '%s'
            	    ) 
                );

                echo date('Y-m-d H:i:s');            
                break;
        }
        
        exit;
    }

	/**
	 * Registers query vars for API access
	 *
	 * @access public
	 * @since 1.0.0
	 * @param array $vars Query vars
	 * @return array $vars New query vars
	 */
    public function gpstracker_query_vars( $query_vars ) {
        $query_vars[] = 'latitude';
        $query_vars[] = 'longitude';
        $query_vars[] = 'username';
        $query_vars[] = 'phonenumber';        
        $query_vars[] = 'sessionid';
        $query_vars[] = 'speed';
        $query_vars[] = 'direction';
        $query_vars[] = 'distance';
        $query_vars[] = 'gpstime';
        $query_vars[] = 'locationmethod';
        $query_vars[] = 'accuracy';
        $query_vars[] = 'extrainfo';
        $query_vars[] = 'eventtype';
        $query_vars[] = 'wpnonce';        
        return $query_vars;
    }
} 
?>
