//
//  BTTcpClientManager.h
//

#import <Foundation/Foundation.h>


@class BTSendBuffer;

@interface BTTcpClientManager : NSObject<NSStreamDelegate>
{
@private
    NSInputStream *_inStream;
    NSOutputStream *_outStream;
    NSLock *_receiveLock;
	NSMutableData *_receiveBuffer;
    NSLock *_sendLock;
	NSMutableArray *_sendBuffers;
	BTSendBuffer *_lastSendBuffer;
	BOOL _noDataSent;
    int32_t cDataLen;
}

+(instancetype)instance;

-(void)connect:(NSString *)ipAdr
          port:(NSInteger)port
        status:(NSInteger)status;

-(void)disconnect;
-(void)writeToSocket:(NSMutableData *)data;

@end
