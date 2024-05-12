//
//  BTPlatformPublicDefine.h
//  live_ios
//
//  Created by smileflutter on 2023/7/15.
//

#ifndef BTPlatformPublicDefine_h
#define BTPlatformPublicDefine_h


#define BTTheBundleVerison                         (bundleVerison())

#define BTCurrentIOSVersion                        [[[UIDevice currentDevice] systemVersion] floatValue]

#define BTSystemVersionEqualTo(v)                  ([[[UIDevice currentDevice] systemVersion] compare:v options:NSNumericSearch] == NSOrderedSame)
#define BTSystemVersionBiggerThan(v)               ([[[UIDevice currentDevice] systemVersion] compare:v options:NSNumericSearch] == NSOrderedDescending)
#define BTSystemVersionBiggerThanOrEqualTo(v)      ([[[UIDevice currentDevice] systemVersion] compare:v options:NSNumericSearch] != NSOrderedAscending)
#define BTSystemVersionSmallerThan(v)              ([[[UIDevice currentDevice] systemVersion] compare:v options:NSNumericSearch] == NSOrderedAscending)
#define BTSystemVersionSmallerThanOrEqualTo(v)     ([[[UIDevice currentDevice] systemVersion] compare:v options:NSNumericSearch] != NSOrderedDescending)

#define BTIsIOS5OrLater                            BTSystemVersionBiggerThanOrEqualTo(@"5.0")
#define BTIsIOS6OrLater                            BTSystemVersionBiggerThanOrEqualTo(@"6.0")
#define BTIsIOS7OrLater                            BTSystemVersionBiggerThanOrEqualTo(@"7.0")

// 是否高清屏
#define BTIsRetina ([UIScreen instancesRespondToSelector:@selector(currentMode)] ? CGSizeEqualToSize(CGSizeMake(640, 960), [[UIScreen mainScreen] currentMode].size) : NO)

#define BTIsSimulator                              (NSNotFound != [[[UIDevice currentDevice] model] rangeOfString:@"Simulator"].location)

#define BTIsPad                                    (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad)
#define BTSomeThing                                (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad) ? ipad : iphone



#define BTIsIPhone              ([[[UIDevice currentDevice] model] isEqualToString:@"iPhone"])
//#define BTIsIPhone              (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPhone)

#define BTIsIPad                ([[[UIDevice currentDevice] model] isEqualToString:@"iPad"])

#define BTIsIPod                ([[[UIDevice currentDevice] model] isEqualToString:@"iPod touch"])

#define BTIsIPhoneSE            [[UIScreen mainScreen] bounds].size.width == 320.0f && [[UIScreen mainScreen] bounds].size.height == 568.0f

#define BTIsIPhone4             ([[UIScreen mainScreen] bounds].size.height == 480)

#define BTIsIPhone7             [[UIScreen mainScreen] bounds].size.width == 375.0f && [[UIScreen mainScreen] bounds].size.height == 667.0f

#define BTIsIPhone7Plus         [[UIScreen mainScreen] bounds].size.width == 414.0f && [[UIScreen mainScreen] bounds].size.height == 736.0f

#define BTIsIOS7                [[UIDevice currentDevice].systemVersion doubleValue] >= 7.0 ? YES : NO

#define BTIsIPhoneX             BTScreenWidth >= 375.0f && BTScreenHeight >= 812.0f && BTIsIPhone

#define BTIOS8OrLater           (([[[UIDevice currentDevice] systemVersion] floatValue] >= 8.0) ? (YES) : (NO))

#define BTSystemVersion         [[UIDevice currentDevice] systemVersion]

#define BTSystemFloatVersion    [[[UIDevice currentDevice] systemVersion] floatValue]

#if __has_feature(objc_arc)
// 编译器是ARC环境
#else
// 编译器是MRC环境
#endif

#if TARGET_OS_IPHONE
//iPhone Device
#endif

#if TARGET_IPHONE_SIMULATOR
//iPhone Simulator
#endif


#endif /* BTPlatformPublicDefine_h */
