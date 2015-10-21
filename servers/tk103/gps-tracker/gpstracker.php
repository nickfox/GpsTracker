<?php
/**
 * Gps Tracker
 *
 * @package   Gps_Tracker
 * @author    Nick Fox <nickfox@websmithing.com>
 * @license   MIT/GPLv2 or later
 * @link      https://www.websmithing.com/gps-tracker
 * @copyright 2014 Nick Fox
 *
 * @wordpress-plugin
 * Plugin Name:       Gps Tracker
 * Plugin URI:        https://www.websmithing.com/gps-tracker
 * Description:       Track Android cell phones in real time and store routes for later viewing.
 * Version:           1.0.3
 * Author:            Nick Fox
 * Author URI:        https://www.websmithing.com/hire-me
 * Text Domain:       gpstracker
 * License:           MIT/GPLv2 or later
 * License URI:       https://opensource.org/licenses/MIT, http://www.gnu.org/licenses/gpl-2.0.html
 * Domain Path:       /languages
 * GitHub Plugin URI: https://github.com/nickfox/GpsTracker-Wordpress-Plugin
 */

// Exit if accessed directly
if ( ! defined( 'ABSPATH' ) ) exit;

// Plugin version
if ( ! defined( 'GPSTRACKER_VERSION' ) ) {
	define( 'GPSTRACKER_VERSION', '1.0.3' );
}

// Plugin Folder Path
if ( ! defined( 'GPSTRACKER_PLUGIN_DIR' ) ) {
	define( 'GPSTRACKER_PLUGIN_DIR', plugin_dir_path( __FILE__ ) );
}

// Plugin Folder URL
if ( ! defined( 'GPSTRACKER_PLUGIN_URL' ) ) {
	define( 'GPSTRACKER_PLUGIN_URL', plugin_dir_url( __FILE__ ) );
}

// Public facing functionality

require_once( GPSTRACKER_PLUGIN_DIR . 'public/class-gpstracker.php' );

// Register hooks that are fired when the plugin is activated or deactivated.
// When the plugin is deleted, the uninstall.php file is loaded.

require_once GPSTRACKER_PLUGIN_DIR . 'includes/class-gpstracker-setup.php';

register_activation_hook( __FILE__, array( 'Gps_Tracker_Setup', 'activate' ) );
register_deactivation_hook( __FILE__, array( 'Gps_Tracker_Setup', 'deactivate' ) );

add_action( 'plugins_loaded', array( 'Gps_Tracker', 'get_instance' ) );

// Admin and dashboard functionality

if ( is_admin() && ( ! defined( 'DOING_AJAX' ) || ! DOING_AJAX ) ) {
	require_once( GPSTRACKER_PLUGIN_DIR . 'admin/class-gpstracker-admin.php' );
	add_action( 'plugins_loaded', array( 'Gps_Tracker_Admin', 'get_instance' ) );
}
