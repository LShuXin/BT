//
//  BTStdPublicDefine.h
//  bt_ios
//
//  Created by LShuXin on 14-5-23.
//

#ifndef BTStdPublicDefine_h
#define BTStdPublicDefine_h


#import <Foundation/Foundation.h>


#define BTObjectOrNull(obj)                          ((obj) ? (obj) : [NSNull null])
#define BTObjectOrEmptyStr(obj)                      ((obj) ? (obj) : @"")
#define BTIsNull(x)                                  (!x || [x isKindOfClass:[NSNull class]])
#define BTToInt(x)                                   (BTIsNull(x) ? 0 : [x intValue])
#define BTIsEmptyString(x)                           (BTIsNull(x) || [x isEqual:@""] || [x isEqual:@"(null)"])
#define BTSleep(s);                                  [NSThread sleepForTimeInterval:s];
#define BTSyn(x)                                     @synthesize x = _##x
#define BTBeginAutoPool                              NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init]; {
#define BTEndAutoPool                                } [pool release];
#define BTSkipSpace(c)                               while (isspace(*c)) ++c
#define BTSkipUntil(c,x)                             while (x != *c) ++c
#define BTIntToNumber(int)                           ([NSNumber numberWithInt:int])
#define BTNotificationCenter(name, object, userInfo) [[NSNotificationCenter defaultCenter] postNotificationName:name object:object userInfo:userInfo]
#define BTWeakObj(weakObj)                           __weak __typeof(&*weakObj)weak_##weakObj = weakObj;
#define BTWeakSelf(weakSelf)                         __weak __typeof(&*self)weakSelf = self;

extern NSString * const kInvited;
extern NSString * const kUserSetting;
extern NSString * const kLastLoginUser;
extern NSString * const kHasAlertVIP;
extern NSString * const kLastPosition;
extern NSString * const kAccessToken;
extern NSString * const kRefreshToken;
extern NSString * const kTokenExpiredTime;
extern NSString * const kAppVersion;
extern NSString * const kArrowCount;

#undef	AS_SINGLETON
#define AS_SINGLETON( __class ) \
+(__class *)sharedInstance;

#undef	DEF_SINGLETON
#define DEF_SINGLETON( __class ) \
+(__class *)sharedInstance \
{ \
static dispatch_once_t once; \
static __class * __singleton__; \
dispatch_once( &once, ^{ __singleton__ = [[__class alloc] init]; } ); \
return __singleton__; \
}

@protocol BTShareObjDelegate<NSObject>
-(NSString*)shareTypeName;
@end

char pinyinFirstLetter(unsigned short hanzi);
char getFirstChar(const NSString *str);


#endif /* BTStdPublicDefine_h */
