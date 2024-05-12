//
//  NSDictionary+BTSafe.m
//

#import <Foundation/Foundation.h>


@interface NSDictionary(BTSafe)
-(id)safeObjectForKey:(id)key;
-(int)intValueForKey:(id)key;
-(double)doubleValueForKey:(id)key;
-(NSString *)stringValueForKey:(id)key;
@end


@interface NSMutableDictionary(BTSafe)
-(void)safeSetObject:(id)anObject forKey:(id)aKey;
-(void)setIntValue:(int)value forKey:(id)aKey;
-(void)setDoubleValue:(double)value forKey:(id)aKey;
-(void)setStringValueForKey:(NSString *)string forKey:(id)aKey;
@end


@interface NSArray(Exception)
-(id)objectForKey:(id)key;
@end
