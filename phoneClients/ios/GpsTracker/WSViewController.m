//
//  WSViewController.m
//  GpsTracker
//
//  Created by Nick Fox on 1/1/14.
//  Copyright (c) 2014 Nick Fox. All rights reserved.
//

#import "WSViewController.h"
#import <CoreLocation/CoreLocation.h>
#import "AFHTTPRequestOperationManager.h"

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
    NSUUID *guid;
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
    locationManager.desiredAccuracy = kCLLocationAccuracyNearestTenMeters;
    locationManager.delegate = self;
    // locationManager.activityType = CLActivityTypeAutomotiveNavigation;
    locationManager.distanceFilter = 0; // meters
    
    totalDistanceInMeters = 0;
    firstTimeGettingPosition = YES;
    
    guid = [NSUUID UUID];
    
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
    locationManager.desiredAccuracy = kCLLocationAccuracyNearestTenMeters;
    locationManager.distanceFilter = 0;
}

- (void)locationManager:(CLLocationManager *)manager didUpdateLocations:(NSArray *)locations
{
    CLLocation *location = [locations lastObject];
    
    if (location.horizontalAccuracy < 100.0 && location.coordinate.latitude != 0 && location.coordinate.longitude != 0) {
        
        if (firstTimeGettingPosition) {
            firstTimeGettingPosition = NO;
        } else {
            CLLocationDistance distance = [location distanceFromLocation:previousLocation];
            totalDistanceInMeters += distance;
        }
        
        NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
        [dateFormatter setDateFormat:@"yyyy-MM-dd HH:mm:ss"]; // mysql format
        NSString *timeStamp = [dateFormatter stringFromDate:location.timestamp];
    
        NSString *latitude = [NSString stringWithFormat:@"%f", location.coordinate.latitude];
        NSString *longitude = [NSString stringWithFormat:@"%f", location.coordinate.longitude];
        NSString *speed = [NSString stringWithFormat:@"%f", location.speed];
        NSString *accuracy = [NSString stringWithFormat:@"%f", location.horizontalAccuracy];
        NSString *direction = [NSString stringWithFormat:@"%f", location.course];
        NSString *altitude = [NSString stringWithFormat:@"%f", location.altitude];
        NSString *totalDistanceString = [NSString stringWithFormat:@"%d", totalDistanceInMeters];
        
        // note that the guid is created in startTracking method above
        [self updateWebsiteWithLatitde:latitude longitude:longitude speed:speed date:timeStamp distance:totalDistanceString sessionID:[guid UUIDString] accuracy:accuracy extraInfo:altitude direction:direction];
    }
    
    NSLog(@"lat/lng: %f/%f accuracy: %f", location.coordinate.latitude, location.coordinate.longitude, location.horizontalAccuracy);
    
    previousLocation = location;
}

- (void)locationManager:(CLLocationManager *)manager didFailWithError:(NSError *)error
{
        NSLog(@"locationManager error: %@", [error description]);
}

- (void)updateWebsiteWithLatitde:(NSString *)latitude longitude:(NSString *)longitude speed:(NSString *)speed date:(NSString *)date distance:(NSString *)distance sessionID:(NSString *)sessionID accuracy:(NSString *)accuracy extraInfo:(NSString *)extraInfo direction:(NSString *)direction
{
    AFHTTPRequestOperationManager *manager = [AFHTTPRequestOperationManager manager];
    
    NSDictionary *parameters = @{@"latitude": latitude,
                                 @"longitude": longitude,
                                 @"speed": speed,
                                 @"date": date,
                                 @"locationmethod": @"n/a",
                                 @"distance": distance,
                                 @"phonenumber": @"iosUser",
                                 @"sessionid": sessionID,
                                 @"extrainfo": extraInfo,
                                 @"accuracy": accuracy,
                                 @"eventtype": @"ios",
                                 @"direction": direction};
    
    [manager POST:@"http://www.websmithing.com/gpstracker2/getgooglemap3.php" parameters:parameters success:^(AFHTTPRequestOperation *operation, id responseObject) {
        NSLog(@"Response: %@", responseObject);
    } failure:^(AFHTTPRequestOperation *operation, NSError *error) {
        NSLog(@"AFHTTPRequestOperation Error: %@", error);
    }];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

@end
