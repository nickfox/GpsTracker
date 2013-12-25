//
//  WSMViewController.m
//  GpsTracker
//
//  Created by Nick Fox on 12/23/13.
//  Copyright (c) 2013 Nick Fox. All rights reserved.
//

#import "WSMViewController.h"
#import <CoreLocation/CoreLocation.h>


@interface WSMViewController () <CLLocationManagerDelegate>

@end

@implementation WSMViewController
{
    CLLocationManager *locationManager;
    bool firstTimeGettingPosition;
    CLLocation *previousLocation;
    int totalDistanceInMeters;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    firstTimeGettingPosition = true;
    totalDistanceInMeters = 0;
    
    locationManager = [[CLLocationManager alloc] init];
    locationManager.delegate = self;
	locationManager.desiredAccuracy = kCLLocationAccuracyNearestTenMeters;
    [locationManager startUpdatingLocation];
    
}

- (void)locationManager:(CLLocationManager *)manager didUpdateLocations:(NSArray *)locations
{
    if (firstTimeGettingPosition) {
        firstTimeGettingPosition = false;
    } else {
        CLLocation *currentLocation = locations[0];
        CLLocationDistance distance = [currentLocation distanceFromLocation:previousLocation];
        totalDistanceInMeters += (int)distance;
    }
    
    previousLocation = locations[0];
    
    
}

- (void)locationManager:(CLLocationManager *)manager didFailWithError:(NSError *)error
{
    
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

@end
