# THE ORIGINAL GPS TRACKER v7.0.0

![GPS Tracker](https://raw.githubusercontent.com/nickfox/GpsTracker/master/gpstracker_small.png)

## The Most Popular Open Source GPS Tracking System
*[Over 2.2 million downloads](https://sourceforge.net/projects/gpsmapper/files/stats/timeline?dates=2000-01-21+to+2025-04-21) • [800+ GitHub stars](https://github.com/nickfox/GpsTracker) • Active since 2007*

GPS Tracker is a complete GPS tracking system that allows you to track mobile devices in real-time using Google or Open Street Maps and other map providers. This project has been actively maintained since 2007, with Version 7 representing a complete rewrite of the core components with modern code practices and frameworks.

## What's New in Version 7

Version 7 is a major update with complete rewrites of the three primary components:

**Live Demo**: View the tracking system in action at [https://www.websmithing.com/gpstracker/map](https://www.websmithing.com/gpstracker/map)

- **PHP/JS Server**: Modernized with clean architecture and responsive design
- **Android Client**: Completely rewritten in Kotlin with MVVM architecture
- **iOS Client**: New Swift implementation with SwiftUI and MVVM architecture

All components feature production-quality code with comprehensive inline documentation. This rewrite was done with the assistance of Google Gemini Pro 2.5 exp 03-25.

## System Architecture

GPS Tracker consists of three main components:

1. **Mobile Clients**: Native apps for iOS and Android that capture location data
2. **Server**: PHP/JS backend that receives, stores, and processes location updates
3. **Web Interface**: Responsive dashboard for viewing real-time and historical tracking data

![gpstrackerandroid](https://www.websmithing.com/images/gpstracker-dark.jpg)

## Features

- Real-time tracking with customizable update intervals
- Background tracking on mobile devices
- Multiple map providers (Google Maps, OpenStreetMaps, etc.)
- Responsive web interface using modern frameworks
- Comprehensive route history and playback
- Distance, speed, and battery level tracking
- Support for multiple devices and users
- Free and open source

## Installation Instructions

### PHP/JS Server

#### Requirements
- PHP 7.4+
- MySQL, PostgreSQL, or SQLite database
- Apache or Nginx web server

#### Apache Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/nickfox/GpsTracker.git
   ```

2. Copy the server files to your web directory:
   ```bash
   cp -r GpsTracker/servers/php/* /var/www/html/gpstracker/
   ```

3. Set proper permissions:
   ```bash
   chmod -R 755 /var/www/html/gpstracker/
   chown -R www-data:www-data /var/www/html/gpstracker/
   ```

4. Configure your environment settings:
   ```bash
   # Copy the sample environment file
   cp .env.example .env
   
   # Edit the .env file with your settings
   vim .env  # or use your preferred text editor
   ```
   
   The `.env` file contains all necessary configuration options with helpful comments. Key settings to configure:
   
   - Database connection (MySQL, PostgreSQL, or SQLite)
   - Map provider (OpenStreetMap by default, or Google Maps with API key)
   - Application settings like debug mode and timezone
   - Measurement units (miles/kilometers)

5. Create the database schema:
   - For SQLite: The database file will be created automatically in the `sqlite` directory
   - For MySQL/PostgreSQL: Create the database manually before running the application:
     ```sql
     CREATE TABLE locations (
         id INT AUTO_INCREMENT PRIMARY KEY,
         device_id VARCHAR(50) NOT NULL,
         latitude DECIMAL(10, 8) NOT NULL,
         longitude DECIMAL(11, 8) NOT NULL,
         speed DECIMAL(5, 2),
         heading INT,
         altitude DECIMAL(7, 2),
         accuracy DECIMAL(6, 2),
         event_time DATETIME NOT NULL,
         battery_level INT,
         app_version VARCHAR(10),
         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
     );
     
     CREATE TABLE routes (
         id INT AUTO_INCREMENT PRIMARY KEY,
         device_id VARCHAR(50) NOT NULL,
         route_name VARCHAR(100) NOT NULL,
         route_color VARCHAR(7) DEFAULT '#0000FF',
         start_time DATETIME NOT NULL,
         end_time DATETIME,
         distance DECIMAL(10, 2) DEFAULT 0,
         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
     );
     ```

#### Nginx Installation

1. Follow steps 1-4 from the Apache installation.

2. Add this to your Nginx server configuration:
   ```nginx
   server {
       listen 80;
       server_name your-domain.com;
       root /var/www/html/gpstracker;
       index index.php index.html;

       location / {
           try_files $uri $uri/ /index.php?$query_string;
       }

       location ~ \.php$ {
           include snippets/fastcgi-php.conf;
           fastcgi_pass unix:/var/run/php/php7.4-fpm.sock;
       }

       location ~ /\.ht {
           deny all;
       }
   }
   ```

3. Restart Nginx:
   ```bash
   sudo systemctl restart nginx
   ```

4. Continue with step 5 from the Apache installation.

### Android Client

#### Requirements
- Android Studio 4.0+
- Android SDK 21+

#### Installation (Android app coming soon to Google Play)

1. Open Android Studio

2. Select "Open an existing project"

3. Navigate to `GpsTracker/phoneClients/android-kotlin`

4. Update the server URL in `Constants.kt`:
   ```kotlin
   const val SERVER_URL = "http://your-server.com/gpstracker"
   ```

5. Build and run the application on your device

### iOS Client

#### Requirements
- Xcode 16.2+
- iOS 15.0+
- Swift 5.8+

#### Installation

1. Open Xcode

2. Select "Open a project or file"

3. Navigate to `GpsTracker/phoneClients/ios-swift/GPSTracker.xcodeproj`

4. Update the server URL in `APIService.swift`:
   ```swift
   private var baseURL: URL {
       return URL(string: "http://your-server.com/gpstracker/api/location")!
   }
   ```

5. Build and run the application on your device

## Legacy Components

The following components from previous versions are still available but are not actively maintained:

- ASP.NET with SQL Server backend
- Windows Phone client
- Java ME client
- WordPress plugin (with Android client)

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Project History

GPS Tracker began in 2007 and has grown into one of the most popular open source GPS tracking solutions worldwide, with over 2.2 million downloads since its initial release. It has been used in fleet management, personal safety applications, research projects, and many other scenarios across the globe.

## Contact

Nick Fox - [Website](https://www.websmithing.com) - [GitHub](https://github.com/nickfox)

## Acknowledgements

- Thanks to the millions of users worldwide
- Thanks to all contributors who have helped improve this project over the years