//
//  BTScreenUtils.m
//  TeamTalk
//
//  Created by apple on 2024/3/31.
//  Copyright © 2024 Michael Hu. All rights reserved.
//

#import "BTScreenUtils.h"

@implementation BTScreenUtils

static CGFloat kDesignWidth;
static CGFloat kDesignHeight;

+(void)initializeWithDesignWidth:(CGFloat)designWidth designHeight:(CGFloat)designHeight
{
    kDesignWidth = designWidth;
    kDesignHeight = designHeight;
}

+(CGFloat)screenWidth
{
    return [UIScreen mainScreen].bounds.size.width;
}

+(CGFloat)screenHeight
{
    return [UIScreen mainScreen].bounds.size.height;
}

+(CGFloat)adaptedWidth:(CGFloat)width
{
    CGFloat screenWidth = [self screenWidth];
    // 根据屏幕宽度和设计稿宽度比例计算实际尺寸
    return width / kDesignWidth * screenWidth;
}

+(CGFloat)adaptedHeight:(CGFloat)height
{
    CGFloat screenHeight = [self screenHeight];
    // 根据屏幕高度和设计稿高度比例计算实际尺寸
    return height / kDesignHeight * screenHeight;
}

+(UIFont *)adaptedFont:(CGFloat)fontSize
{
    CGFloat screenWidth = [self screenWidth];
    CGFloat screenHeight = [self screenHeight];
    CGFloat widthScale = screenWidth / kDesignWidth;
    CGFloat heightScale = screenHeight / kDesignHeight;
    CGFloat scale = MIN(widthScale, heightScale); // 取较小的缩放比例
    CGFloat adaptedSize = fontSize * scale;
    return [UIFont systemFontOfSize:adaptedSize];
}

@end
