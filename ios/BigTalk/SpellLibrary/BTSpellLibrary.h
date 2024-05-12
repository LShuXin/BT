//
//  BTSpellLibrary.h
//

#import <Foundation/Foundation.h>
#import "BTUserEntity.h"
#import "BTGroupEntity.h"


@interface BTSpellLibrary : NSObject
+(BTSpellLibrary *)instance;

-(void)clearAllSpell;
-(void)clearSpellById:(NSString *)objctId;

/**
 *  @param sender user or group
 */
-(void)addSpellForObject:(id)sender;
-(void)addDeparmentSpellForObject:(id)sender;

-(BOOL)isEmpty;

-(NSMutableArray *)checkoutForWordsForSpell:(NSString *)spell;
-(NSMutableArray *)checkoutForWordsForSpell_Deparment:(NSString *)spell;

-(NSString *)getSpellForWord:(NSString *)word;

/**
 *  将拼音进行简全缩写
 *  @param sender 完整拼音的数组
 *  @param count  完整拼音的个数
 *
 *  @return 结果
 */
- (NSString *)briefSpellWordFromSpellArray:(NSArray *)sender fullWord:(int)count;

@end
