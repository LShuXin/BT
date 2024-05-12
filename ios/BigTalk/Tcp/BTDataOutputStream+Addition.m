//
//  BTDataOutputStream+Addition.m
//

#import "BTDataOutputStream+Addition.h"
#import "NSStream+NSStreamAddition.h"


@implementation BTDataOutputStream(Addition)

-(void)writeTcpProtocolHeaderUseServiceID:(int16_t)servieID
                               commandID:(int16_t)commandID
                                   seqNo:(uint16_t)seqNo
{
    [self writeShort:IM_PDU_VERSION];
    [self writeShort:0];
    [self writeShort:servieID];
    [self writeShort:commandID];
    // [self writeShort:0];
    [self writeShort:seqNo];
    [self writeShort:1];
}

@end
