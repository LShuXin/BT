//
//  BTDataOutputStream+Addition.h
//

#import "BTDataOutputStream.h"


@interface BTDataOutputStream(Addition)

-(void)writeTcpProtocolHeaderUseServiceID:(int16_t)serviceID
                                commandID:(int16_t)commandID
                                    seqNo:(uint16_t)seqNo;

@end
