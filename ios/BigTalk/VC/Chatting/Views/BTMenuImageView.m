//
//  BTMenuImageView.m
//

#import "BTMenuImageView.h"

@implementation BTMenuImageView
{
    BTImageShowMenu _showMenu;
}

-(id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self)
    {
        // Initialization code
        [self attachTapHandler];
    }
    return self;
}

-(void)setShowMenu:(BTImageShowMenu)menu
{
    _showMenu = menu;
}

-(BOOL)canBecomeFirstResponder
{
    return YES;
}

//"反馈"关心的功能
-(BOOL)canPerformAction:(SEL)action withSender:(id)sender
{
    if ((_showMenu & SHOW_EARPHONE_PLAY) && action == @selector(earphonePlay:))
    {
        return YES;
    }
    else if ((_showMenu & SHOW_SEND_AGAIN) && action == @selector(sendAgain:))
    {
        return YES;
    }
    else if (_showMenu & SHOW_SPEAKER_PLAY && action == @selector(speakerPlay:))
    {
        return YES;
    }
    else if (_showMenu & SHOW_COPY && action == @selector(copyString:))
    {
        return YES;
    }
    else if (_showMenu & SHOW_PREVIEW && action == @selector(imagePreview:))
    {
        return YES;
    }
    return NO;
}

// UILabel默认是不接收事件的，我们需要自己添加touch事件
-(void)attachTapHandler
{
    self.userInteractionEnabled = YES;  //用户交互的总开关
    UITapGestureRecognizer *touch = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(handleTap:)];
    touch.numberOfTapsRequired = 1;
    [self addGestureRecognizer:touch];
    
    UILongPressGestureRecognizer *press = [[UILongPressGestureRecognizer alloc]initWithTarget:self action:@selector(longPress:)];
    press.minimumPressDuration = 0.5;
    [self addGestureRecognizer:press];
}

-(void)awakeFromNib
{
    [super awakeFromNib];
    [self attachTapHandler];
}

-(void)handleTap:(UIGestureRecognizer *) recognizer
{
    if ([self.delegate respondsToSelector:@selector(tapTheImageView:)])
    {
        [self.delegate tapTheImageView:self];
    }
}

-(void)longPress:(UILongPressGestureRecognizer *)recognizer
{
    if (recognizer.state == UIGestureRecognizerStateBegan)
    {
        [self becomeFirstResponder];
        UIMenuItem *copyItem = [[UIMenuItem alloc] initWithTitle:@"复制" action:@selector(copyString:)];
        UIMenuItem *sendAgain = [[UIMenuItem alloc] initWithTitle:@"重发" action:@selector(sendAgain:)];
        UIMenuItem *earphonePlayItem = [[UIMenuItem alloc] initWithTitle:@"听筒播放" action:@selector(earphonePlay:)];
        UIMenuItem *speakerPlayItem = [[UIMenuItem alloc] initWithTitle:@"扬声器播放" action:@selector(speakerPlay:)];
        UIMenuItem *previewItem = [[UIMenuItem alloc] initWithTitle:@"预览" action:@selector(imagePreview:)];
        
        UIMenuController *menu = [UIMenuController sharedMenuController];
        [menu setMenuItems:nil];
        [menu setMenuItems:[NSArray arrayWithObjects:copyItem, sendAgain, earphonePlayItem, speakerPlayItem, previewItem, nil]];
        [menu setTargetRect:self.frame inView:self.superview];
        [menu setMenuVisible:YES animated:YES];
        NSLog(@"menuItems:%@", menu.menuItems);
    }
}

#pragma mark - menu selecter
-(void)earphonePlay:(id)sender
{
    //听筒播放
    if ([self.delegate respondsToSelector:@selector(clickTheEarphonePlay:)])
    {
        [self.delegate clickTheEarphonePlay:self];
    }
}

-(void)speakerPlay:(id)sender
{
    //扬声器播放
    if ([self.delegate respondsToSelector:@selector(clickTheSpeakerPlay:)])
    {
        [self.delegate clickTheSpeakerPlay:self];
    }
}

-(void)sendAgain:(id)sender
{
    //重发
    if ([self.delegate respondsToSelector:@selector(clickTheSendAgain:)])
    {
        [self.delegate clickTheSendAgain:self];
    }
}

-(void)copyString:(id)sender
{
    //复制
    if ([self.delegate respondsToSelector:@selector(clickTheCopy:)])
    {
        [self.delegate clickTheCopy:self];
    }
}

-(void)imagePreview:(id)sender
{
    //预览
    if ([self.delegate respondsToSelector:@selector(clickThePreview:)])
    {
        [self.delegate clickThePreview:self];
    }
}
@end
