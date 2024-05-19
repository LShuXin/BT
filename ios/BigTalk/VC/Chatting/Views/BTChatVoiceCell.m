//
//  BTChatVoiceCell.m
//

#import "BTChatVoiceCell.h"
#import "BTMessageEntity.h"
#import "UIView+BTAddition.h"
#import "PlayerManager.h"
#import "RecorderManager.h"
#import "BTDatabaseUtil.h"
#import "BTMessageSendManager.h"
#import "BTSessionModule.h"

static float const maxCellLength = 180;
static float const minCellLength = 17;

@interface BTChatVoiceCell(privateAPI)
-(float)lengthForVoiceLength:(float)voiceLength;
@end


@implementation BTChatVoiceCell
{
    NSString *_voicePath;
}

-(id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self)
    {
        _voiceImageView = [[UIImageView alloc] initWithFrame:CGRectMake(0, 0, 0, 0)];
        [self.contentView addSubview:_voiceImageView];
        
        _timeLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, 0, 0)];
        [_timeLabel setFont:[UIFont systemFontOfSize:10]];
        [_timeLabel setBackgroundColor:[UIColor clearColor]];
        [self.contentView addSubview:_timeLabel];
        
        _playedLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, 10, 10)];
        [_playedLabel setBackgroundColor:[UIColor redColor]];
        [_playedLabel.layer setCornerRadius:5];
        [_playedLabel setClipsToBounds:YES];
        [self.contentView addSubview:_playedLabel];
    }
    return self;
}

-(void)setContent:(BTMessageEntity *)content
{
    [super setContent:content];
    
    if ([content.info[kBTVoicePlayed] intValue])
    {
        [_playedLabel setHidden:YES];
    }
    else
    {
        [_playedLabel setHidden:NO];
    }
    
    _voicePath = [content.msgContent copy];
    NSArray *imageArray = nil;
    
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
    
    switch (self.location)
    {
        case BUBBLE_LEFT:
            imageArray = @[[UIImage imageNamed:@"dd_left_voice_one"],
                           [UIImage imageNamed:@"dd_left_voice_two"],
                           [UIImage imageNamed:@"dd_left_voice_three"]];
            [_voiceImageView setContentMode:UIViewContentModeLeft];
            [_voiceImageView setImage:[UIImage imageNamed:@"dd_left_voice_three"]];
            break;
        case BUBBLE_RIGHT:
            imageArray = @[[UIImage imageNamed:@"dd_right_voice_one"],
                           [UIImage imageNamed:@"dd_right_voice_two"],
                           [UIImage imageNamed:@"dd_right_voice_three"]];
            [_voiceImageView setContentMode:UIViewContentModeRight];
            [_voiceImageView setImage:[UIImage imageNamed:@"dd_right_voice_three"]];
            self.activityView.right = self.bubbleImageView.left - 25;
            self.sendFailuredImageView.right = self.bubbleImageView.left - 25;
            [_playedLabel setHidden:YES];
            break;
    }
    float voiceLength = [content.info[kBTVoiceLength] floatValue];
    [_voiceImageView setAnimationImages:imageArray];
    [_voiceImageView setAnimationRepeatCount:voiceLength];
    [_voiceImageView setAnimationDuration:1];
    
    NSUInteger timeLength = [content.info[kBTVoiceLength] longValue];
    NSString *lengthString = [NSString stringWithFormat:@"%ld\"",timeLength];
    [_timeLabel setText:lengthString];
}

-(void)showVoicePlayed
{
    [_playedLabel setHidden:YES];
}

-(void)stopVoicePlayAnimation
{
    [_voiceImageView stopAnimating];
}

#pragma mark -
#pragma mark BTChatCellProtocol Protocol
-(CGSize)sizeForContent:(BTMessageEntity *)content
{
    float voiceLength = [content.info[kBTVoiceLength] floatValue];
    float width = [self lengthForVoiceLength:voiceLength];
    return CGSizeMake(width, 17);
}

-(float)contentUpGapWithBubble
{
    return 13;
}

-(float)contentDownGapWithBubble
{
    return 13;
}

-(float)contentLeftGapWithBubble
{
    switch (self.location)
    {
        case BUBBLE_RIGHT:
            return 0;
        case BUBBLE_LEFT:
            return 15;
    }
    return 0;
}

-(float)contentRightGapWithBubble
{
    switch (self.location)
    {
        case BUBBLE_RIGHT:
            return 15;
            break;
        case BUBBLE_LEFT:
            return 0;
            break;
    }
    return 0;
}

-(void)layoutContentView:(BTMessageEntity *)content
{
    float y = self.bubbleImageView.top + [self contentUpGapWithBubble];
    [_voiceImageView setFrame:CGRectMake(0, y, 11, 17)];
    switch (self.location)
    {
        case BUBBLE_RIGHT:
            _voiceImageView.left = self.bubbleImageView.left + [self contentLeftGapWithBubble] + 6.0;
            [_timeLabel setFrame:CGRectMake(_voiceImageView.right + 3, 0, 20, 15)];
            _timeLabel.centerY = self.bubbleImageView.centerY;
            [_timeLabel setTextAlignment:NSTextAlignmentLeft];
            
            _playedLabel.left = _timeLabel.left + 3;
            _playedLabel.top = self.bubbleImageView.top - 2;
        
            break;
        case BUBBLE_LEFT:
            _voiceImageView.right = self.bubbleImageView.right - [self contentRightGapWithBubble];
            [_timeLabel setFrame:CGRectMake(0, 0, 20, 15)];
            _timeLabel.right = self.bubbleImageView.left - 5;
            _timeLabel.centerY = self.bubbleImageView.centerY;
            [_timeLabel setTextAlignment:NSTextAlignmentRight];
            
            _playedLabel.right = _timeLabel.right - 3;
            _playedLabel.top = self.bubbleImageView.top - 2;
            break;
    }
}

- (float)cellHeightForMessage:(BTMessageEntity *)message
{
    return 27 + 2 * bt_bubbleUpDown;
}


#pragma mark - 
#pragma mark PrivateAPI
- (float)lengthForVoiceLength:(float)voiceLength
{
    float gap = maxCellLength - minCellLength;
    if (voiceLength > 20)
    {
        return maxCellLength;
    }
    else
    {
        float length = (gap / 20) * voiceLength + minCellLength;
        return length;
    }
}

#pragma mark -
#pragma mark BTMenuImageView Delegate
-(void)clickTheCopy:(BTMenuImageView *)imageView
{

}

-(void)clickTheEarphonePlay:(BTMenuImageView *)imageView
{
    [_voiceImageView startAnimating];
    if (self.earphonePlay)
    {
        self.earphonePlay();
    }
}

-(void)clickTheSpeakerPlay:(BTMenuImageView *)imageView
{
    [_voiceImageView startAnimating];
    if (self.speakerPlay)
    {
        self.speakerPlay();
    }
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
    if (![_voiceImageView isAnimating])
    {
        [_voiceImageView startAnimating];
        [super tapTheImageView:imageView];
    }
    else
    {
        [_voiceImageView stopAnimating];
        [super tapTheImageView:imageView];
    }
}

-(void)sendVoiceAgain:(BTMessageEntity *)message
{
    [self showSending];
    NSString *filePath = message.msgContent;
    NSMutableData *muData = [[NSMutableData alloc] init];
    NSData *data = [NSData dataWithContentsOfFile:filePath];
    int length = [RecorderManager sharedManager].recordedTimeInterval;
    int8_t ch[4];
    for (int32_t i = 0; i < 4; i++)
    {
        ch[i] = ((length >> ((3 - i) * 8)) & 0x0ff);
    }
    [muData appendBytes:ch length:4];
    [muData appendData:data];
    BTLog(@"send void message, data: %@, filePath: %@, sessionId: %@, isGroup: %d, message: %@",
          muData,
          filePath,
          message.sessionId,
          message.isGroupMessage,
          message);
    [[BTMessageSendManager instance] sendVoiceMessage:muData
                                             filePath:filePath
                                            sessionId:message.sessionId
                                              isGroup:[message isGroupMessage]
                                              message:message
                                              session:[[BTSessionModule sharedInstance] getSessionById:message.sessionId]
                                           completion:^(BTMessageEntity *theMessage, NSError *error) {
        if (!error)
        {
              [self showSendSuccess];
        }
        else
        {
              [self showSendFailure];
        }
    }];
}
@end
