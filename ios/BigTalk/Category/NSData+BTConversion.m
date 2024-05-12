//
//  NSData+BTConversion.m
//

#import "NSData+BTConversion.h"


@implementation NSData(BTConversion)
-(NSString *)hexadecimalString
{
    // 将NSData对象的字节数据取出，并将其转换为无符号字符指针，以便后续遍历
    const unsigned char *dataBuffer = (const unsigned char *)[self bytes];
    
    // 如果为空，则直接返回一个空的NSString对象。
    if (!dataBuffer)
    {
        return [NSString string];
    }
       
    
    NSUInteger        dataLength  = [self length];
    // 创建了一个可变字符串对象，用于存储转换后的十六进制字符串，初始化容量为数据长度的两倍，因为每个字节需要两个十六进制字符来表示
    NSMutableString   *hexString  = [NSMutableString stringWithCapacity:(dataLength * 2)];
    
    for (int i = 0; i < dataLength; ++i)
    {
        // 将当前字节转换为两位的十六进制字符串
        [hexString appendString:[NSString stringWithFormat:@"%02lx", (unsigned long)dataBuffer[i]]];
    }
      
    return [NSString stringWithString:hexString];
}

@end
