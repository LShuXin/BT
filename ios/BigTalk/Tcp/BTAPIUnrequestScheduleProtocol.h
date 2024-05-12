//
//  BTAPIUnrequestScheduleProtocol.h
//  不主动发出请求，仅仅被动接收服务端通知类的接口
//

#import <Foundation/Foundation.h>

// 解包的 block
typedef id(^UnrequestAPIAnalysis)(NSData *data);


@protocol BTAPIUnrequestScheduleProtocol<NSObject>

@required

/**
 * 服务端主动推送的数据包中的 serviceID
 */
-(int)responseServiceID;

/**
 * 服务端主动推送的数据包中的 commandID
 */
-(int)responseCommandID;

/**
 *  解析服务端主动推送的数据包的 block
 */
-(UnrequestAPIAnalysis)unrequestAnalysis;
@end
