//
//  UiColor+HexColor.h
//  GpsTracker
//
//  Created by Nick Fox on 6/25/14.
//  Copyright (c) 2014 Nick Fox. All rights reserved.
//

@interface UIColor (HexColor)

// this category allows us to create a UIColor using a hex color value like #FF00FF
+ (UIColor *)colorFromHexString:(NSString *)hexString andAlpha:(float)alphaValue;

@end
