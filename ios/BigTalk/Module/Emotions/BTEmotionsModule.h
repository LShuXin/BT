//
//  BTEmotionsModule.h
//

#import <Foundation/Foundation.h>

@interface BTEmotionsModule : NSObject
// keys
@property(nonatomic, readonly)NSMutableArray *emotions;
// the following two reverse each other
@property(nonatomic, readonly)NSDictionary *emotionUnicodeDic;
@property(nonatomic, readonly)NSDictionary *unicodeEmotionDic;
// map of key length
@property(nonatomic, readonly)NSDictionary *emotionLength;
+(instancetype)shareInstance;
@end
