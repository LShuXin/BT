//
//  BTEmotionCell.h
//

#import "BTChatImageCell.h"


@class BTMessageEntity;

@interface BTEmotionCell : BTChatImageCell<BTChatCellProtocol>
-(void)sendTextAgain:(BTMessageEntity *)msg;
@end
