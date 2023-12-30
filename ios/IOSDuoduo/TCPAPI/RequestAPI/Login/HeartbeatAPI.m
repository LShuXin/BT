//
//  DDHeartbeatAPI.m
//  IOSDuoduo
//
//  Created by 独嘉 on 14-5-26.
//  Copyright (c) 2014年 dujia. All rights reserved.
//

#import "HeartbeatAPI.h"
#import "IMOther.pbobjc.h"


@implementation HeartbeatAPI
/**
 *  请求超时时间
 *
 *  @return 超时时间
 */
-(int)requestTimeOutTimeInterval
{
    return 0;
}

/**
 *  请求的serviceID
 *
 *  @return 对应的serviceID
 */
-(int)requestServiceID
{
    return DDHEARTBEAT_SID;
}

/**
 *  请求返回的serviceID
 *
 *  @return 对应的serviceID
 */
-(int)responseServiceID
{
    return DDHEARTBEAT_SID;
}

/**
 *  请求的commendID
 *
 *  @return 对应的commendID
 */
-(int)requestCommendID
{
    return REQ_CID;
}

/**
 *  请求返回的commendID
 *
 *  @return 对应的commendID
 */
-(int)responseCommendID
{
    return RES_CID;
}

/**
 *  解析数据的block
 *
 *  @return 解析数据的block
 */
-(Analysis)analysisReturnData
{
    Analysis analysis = (id)^(NSData* data)
    {
        
    };
    return analysis;
}

/**
 *  打包数据的block
 *
 *  @return 打包数据的block
 */
-(Package)packageRequestObject
{
    Package package = (id)^(id object, UInt32 seqNo)
    {
        IMHeartBeat* heartBeat = [[IMHeartBeat alloc] init];
        DDDataOutputStream* dataout = [[DDDataOutputStream alloc] init];
        [dataout writeInt:0];
        [dataout writeTcpProtocolHeader:DDHEARTBEAT_SID
                                    cId:REQ_CID
                                  seqNo:seqNo];
        [dataout directWriteBytes:[heartBeat data]];
        [dataout writeDataCount];
        return [dataout toByteArray];
    };
    return package;
}
@end
