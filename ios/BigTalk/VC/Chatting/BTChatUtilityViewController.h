//
//  BTChatUtilityViewController.h
//

#import <UIKit/UIKit.h>

#import "AQGridViewController.h"
#import "AQGridView.h"

@interface BTChatUtilityViewController : UIViewController<UIImagePickerControllerDelegate, UINavigationControllerDelegate>
@property(nonatomic, strong)UIImagePickerController *imagePicker;
@end
