//
//  BTChatCellProtocol.h
//

#import <Foundation/Foundation.h>


@class BTMessageEntity;


@protocol BTChatCellProtocol<NSObject>
-(CGSize)sizeForContent:(BTMessageEntity *)content;
-(float)contentUpGapWithBubble;
-(float)contentDownGapWithBubble;
-(float)contentLeftGapWithBubble;
-(float)contentRightGapWithBubble;
-(void)layoutContentView:(BTMessageEntity *)content;
-(float)cellHeightForMessage:(BTMessageEntity *)message;
@end
