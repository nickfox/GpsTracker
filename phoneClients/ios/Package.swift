// swift-tools-version: 5.7
import PackageDescription

let package = Package(
    name: "GPSTracker",
    platforms: [
        .iOS(.v17) // Ensure this matches your Xcode project's minimum deployment target
    ],
    products: [
        .library(
            name: "GPSTracker",
            targets: ["GPSTracker"]
        ),
    ],
    dependencies: [
        // Add any external SPM dependencies here if needed
    ],
    targets: [
        .target(
            name: "GPSTracker",
            dependencies: [], // UIKit is implicitly included for iOS targets
            path: "GPSTracker" // Ensure this matches the folder containing your source files
        ),
        .testTarget(
            name: "GPSTrackerTests",
            dependencies: ["GPSTracker"],
            path: "GPSTrackerTests"
        ),
    ]
)
