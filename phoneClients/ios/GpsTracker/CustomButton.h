#import <UIKit/UIKit.h>
#import <QuartzCore/QuartzCore.h>

@interface CustomButton : UIButton

- (void)setButtonColor:(NSString *)buttonHexColor andHighLightColor:(NSString *)highlightHexColor andTextColor:(NSString *)textHexColor andHighlightTextColor:(NSString *)highlighTextHexColor;

@end
