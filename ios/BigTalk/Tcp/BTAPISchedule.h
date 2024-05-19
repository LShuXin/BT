//
//  BTAPISchedule.h
//

#import <Foundation/Foundation.h>
#import "BTAPIScheduleProtocol.h"
#import "BTAPIUnrequestScheduleProtocol.h"


typedef struct BTServer_Response_Data_Header {
    int serviceID;
    int commandID;
    int seqNo;
} BTServerDataHeader;


NS_INLINE BTServerDataHeader BTMakeServerDataHeader(int serviceID, int commandID, int seqNo)
{
    BTServerDataHeader header;
    header.serviceID = serviceID;
    header.commandID = commandID;
    header.seqNo = seqNo;
    return header;
}


// TODO:应该有自己的专属线程
@interface BTAPISchedule : NSObject

@property(nonatomic, readonly)dispatch_queue_t apiScheduleQueue;

+(instancetype)instance;

/**
 * 此接口只应该在 BTSuperAPI 中被使用
 * 将 api 放到请求 map + 响应 map 中
 * 当请求返回时从响应 map 中查找该 api 并调用该 api 的 completion block
 * 并且需要同时从请求 map + 响应 map 中移除该 api
 * 不关注 api 调用是否超时，当 analysisReturnData 为空时也不关注是否有返回
 */
-(BOOL)registerApi:(id<BTAPIScheduleProtocol>)api;

/**
 *  此接口只应该在 BTSuperAPI 中被使用
 *  不会执行任何 api 注册工作
 *  系统会在 requestTimeOutTimeInterval 时间之后判断请求 map 中是否存在该 api
 *  如果存在并且 api 的请求时间超过了 api 的 requestTimeOutTimeInterval 则说明
 *  该 api 请求已经超时，需要从响应 map 中查找该 api 并调用 api 的 completion block
 *  并且需要同时从请求 map 和响应 map 中移除该 api
 */
-(void)registerTimeoutApi:(id<BTAPIScheduleProtocol>)api;

/**
 * 这个接口主要是用于注册一些服务端主动通知客户端的事件
 * 当数据到来时会根据 serviceID、commandID 去调用注册过的 api 的 completion 方法
 */
-(BOOL)registerUnrequestAPI:(id<BTAPIUnrequestScheduleProtocol>)api;

/**
 * 当该方法被调用时，说明有数据从服务端到来，此时会根据服务端数据的 serviceID、commanID， seqNo 去响应 map 中查
 * 找对应的 api 并调用它的 completion 方法，调用成功或者失败后都应该从请求 map、响应 map 中删除该api；
 * 如果数据返回时在响应 map 中找不到相关的 api， 则认为该数据是服务端主动推送的数据，去指定 map 中查找相关的回调即可
 */
-(void)receiveServerData:(NSData *)data forDataType:(BTServerDataHeader)serverDataHeader;

/**
 *  通过 Tcp 客户端向服务端发送数据
 */
-(void)sendData:(NSMutableData *)data;
@end
