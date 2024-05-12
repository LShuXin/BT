//
//  NSString+BTPath.m
//

#import "NSString+BTPath.h"
#import "BTUserModule.h"
#import "BTRuntimeStatus.h"


@implementation NSString(BTPath)

+(NSString *)userExclusiveDirectoryPath
{
    NSString *myName = BTRuntime.user.objId;
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *documentsDirectory = [paths objectAtIndex:0];
    NSString *directoryPath = [documentsDirectory stringByAppendingPathComponent:myName];
    NSFileManager *fileManager = [NSFileManager defaultManager];
    if ([fileManager fileExistsAtPath:directoryPath])
    {
        // create any missing parent directories along the way if necessary.
        [fileManager createDirectoryAtPath:directoryPath
               withIntermediateDirectories:YES
                                attributes:nil
                                     error:nil];
    }
    return directoryPath;
}

@end
