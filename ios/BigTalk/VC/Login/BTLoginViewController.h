//
//  BTLoginViewController.h
//

#import <UIKit/UIKit.h>
#import "UIButton+JSMessagesView.h"
#import "BTConsts.h"


@interface BTLoginViewController : UIViewController<UITextFieldDelegate>

@property(assign) BOOL isAutoLogin;
@property(nonatomic, retain)UIImageView *landspace;
@property(nonatomic, retain)UITextField *userNameTextField;
@property(nonatomic, retain)UITextField *userPassTextField;
@property(nonatomic, retain)UIButton *userLoginBtn;
@property(assign)BOOL isRelogin;

-(void)login;
-(void)hiddenKeyboard;
-(void)showEditServerAddress;

@end
