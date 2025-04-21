<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><?= config('app.name', 'GPS Tracker') ?></title>
    
    <!-- CSS -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="assets/css/styles.css">
    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"
          integrity="sha256-p4NxAoJBhIIN+hmNHrzRCf9tD/miZyoHS5obTRR9BMY="
          crossorigin="">
    
    <!-- Leaflet Maps (Updated with Integrity and Crossorigin) -->
    <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js" 
            integrity="sha256-20nQCchB9co0qIjJZRGuk2/Z9VM+kNiyxNV1lvTlZBo=" 
            crossorigin=""></script>
    
    <!-- Google Maps -->
    <?php if (!empty(config('maps.google_maps_key', ''))): ?>
    <script src="https://maps.googleapis.com/maps/api/js?key=<?= config('maps.google_maps_key') ?>&libraries=places" async defer></script>
    <?php else: ?>
    <script src="https://maps.googleapis.com/maps/api/js?libraries=places" async defer></script>
    <?php endif; ?>
    
    <!-- jQuery (required for the app) -->
    <script src="https://code.jquery.com/jquery-3.6.4.min.js"></script>
    
    <!-- App JS -->
    <script src="assets/js/leaflet-plugins/google.js"></script>
    <script src="assets/js/leaflet-plugins/bing.js"></script>
    <script src="assets/js/app.js"></script>
    
    <?php if (isset($theme) && $theme === 'dark'): ?>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootswatch@5.2.3/dist/darkly/bootstrap.min.css">
    <link rel="stylesheet" href="assets/css/dark-theme.css">
    <?php elseif (isset($theme) && $theme === 'blue'): ?>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootswatch@5.2.3/dist/cerulean/bootstrap.min.css">
    <?php else: ?>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootswatch@5.2.3/dist/flatly/bootstrap.min.css">
    <?php endif; ?>
</head>
<body>
    <div class="container-fluid">
        <div class="row header">
            <div class="col-sm-6" id="toplogo">
                <img id="logo-image" src="assets/images/gpstracker-man-37.png" alt="GPS Tracker Logo">
                <span class="app-title"><?= config('app.name', 'GPS Tracker') ?></span>
            </div>
            <div class="col-sm-6" id="messages"></div>
        </div>
        
        <div class="row">
            <div class="col-sm-12" id="mapdiv">
                <div id="map-canvas" data-map-provider="<?= config('maps.default_provider', 'openstreetmap') ?>"></div>
            </div>
        </div>
        
        <div class="row">
            <div class="col-12" id="selectdiv">
                <select id="routeSelect" class="form-select" tabindex="1"></select>
            </div>
        </div>
        
        <div class="row buttons-row">
            <div class="col-sm-3 col-6 mb-2 deletediv">
                <button type="button" id="delete" class="btn btn-danger w-100" tabindex="2">Delete</button>
            </div>
            <div class="col-sm-3 col-6 mb-2 autorefreshdiv">
                <button type="button" id="autorefresh" class="btn btn-primary w-100" tabindex="3">Auto Refresh Off</button>
            </div>
            <div class="col-sm-3 col-6 mb-2 refreshdiv">
                <button type="button" id="refresh" class="btn btn-primary w-100" tabindex="4">Refresh</button>
            </div>
            <div class="col-sm-3 col-6 mb-2 viewalldiv">
                <button type="button" id="viewall" class="btn btn-primary w-100" tabindex="5">View All</button>
            </div>
        </div>
    </div>
    
    <script>
        // Initialize the app when DOM is ready
        $(document).ready(function() {
            // Call GPSTracker.init() directly to ensure single initialization point
            GPSTracker.init();
        });
    </script>
</body>
</html>
