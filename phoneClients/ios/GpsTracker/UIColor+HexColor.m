//
//  UiColor+HexColor.m
//  GpsTracker
//
//  Created by Nick Fox on 6/25/14.
//  Copyright (c) 2014 Nick Fox. All rights reserved.
//

#import "UIColor+HexColor.h"

// modified from https://gist.github.com/simonwhitaker/1219029

@implementation UIColor (HexColor)

+ (UIColor *)colorFromHexString:(NSString *)hexValue andAlpha:(float)alphaValue
{
    UIColor *defaultColor = [UIColor blackColor];
    
    // Strip leading # if there is one
    if ([hexValue hasPrefix:@"#"] && [hexValue length] > 1) {
        hexValue = [hexValue substringFromIndex:1];
    }
    
    NSUInteger componentLength = 0;
    if ([hexValue length] == 3)
        componentLength = 1;
    else if ([hexValue length] == 6)
        componentLength = 2;
    else
        return defaultColor;
    
    BOOL isValid = YES;
    CGFloat components[3];
    
    for (NSUInteger i = 0; i < 3; i++) {
        NSString *component = [hexValue substringWithRange:NSMakeRange(componentLength * i, componentLength)];
        if (componentLength == 1) {
            component = [component stringByAppendingString:component];
        }
        NSScanner *scanner = [NSScanner scannerWithString:component];
        unsigned int value;
        isValid &= [scanner scanHexInt:&value];
        components[i] = (CGFloat)value / 256.0;
    }
    
    if (!isValid) {
        return defaultColor;
    }
    
    if (alphaValue < 0.0 || alphaValue > 1.0 ) {
        alphaValue = 1.0;
    }
    
    return [UIColor colorWithRed:components[0]
                           green:components[1]
                            blue:components[2]
                           alpha:alphaValue];
}
    
@end
