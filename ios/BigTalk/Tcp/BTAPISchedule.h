//
//  BTAPISchedule.h
//

#import <Foundation/Foundation.h>
#import "BTAPIScheduleProtocol.h"
#import "BTAPIUnrequestScheduleProtocol.h"


typedef struct Response_Server_Data_Header {
    int serviceID;
    int commandID;
    int seqNo;
} ServerDataHeader;


NS_INLINE ServerDataHeader BTMakeServerDataHeader(int serviceID, int commandID, int seqNo)
{
    ServerDataHeader header;
    header.serviceID = serviceID;
    header.commandID = commandID;
    header.seqNo = seqNo;
    return header;
}


// TODO:应该有自己的专属线程
@interface BTAPISchedule : NSObject

@property(nonatomic,readonly)dispatch_queue_t apiScheduleQueue;

+(instancetype)instance;

/**
 * 将 api 放到一个 map 中，当请求返回时从 map 中查找该 api 并且调用本 api 的 completion block
 */
-(BOOL)registerApi:(id<BTAPIScheduleProtocol>)api;

/**
 *  此接口只应该在 BTSuperAPI 中被使用
 *  api 的 requestTimeOutTimeInterval 返回0时永远不会超时
 *  此接口的作用是将该 api 放到一个超时 map 中，当 requestTimeOutTimeInterval 时间到后
 *  如果该 api 存在的话说明该 api 超时了，此时需要从请求 map 和 超时 map 中删除该 api
 *  并且调用 api 的 completion block
 */
-(void)registerTimeoutApi:(id<BTAPIScheduleProtocol>)api;

/**
 * 当该方法被调用时，说明有数据从服务端到来，此时会根据服务端数据的 serviceID、commanID 去查找对应的 api 并且调用其
 * 解包方法和 completion 方法
 */
-(void)receiveServerData:(NSData *)data forDataType:(ServerDataHeader)serverDataHeader;

/**
 * 这个接口主要是用于注册一些服务端主动通知客户端的事件
 * 当数据到来时还是会根据 serviceID 和 commandID 去调用该 api 的解包方法、completion 方法
 */
-(BOOL)registerUnrequestAPI:(id<BTAPIUnrequestScheduleProtocol>)api;

/**
 *  通过 Tcp 客户端向服务端发送数据
 */
-(void)sendData:(NSMutableData *)data;
@end
