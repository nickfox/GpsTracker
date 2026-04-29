<?php
$token = 'CHANGE_ME_SECRET';

if (($_REQUEST['token'] ?? '') !== $token) {
    http_response_code(403);
    exit('forbidden');
}

$pdo = new PDO(
    'mysql:host=localhost;dbname=gpstracker;charset=utf8mb4',
    'db_user',
    'db_password',
    [PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION]
);

$lat = filter_var($_REQUEST['lat'] ?? null, FILTER_VALIDATE_FLOAT);
$lon = filter_var($_REQUEST['lon'] ?? null, FILTER_VALIDATE_FLOAT);

if ($lat === false || $lon === false) {
    http_response_code(400);
    exit('missing lat/lon');
}

$rawTime = $_REQUEST['time'] ?? time();
if (is_numeric($rawTime)) {
    $ts = (int)$rawTime;
    if ($ts > 100000000000) {
        $ts = intdiv($ts, 1000); // milliseconds to seconds
    }
} else {
    $ts = time();
}

$stmt = $pdo->prepare("
    INSERT INTO locations
    (device_id, latitude, longitude, speed, heading, altitude, accuracy, event_time, battery_level, app_version)
    VALUES
    (:device_id, :lat, :lon, :speed, :heading, :alt, :accuracy, :event_time, :battery, 'locus')
");

$stmt->execute([
    ':device_id' => $_REQUEST['device'] ?? 'locus-phone',
    ':lat' => $lat,
    ':lon' => $lon,
    ':speed' => $_REQUEST['speed'] ?? null,
    ':heading' => $_REQUEST['bearing'] ?? null,
    ':alt' => $_REQUEST['alt'] ?? null,
    ':accuracy' => $_REQUEST['accuracy'] ?? null,
    ':event_time' => gmdate('Y-m-d H:i:s', $ts),
    ':battery' => $_REQUEST['battery'] ?? null,
]);

echo 'OK';

