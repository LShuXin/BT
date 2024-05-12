//
//  BTChatTextCell.m
//

#import "BTChatTextCell.h"
#import "UIView+BTAddition.h"
#import "BTPublicDefine.h"
#import "BTDatabaseUtil.h"
#import "BTMessageSendManager.h"
#import "BTSessionModule.h"


static int const fontsize = 16;
static float const maxContentWidth = 200;

@interface BTChatTextCell(PrivateAPI)
-(void)layoutLeftLocationContent:(NSString *)content;
-(void)layoutRightLocationContent:(NSString *)content;
@end

@implementation BTChatTextCell

-(id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self)
    {
        self.contentLabel = [[UILabel alloc] init];
        [self.contentView addSubview:self.contentLabel];
    }
    return self;
}

-(void)setSelected:(BOOL)selected animated:(BOOL)animated
{
    [super setSelected:selected animated:animated];
}

-(void)setContent:(BTMessageEntity *)content
{
    [super setContent:content];
    [self.contentLabel setFont:[UIFont systemFontOfSize:fontsize]];
    [self.contentLabel setNumberOfLines:10000];
    [self.contentLabel setBackgroundColor:[UIColor clearColor]];
    [self.contentLabel setText:content.msgContent];
    
    switch (self.location)
    {
        case BUBBLE_LEFT:
            [self.contentLabel setTextColor:RGB(61, 61, 61)];
            break;
        case BUBBLE_RIGHT:
            [self.contentLabel setTextColor:[UIColor whiteColor]];
            break;
    }
}

#pragma mark - BTChatCellProtocol
-(CGSize)sizeForContent:(BTMessageEntity *)message
{
    NSString *content = message.msgContent;
    UIFont *font = [UIFont systemFontOfSize:fontsize];
    CGSize size = [content sizeWithFont:font
                      constrainedToSize:CGSizeMake(maxContentWidth, 1000000)
                          lineBreakMode:NSLineBreakByWordWrapping];
    return size;
}

-(float)contentUpGapWithBubble
{
    return 12;
}

-(float)contentDownGapWithBubble
{
    return 12;
}

-(float)contentLeftGapWithBubble
{
    switch (self.location)
    {
        case BUBBLE_LEFT:
            return 20;
        case BUBBLE_RIGHT:
            return 10;
    }
}

-(float)contentRightGapWithBubble
{
    switch (self.location)
    {
        case BUBBLE_LEFT:
            return 10;
        case BUBBLE_RIGHT:
            return 20;
    }
}

-(void)layoutContentView:(BTMessageEntity *)content
{
    float x = self.bubbleImageView.left + [self contentLeftGapWithBubble];
    float y = self.bubbleImageView.top + [self contentUpGapWithBubble];
    CGSize size = [self sizeForContent:content];
    [self.contentLabel setFrame:CGRectMake(x, y, size.width, size.height + 3)];
}

-(float)cellHeightForMessage:(BTMessageEntity *)message
{
    CGSize size = [self sizeForContent:message];
    float height = [self contentUpGapWithBubble] + [self contentDownGapWithBubble] + size.height + bt_bubbleUpDown * 2;
    return height;
}

#pragma mark -
#pragma mark BTMenuImageView Delegate
-(void)clickTheCopy:(BTMenuImageView *)imageView
{
    UIPasteboard *pboard = [UIPasteboard generalPasteboard];
    pboard.string = self.contentLabel.text;
}

-(void)clickTheEarphonePlay:(BTMenuImageView *)imageView
{
}

-(void)clickTheSpeakerPlay:(BTMenuImageView *)imageView
{
}

-(void)clickTheSendAgain:(BTMenuImageView *)imageView
{
    if (self.sendAgain)
    {
        self.sendAgain();
    }
}

-(void)tapTheImageView:(BTMenuImageView *)imageView
{
    [super tapTheImageView:imageView];
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
