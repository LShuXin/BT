//
//  BTStringPublicDefine.h
//  bt_ios
//
//  Created by smileflutter on 2023/7/15.
//

#ifndef BTStringPublicDefine_h
#define BTStringPublicDefine_h


#define _(x)                                NSLocalizedString(x, @"")

#define BTConcatStringWith(x, y)            [NSString stringWithFormat:@"%@%@", x, y]

#define BTStringfy(S)                       #S

#define BTIsStringEmpty(string)             ([string isKindOfClass:[NSNull class]] || string.length == 0 ? YES : NO)

#define BTIsArrayEmpty(array)               ([array isKindOfClass:[NSNull class]] || array.count == 0 ? YES : NO)

#define BTIsDictionaryEmpty(dictionary)     ([dictionary isKindOfClass:[NSNull class]] || dictionary.allKeys == 0 ? YES : NO)

//// 需要解两次才解开的宏
//#define DEFER_STRINGIFY(S) STRINGIFY(S)
//
//
//#define PRAGMA_MESSAGE(MSG) _Pragma(STRINGIFY(message(MSG)))
//
//
//// 为warning增加更多信息
//#define FORMATTED_MESSAGE(MSG) "[TODO-" DEFER_STRINGIFY(__COUNTER__) "] " MSG " \n" DEFER_STRINGIFY(__FILE__) " line " DEFER_STRINGIFY(__LINE__)
//
//
//// 使宏前面可以加@
//#define KEYWORDIFY try {} @catch (...) {}
//
//
//// 最终使用的宏
//#define TODO(MSG) KEYWORDIFY PRAGMA_MESSAGE(FORMATTED_MESSAGE(MSG))


#endif /* BTStringPublicDefine_h */
