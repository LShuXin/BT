//
//  BTDbHelper.h
//  https://github.com/google/leveldb
//

#import <Foundation/Foundation.h>


@interface BTDbHelper : NSObject

+(BTDbHelper *)levelDBWithPath:(NSString *)path;
-(id)initWithPath:(NSString *)path;

// Getting Default Values
-(BOOL)boolForKey:(NSString *)aKey;
-(double)floatForKey:(NSString *)aKey;
-(NSInteger)intForKey:(NSString *)aKey;
-(NSString *)stringForKey:(NSString *)aKey;
-(NSData *)dataForKey:(NSString *)aKey;
-(id)objectForKey:(NSString *)aKey;

// Setting Default Values
-(BOOL)setBool:(BOOL)value forKey:(NSString *)aKey;
-(BOOL)setInt:(NSInteger)value forKey:(NSString *)aKey;
-(BOOL)setFloat:(double)value forKey:(NSString *)aKey;
-(BOOL)setString:(NSString *)value forKey:(NSString *)aKey;
-(BOOL)setData:(NSData *)value forKey:(NSString *)aKey;
-(BOOL)setObject:(id)value forKey:(NSString *)aKey;

-(BOOL)removeValueForKey:(NSString *)aKey;

-(NSArray *)allKeys;

-(void)enumerateKeys:(void(^)(NSString *key, BOOL *stop))block;

-(BOOL)clear;

-(BOOL)deleteDb;

@end
