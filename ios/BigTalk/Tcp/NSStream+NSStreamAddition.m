//
//  NSStream+NSStreamAddition.m
//

#import "NSStream+NSStreamAddition.h"


@implementation NSStream(NSStreamAddition)

+(void)getStreamsToHostNamed:(NSString *)hostName
                        port:(NSInteger)port
                 inputStream:(NSInputStream **)inputStream
                outputStream:(NSOutputStream **)outputStream
{
    CFHostRef           host;
    CFReadStreamRef     readStream;
    CFWriteStreamRef    writeStream;
	
    readStream = NULL;
    writeStream = NULL;
    
    host = CFHostCreateWithName(NULL, (__bridge CFStringRef)hostName);
    if (host != NULL)
    {
        // 这里是建立 socket 长链接的关键调用
        // 打开后会获得输入输出流的指针
        // 输入输出流要及时关闭
        (void)CFStreamCreatePairWithSocketToCFHost(NULL, host, (SInt32)port, &readStream, &writeStream);
        CFRelease(host);
    }
    
    // 传入的 inputStream 为空指针时要及时释放 readStream
    if (inputStream == NULL)
    {
        if (readStream != NULL)
        {
            CFRelease(readStream);
        }
    }
    else
    {
        *inputStream = (__bridge NSInputStream *) readStream;
    }
    
    // 传入的 outputStream 为空时，要及时释放打开的 writeStream
    if (outputStream == NULL)
    {
        if (writeStream != NULL)
        {
            CFRelease(writeStream);
        }
    }
    else
    {
        *outputStream =(__bridge NSOutputStream *) writeStream;
    }
}

@end


@implementation NSMutableData(NSMutableDataExtension)

-(void)writeBool:(BOOL)value
{
}

-(void)writeByte:(uint8_t)value
{
}

-(void)writeShort:(short)v
{
    int8_t ch[2];
    ch[0] = (v & 0x0ff00) >> 8;
    ch[1] = (v & 0x0ff);
    [self appendBytes:ch length:2];
}

-(void)writeInt:(int)aint
{
    char tag = 'I';
    [self appendBytes:&tag length:1];
    unsigned char b32 = aint >> 24;
    unsigned char b24 = (aint >> 16) & 0x000000FF;
    unsigned char b16 = (aint >> 8) & 0x000000FF;
    unsigned char b8 = aint & 0x000000FF;
    [self appendBytes:&b32 length:1];
    [self appendBytes:&b24 length:1];
    [self appendBytes:&b16 length:1];
    [self appendBytes:&b8 length:1];
}

-(void)writeLong:(long long)value
{
}

-(void)writeFloat:(float)value
{
    
}

-(void)writeDouble:(double)value
{
}

-(void)writeUTF:(NSString*)value
{
}

@end
