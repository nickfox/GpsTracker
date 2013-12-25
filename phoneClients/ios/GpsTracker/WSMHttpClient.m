//
//  WSMHttpClient.m
//  GpsTracker
//
//  Created by Nick Fox on 12/24/13.
//  Copyright (c) 2013 Nick Fox. All rights reserved.
//

#import "WSMHttpClient.h"

@interface WSMHttpClient ()
{
    //NSString *defaultUploadWebsite;
}
@end

@implementation WSMHttpClient


- (id)init;
{
    if((self = [super init]))
    {
        //defaultUploadWebsite = @"http://www.websmithing.com/gpstracker2/getgooglemap3.php";
        //NSLog(@"archiveFilePath: %@", [self archiveFilePath]);
    }
    
    return self;
}

- (void)sendLocationToWebsite {
    
    static NSString *defaultUploadWebsite = @"http://www.websmithing.com/gpstracker2/getgooglemap3.php";
    NSURLRequest *defaultUploadWebsiteRequest = [NSURLRequest requestWithURL:[NSURL URLWithString:defaultUploadWebsite]];
    
    [NSURLConnection sendAsynchronousRequest:defaultUploadWebsiteRequest queue:[NSOperationQueue mainQueue]
                           completionHandler:^(NSURLResponse *response, NSData *data, NSError *error) {
                               
                               // back on the main thread, check for errors, if no errors start the parsing
                               //
                               [UIApplication sharedApplication].networkActivityIndicatorVisible = NO;
                               
                               // here we check for any returned NSError from the server, "and" we also check for any http response errors
                               if (error != nil) {
                                   //[self handleError:error];
                               }
                               else {
                                   // check for any response errors
                                   NSHTTPURLResponse *httpResponse = (NSHTTPURLResponse *)response;
                                   if ((([httpResponse statusCode]/100) == 2) && [[response MIMEType] isEqual:@"application/atom+xml"]) {
                                       
                                       // Update the UI and start parsing the data,
                                       // Spawn an NSOperation to parse the earthquake data so that the UI is not
                                       // blocked while the application parses the XML data.
                                       //
                                       //APLParseOperation *parseOperation = [[APLParseOperation alloc] initWithData:data];
                                       //[self.parseQueue addOperation:parseOperation];
                                   }
                                   else {
                                       NSString *errorString =
                                       NSLocalizedString(@"HTTP Error", @"Error message displayed when receving a connection error.");
                                       NSDictionary *userInfo = @{NSLocalizedDescriptionKey : errorString};
                                       //NSError *reportError = [NSError errorWithDomain:@"HTTP" code:[httpResponse statusCode] userInfo:userInfo];
                                       //[self handleError:reportError];
                                   }
                               }
                           }];
    
    
}
@end