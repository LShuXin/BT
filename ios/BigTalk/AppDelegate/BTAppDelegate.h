//
//  BTAppDelegate.h
//

#import <UIKit/UIKit.h>
#import "BTPublicDefine.h"
#import "BTMainViewController.h"


#define BTApp           ([UIApplication sharedApplication])
#define BTAppDel        ((BTAppDelegate *)BTApp.delegate)


@interface BTAppDelegate : UIResponder<UIApplicationDelegate>
@property(strong, nonatomic)UIWindow *window;
@property(nonatomic, strong)UINavigationController *mainNavigationController;
@property(strong)BTMainViewController *mainViewController;
@end
