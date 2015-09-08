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
@property (weak, nonatomic) IBOutlet UILabel *accuracyLevelLabel;
@property (weak, nonatomic) IBOutlet UILabel *sessionIDLabel;
@end

@implementation WSViewController
{
    CLLocationManager *locationManager;
    CLLocation *previousLocation;
    double totalDistanceInMeters;
    bool currentlyTracking;
    bool firstTimeGettingPosition;
    NSUUID *guid;
    NSDate *lastWebsiteUpdateTime;
    int timeIntervalInSeconds;
    bool increasedAccuracy;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    currentlyTracking = NO;
    timeIntervalInSeconds = 60; // change this to the time interval you want
    
    BOOL appIDIsSet = [[NSUserDefaults standardUserDefaults] boolForKey:@"appIDIsSet"];
    if (!appIDIsSet) {
        [[NSUserDefaults standardUserDefaults] setBool:YES forKey:@"appIDIsSet"];
        [[NSUserDefaults standardUserDefaults] setObject:[[NSUUID UUID] UUIDString] forKey:@"appID"];
        [[NSUserDefaults standardUserDefaults] synchronize];
    }
}

- (void)startTracking
{
    NSLog(@"start tracking");
    
    locationManager = [[CLLocationManager alloc] init];
    locationManager.desiredAccuracy = kCLLocationAccuracyNearestTenMeters;
    locationManager.distanceFilter = 0; // meters
    //locationManager.pausesLocationUpdatesAutomatically = NO; // YES is default
    locationManager.activityType = CLActivityTypeAutomotiveNavigation;
    locationManager.delegate = self;
    
    guid = [NSUUID UUID];
    totalDistanceInMeters = 0;
    increasedAccuracy = YES;
    firstTimeGettingPosition = YES;
    lastWebsiteUpdateTime = [NSDate date]; // new timestamp
    [self updateAccuracyLevel:@"high"];
    [self sessionIDLabel].text = [guid UUIDString];
    
    [locationManager startUpdatingLocation];
}

- (void)stopTracking
{
    NSLog(@"stop tracking");
    
    [self sessionIDLabel].text = @"phoneNumber:";
    [locationManager stopUpdatingLocation];
    locationManager = nil;
}

- (IBAction)handleTrackingButton:(id)sender
{
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
    locationManager.distanceFilter = 5;
    increasedAccuracy = NO;
    [self updateAccuracyLevel:@"low"];
}

- (void)increaseTrackingAccuracy
{
    locationManager.desiredAccuracy = kCLLocationAccuracyNearestTenMeters;
    locationManager.distanceFilter = 0;
    increasedAccuracy = YES;
    [self updateAccuracyLevel:@"high"];
}

- (void)locationManager:(CLLocationManager *)manager didUpdateLocations:(NSArray *)locations
{
    CLLocation *location = [locations lastObject];
    
    // I learned this method of getting a time interval from xs2bush on stackoverflow and wanted to give that person
    // credit for this, thanks. http://stackoverflow.com/a/6466152/125615
    
    NSTimeInterval secondsSinceLastWebsiteUpdate = fabs([lastWebsiteUpdateTime timeIntervalSinceNow]);
    if (firstTimeGettingPosition || (secondsSinceLastWebsiteUpdate > timeIntervalInSeconds)) { // currently one minute
        
        if (location.horizontalAccuracy < 500.0 && location.coordinate.latitude != 0 && location.coordinate.longitude != 0) {
            
            if (increasedAccuracy) {
                [self reduceTrackingAccuracy];
            }
            
            if (firstTimeGettingPosition) {
                firstTimeGettingPosition = NO;
            } else {
                CLLocationDistance distance = [location distanceFromLocation:previousLocation];
                totalDistanceInMeters += distance; 
            }
 
            previousLocation = location;
            
            NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
            [dateFormatter setDateFormat:@"yyyy-MM-dd%20HH:mm:ss"]; // mysql format
            NSString *timeStamp = [dateFormatter stringFromDate:location.timestamp];
            NSString *latitude = [NSString stringWithFormat:@"%f", location.coordinate.latitude];
            NSString *longitude = [NSString stringWithFormat:@"%f", location.coordinate.longitude];
            NSString *speed = [NSString stringWithFormat:@"%d", (int)location.speed];
            NSString *accuracy = [NSString stringWithFormat:@"%d", (int)location.horizontalAccuracy];
            NSString *direction = [NSString stringWithFormat:@"%d", (int)location.course];
            NSString *altitude = [NSString stringWithFormat:@"altitude: %dm", (int)location.altitude];
            NSString *totalDistanceString = [NSString stringWithFormat:@"%d", (int)totalDistanceInMeters];
            
            // note that the guid is created in startTracking method above
            [self updateWebsiteWithLatitde:latitude longitude:longitude speed:speed date:timeStamp distance:totalDistanceString sessionID:[guid UUIDString] accuracy:accuracy extraInfo:altitude direction:direction];
            
            lastWebsiteUpdateTime = [NSDate date]; // new timestamp
            
        } else if (!increasedAccuracy) {
            [self increaseTrackingAccuracy];
        }
    }
    
    if ([[UIApplication sharedApplication] applicationState] == UIApplicationStateActive) {
        [self updateUIWithLocationData:location];
    }
}

- (void)updateAccuracyLevel:(NSString *)accuracyLevel
{
    [self accuracyLevelLabel].text= [NSString stringWithFormat:@"accuracy level: %@", accuracyLevel];
}

- (void)updateUIWithLocationData:(CLLocation *)location
{
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    [dateFormatter setDateFormat:@"dd/MM/yyyy HH:mm"];
    [self timestampLabel].text = [NSString stringWithFormat:@"timestamp: %@",[dateFormatter stringFromDate:location.timestamp]];
    [self latitudeLabel].text = [NSString stringWithFormat:@"latitude: %f", location.coordinate.latitude];
    [self longitudeLabel].text = [NSString stringWithFormat:@"longitude: %f", location.coordinate.longitude];
    [self accuracyLabel].text= [NSString stringWithFormat:@"accuracy: %dm", (int)location.horizontalAccuracy];
}

- (void)locationManager:(CLLocationManager *)manager didFailWithError:(NSError *)error
{
        NSLog(@"locationManager error: %@", [error description]);
}

- (void)updateWebsiteWithLatitde:(NSString *)latitude longitude:(NSString *)longitude speed:(NSString *)speed date:(NSString *)date distance:(NSString *)distance sessionID:(NSString *)sessionID accuracy:(NSString *)accuracy extraInfo:(NSString *)extraInfo direction:(NSString *)direction
{
	// use the websmithing defaultUploadWebsite for testing, change the *phoneNumber* form variable to something you
	// know and then check your location with your browser here: https://www.websmithing.com/gpstracker/displaymap.php
	
    NSString *defaultUploadWebsite = @"https://www.websmithing.com/gpstracker/updatelocation.php";
    
    AFHTTPRequestOperationManager *manager = [AFHTTPRequestOperationManager manager];
    manager.responseSerializer = [AFHTTPResponseSerializer serializer];
    
    NSDictionary *parameters = @{@"latitude": latitude,
                                 @"longitude": longitude,
                                 @"speed": speed,
                                 @"date": date,
                                 @"locationmethod": @"n/a",
                                 @"distance": distance,
                                 @"username": @"iosUser1",
                                 @"phonenumber": [[NSUserDefaults standardUserDefaults] stringForKey:@"appID"],
                                 @"sessionid": sessionID,
                                 @"extrainfo": extraInfo,
                                 @"accuracy": accuracy,
                                 @"eventtype": @"ios",
                                 @"direction": direction};
    
    [manager GET:defaultUploadWebsite parameters:parameters
         success:^(AFHTTPRequestOperation *operation, id responseObject) {
             NSString *response = [[NSString alloc] initWithData:responseObject encoding:NSUTF8StringEncoding];
             NSLog(@"Response: %@", response);
    }
         failure:^(AFHTTPRequestOperation *operation, NSError *error) {
             NSLog(@"AFHTTPRequestOperation Error: %@", [error description]);
    }];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

@end
