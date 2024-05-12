//
//  BTChatBaseCell.h
//

#import "BTChatBaseCell.h"


@class BTMenuLabel;

@interface BTChatTextCell : BTChatBaseCell<BTChatCellProtocol>
@property(nonatomic, retain)UILabel *contentLabel;
-(void)sendTextAgain:(BTMessageEntity *)message;
@end
