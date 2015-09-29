<?php
/**
 * Gps_Tracker
 *
 * @package   Gps_Tracker
 * @category  Core
 * @author    Nick Fox <nickfox@websmithing.com>
 * @license   MIT/GPLv2 or later
 * @link      https://www.websmithing.com/gps-tracker
 * @copyright 2014 Nick Fox
 */

// Exit if accessed directly
if ( ! defined( 'ABSPATH' ) ) exit;

if ( ! class_exists( 'Gps_Tracker' ) ) :

/**
 * Main Gps_Tracker Class
 *
 * @since 1.0.0
 */
class Gps_Tracker {

	/**
	 * Plugin version, used for cache-busting of style and script file references.
	 *
	 * @since 1.0.0
	 * @var string
	 */
	const VERSION = '1.0.3';

	/**
	 * Unique identifier for your plugin.
	 *
	 * The variable name is used as the text domain when internationalizing strings
	 * of text. Its value should match the Text Domain file header in the main
	 * plugin file.
	 *
	 * @since 1.0.0
	 * @var string
	 */
	protected $plugin_slug = 'gpstracker';

	/**
	 * Instance of this class.
	 *
	 * @since 1.0.0
	 * @var object
	 */
	protected static $instance = null;

	/**
	 * Gps Tracker Endpoint Object
	 *
	 * @var object
	 * @since 1.0.0
	 */
	public $gpstracker_endpoint;

	/**
	 * Gps Tracker Ajax Object
	 *
	 * @var object
	 * @since 1.0.0
	 */
	public $gpstracker_ajax;

	/**
	 * Initialize the plugin by setting localization and loading public scripts
	 * and styles.
	 *
	 * @since 1.0.0
	 */
	private function __construct() {
        $this->includes();
        $this->gpstracker_endpoint = new Gps_Tracker_Endpoint();
        $this->gpstracker_ajax = new Gps_Tracker_Ajax(); 

		// load plugin text domain
		add_action( 'init', array( $this, 'load_plugin_textdomain' ) );

		// load public-facing style sheet and javascript
		add_action( 'wp_enqueue_scripts', array( $this, 'enqueue_styles' ) );
		add_action( 'wp_enqueue_scripts', array( $this, 'enqueue_scripts' ) );

        // to use this plugin, add this shortcode to any page or post: [gps_tracker]  
        add_shortcode( 'gps_tracker', array( $this,'gpstracker_map_shortcode' ) );
	}

	/**
	 * Return the plugin slug.
	 *
	 * @since 1.0.0
	 * @return string
	 */
	public function get_plugin_slug() {
		return $this->plugin_slug;
	}

	/**
	 * Return an instance of this class.
	 *
	 * @since 1.0.0
	 * @return object   A single instance of this class.
	 */
	public static function get_instance() {
		// If the single instance hasn't been set, set it now.
		if ( null == self::$instance ) {
			self::$instance = new self;
		}

		return self::$instance;
	}
    
	/**
	 * Throw error on object clone
	 *
	 * The whole idea of the singleton design pattern is that there is a single
	 * object therefore, we don't want the object to be cloned.
	 *
	 * @since 1.0.0
	 * @access protected
	 * @return void
	 */
	public function __clone() {
		// Cloning instances of the class is forbidden
		_doing_it_wrong( __FUNCTION__, __( 'not good.', 'gpstracker' ), '1.0.0' );
	}

	/**
	 * Disable unserializing of the class
	 *
	 * @since 1.0.0
	 * @access protected
	 * @return void
	 */
	public function __wakeup() {
		// Unserializing instances of the class is forbidden
		_doing_it_wrong( __FUNCTION__, __( 'not good.', 'gpstracker' ), '1.0.0' );
	}
        
	/**
	 * Include required files.
	 *
	 * @access private
	 * @since 1.0.0
	 * @return void
	 */
	private function includes() {
		require_once GPSTRACKER_PLUGIN_DIR . 'includes/class-gpstracker-gamajo-template-loader.php';
		require_once GPSTRACKER_PLUGIN_DIR . 'includes/class-gpstracker-template-loader.php';
		require_once GPSTRACKER_PLUGIN_DIR . 'public/includes/class-gpstracker-ajax.php';
        require_once GPSTRACKER_PLUGIN_DIR . 'public/includes/class-gpstracker-endpoint.php';
    }
        
    /**
     * Gps Tracker Map Shortcode
     *
     * Displays the map, dropdown boxes and buttons for Gps Tracker 
     *
     * @since 1.0.0
     * @return string
     */
    public function gpstracker_map_shortcode() {
        $templates = new GpsTracker_Template_Loader();

        ob_start();
        $templates->get_template_part( 'shortcode', 'gpstracker-map' );
        return ob_get_clean();
    }

	/**
	 * Load the plugin text domain for translation.
	 *
	 * @since 1.0.0
	 */
	public function load_plugin_textdomain() {
		$domain = $this->plugin_slug;
		$locale = apply_filters( 'plugin_locale', get_locale(), $domain );

		load_textdomain( $domain, trailingslashit( WP_LANG_DIR ) . $domain . '/' . $domain . '-' . $locale . '.mo' );
		load_plugin_textdomain( $domain, FALSE, basename( plugin_dir_path( dirname( __FILE__ ) ) ) . '/languages/' );
	}

	/**
	 * Register and enqueue public-facing style sheet.
	 *
	 * @since 1.0.0
	 */
	public function enqueue_styles() {   
        wp_enqueue_style( $this->plugin_slug . '-leaflet-styles', plugins_url( 'assets/js/leaflet-0.7.5/leaflet.css', __FILE__ ), array(), self::VERSION );              
        wp_enqueue_style('gpstracker-bootstrap', '//maxcdn.bootstrapcdn.com/bootswatch/3.3.5/superhero/bootstrap.min.css', false, '3.3.0', 'all');
        wp_enqueue_style( $this->plugin_slug . '-light-styles', plugins_url( 'assets/css/light.css', __FILE__ ), array(), self::VERSION ); 
    }

	/**
	 * Register and enqueues public-facing JavaScript files.
	 *
	 * @since 1.0.0
	 */
	public function enqueue_scripts() {     
        wp_enqueue_script( $this->plugin_slug . '-gpstracker-google-maps', '//maps.google.com/maps/api/js?v=3&sensor=false&libraries=adsense', array(), self::VERSION );
        wp_enqueue_script( $this->plugin_slug . '-gpstracker-map-js', plugins_url( 'assets/js/gpstracker-map.js', __FILE__ ), array('jquery'), self::VERSION );
        wp_enqueue_script( $this->plugin_slug . '-gpstracker-leaflet-js', plugins_url( 'assets/js/leaflet-0.7.5/leaflet.js', __FILE__ ), array('jquery'), self::VERSION );
        wp_enqueue_script( $this->plugin_slug . '-gpstracker-google-js', plugins_url( 'assets/js/leaflet-plugins/google.js', __FILE__ ), array('jquery'), self::VERSION );
        wp_enqueue_script( $this->plugin_slug . '-gpstracker-bing-js', plugins_url( 'assets/js/leaflet-plugins/bing.js', __FILE__ ), array('jquery'), self::VERSION );        
        
        wp_localize_script( $this->plugin_slug . '-gpstracker-map-js', 'map_js_vars', array(
            'plugin_url'                    => GPSTRACKER_PLUGIN_URL,
            'ajax_url'                      => admin_url('admin-ajax.php'),
            'get_routes_nonce'              => wp_create_nonce('get-routes-nonce'),
            'get_geojson_route_nonce'       => wp_create_nonce('get-geojson-route-nonce'),
            'get_all_geojson_routes_nonce'  => wp_create_nonce('get-all-geojson-routes-nonce'),
            'delete_route_nonce'            => wp_create_nonce('delete-route-nonce')
        	)
        );  
	}
}
endif; // End if class_exists
