//
//  BTDepartment.m
//

#import "BTDepartmentAPI.h"


@implementation BTDepartmentAPI

-(int)requestTimeOutTimeInterval
{
    return TimeOutTimeInterval;
}

-(int)requestServiceID
{
    return 2;
}

-(int)requestCommandID
{
    return 18;
}

-(int)responseServiceID
{
    return 2;
}

-(int)responseCommandID
{
    return 19;
}

-(Analysis)analysisReturnData
{
    
    Analysis analysis = (id)^(NSData *data) {
        BTDataInputStream *inputStream = [BTDataInputStream dataInputStreamWithData:data];
        NSInteger departCount = [inputStream readInt];
        NSMutableArray *array = [NSMutableArray new];
        for (int i = 0 ; i < departCount; i++)
        {
            NSString *departId = [inputStream readUTF];
            NSString *title = [inputStream readUTF];
            NSString *description = [inputStream readUTF];
            NSString *parentId = [inputStream readUTF];
            NSString *leader = [inputStream readUTF];
            NSInteger isDelete = [inputStream readInt];
            
            NSDictionary *result = @{@"departCount": @(departCount),
                                     @"departId": departId,
                                     @"title": title,
                                     @"description": description,
                                     @"parentId": parentId,
                                     @"leader": leader,
                                     @"isDelete": @(isDelete)
                                     };
            [array addObject:result];
        }
        return array;
    };
    return analysis;
}

-(Package)packageRequestObject
{
    Package package = (id)^(id object, uint32_t seqNo) {
        BTDataOutputStream *outputStream = [[BTDataOutputStream alloc] init];
        [outputStream writeInt:IM_PDU_HEADER_LEN];
        [outputStream writeTcpProtocolHeaderUseServiceID:2
                                               commandID:18
                                                   seqNo:seqNo];
    
        return [outputStream toByteArray];
    };
    return package;
}

@end
