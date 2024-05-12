//
//  NSString+BTAdditions.h
//

#import <Foundation/Foundation.h>


@interface NSString(BTAdditions)
+(NSString *)documentPath;
+(NSString *)cachePath;
+(NSString *)formatCurDate;
+(NSString *)formatCurDay;
+(NSString *)getAppVer;
-(NSString *)removeAllSpace;
+(NSString *)formatCurDayForVersion;
-(NSURL *)toURL;
-(BOOL)isEmail;
-(BOOL)isEmpty;
-(NSString *)MD5;
-(NSString *)trim;

-(BOOL)isOlderVersionThan:(NSString *)otherVersion;
-(BOOL)isNewerVersionThan:(NSString *)otherVersion;

@end
