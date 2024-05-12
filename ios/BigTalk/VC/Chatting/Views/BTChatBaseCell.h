//
//  BTChatBaseCell.h
//

#import <UIKit/UIKit.h>
#import "BTChatCellProtocol.h"
#import "BTMessageEntity.h"
#import "BTSessionEntity.h"
#import "BTMenuImageView.h"


extern CGFloat const bt_avatarEdge;                 //头像到边缘的距离
extern CGFloat const bt_avatarBubbleGap;            //头像和气泡之间的距离
extern CGFloat const bt_bubbleUpDown;               //气泡到上下边缘的距离

typedef void(^BTSendAgain)();
typedef void(^BTTapInBubble)();
typedef NS_ENUM(NSUInteger, BTBubbleLocationType)
{
    BUBBLE_LEFT,
    BUBBLE_RIGHT
};

@interface BTChatBaseCell : UITableViewCell<BTMenuImageViewDelegate, UIAlertViewDelegate>
@property(nonatomic, assign)BTBubbleLocationType location;
@property(nonatomic, retain)BTMenuImageView *bubbleImageView;
@property(nonatomic, retain)UIImageView *userAvatar;
@property(strong)UILabel *userName;
@property(nonatomic, retain)UIActivityIndicatorView *activityView;
@property(nonatomic, retain)UIImageView *sendFailuredImageView;
@property(nonatomic, copy)BTSendAgain sendAgain;
@property(nonatomic, copy)BTTapInBubble tapInBubble;
@property(strong)BTSessionEntity *session;
-(void)setContent:(BTMessageEntity *)content;
-(void)showSendFailure;
-(void)showSendSuccess;
-(void)showSending;
@end
