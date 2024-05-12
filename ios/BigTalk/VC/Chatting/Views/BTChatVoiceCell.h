//
//  BTChatVoiceCell.h
//

#import "BTChatBaseCell.h"

typedef void(^BTEarphonePlay)();
typedef void(^BTSpearerPlay)();

@interface BTChatVoiceCell : BTChatBaseCell<BTChatCellProtocol>
{
    UIImageView *_voiceImageView;
    UILabel *_timeLabel;
    UILabel *_playedLabel;
}

@property(nonatomic, copy)BTEarphonePlay earphonePlay;
@property(nonatomic, copy)BTSpearerPlay speakerPlay;

-(void)showVoicePlayed;
-(void)stopVoicePlayAnimation;
-(void)sendVoiceAgain:(BTMessageEntity *)message;
@end
