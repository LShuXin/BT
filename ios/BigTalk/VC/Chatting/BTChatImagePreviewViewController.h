//
//  BTChatImagePreviewViewController.h
//

#import <UIKit/UIKit.h>
#import "MWPhotoBrowser.h"


@interface BTChatImagePreviewViewController : UIViewController<MWPhotoBrowserDelegate, UIActionSheetDelegate>
@property(nonatomic, strong)NSMutableArray *photos;
@property(strong)UIImage *previewImage;
@end
