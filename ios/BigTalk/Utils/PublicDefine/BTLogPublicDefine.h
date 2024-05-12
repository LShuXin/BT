//
//  BTLogPublicDefine.h
//  bt_ios
//
//  Created by LShuXin on 24-4-14.
//


#import <Foundation/Foundation.h>


#ifndef BTLogPublicDefine_h
#define BTLogPublicDefine_h


#ifdef DEBUG
// e.g.:
// BTLog(@"Message");
// BTLog(@"Message %@", variable);
#define BTLog(...)                  NSLog(@"%s(%d):\n%@\n", __func__, __LINE__, [NSString stringWithFormat:__VA_ARGS__])
// e.g.:
// BTPrettyLog(@"Message");
// BTPrettyLog(@"Message %@", variable);
#define BTPrettyLog(format, ...)        NSLog(@"%s(%d): " format, __PRETTY_FUNCTION__, __LINE__, ##__VA_ARGS__)
// e.g.:
// BTLogOut(@"Message");
// BTLogOut(@"Message %@", variable);
#define BTLogOut(format, ...)       BTLog(@"[%s][%d]" format, __func__, __LINE__, ##__VA_ARGS__);
// e.g.:
// BTLogOutMethodFun();
#define BTLogOutMethodFun()         BTLog(@"[%@] %@", NSStringFromClass([self class]), NSStringFromSelector(_cmd));
// e.g.:
// BTLogError(@"Error Message");
// BTLogError(@"Error Message %@", variable);
#define BTLogError(format, ...)     BTLog(@"[error][%s][%d]" format, __func__, __LINE__, ##__VA_ARGS__);
// e.g.:
// BTLogWaring(@"Warning Message");
// BTLogWaring(@"Warning Message %@", variable);
#define BTLogWaring(format, ...)    BTLog(@"[waring][%s][%d]" format, __func__, __LINE__, ##__VA_ARGS__);
#define BTLogTeym(format, ...)      {}
#else
#define BTLog(...)                  {}
#define BTPrettyLog(xx, ...)        {}
#define BTLogOut(format, ...)       {}
#define BTLogOutMethodFun()         {}
#define BTLogError(format, ...)     {}
#define BTLogWaring(format, ...)    {}
#define BTLogTeym(format, ...)      {}
#endif


// __VA_ARGS__ 将可变参数原封不动替换
// ##__VA_ARGS__ 将可变参数原封不动替换，并在为空时去掉前面的逗号

#endif /* BTLogPublicDefine_h */
