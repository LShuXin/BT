//
//  NSDictionary+BTJSON.m
//

#import "NSDictionary+BTJSON.h"

@implementation NSDictionary(BTJSON)
-(NSString *)jsonString
{
    NSData *infoJsonData = [NSJSONSerialization dataWithJSONObject:self options:NSJSONWritingPrettyPrinted error:nil];
    NSString *json = [[NSString alloc] initWithData:infoJsonData encoding:NSUTF8StringEncoding];
    return json;
}

+(NSDictionary *)initWithJsonString:(NSString *)json
{
    NSData *infoData = [json dataUsingEncoding:NSUTF8StringEncoding];
    NSDictionary *info = [NSJSONSerialization JSONObjectWithData:infoData options:0 error:nil];
    return info;
}
@end
