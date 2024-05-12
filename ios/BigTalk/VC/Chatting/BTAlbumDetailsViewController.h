//
//  BTAlbumDetailsViewController.h
//

#import "AQGridViewController.h"
#import <AssetsLibrary/AssetsLibrary.h>
#import "BTRootViewController.h"


@class BTAlbumDetailsBottomBar;

@interface BTAlbumDetailsViewController : BTRootViewController<AQGridViewDataSource, AQGridViewDelegate>
@property(nonatomic, strong)ALAssetsGroup *assetsGroup;
@property(nonatomic, strong)NSMutableArray *assetsArray;
@property(nonatomic, strong)NSMutableArray *choosePhotosArray;
@property(nonatomic, strong)AQGridView *gridView;
@property(nonatomic, strong)BTAlbumDetailsBottomBar *bar;
@end
