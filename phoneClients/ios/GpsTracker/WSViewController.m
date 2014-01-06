//
//  WSViewController.m
//  GpsTracker
//
//  Created by Nick Fox on 1/1/14.
//  Copyright (c) 2014 Nick Fox. All rights reserved.
//

#import "WSViewController.h"
#import <CoreLocation/CoreLocation.h>

@interface WSViewController () <CLLocationManagerDelegate>
@property (weak, nonatomic) IBOutlet UILabel *latitudeLabel;
@property (weak, nonatomic) IBOutlet UILabel *longitudeLabel;
@property (weak, nonatomic) IBOutlet UILabel *accuracyLabel;
@property (weak, nonatomic) IBOutlet UILabel *timestampLabel;
@property (weak, nonatomic) IBOutlet UIButton *trackingButton;
@end

@implementation WSViewController
{
    CLLocationManager *locationManager;
    CLLocation *previousLocation;
    int totalDistanceInMeters;
    bool currentlyTracking;
    bool firstTimeGettingPosition;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    currentlyTracking = NO;

}

- (void)startTracking
{
    NSLog(@"start tracking ");
    locationManager = [[CLLocationManager alloc] init];
    locationManager.desiredAccuracy = kCLLocationAccuracyBest;
    locationManager.delegate = self;
    // locationManager.activityType = CLActivityTypeAutomotiveNavigation;
    locationManager.distanceFilter = 0; // meters
    
    totalDistanceInMeters = 0;
    firstTimeGettingPosition = YES;
    
    [locationManager startUpdatingLocation];
}

- (void)stopTracking
{
    NSLog(@"stop tracking");
    [locationManager stopUpdatingLocation];
    locationManager = nil;
}
- (IBAction)handleTrackingButton:(id)sender {
    if (currentlyTracking) {
        [self stopTracking];
        currentlyTracking = NO;
        [self.trackingButton setTitle:@"start tracking" forState:UIControlStateNormal];
    } else {
        [self startTracking];
        currentlyTracking = YES;
        [self.trackingButton setTitle:@"stop tracking" forState:UIControlStateNormal];
    }
}

- (void)reduceTrackingAccuracy
{
   locationManager.desiredAccuracy = kCLLocationAccuracyThreeKilometers;
    locationManager.distanceFilter = 9999;
}

- (void)increaseTrackingAccuracy
{
    locationManager.desiredAccuracy = kCLLocationAccuracyBest;
    locationManager.distanceFilter = 0;
}

- (void)locationManager:(CLLocationManager *)manager


                       :(NSArray *)locations
{
    CLLocation *location = [locations lastObject];
    
    if (location.horizontalAccuracy < 100.0) {
        
    }
    
    NSLog(@"lat/lng: %f/%f accuracy: %f", location.coordinate.latitude, location.coordinate.longitude, location.horizontalAccuracy);
    
    
    if (firstTimeGettingPosition) {
        firstTimeGettingPosition = NO;
    } else {
        CLLocationDistance distance = [location distanceFromLocation:previousLocation];
        totalDistanceInMeters += distance;
    }
    
    previousLocation = location;
}

- (void)locationManager:(CLLocationManager *)manager didFailWithError:(NSError *)error
{
        NSLog(@"locationManager error: %@", [error description]);
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

@end
