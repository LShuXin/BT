//
//  BTChatImageCell.h
//

#import "BTChatBaseCell.h"
#import "MWPhotoBrowser.h"


typedef void(^BTPreview)();
typedef void(^BTTapPreview)();


@interface BTChatImageCell : BTChatBaseCell<BTChatCellProtocol, MWPhotoBrowserDelegate>
@property(nonatomic, strong)UIImageView *msgImgView;
@property(nonatomic, strong)NSMutableArray *photos;
@property(nonatomic, strong)BTPreview preview;
-(void)showPreview;
-(void)sendImageAgain:(BTMessageEntity *)message;
@end
