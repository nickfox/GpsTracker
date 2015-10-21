<?php
/**
 * Handles all the ajax requests for the Gps Tracker map, dropdown boxes and buttons
 *
 * @package    Gps_Tracker
 * @subpackage Classes/Ajax
 * @author     Nick Fox <nickfox@websmithing.com>
 * @license    MIT/GPLv2 or later
 * @link       https://www.websmithing.com/gps-tracker
 * @copyright  2014 Nick Fox
 */

// Exit if accessed directly
if ( ! defined( 'ABSPATH' ) ) exit;

/**
 * Gps_Tracker_Ajax Class
 *
 * @since 1.0.0
 */
class Gps_Tracker_Ajax {
        
	/**
	 * Set up the Gps Tracker Ajax Class
	 *
	 * @since 1.0.0
	 */        
    public function __construct() {            
        add_action( 'wp_ajax_get_routes',                    array( $this, 'get_gps_routes' ) );
        add_action( 'wp_ajax_nopriv_get_routes',             array( $this, 'get_gps_routes' ) ); 
        add_action( 'wp_ajax_get_all_geojson_routes',        array( $this, 'get_all_geojson_routes' ) );
        add_action( 'wp_ajax_nopriv_get_all_geojson_routes', array( $this, 'get_all_geojson_routes' ) );
        add_action( 'wp_ajax_get_geojson_route',             array( $this, 'get_geojson_route' ) );
        add_action( 'wp_ajax_nopriv_get_geojson_route',      array( $this, 'get_geojson_route' ) );
        add_action( 'wp_ajax_delete_route',                  array( $this, 'delete_gps_route' ) );
        add_action( 'wp_ajax_nopriv_delete_route',           array( $this, 'delete_gps_route' ) );
        
        
    }

	/**
	 * Get all gps routes from the database, triggered when the plugin shortcode is executed
	 *
	 * @access public
	 * @since 1.0.0
	 */
    public function get_gps_routes() {
        //exit(var_dump($_POST));
        
        if ( ! wp_verify_nonce( $_POST['get_routes_nonce'], 'get-routes-nonce' ) ) {
            exit;
        } else {          
            header( 'Content-Type: application/json' );
        
            global $wpdb;
            $procedure_name =  $wpdb->prefix . 'get_routes';
            $gps_routes = $wpdb->get_results("CALL {$procedure_name};"); 
       
            if ( 0 == $wpdb->num_rows ) {
                echo '0';
                exit;
            }
       
            $json = '{"routes": [';
           
            foreach ($gps_routes as $route) {
                $json .= $route->json;
                $json .= ','; 
            }
        
            $json = rtrim($json, ',');
            $json .= ']}';   

            echo $json;
            exit;
        }
    }

	/**
	 * Gets a single gps route in geojson format from the database, triggered by selecting
     * a route in the route dropdown box under the map.
	 *
	 * @access public
	 * @since 1.0.0
	 */    
    public function get_geojson_route() {
        if ( ! wp_verify_nonce( $_POST['get_geojson_route_nonce'], 'get-geojson-route-nonce' ) ) {
            exit;
        } else {
            header( 'Content-Type: application/json' );

            global $wpdb;
            $session_id = $_POST['session_id'];
            $procedure_name =  $wpdb->prefix . 'get_geojson_route';
            $gps_locations = $wpdb->get_results($wpdb->prepare(
                "CALL {$procedure_name}(%s);", 
                array(
                    $session_id
                )
            )); 
       
            if ( 0 == $wpdb->num_rows ) {
                echo '0';
                exit;
            }
       
            $json = '{"type": "FeatureCollection", "features": [';
           
            foreach ( $gps_locations as $location ) {
                $json .= $location->geojson;
                $json .= ','; 
            }
        
            $json = rtrim($json, ',');
            $json .= ']}';   
                      
            echo $json;
            exit;
        }            
    }


	/**
	 * Get all routes in geojson format when page loads or when called from view all button
	 *
	 * @access public
	 * @since 1.0.0
	 */    
    public function get_all_geojson_routes() {
        if ( ! wp_verify_nonce( $_POST['get_all_geojson_routes_nonce'], 'get-all-geojson-routes-nonce' ) ) {
            exit;
        } else {
            header( 'Content-Type: application/json' );
            global $wpdb;
            $procedure_name =  $wpdb->prefix . 'get_all_geojson_routes';
            $gps_locations = $wpdb->get_results("CALL {$procedure_name};");

            if ( 0 == $wpdb->num_rows ) {
                echo '0';
                exit;
            }
       
            $geojson = '{"type": "FeatureCollection", "features": [';
           
            foreach ( $gps_locations as $location ) {
                $geojson .= $location->geojson;
                $geojson .= ','; 
            }
        
            $geojson = rtrim($geojson, ',');
            $geojson .= ']}';   

            echo $geojson;
            exit;
       }            
    }

	/**
	 * Deletes a gps route from the database using the Delete button under the map
	 *
	 * @access public
	 * @since 1.0.0
	 */    
    public function delete_gps_route() {
        if ( ! wp_verify_nonce( $_POST['delete_route_nonce'], 'delete-route-nonce' ) ) {
            exit;
        } else {          
            global $wpdb;
            $session_id = $_POST['session_id'];
            $table_name = $wpdb->prefix . 'gps_locations';

            $wpdb->delete( $table_name, array( 'session_id' => $session_id ) );
            
            exit;
        }
    }
} 
?>
