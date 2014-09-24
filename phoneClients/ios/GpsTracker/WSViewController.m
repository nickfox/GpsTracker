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
#import "UIColor+HexColor.h"
#import "CustomButton.h"

@interface WSViewController () <CLLocationManagerDelegate>
@property (weak, nonatomic) IBOutlet CustomButton *trackingButton;
@property (weak, nonatomic) IBOutlet UITextField *uploadWebsiteTextField;
@property (weak, nonatomic) IBOutlet UITextField *userNameTextField;
@property (weak, nonatomic) IBOutlet UISegmentedControl *intervalControl;
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
    NSString *defaultUploadWebsite;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    // use the websmithing defaultUploadWebsite for testing, change the userName parameter to something you
	// know and then check your location with your browser here: https://www.websmithing.com/gpstracker/displaymap.php
	
    defaultUploadWebsite = @"https://www.websmithing.com/gpstracker/updatelocation.php";
    self.uploadWebsiteTextField.text = defaultUploadWebsite;
    
    [self.trackingButton setButtonColor:@"#ff0033" andHighLightColor:@"#ff7691" andTextColor:@"#FFFFFF" andHighlightTextColor:@"#333333"];

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
    locationManager.pausesLocationUpdatesAutomatically = NO; // YES is default
    locationManager.activityType = CLActivityTypeAutomotiveNavigation;
    locationManager.delegate = self;
    
    guid = [NSUUID UUID];
    totalDistanceInMeters = 0;
    increasedAccuracy = YES;
    firstTimeGettingPosition = YES;
    lastWebsiteUpdateTime = [NSDate date]; // new timestamp
    
    [locationManager startUpdatingLocation];
}




- (void)stopTracking
{
    NSLog(@"stop tracking");

    [locationManager stopUpdatingLocation];
    locationManager = nil;
}

- (void)checkTextFields {
    NSLog(@"check Text Fields");
    
    NSString *uploadWebsite = [self.uploadWebsiteTextField.text stringByTrimmingCharactersInSet:
                               [NSCharacterSet whitespaceCharacterSet]];
    
    NSString *userName = [self.userNameTextField.text stringByTrimmingCharactersInSet:
                               [NSCharacterSet whitespaceCharacterSet]];
    
    if (uploadWebsite.length == 0 || userName.length == 0) {
        NSLog(@"make your user name longer.");
    } else {
        NSLog(@"it's ok.");
    }
    
}

- (IBAction)handleTrackingButton:(id)sender
{
    if (currentlyTracking) {
        [self stopTracking];
        currentlyTracking = NO;
 
        // set to RED
        [self.trackingButton setButtonColor:@"#ff0033" andHighLightColor:@"#ff7691" andTextColor:@"#FFFFFF" andHighlightTextColor:@"#333333"];
        [self.trackingButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        
        // here is the red color, #ff0033 and its highlight, #ff7691
        // here is the green color, #33ffcc and it's highlight, #a9ffe9
        
        [self.trackingButton setTitle:@"Tracking is Off" forState:UIControlStateNormal];
    } else {
        [self startTracking];
        currentlyTracking = YES;
        
        [self checkTextFields];
        
        // set to GREEN
        [self.trackingButton setButtonColor:@"#33ffcc" andHighLightColor:@"#a9ffe9" andTextColor:@"#000000" andHighlightTextColor:@"#999999"];
        [self.trackingButton setTitleColor:[UIColor blackColor] forState:UIControlStateNormal];
        [self.trackingButton setTitle:@"Tracking is On" forState:UIControlStateNormal];
    }
}

- (void)reduceTrackingAccuracy
{
   locationManager.desiredAccuracy = kCLLocationAccuracyThreeKilometers;
    locationManager.distanceFilter = 5;
    increasedAccuracy = NO;
}

- (void)increaseTrackingAccuracy
{
    locationManager.desiredAccuracy = kCLLocationAccuracyNearestTenMeters;
    locationManager.distanceFilter = 0;
    increasedAccuracy = YES;
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
    
    NSString *trackingAccuracy = (increasedAccuracy) ? @"high" : @"low";
    NSLog(@"tracking accuracy: %@ lat/lng: %f/%f accuracy: %dm", trackingAccuracy, location.coordinate.latitude, location.coordinate.longitude, (int)location.horizontalAccuracy);
    
}

- (void)locationManager:(CLLocationManager *)manager didFailWithError:(NSError *)error
{
        NSLog(@"locationManager error: %@", [error description]);
}

- (void)updateWebsiteWithLatitde:(NSString *)latitude longitude:(NSString *)longitude speed:(NSString *)speed date:(NSString *)date distance:(NSString *)distance sessionID:(NSString *)sessionID accuracy:(NSString *)accuracy extraInfo:(NSString *)extraInfo direction:(NSString *)direction
{

    
    AFHTTPRequestOperationManager *manager = [AFHTTPRequestOperationManager manager];
    manager.responseSerializer = [AFHTTPResponseSerializer serializer];
    
    NSDictionary *parameters = @{@"latitude": latitude,
                                 @"longitude": longitude,
                                 @"speed": speed,
                                 @"date": date,
                                 @"locationmethod": @"n/a",
                                 @"distance": distance,
                                 @"username": @"iosUser01",
                                 @"phonenumber": [[NSUserDefaults standardUserDefaults] stringForKey:@"appID"],
                                 @"sessionid": sessionID,
                                 @"extrainfo": extraInfo,
                                 @"accuracy": accuracy,
                                 @"eventtype": @"ios",
                                 @"direction": direction};
    
    [manager GET:defaultUploadWebsite parameters:parameters success:^(AFHTTPRequestOperation *operation, id responseObject) {
        NSLog(@"location sent to website.");
    } failure:^(AFHTTPRequestOperation *operation, NSError *error) {
        NSLog(@"AFHTTPRequestOperation Error: %@", [error description]);
    }];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

@end
