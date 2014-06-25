
#import "CustomButton.h"
#import "UIColor+HexColor.h"

@interface CustomButton ()
    @property (assign, nonatomic) BOOL tapped;
    @property (assign, nonatomic) BOOL setupLayers;
    @property (strong, nonatomic) CALayer *backgroundLayer;
    @property (strong, nonatomic) CALayer *highlightBackgroundLayer;
    @property (strong, nonatomic) CALayer *innerGlow;
    @property (strong, nonatomic) NSString *buttonHexColor;
    @property (strong, nonatomic) NSString *highlightHexColor;
    @property (strong, nonatomic) NSString *textHexColor;
    @property (strong, nonatomic) NSString *highlighTextHexColor;
@end

@implementation CustomButton

#pragma mark -

- (void)drawRect:(CGRect)rect
{
    // this is not drawing properly when changing orientation, but i'm staying in portrait anyway, will deal with it later
    
    if (!_setupLayers)
    {
        self.layer.cornerRadius = 4.5f;
        self.layer.masksToBounds = YES;
        self.layer.borderWidth = 1;        
        self.layer.borderColor = [UIColor colorFromHexString:@"#999999" andAlpha:1.0].CGColor;
        
        // [self setInnerGlow];
        [self setBackgroundLayer];
        [self setHighlightBackgroundLayer];
    }
    
    if (_tapped)
    {
        //NSLog(@"highlighted");
        _highlightBackgroundLayer.hidden = NO;
        // self.titleLabel.textColor = [UIColor colorFromHexString:@"#999999" andAlpha:1.0];
        self.titleLabel.textColor = [UIColor colorFromHexString:_highlighTextHexColor andAlpha:1.0];
        _backgroundLayer.backgroundColor = [UIColor colorFromHexString:_highlightHexColor andAlpha:1.0].CGColor;
    }
    else
    {
        //NSLog(@"not highlighted");
        _highlightBackgroundLayer.hidden = YES;
        // self.titleLabel.textColor = [UIColor colorFromHexString:@"#333333" andAlpha:1.0];
        self.titleLabel.textColor = [UIColor colorFromHexString:_textHexColor andAlpha:1.0];
        _backgroundLayer.backgroundColor = [UIColor colorFromHexString:_buttonHexColor andAlpha:1.0].CGColor;
    }
    
    _setupLayers = YES;
}

#pragma mark - Layer setters

- (void)setBackgroundLayer
{
    if (!_backgroundLayer)
    {
        _backgroundLayer = [CAGradientLayer layer];
        _backgroundLayer.frame = self.bounds;
        _backgroundLayer.backgroundColor = [UIColor colorFromHexString:_buttonHexColor andAlpha:1.0].CGColor;
        
        [self.layer insertSublayer:_backgroundLayer atIndex:0];
        
        // here is the red color, #ff0033 and its highlight, #ff7691
        // here is the green color, #33ffcc and it's highlight, #a9ffe9
    }
}

- (void)setHighlightBackgroundLayer
{
    if (!_highlightBackgroundLayer)
    {
        _highlightBackgroundLayer = [CAGradientLayer layer];
        _highlightBackgroundLayer.frame = self.bounds;
        _highlightBackgroundLayer.backgroundColor = [UIColor colorFromHexString:_highlightHexColor andAlpha:1.0].CGColor;
        [self.layer insertSublayer:_highlightBackgroundLayer atIndex:1];
    }
}

- (void)setInnerGlow
{
    if (!_innerGlow)
    {
        _innerGlow = [CALayer layer];
        CGRect innerGlowFrame = CGRectMake(self.bounds.origin.x+1, self.bounds.origin.y+1, self.bounds.size.width-2, self.bounds.size.height-2);
        _innerGlow.frame = innerGlowFrame;
        _innerGlow.cornerRadius= 4.5f;
        _innerGlow.borderWidth = 1;
        _innerGlow.borderColor = [[UIColor whiteColor] CGColor];
        _innerGlow.opacity = 0.5;
        
        [self.layer insertSublayer:_innerGlow atIndex:2];
    }
}

- (void)setButtonColor:(NSString *)buttonHexColor andHighLightColor:(NSString *)highlightHexColor andTextColor:(NSString *)textHexColor andHighlightTextColor:(NSString *)highlighTextHexColor {
    _buttonHexColor = buttonHexColor;
    _highlightHexColor = highlightHexColor;
    _textHexColor = textHexColor;
    _highlighTextHexColor = highlighTextHexColor;
    
    //[self setNeedsDisplay];
}

#pragma mark - Touch event overrides

-(void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event
{
    _tapped = YES;
    [self setNeedsDisplay];
    [super touchesBegan:touches withEvent:event];
}

-(void)touchesEnded:(NSSet *)touches withEvent:(UIEvent *)event
{
    _tapped = NO;
    [self setNeedsDisplay];
    [super touchesEnded:touches withEvent:event];
}

-(void)touchesMoved:(NSSet *)touches withEvent:(UIEvent *)event
{
    CGPoint touchPoint = [[touches anyObject] locationInView:self];
    CGRect testRect = CGRectMake(0, 0, self.frame.size.width, self.frame.size.height);
    
    if (CGRectContainsPoint(testRect, touchPoint))
    {
        _tapped = YES;
        [self setNeedsDisplay];
    }
    
    else
    {
        _tapped = NO;
        [self setNeedsDisplay];
    }
    
    [super touchesMoved:touches withEvent:event];
}

@end
