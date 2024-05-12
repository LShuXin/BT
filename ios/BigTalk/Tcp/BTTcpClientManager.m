//
//  BTTcpClientManager.m
//

#import "BTTcpClientManager.h"
#import "NSStream+NSStreamAddition.h"
#import "BTSendBuffer.h"
#import "BTTcpProtocolHeader.h"
#import "BTDataInputStream.h"
#import "BTDataOutputStream.h"
#import "BTAPISchedule.h"
#import "BTClientState.h"


@interface BTTcpClientManager(PrivateAPI)
// 连接成功
-(void)p_handleConntectOpenCompletedStream:(NSStream *)aStream;
-(void)p_handleEventErrorOccurredStream:(NSStream *)aStream;
-(void)p_handleEventEndEncounteredStream:(NSStream *)aStream;
// 可读
-(void)p_handleEventHasBytesAvailableStream:(NSStream *)aStream;
// 可写
-(void)p_handleEventHasSpaceAvailableStream:(NSStream *)aStream;
@end


@implementation BTTcpClientManager
+(instancetype)instance
{
    static BTTcpClientManager *g_tcpClientManager;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        g_tcpClientManager = [[BTTcpClientManager alloc] init];
    });
    return g_tcpClientManager;
}


#pragma mark - PublicAPI
-(void)connect:(NSString *)ipAdr port:(NSInteger)port status:(NSInteger)status
{
    BTLog(@"BigTalk Tcp Client Start Connect IPAddress:%@ Port:%ld", ipAdr, (long)port);
    cDataLen = 0;
    
    _receiveBuffer = [NSMutableData data];
    _sendBuffers = [NSMutableArray array];
    _noDataSent = NO;
    
    _receiveLock = [[NSLock alloc] init];
    _sendLock = [[NSLock alloc] init];
    
    NSInputStream  *tempInput  = nil;
    NSOutputStream *tempOutput = nil;
    
    [NSStream getStreamsToHostNamed:ipAdr
                               port:port
                        inputStream:&tempInput
                       outputStream:&tempOutput];
    
    _inStream  = tempInput;
    _outStream = tempOutput;
    
    [_inStream setDelegate:self];
    [_outStream setDelegate:self];
    
    [_inStream scheduleInRunLoop:[NSRunLoop currentRunLoop] forMode:NSDefaultRunLoopMode];
    [_outStream scheduleInRunLoop:[NSRunLoop currentRunLoop] forMode:NSDefaultRunLoopMode];
    
    [_inStream open];
    [_outStream open];
}

-(void)disconnect
{
    BTLog(@"BigTalk Tcp Client Disconnected");
    
    cDataLen = 0;
    
	_receiveLock = nil;
	_sendLock = nil;
    if (_inStream)
    {
        [_inStream close];
        [_inStream removeFromRunLoop:[NSRunLoop currentRunLoop] forMode:NSDefaultRunLoopMode];
    }
	_inStream = nil;
    
    if (_outStream)
    {
        [_outStream close];
        [_outStream removeFromRunLoop:[NSRunLoop currentRunLoop] forMode:NSDefaultRunLoopMode];
    }
	_outStream = nil;
    
	_receiveBuffer = nil;
	_sendBuffers = nil;
	_lastSendBuffer = nil;
    
    [BTNotificationHelper postNotification:BTNotificationTcpLinkDisconnect userInfo:nil object:nil];
}

-(void)writeToSocket:(NSMutableData *)data
{
    [_sendLock lock];
    
    @try
    {
        // 没有数据发送时才会直接发送，否则将写入发送缓冲区
        if (_noDataSent == YES)
        {
            NSInteger len;
            len = [_outStream write:[data mutableBytes] maxLength:[data length]];
            // 标记已经有数据在发送中了
            _noDataSent = NO;
            BTLog(@"WRITE - Written directly to outStream len:%li", (long)len);
            // TCP 窗口太小，没有一次性发送完毕，将剩余的数据放入发送缓冲区，并且返回
            // 当可写事件到来时会调用相关的方法将发送缓冲区中的数据发送出去
            if (len < [data length])
            {
                BTLog(@"WRITE - Creating a new buffer for remaining data len:%u", [data length] - len);
                _lastSendBuffer = [BTSendBuffer dataWithNSData:[data subdataWithRange:NSMakeRange([data length] - len, [data length])]];
                [_sendBuffers addObject:_lastSendBuffer];
            }
            return;
        }
        
        // 如果有数据正在发送中了并且已经存在发送缓冲区，则直接将数据放入发送缓冲区，并且直接返回
        // 当可写事件到来时会调用相关的方法将发送缓冲区中的数据发送出去
        if (_lastSendBuffer)
        {
            NSInteger lastSendBufferLength;
            NSInteger newDataLength;
            lastSendBufferLength = [_lastSendBuffer length];
            newDataLength = [data length];
            if (lastSendBufferLength < 1024)
            {
                BTLog(@"WRITE - Have a buffer with enough space, appending data to it");
                [_lastSendBuffer appendData:data];
                return;
            }
        }
        
        // 既有数据在发送中，又没有发送缓冲区时
        // 新建一个发送缓冲区
        BTLog(@"WRITE - Creating a new buffer");
        _lastSendBuffer = [BTSendBuffer dataWithNSData:data];
        [_sendBuffers addObject:_lastSendBuffer];
        
    }
    @catch (NSException *exception)
    {
        BTLog(@"NSException:%@ in writeToSocket.", exception);
    }
    @finally
    {
        [_sendLock unlock];
    }
}

#pragma mark - NSStream Delegate
-(void)stream:(NSStream *)aStream handleEvent:(NSStreamEvent)eventCode
{
    switch (eventCode)
    {
        case NSStreamEventNone:
			BTLog(@"NSStreamEvent: NSStreamEventNone.");
			break;
        case NSStreamEventOpenCompleted:
			[self p_handleConntectOpenCompletedStream:aStream];
			break;
		case NSStreamEventHasSpaceAvailable:
            [self p_handleEventHasSpaceAvailableStream:aStream];
            break;
		case NSStreamEventErrorOccurred:
			[self p_handleEventErrorOccurredStream:aStream];
			break;
		case NSStreamEventEndEncountered:
			[self p_handleEventEndEncounteredStream:aStream];
			break;
        case NSStreamEventHasBytesAvailable:
            [self p_handleEventHasBytesAvailableStream:aStream];
            break;
    }
}

#pragma mark - PrivateAPI
-(void)p_handleConntectOpenCompletedStream:(NSStream *)aStream
{
    BTLog(@"Tcp Connection Establisded.");
    if (aStream == _outStream)
    {
        [BTNotificationHelper postNotification:BTNotificationTcpLinkConnectComplete userInfo:nil object:nil];
    }
}

// 可写事件到来，开始将缓冲区中的数据发送出去
-(void)p_handleEventHasSpaceAvailableStream:(NSStream *)aStream
{
    BTLog(@"Tcp Connection Can Write Data Now.");
    [_sendLock lock];
    
    @try
    {
        // 没有数据需要发送
        if (![_sendBuffers count])
        {
            BTLog(@"WRITE - No data to send");
            _noDataSent = YES;
            return;
        }
        
        BTSendBuffer *sendBuffer = [_sendBuffers objectAtIndex:0];
        
        NSInteger sendBufferLength = [sendBuffer length];
        
        if (!sendBufferLength)
        {
            if (sendBuffer == _lastSendBuffer)
            {
                _lastSendBuffer = nil;
            }
            
            [_sendBuffers removeObjectAtIndex:0];
            
            BTLog(@"WRITE - No data to send");
            
            _noDataSent = YES;
            
            return;
        }
        
        // 一次最大发送 1024 bytes
        NSInteger len = ((sendBufferLength - [sendBuffer sendPos] >= 1024) ? 1024 : (sendBufferLength - [sendBuffer sendPos]));
        // 没有数据可发送
        if (!len)
        {
            if (sendBuffer == _lastSendBuffer)
            {
                _lastSendBuffer = nil;
            }
            
            [_sendBuffers removeObjectAtIndex:0];
            
            BTLog(@"WRITE - No data to send");
            
            _noDataSent = YES;
            
            return;
        }
        
        len = [_outStream write:((const uint8_t *)[sendBuffer mutableBytes] + [sendBuffer sendPos]) maxLength:len];
        BTLog(@"WRITE - Written directly to outStream len:%lid", (long)len);
        [sendBuffer consumeData:len];
        
        if (![sendBuffer length])
        {
            if (sendBuffer == _lastSendBuffer)
            {
                _lastSendBuffer = nil;
            }
            
            [_sendBuffers removeObjectAtIndex:0];
        }
        
        _noDataSent = NO;
        
        return;
    }
    @catch (NSException *exception)
    {
        BTLog(@"NSException:", exception);
    }
    @finally
    {
        [_sendLock unlock];
    }
}

-(void)p_handleEventErrorOccurredStream:(NSStream *)aStream
{
    BTLog(@"Tcp Connection Error Occurred.");
    [self disconnect];
    [BTClientState shareInstance].userState = USER_OFF_LINE;
}

-(void)p_handleEventEndEncounteredStream:(NSStream *)aStream
{
    BTLog(@"Tcp Connection NSStreamEventEndEncountered");
    cDataLen = 0;
}

-(void)p_handleEventHasBytesAvailableStream:(NSStream *)aStream
{
    BTLog(@"Tcp Connection Has Bytes Available");
    if (aStream)
    {
        // 包最多 1024 个字节
        uint8_t buf[1024];
        NSInteger len = 0;
        len = [(NSInputStream *)aStream read:buf maxLength:1024];
        
        if (len > 0)
        {
            [_receiveLock lock];
            [_receiveBuffer appendBytes:(const void*)buf length:len];
            
            // 包头的大小是16字节
            // 只有缓冲区的大小超过16字节时才会去去判断
            // 当缓冲区的长度足够16字节但是不够一个包长时终止循环
            // 当有新的数据到来时再进入此函数
            while ([_receiveBuffer length] >= 16)
            {
                NSRange range = NSMakeRange(0, 16);
                // 试图取出包头数据
                NSData *headerData = [_receiveBuffer subdataWithRange:range];
                // 包装一下原始数据，方便按照数据类型读取数据
                BTDataInputStream *inputData = [BTDataInputStream dataInputStreamWithData:headerData];
                // 包头的前四个字节代表包的大小
                uint32_t pduLen = [inputData readInt];
                if (pduLen > (uint32_t)[_receiveBuffer length])
                {
                    BTLog(@"数据不够一个包长");
                    break;
                }
                
                BTTcpProtocolHeader *tcpHeader = [[BTTcpProtocolHeader alloc] init];
                tcpHeader.version = [inputData readShort];
                tcpHeader.flag = [inputData readShort];
                tcpHeader.serviceId = [inputData readShort];
                tcpHeader.commandId = [inputData readShort];
                tcpHeader.reserved = [inputData readShort];
                tcpHeader.error = [inputData readShort];
                BTLog(@"received a packet serviceId = %d, commandId = %d", tcpHeader.serviceId, tcpHeader.commandId);
                // 前16 个字节已经读取过了，只需要读取剩余的 pduLen - 16 个字节
                range = NSMakeRange(16, pduLen - 16);
                // 包体数据
                NSData *payloadData = [_receiveBuffer subdataWithRange:range];
                // 减去一个包大小的数据时候还剩的数据
                uint32_t remainLen = (int)[_receiveBuffer length] - pduLen;
                range = NSMakeRange(pduLen, remainLen);
                // 将剩余的数据取出来，覆盖接收缓冲区中的数据
                NSData *remainData = [_receiveBuffer subdataWithRange:range];
                [_receiveBuffer setData:remainData];
                ServerDataHeader serverDataHeader = BTMakeServerDataHeader(tcpHeader.serviceId, tcpHeader.commandId, tcpHeader.reserved);
                
                // 说明收到了既含有包头又含有包体的数据包
                if (payloadData.length > 0)
                {
                    [[BTAPISchedule instance] receiveServerData:payloadData forDataType:serverDataHeader];
                }
                // 只要有数据可读就算作是一次心跳包
                [BTNotificationHelper postNotification:BTNotificationServerHeartBeat userInfo:nil object:nil];
            }
            
            [_receiveLock unlock];
        }
        else
        {
            BTLog(@"No Data Available");
        }
    }
}

@end
