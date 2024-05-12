//
//  BTAnalysisImage.m
//

#import "BTAnalysisImage.h"
#import "BTMessageModule.h"


@implementation BTAnalysisImage
+(void)analysisImage:(BTMessageEntity *)message completion:(void(^)(NSMutableArray *array))completion
{
    NSMutableArray *arr = [NSMutableArray new];
    if (message.msgContent.length > 0)
    {
        NSMutableString *string = [NSMutableString stringWithString:message.msgContent];
        [string replaceOccurrencesOfString:kBTImageMessageSuffix
                                withString:kBTImageMessagePrefix
                                   options:NSCaseInsensitiveSearch
                                     range:NSMakeRange(0, string.length)];
        NSArray *msgArr = [string componentsSeparatedByString:kBTImageMessagePrefix];
        if ([msgArr count] > 0)
        {
            for (NSString *msg in msgArr)
            {
                if (msg.length > 0)
                {
                    BTMessageEntity *tempMessage = [message copy];
                    if ([msg hasPrefix:@"http:"])
                    {
                        tempMessage.msgContentType = MSG_TYPE_IMAGE;
                    }
                    tempMessage.msgId = [BTMessageModule  generateMessageId];
                    tempMessage.msgContent = msg;
                    [arr addObject:tempMessage];
                }
            }
        }
        else
        {
            if ([string hasPrefix:@"http:"])
            {
                message.msgContentType = MSG_TYPE_IMAGE;
                message.msgContent = string;
            }
            [arr addObject:message];
        }
    }
    
    completion([[NSMutableArray alloc] initWithObjects:arr, nil]);
}
@end
