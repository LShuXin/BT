//
//  BTEmotionCell.m
//

#import "BTEmotionCell.h"
#import "BTEmotionsModule.h"
#import "BTMessageSendManager.h"
#import "UIView+BTAddition.h"
#import "BTSessionModule.h"
#import "UIImage+GIF.h"


@implementation BTEmotionCell

-(id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self)
    {
        self.msgImgView.userInteractionEnabled = NO;
        [self.msgImgView setClipsToBounds:YES];
        [self.msgImgView setContentMode:UIViewContentModeScaleAspectFill];
    }
    return self;
}

#pragma mark -
#pragma mark DDChatCellProtocol Protocol
-(CGSize)sizeForContent:(BTMessageEntity *)content
{
    float height = 133;
    float width = 100;
    return CGSizeMake(width, height);
}

-(float)contentUpGapWithBubble
{
    return 1;
}

-(float)contentDownGapWithBubble
{
    return 1;
}

-(float)contentLeftGapWithBubble
{
    switch (self.location)
    {
        case BUBBLE_RIGHT:
            return 1;
        case BUBBLE_LEFT:
            return 8.5;
    }
    return 0;
}

-(float)contentRightGapWithBubble
{
    switch (self.location)
    {
        case BUBBLE_RIGHT:
            return 6.5;
            break;
        case BUBBLE_LEFT:
            return 1;
            break;
    }
    return 0;
}

-(void)layoutContentView:(BTMessageEntity *)content
{
    float x = self.bubbleImageView.left + [self contentLeftGapWithBubble];
    float y = self.bubbleImageView.top + [self contentUpGapWithBubble];
    CGSize size = [self sizeForContent:content];
    [self.msgImgView setFrame:CGRectMake(x, y, size.width, size.height)];
}

-(float)cellHeightForMessage:(BTMessageEntity *)message
{
    return 27 + 2 * bt_bubbleUpDown;
}

-(void)setContent:(BTMessageEntity *)content
{
    [super setContent:content];
    NSString *emotionStr = content.msgContent;
    NSString *emotionImageStr = [[BTEmotionsModule shareInstance].emotionUnicodeDic objectForKey:emotionStr];
    NSArray *array = [emotionImageStr componentsSeparatedByString:@"."];
    UIImage *emotion = [UIImage sd_animatedGIFNamed:array[0]];
    if (emotion)
    {
        [self.msgImgView setImage:emotion];
        [self.bubbleImageView setHidden:YES];
    }
}

-(void)sendTextAgain:(BTMessageEntity *)message
{
    message.state = MSG_SENDING;
    [self showSending];
    [[BTMessageSendManager instance] sendMessage:message
                                         isGroup:[message isGroupMessage]
                                         session:[[BTSessionModule sharedInstance] getSessionById:message.sessionId]
                                      completion:^(BTMessageEntity *theMessage, NSError *error) {
        
        [self showSendSuccess];
    } error:^(NSError *error) {
        [self showSendFailure];
    }];
}

@end
