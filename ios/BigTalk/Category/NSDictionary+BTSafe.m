//
//  NSDictionary+BTSafe.m
//

#import "NSDictionary+BTSafe.h"
#import "NSString+BTAdditions.h"


#define isValidKey(key)       ((key) != nil && ![key isKindOfClass:[NSNull class]])
#define isValidValue(value)   (((value) != nil) && ![value isKindOfClass:[NSNull class]])


@implementation NSDictionary(BTSafe)

-(id)safeObjectForKey:(id)key
{
    if (!isValidKey(key))
    {
        return nil;
    }
    id obj = [self objectForKey:key];
    if (!isValidValue(obj))
    {
        return nil;
    }
    return obj;
}

-(int)intValueForKey:(id)key
{
    id obj = [self safeObjectForKey:key];
    return [obj intValue];
}

-(double)doubleValueForKey:(id)key
{
    id obj = [self safeObjectForKey:key];
    return [obj doubleValue];
}

-(NSString *)stringValueForKey:(id)key
{
    id obj = [self safeObjectForKey:key];
    if ([obj respondsToSelector:@selector(stringValue)])
    {
        return [obj stringValue];
    }
    
    return nil;
}

@end


@implementation NSMutableDictionary(BTSafe)

-(void)safeSetObject:(id)anObject forKey:(id)aKey
{
    if (!isValidKey(aKey))
    {
        return;
    }
    if ([aKey isKindOfClass:[NSString class]])
    {
        [self setValue:anObject forKey:aKey];
    }
    else
    {
        if (anObject != nil)
        {
            [self setObject:anObject forKey:aKey];
        }
        else
        {
            [self removeObjectForKey:aKey];
        }
    }
}

-(void)setIntValue:(int)value forKey:(id)aKey
{
    [self safeSetObject:[[NSNumber numberWithInt:value] stringValue] forKey:aKey];
}

-(void)setDoubleValue:(double)value forKey:(id)aKey
{
    [self safeSetObject:[[NSNumber numberWithDouble:value] stringValue] forKey:aKey];
}

-(void)setStringValueForKey:(NSString *)string forKey:(id)aKey
{
    [self safeSetObject:string forKey:aKey];
}

@end


@implementation NSArray(BTException)

-(id)objectForKey:(id)key
{
#ifdef DEBUG
    NSAssert(NO, @"NSArray should not call objectForKey, you should check your code!");
    return nil;
#else
    return nil;
#endif
}

@end
