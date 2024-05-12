//
//  BTAlbumDetailsBottomBar.m
//

#import "BTAlbumDetailsBottomBar.h"
#import "BTPublicDefine.h"
#import "BTChattingMainViewController.h"


@implementation BTAlbumDetailsBottomBar

-(id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self)
    {
        UIImageView *line = [[UIImageView alloc] initWithFrame:CGRectMake(0, 0, frame.size.width, 1)];
        line.backgroundColor = RGB(228, 228, 228);
        [self addSubview:line];
        
        self.backgroundColor = RGB(249, 249, 249);
        
        UIButton *preview = [UIButton buttonWithType:UIButtonTypeCustom];
        preview.frame = CGRectMake(13, 5, 75, 35);
        [preview setTitle:@"预览" forState:UIControlStateNormal];
        [preview setTitle:@"预览" forState:UIControlStateHighlighted];
        [preview setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        [preview setTitleColor:[UIColor whiteColor] forState:UIControlStateHighlighted];
        [preview setBackgroundColor:RGB(34, 157, 254)];
        [preview addTarget:self action:@selector(previewPicture:) forControlEvents:UIControlEventTouchUpInside];
        [preview setClipsToBounds:YES];
        [preview.layer setCornerRadius:3];
        [self addSubview:preview];
        
        self.send = [UIButton buttonWithType:UIButtonTypeCustom];
        self.send.frame = CGRectMake(BTFullWidth - 90, 5, 75, 35);
        [self.send setTitle:@"发送" forState:UIControlStateNormal];
        [self.send setTitle:@"发送" forState:UIControlStateHighlighted];
        [self.send setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        [self.send setTitleColor:[UIColor whiteColor] forState:UIControlStateHighlighted];
        [self.send setBackgroundColor:RGB(34, 157, 254)];
        [self.send addTarget:self action:@selector(sendPicture:) forControlEvents:UIControlEventTouchUpInside];
        [self.send setClipsToBounds:YES];
        [self.send.layer setCornerRadius:3];
        [self addSubview:self.send];
    }
    return self;
}

-(void)setSendButtonTitle:(int)num
{
    [self.send setTitle:[NSString stringWithFormat:@"%@(%d)", @"发送", num] forState:UIControlStateNormal];
    [self.send setTitle:[NSString stringWithFormat:@"%@(%d)", @"发送", num] forState:UIControlStateHighlighted];
}

-(void)previewPicture:(id)sender
{
    self.block(0);
}

-(void)sendPicture:(id)sender
{
    self.block(1);
}

-(void)dealloc
{
    self.block = nil;
}



@end
