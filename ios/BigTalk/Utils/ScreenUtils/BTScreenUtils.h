#import <UIKit/UIKit.h>

@interface BTScreenUtils : NSObject

// 初始化屏幕适配工具类
+(void)initializeWithDesignWidth:(CGFloat)designWidth designHeight:(CGFloat)designHeight;

// 获取屏幕宽度
+(CGFloat)screenWidth;

// 获取屏幕高度
+(CGFloat)screenHeight;

// 根据设计稿尺寸和屏幕宽度，计算实际尺寸
+(CGFloat)adaptedWidth:(CGFloat)width;

// 根据设计稿尺寸和屏幕高度，计算实际尺寸
+(CGFloat)adaptedHeight:(CGFloat)height;

// 根据设计稿字体大小和屏幕尺寸，计算实际字体大小
+(UIFont *)adaptedFont:(CGFloat)fontSize;

@end

