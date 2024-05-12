//
//  BTChatBaseCell.m
//

#import "BTChatBaseCell.h"
#import "BTUserModule.h"
#import "BTChattingMainViewController.h"
#import "BTPublicProfileViewController.h"
#import <UIImageView+WebCache.h>
#import "UIView+BTAddition.h"


CGFloat const bt_avatarEdge = 5.0;                 //头像到边缘的距离
CGFloat const bt_avatarBubbleGap = 10;             //头像和气泡之间的距离
CGFloat const bt_bubbleUpDown = 10;                //气泡到上下边缘的距离


@interface BTChatBaseCell()
@property(copy)NSString * currentUserId;
@end


@implementation BTChatBaseCell

-(id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self)
    {
        [self.contentView setBackgroundColor:[UIColor clearColor]];
        [self setBackgroundColor:[UIColor clearColor]];
        
        self.userAvatar = [[UIImageView alloc] initWithFrame:CGRectMake(0, 0, 40, 40)];
        [self.userAvatar setUserInteractionEnabled:YES];
        [self.contentView addSubview:self.userAvatar];
        UITapGestureRecognizer *openProfile = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(openProfilePage)];
        [self.userAvatar addGestureRecognizer:openProfile];
        
        self.userName = [[UILabel alloc] initWithFrame:CGRectMake(60, 10, 100, 15)];
        [self.userName setBackgroundColor:[UIColor clearColor]];
        [self.userName setFont:[UIFont systemFontOfSize:13.0]];
        [self.userName setTextColor:[UIColor grayColor]];
        [self.contentView addSubview:self.userName];
        
        self.bubbleImageView = [[BTMenuImageView alloc] initWithFrame:CGRectMake(0, 0, 0, 0)];
        [self.contentView addSubview:self.bubbleImageView];
        [self.bubbleImageView setUserInteractionEnabled:YES];
        self.bubbleImageView.delegate = self;
        self.bubbleImageView.tag = 1000;
        
        self.activityView = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleGray];
        [self.activityView setHidesWhenStopped:YES];
        [self.activityView setHidden:YES];
        [self.contentView addSubview:self.activityView];
        
        self.sendFailuredImageView = [[UIImageView alloc] initWithFrame:CGRectMake(0, 0, 20, 20)];
        [self.sendFailuredImageView setImage:[UIImage imageNamed:@"dd_send_failed"]];
        [self.sendFailuredImageView setHidden:YES];
        self.sendFailuredImageView.userInteractionEnabled=YES;
        [self.contentView addSubview:self.sendFailuredImageView];
        [self.contentView setAutoresizesSubviews:NO];
        UITapGestureRecognizer *pan = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(clickTheSendAgain)];
        [self.sendFailuredImageView addGestureRecognizer:pan];
    }
    return self;
}

-(void)openProfilePage
{
    if (self.currentUserId)
    {
        [[BTUserModule shareInstance] getUserByUserId:self.currentUserId completion:^(BTUserEntity *user) {
            BTPublicProfileViewController *public = [BTPublicProfileViewController new];
            public.user = user;
            [[BTChattingMainViewController shareInstance].navigationController pushViewController:public animated:YES];
        }];
    }
}

-(void)clickTheSendAgain
{
    UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"重发"
                                                    message:@"是否重新发送此消息"
                                                   delegate:self
                                          cancelButtonTitle:@"取消"
                                          otherButtonTitles:@"确定", nil];
    [alert show];
}

-(void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex
{
    if (buttonIndex == 1)
    {
        [self clickTheSendAgain:nil];
    }
}

-(void)setSelected:(BOOL)selected animated:(BOOL)animated
{
    [super setSelected:selected animated:animated];
}

-(void)setContent:(BTMessageEntity *)content
{
    id<BTChatCellProtocol> cell = (id<BTChatCellProtocol>)self;
    
    // 设置头像位置
    switch (self.location)
    {
        case BUBBLE_LEFT:
            self.userAvatar.left = bt_avatarEdge;
            break;
        case BUBBLE_RIGHT:
            self.userAvatar.right = BTFullWidth - bt_avatarEdge;
            break;
        default:
            break;
    }
    [self.userAvatar setContentMode:UIViewContentModeScaleAspectFill];
    [self.userAvatar setClipsToBounds:YES];
    [self.userAvatar setTop:bt_bubbleUpDown];
    self.currentUserId = content.senderId;
    [[BTUserModule shareInstance] getUserByUserId:content.senderId completion:^(BTUserEntity *user) {
        NSURL *avatarURL = [NSURL URLWithString:[user getAvatarUrl]];
        [self.userAvatar sd_setImageWithURL:avatarURL placeholderImage:[UIImage imageNamed:@"user_placeholder"]];
        [self.userName setText:user.nick];
    }];
    
    //设置气泡位置
    CGSize size = [cell sizeForContent:content];
    float bubbleY = bt_bubbleUpDown;
    float bubbleheight = [cell contentUpGapWithBubble] + size.height + [cell contentDownGapWithBubble];
    float bubbleWidth = [cell contentLeftGapWithBubble] + size.width + [cell contentRightGapWithBubble];
    float bubbleX = 0;
    UIImage *bubbleImage = nil;
    switch (self.location)
    {
        case BUBBLE_LEFT:
            [self.userName setHidden:NO];
            bubbleImage = [UIImage imageNamed:@"left"];
            bubbleX = bt_avatarEdge + self.userAvatar.width + bt_avatarBubbleGap;
            break;
        case BUBBLE_RIGHT:
            [self.userName setHidden:YES];
            bubbleImage = [UIImage imageNamed:@"right"];
            bubbleX =  BTFullWidth - bt_avatarEdge - self.userAvatar.width - bt_avatarBubbleGap - bubbleWidth;
            break;
        default:
            break;
    }
    if (self.session.sessionType == SessionType_SessionTypeSingle)
    {
        [self.bubbleImageView setFrame:CGRectMake(bubbleX, bt_avatarEdge * 2, bubbleWidth, bubbleheight)];
        [self.userName setHidden:YES];
    }
    else
    {
        [self.bubbleImageView setFrame:CGRectMake(bubbleX, bubbleY + 20, bubbleWidth, bubbleheight)];
        if (self.location != BUBBLE_RIGHT)
        {
            [self.userName setHidden:NO];
        }
        else
        {
          [self.bubbleImageView setFrame:CGRectMake(bubbleX, bt_avatarEdge * 2, bubbleWidth, bubbleheight)];
        }
    }
    bubbleImage = [bubbleImage stretchableImageWithLeftCapWidth:20 topCapHeight:20];
    [self.bubbleImageView setImage:bubbleImage];
    
    //设置菊花位置
    switch (self.location)
    {
        case BUBBLE_LEFT:
            self.activityView.left = self.bubbleImageView.right + 10;
            self.sendFailuredImageView.left = self.bubbleImageView.right + 10;
            break;
        case BUBBLE_RIGHT:
            self.activityView.right = self.bubbleImageView.left - 10;
            self.sendFailuredImageView.right = self.bubbleImageView.left - 10;
            break;
        default:
            break;
    }
    
    BTImageShowMenu showMenu = 0;
    switch (content.state)
    {
        case MSG_SENDING:
            [self.activityView startAnimating];
            self.sendFailuredImageView.hidden = YES;
            break;
        case MSG_SEND_FAILURE:
            [self.activityView stopAnimating];
            self.sendFailuredImageView.hidden = NO;
            showMenu = SHOW_SEND_AGAIN;
            break;
        case MSG_SEND_SUCCESS:
            [self.activityView stopAnimating];
            self.sendFailuredImageView.hidden = YES;
            break;
    }
    
    self.activityView.centerY = self.bubbleImageView.centerY;
    self.sendFailuredImageView.centerY = self.bubbleImageView.centerY;
    
    //设置菜单
    switch (content.msgContentType)
    {
        case MSG_TYPE_IMAGE:
            showMenu = showMenu | SHOW_PREVIEW;
            break;
        case MSG_TYPE_TEXT:
            showMenu = showMenu | SHOW_COPY;
            break;
        case MSG_TYPE_VOICE:
            showMenu = showMenu | SHOW_EARPHONE_PLAY | SHOW_SPEAKER_PLAY;
            break;

    }
    
    [self.bubbleImageView setShowMenu:showMenu];
    
    // 设置内容位置
    [cell layoutContentView:content];
}

-(void)showSendFailure
{
    [self.activityView stopAnimating];
    self.sendFailuredImageView.hidden = NO;
    BTImageShowMenu showMenu = self.bubbleImageView.showMenu | SHOW_SEND_AGAIN;
    [self.bubbleImageView setShowMenu:showMenu];
}

-(void)showSendSuccess
{
    [self.activityView stopAnimating];
    self.sendFailuredImageView.hidden = YES;
}

-(void)showSending
{
    [self.activityView startAnimating];
    self.sendFailuredImageView.hidden = YES;
}

#pragma mark -
#pragma mark BTMenuImageView Delegate
-(void)clickTheCopy:(BTMenuImageView *)imageView
{
    //子类去继承
}

-(void)clickTheEarphonePlay:(BTMenuImageView *)imageView
{
    //子类去继承
}

-(void)clickTheSpeakerPlay:(BTMenuImageView *)imageView
{
    //子类去继承
}

-(void)clickTheSendAgain:(BTMenuImageView *)imageView
{
    //子类去继承
}

-(void)tapTheImageView:(BTMenuImageView *)imageView
{
    if (self.tapInBubble)
    {
        self.tapInBubble();
    }
}

-(void)clickThePreview:(BTMenuImageView *)imageView
{
    //子类去继承
}
@end
