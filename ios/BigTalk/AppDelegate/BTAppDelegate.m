//
//  BTAppDelegate.m
//
//

#import "BTAppDelegate.h"
#import "BTLoginViewController.h"
#import "BTRuntimeStatus.h"
#import "BTClientState.h"
#import "BTChattingMainViewController.h"
#import "BTRuntimeStatus.h"
#import "NSDictionary+BTSafe.h"
#import "BTSendPushTokenAPI.h"
#import "BTClientStateMaintenanceManager.h"
#import "BTPublicDefine.h"
#import "BTSessionEntity.h"
#import "BTMainViewController.h"
#import "BTMessageModule.h"
#import "MobClick.h"
#import "BTLoginModule.h"
#import "BTTcpClientManager.h"
#import "BTSessionModule.h"
#import <objc/runtime.h>


@interface BTAppDelegate()
@property(assign)BOOL isOpenApp;
@end


@implementation BTAppDelegate

-(BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
    [BTClientStateMaintenanceManager shareInstance];
    [[NSURLCache sharedURLCache] removeAllCachedResponses];
    if ([application respondsToSelector:@selector(registerUserNotificationSettings:)])
    {
        // for iOS 8
        UIUserNotificationSettings *settings = [UIUserNotificationSettings settingsForTypes:UIUserNotificationTypeAlert
                                                                                           | UIUserNotificationTypeBadge
                                                                                           | UIUserNotificationTypeSound
                                                                                 categories:nil];
        [[UIApplication sharedApplication] registerUserNotificationSettings:settings];
    }
    else
    {
        // for iOS 7 or iOS 6
        [[UIApplication sharedApplication] registerForRemoteNotificationTypes:(UIRemoteNotificationTypeBadge | UIRemoteNotificationTypeSound | UIRemoteNotificationTypeAlert)];
    }

    if (BTSystemFloatVersion >= 8)
    {
        [[UINavigationBar appearance] setTranslucent:YES];
    }
    [[UINavigationBar appearance] setBarStyle:UIBarStyleDefault];
    [[UINavigationBar appearance] setTitleTextAttributes:[NSDictionary dictionaryWithObject:[UIColor blackColor] forKey:UITextAttributeTextColor]];
    self.window = [[UIWindow alloc] initWithFrame:[[UIScreen mainScreen] bounds]];
    // Override point for customization after application launch.
    self.window.backgroundColor = [UIColor whiteColor];
    [BTRuntimeStatus instance];
    
    self.mainViewController = [BTMainViewController new];
    self.mainNavigationController = self.mainViewController.nvc1;

    BTLoginViewController *loginVC = [[BTLoginViewController alloc] init];
    UINavigationController *nvc = [[UINavigationController alloc] initWithRootViewController:loginVC];
    self.window.rootViewController = nvc;
    NSDictionary *pushDict = [launchOptions objectForKey:UIApplicationLaunchOptionsRemoteNotificationKey];
    if (pushDict)
    {
        [self application:application didReceiveRemoteNotification:pushDict];
    }

    [self.window makeKeyAndVisible];
    // [[self class] installCustomFont];
    return YES;
}

+(void)installCustomFont
{
    SEL systemFontSelector = @selector(systemFontOfSize:);
    Method oldMethod = class_getClassMethod([UIFont class], systemFontSelector);
    Method newMethod = class_getClassMethod([self class], systemFontSelector);
    method_exchangeImplementations(oldMethod, newMethod);  // exchange可用replace替代
}

+(UIFont *)systemFontOfSize:(CGFloat)fontSize
{
    UIFont *font = [UIFont fontWithName:@"FZLanTingHei-L-GBK" size:fontSize];
    return font;
}


-(void)application:(UIApplication *)application didRegisterUserNotificationSettings:(UIUserNotificationSettings *)notificationSettings
{
    [application registerForRemoteNotifications];
}

-(void)applicationWillResignActive:(UIApplication *)application
{
    // BTClientState *clientState = [BTClientState shareInstance];
    // [clientState setUseStateWithoutObserver:USER_OFF_LINE];
    // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary
    // interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the
    // transition to the background state.
    // Use this method to pause ongoing tasks, disable timers, and throttle down OpenGL ES frame rates. Games should use this
    // method to pause the game.
}

-(void)applicationDidEnterBackground:(UIApplication *)application
{
     // BTClientState *clientState = [BTClientState shareInstance];
     // [clientState setUseStateWithoutObserver:USER_OFF_LINE];
     // self.isOpenApp = NO;
     // [[BTTcpClientManager instance] disconnect];
    if ([[BTSessionModule sharedInstance] getAllUnreadMessageCount] == 0)
    {
        [[UIApplication sharedApplication] setApplicationIconBadgeNumber:0];
    }
    else
    {
        [[UIApplication sharedApplication] setApplicationIconBadgeNumber:[[BTSessionModule sharedInstance] getAllUnreadMessageCount]];
    }
    // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information 
    // to restore your application to its current state in case it is terminated later. If your application supports background execution,
    // this method is called instead of applicationWillTerminate: when the user quits.
}

-(void)applicationWillEnterForeground:(UIApplication *)application
{
    // Called as part of the transition from the background to the inactive state; here you can undo many of the changes made on entering the background.
}

-(void)applicationDidBecomeActive:(UIApplication *)application
{
    // self.isOpenApp = YES;
    // if ([BTRuntimeStatus instance].user.objId != nil)
    // {
    //   BTClientState *clientState = [BTClientState shareInstance];
    //   clientState.userState = USER_OFF_LINE;
    // }

    // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, 
    // optionally refresh the user interface.
}

-(void)applicationWillTerminate:(UIApplication *)application
{
    BTLog(@"kill the app");
    // 程序被杀死调用
    // Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
}

-(void)application:(UIApplication *)app didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken
{
    NSString *token = [NSString stringWithFormat:@"%@", deviceToken];
    NSString *dt = [token stringByTrimmingCharactersInSet:[NSCharacterSet characterSetWithCharactersInString:@"<>"]];
    NSString *dn = [dt stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
    BTRuntime.pushToken = [dn stringByReplacingOccurrencesOfString:@" " withString:@""];
    NSLog(@"token......%@", BTRuntime.pushToken);
}

-(void)application:(UIApplication *)app didFailToRegisterForRemoteNotificationsWithError:(NSError *)error
{
    NSString *error_str = [NSString stringWithFormat: @"%@", error];
    NSLog(@"failed to get token, error: %@", error_str);
}

-(void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo
{
    // 处理推送消息
    UIApplicationState state = application.applicationState;
    if (state != UIApplicationStateBackground)
    {
        return;
    }
    NSString *jsonString = [userInfo safeObjectForKey:@"custom"];
    NSData *infoData = [jsonString dataUsingEncoding:NSUTF8StringEncoding];
    NSDictionary *info = [NSJSONSerialization JSONObjectWithData:infoData options:0 error:nil];
    NSInteger fromId = [[info safeObjectForKey:@"fromId"] integerValue];
    SessionType type = (SessionType)[[info safeObjectForKey:@"sessionType"] integerValue];
    NSInteger groupId = [[info safeObjectForKey:@"groupId"] integerValue];
    NSLog(@"推送消息%@", info);
    if (fromId)
    {
        NSInteger sessionId = type == 1 ? fromId : groupId;
        BTSessionEntity *session = [[BTSessionEntity alloc] initWithSessionId:[BTRuntime convertPbIdToLocalId:sessionId sessionType:type]
                                                                  sessionType:type];
 
        [[BTChattingMainViewController shareInstance] showChattingContentForSession:session];
        // 要处理锁屏
        if (![self.mainViewController.nvc1.topViewController isEqual:[BTChattingMainViewController shareInstance]])
        {
            [[BTChattingMainViewController shareInstance] showChattingContentForSession:session];
            [self.mainViewController.nvc1 pushViewController:[BTChattingMainViewController shareInstance] animated:YES];
        }
    }

    NSLog(@"收到推送消息:%@", userInfo[@"aps"][@"alert"]);
}

@end
