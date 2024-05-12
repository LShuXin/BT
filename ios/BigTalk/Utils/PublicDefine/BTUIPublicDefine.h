//
//  BTUIPublicDefine.h
//  bt_ios
//
//  Created by LShuXin on 2023/7/15.
//

#ifndef BTUIPublicDefine_h
#define BTUIPublicDefine_h


#import "BTPlatformPublicDefine.h"


// screen width & height
#define BTScreenWidth                    ([UIScreen mainScreen].bounds.size.width)
#define BTScreenHeight                   ([UIScreen mainScreen].bounds.size.height)

#define BTFullWidth                      BTScreenWidth
#define BTFullHeight                     (BTScreenHeight - ((BTSystemFloatVersion >= 7) ? 0 : BTStatusBarHeight))

// status bar height
#define BTStatusBarHeight \
^(){\
if (@available(iOS 13.0, *)) {\
    UIStatusBarManager *statusBarManager = [UIApplication sharedApplication].windows.firstObject.windowScene.statusBarManager;\
    return statusBarManager.statusBarFrame.size.height;\
} else {\
    return [UIApplication sharedApplication].statusBarFrame.size.height;\
}\
}()
//#define BTStatusBarHeight [[UIApplication sharedApplication] statusBarFrame].size.height


// navigation bar height
#define BTNavBarHeight                        (44)
//#define BTNavBarHeight                        (BTCurrentIOSVersion >= 7 ? 64.0f : 44.0f)
//#define BTNavBarHeight                        (44 + ((BTSystemFloatVersion >= 7) ? BTStatusBarHeight : 0))

#define BTContentWidth                        BTScreenWidth
#define BTContentHeight                       (BTFullHeight - BTNavBarHeight)

#define BTTheWindowHeight                     ([UIDevice isAfterOS7] ? [UIScreen mainScreen].bounds.size.height : ([UIScreen mainScreen].bounds.size.height - 20))

// status bar height + navigation bar height
#define BTNavBarAndStatusBarHeight            (CGFloat)(BTIsIPhoneX ? (88.0) : (64.0))

// tabbar height
#define BTTabBarHeight                        (CGFloat)(BTIsIPhoneX ? (49.0 + 34.0) : (49.0))

// top safe area height
#define BTTopSafeHeight                       (CGFloat)(BTIsIPhoneX ? (44.0) : (0))

// bottom safe area height
#define BTBottomSafeHeight                    (CGFloat)(BTIsIPhoneX ? (34.0) : (0))

// diff between the height of status bar of iPhoneX and a normal one
#define BTStatusBarDiffHeight                 (CGFloat)(BTIsIPhoneX ? (24.0) : (0))

// status bar height + navigation bar height + tab bar height
#define BTNavBarAndStatusBarAndTabBarHeight   (BTNavBarAndStatusBarHeight + BTTabBarHeight)


#define BTIndicatorHeight \
^(){\
if (@available(iOS 11.0, *)) {\
    UIEdgeInsets safeAreaInsets = [[UIApplication sharedApplication] delegate].window.safeAreaInsets;\
    return safeAreaInsets.bottom;\
} else {\
    return UIEdgeInsetsMake(0, 0, 0, 0).bottom;\
}\
}()


// e.g.
// BTUIColorFromRGB_A(0x123456, 255)
#define BTUIColorFromRGB_A(rgbValue, alphaValue) \
    [UIColor \
    colorWithRed:((float)((rgbValue & 0xFF0000) >> 16))/255.0 \
    green:((float)((rgbValue & 0x00FF00) >> 8))/255.0 \
    blue:((float)(rgbValue & 0x0000FF))/255.0 \
    alpha:alphaValue]


// e.g.
// BTUIColorFromR_G_B_A(255, 255, 255, 255)
#define BTUIColorFromR_G_B_A(r, g, b, a) [UIColor colorWithRed:(r)/255.0 green:(g)/255.0 blue:(b)/255.0 alpha:(a)/1.0]


// e.g.
// BTUIColorFromR_G_B(255, 255, 255)
#define BTUIColorFromR_G_B(r, g, b) [UIColor colorWithHue:r/255.0 saturation:g/255.0 brightness:b/255.0 alpha:1]


// random color
#define BTUIRandomColor EYRGBAColor(arc4random_uniform(256), arc4random_uniform(256), arc4random_uniform(256), 1.0)


// clear color
#define BTUIClearColor [UIColor clearColor]


// bundle UIImage with ext
#define BTBundleUIImageWithExt(file, ext) [UIImage imageWithContentsOfFile:[[NSBundle mainBundle]pathForResource:file ofType:ext]]


// bundle UIImage without ext
#define BTBundleUIImage(A)                [UIImage imageWithContentsOfFile:[[NSBundle mainBundle] pathForResource:A ofType:nil]]


#define BTUIImageNamed(_pointer) [UIImage imageNamed:_pointer]

#define BTUIColorFromRGB_A(rgbValue, alphaValue) \
    [UIColor \
    colorWithRed:((float)((rgbValue & 0xFF0000) >> 16))/255.0 \
    green:((float)((rgbValue & 0x00FF00) >> 8))/255.0 \
    blue:((float)(rgbValue & 0x0000FF))/255.0 \
    alpha:alphaValue]


//可拉伸的图片
#define BTResizableImage(name,top,left,bottom,right) [[UIImage imageNamed:name] resizableImageWithCapInsets:UIEdgeInsetsMake(top,left,bottom,right)]
#define BTResizableImageWithMode(name,top,left,bottom,right,mode) [[UIImage imageNamed:name] resizableImageWithCapInsets:UIEdgeInsetsMake(top,left,bottom,right) resizingMode:mode]


// 设置 view 圆角和边框
#define BTViewBorderRadius(View, Radius, Width, Color)\
\
[View.layer setCornerRadius:(Radius)];\
[View.layer setMasksToBounds:YES];\
[View.layer setBorderWidth:(Width)];\
[View.layer setBorderColor:[Color CGColor]]



// 由角度转换弧度 由弧度转换角度
#define BTDegreesToRadian(x) (M_PI * (x) / 180.0)
#define BTRadianToDegrees(radian) (radian*180.0)/(M_PI)


// 获取图片资源
#define BTGetImage(imageName) [UIImage imageNamed:[NSString stringWithFormat:@"%@",imageName]]


#define RGBA(r,g,b,a)         [UIColor colorWithRed:r/255.0 green:g/255.0 blue:b/255.0 alpha:a]
#define RGB(r,g,b)            [UIColor colorWithRed:r/255.0 green:g/255.0 blue:b/255.0 alpha:1.0]
#define BoldSystemFont(size)  [UIFont boldSystemFontOfSize:size]
#define systemFont(size)      [UIFont systemFontOfSize:size]

// 常用Padding
#define kBTPadding2  2
#define kBTPadding4  4
#define kBTPadding6  6
#define kBTPadding8  8
#define kBTPadding10 10
#define kBTPadding12 12
#define kBTPadding14 14
#define kBTPadding16 16
#define kBTPadding18 18
#define kBTPadding20 20
#define kBTPadding22 22
#define kBTPadding24 24
#define kBTPadding26 26
#define kBTPadding28 28
#define kBTPadding30 30
#define kBTPadding32 32
#define kBTPadding34 34
#define kBTPadding36 36
#define kBTPadding38 38
#define kBTPadding40 40

// 常用Margin
#define kBTMargin2  2
#define kBTMargin4  4
#define kBTMargin6  6
#define kBTMargin8  8
#define kBTMargin10 10
#define kBTMargin12 12
#define kBTMargin16 16
#define kBTMargin18 18
#define kBTMargin20 20
#define kBTMargin22 22
#define kBTMargin24 24
#define kBTMargin26 26
#define kBTMargin28 28
#define kBTMargin30 30
#define kBTMargin32 32
#define kBTMargin34 34
#define kBTMargin36 36
#define kBTMargin38 38
#define kBTMargin40 40
#define kBTMargin42 42
#define kBTMargin44 44
#define kBTMargin46 46
#define kBTMargin48 48
#define kBTMargin50 50
#define kBTMargin52 52
#define kBTMargin54 54
#define kBTMargin56 56
#define kBTMargin58 58
#define kBTMARGIN60 60

// 常用尺寸
#define kBTSIZE80 80.0


#endif /* LUIPublicDefine_h */
