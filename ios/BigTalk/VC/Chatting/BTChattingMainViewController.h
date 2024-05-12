//
//  BTChattingMainViewController.h
//

#import <UIKit/UIKit.h>
#import "JSMessageInputView.h"
#import "RecorderManager.h"
#import "PlayerManager.h"
#import "BTPhoto.h"
#import "JSMessageTextView.h"
#import "BTMessageEntity.h"
#import "BTChattingModule.h"
#import "BTEmotionsViewController.h"
#import "BTPublicDefine.h"

typedef void(^TimeCellAddBlock)(bool isok);
@class BTChatUtilityViewController;
@class BTEmotionsViewController;
@class BTSessionEntity;
@class BTRecordingView;


@interface BTChattingMainViewController : UIViewController<UITextViewDelegate,
                                                        JSMessageInputViewDelegate,
                                                        UITableViewDataSource,
                                                        UITableViewDelegate,
                                                        RecordingDelegate,
                                                        PlayingDelegate,
                                                        UIScrollViewDelegate,
                                                        UIGestureRecognizerDelegate,
                                                        UIAlertViewDelegate,
                                                        BTEmotionsViewControllerDelegate,
                                                        UINavigationControllerDelegate>
{
    BTRecordingView *_recordingView;
}

@property(nonatomic, strong)BTChattingModule *module;
@property(nonatomic, strong)BTChatUtilityViewController *chatUtility;
@property(nonatomic, strong)JSMessageInputView *chatInputView;
@property(assign, nonatomic)CGFloat previousTextViewContentHeight;
@property(nonatomic, retain)UITableView *tableView;
@property(nonatomic, strong)BTEmotionsViewController *emotions;
@property(assign, nonatomic, readonly)UIEdgeInsets originalTableViewContentInset;
@property(nonatomic, strong)UIRefreshControl *refreshControl;
@property(assign)BOOL hadLoadHistory;

+(instancetype)shareInstance;

-(void)sendImageMessage:(BTPhoto *)photo Image:(UIImage *)image;
// use the method to enter chat page from any page
-(void)showChattingContentForSession:(BTSessionEntity *)session;
-(void)insertEmojiFace:(NSString *)string;
-(void)checkSessionLastMsgIdThenUpdate;
-(void)deleteEmojiFace;
-(void)p_popViewController;
-(void)edit;

@end


@interface BTChattingMainViewController(ChattingInput)
- (void)initialInput;
@end
