//
//  NSDictionary+BTJSON.h
//

#import <Foundation/Foundation.h>


@interface NSDictionary(BTJSON)
-(NSString *)jsonString;
+(NSDictionary *)initWithJsonString:(NSString *)json;
@end
