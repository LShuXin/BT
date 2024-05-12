//
//  BTBlurView.m
//

#import "BTBlurView.h"


@interface BTBlurView()
@property(nonatomic, strong)UIToolbar *toolbar;
@end


@implementation BTBlurView
// Creates a view layout object from data in a given unarchiver.
-(instancetype)initWithCoder:(NSCoder *)aDecoder
{
    self = [super initWithCoder:aDecoder];
    if (self)
    {
        [self setup];
    }
    return self;
}

-(instancetype)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self)
    {
        [self setup];
    }
    return self;
}

-(instancetype)init
{
    self = [super init];
    if (self)
    {
        [self setup];
    }
    return self;
}

-(void)setup
{
    // subviews are confined to the bounds of the view.
    // If we don't clip to bounds the toolbar draws a thin shadow on top
    [self setClipsToBounds:YES];
    
    if (![self toolbar])
    {
        [self setToolbar:[[UIToolbar alloc] initWithFrame:[self bounds]]];
        // https://developer.apple.com/documentation/uikit/uiview/1622572-translatesautoresizingmaskintoco?language=objc
        [self.toolbar setTranslatesAutoresizingMaskIntoConstraints:NO];
        [self insertSubview:[self toolbar] atIndex:0];
        
        [self addConstraints:[NSLayoutConstraint constraintsWithVisualFormat:@"H:|[_toolbar]|"
                                                                     options:0
                                                                     metrics:0
                                                                       views:NSDictionaryOfVariableBindings(_toolbar)]];
        [self addConstraints:[NSLayoutConstraint constraintsWithVisualFormat:@"V:|[_toolbar]|"
                                                                     options:0
                                                                     metrics:0
                                                                       views:NSDictionaryOfVariableBindings(_toolbar)]];
    }
}

-(void)setBlurTintColor:(UIColor *)blurTintColor
{
    [self.toolbar setBarTintColor:blurTintColor];
}

@end
