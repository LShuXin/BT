//
//  BTAPIScheduleProtocol.h
//  发出请求并且等待服务端返回结果
//

#import <Foundation/Foundation.h>

// 解包 block
typedef id(^Analysis)(NSData *data);
// 装包 block
typedef NSMutableData*(^Package)(id object, uint16_t seqNO);


@protocol BTAPIScheduleProtocol<NSObject>

@required

/**
 * 通过该方法的返回值确定本 API 是否已经超时
 */
-(int)requestTimeOutTimeInterval;

/**
 *  请求对应的 serviceID
 */
-(int)requestServiceID;

/**
 *  请求返回体中携带的 serviceID
 */
-(int)responseServiceID;

/**
 *  请求对应的 commandID
 */
-(int)requestCommandID;

/**
 *  请求返回体中携带的 commandID
 */
-(int)responseCommandID;

/**
 *  解析请求返回体的 block
 */
-(Analysis)analysisReturnData;

/**
 *  打包请求数据的 block
 */
-(Package)packageRequestObject;

@end
