//
//  BTAblumViewController.h
//

#import <UIKit/UIKit.h>
#import <AssetsLibrary/AssetsLibrary.h>
#import "BTRootViewController.h"


@interface BTAlbumViewController : BTRootViewController<UITableViewDataSource, UITableViewDelegate>
@property(nonatomic, strong)ALAssetsLibrary *assetsLibrary;
@property(nonatomic, strong)NSMutableArray *albumsArray;
@end
