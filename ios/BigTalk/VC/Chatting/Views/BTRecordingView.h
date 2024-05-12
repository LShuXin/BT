//
//  BTRecordingView.h
//

#import <UIKit/UIKit.h>


typedef NS_ENUM(NSUInteger, BTRecordingState)
{
    SHOW_VOLUMN,
    SHOW_CANCEL_SEND,
    SHOW_RECORD_TIME_TOO_SHORT
};

@interface BTRecordingView : UIView
@property(nonatomic, assign)BTRecordingState recordingState;
-(instancetype)initWithState:(BTRecordingState)state;
-(void)setVolume:(float)volume;
@end
