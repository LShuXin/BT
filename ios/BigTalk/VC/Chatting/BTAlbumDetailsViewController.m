//
//  BTAlbumDetailsViewController.m
//

#import "BTAlbumDetailsViewController.h"
#import "BTImageGridViewCell.h"
#import "BTChattingMainViewController.h"
#import "BTAlbumDetailsBottomBar.h"
#import "BTSendPhotoMessageAPI.h"
#import "BTPhoto.h"
#import "BTDatabaseUtil.h"
#import "BTPublicDefine.h"
#import "MWCommon.h"
#import "MBProgressHUD.h"
#import "BTPhotosCache.h"
#import "BTMessageSendManager.h"
#import "MWPhotoBrowser.h"


@interface BTAlbumDetailsViewController()<MWPhotoBrowserDelegate>
@property(nonatomic, strong)NSMutableArray *photos;
@property(strong)NSMutableArray *selections;
@property(strong)MWPhotoBrowser *photoBrowser;
@property(nonatomic, strong)UIButton *button;
@end


@implementation BTAlbumDetailsViewController

-(id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self)
    {
        
    }
    return self;
}

-(void)viewDidLoad
{
    [super viewDidLoad];
    self.title = @"预览";
    self.selections = [[NSMutableArray alloc] initWithCapacity:10];
    [self.view setBackgroundColor:[UIColor whiteColor]];
    self.gridView = [[AQGridView alloc] initWithFrame:CGRectMake(self.view.frame.origin.x,
                                                                 self.view.frame.origin.y,
                                                                 self.view.frame.size.width,
                                                                 self.view.frame.size.height - 45)];
    self.gridView.delegate = self;
	self.gridView.dataSource = self;
    [self.view addSubview:self.gridView];
    self.assetsArray = [NSMutableArray new];
    self.choosePhotosArray = [NSMutableArray new];
    [self.assetsGroup enumerateAssetsUsingBlock:^(ALAsset *result, NSUInteger index, BOOL *stop) {
        if (result)
        {
            [_assetsArray addObject:result];
        }
        if (stop)
        {
            [self.gridView reloadData];
        }
    }];
    
    UIBarButtonItem *item = [[UIBarButtonItem alloc] initWithTitle:@"取消"
                                                             style:UIBarButtonItemStyleDone
                                                            target:self
                                                            action:@selector(backToRoot)];
    
    self.navigationItem.rightBarButtonItem = item;
    self.bar = [[BTAlbumDetailsBottomBar alloc] initWithFrame:CGRectMake(0,
                                                                         self.view.frame.size.height - 45,
                                                                         BTFullWidth,
                                                                         45)];
    
    __weak typeof(self) weakSelf = self;
    self.bar.block = ^(int buttonIndex) {
        if (buttonIndex == 0)
        {
            if ([weakSelf.choosePhotosArray count] == 0)
            {
                return ;
            }
            [weakSelf.selections removeAllObjects];
            self.photoBrowser = [[MWPhotoBrowser alloc] initWithDelegate:weakSelf];
            self.photoBrowser.displayActionButton = NO;
            self.photoBrowser.displayNavArrows = NO;
            self.photoBrowser.wantsFullScreenLayout = YES;
            self.photoBrowser.delayToHideElements=4;
            self.photoBrowser.zoomPhotosToFill = YES;
            self.photoBrowser.displaySelectionButtons = YES;
            
            [self.photoBrowser setCurrentPhotoIndex:0];
            weakSelf.photos = [NSMutableArray new];
            for (int i =0; i<[weakSelf.choosePhotosArray count]; i++) {
                ALAsset *result = [weakSelf.choosePhotosArray objectAtIndex:i];
                ALAssetRepresentation* representation = [result defaultRepresentation];
                if (representation == nil) {
                    CGImageRef ref = [result thumbnail];
                    
                    UIImage *img = [[UIImage alloc]initWithCGImage:ref];
                    
                    MWPhoto *photo =[MWPhoto photoWithImage:img];
                    
                    [self.photos addObject:photo];
                }else
                {
                    
                    CGImageRef ref = [[result defaultRepresentation] fullScreenImage];
                    
                    UIImage *img = [[UIImage alloc]initWithCGImage:ref];
                    
                    MWPhoto *photo =[MWPhoto photoWithImage:img];
                    [self.photos addObject:photo];
                }
                [self.selections addObject:@(1)];
            }
            
            //[self.photoBrowser reloadData];
            UIView *toolView = [[UIView alloc] initWithFrame:CGRectMake(0, self.view.frame.size.height-50, BTFullWidth, 50)];
            [toolView setBackgroundColor:RGBA(0, 0, 0, 0.7)];
            self.button = [UIButton buttonWithType:UIButtonTypeCustom];
            [self.button setBackgroundColor:[UIColor clearColor]];
            [self.button setTitle:[NSString stringWithFormat:@"发送(%ld)",[self.photos count]] forState:UIControlStateNormal];
            [self.button setTitle:[NSString stringWithFormat:@"发送(%ld)",[self.photos count]] forState:UIControlStateSelected];
            [self.button setBackgroundImage:[UIImage imageNamed:@"dd_image_send"] forState:UIControlStateNormal];
            [self.button setBackgroundImage:[UIImage imageNamed:@"dd_image_send"] forState:UIControlStateSelected];
            
            [self.button addTarget:self action:@selector(sendPhotos:) forControlEvents:UIControlEventTouchUpInside];
            NSString *string = [NSString stringWithFormat:@"%@",self.button.titleLabel.text];
            CGSize feelSize = [string sizeWithFont:[UIFont systemFontOfSize:14] constrainedToSize:CGSizeMake(190,0)];
            float  feelWidth = feelSize.width;
            self.button.frame=CGRectMake(BTFullWidth/2-feelWidth+25/2, 7, feelWidth+25, 35);
            [self.button setClipsToBounds:YES];
            [self.button.layer setCornerRadius:3];
            [toolView addSubview:self.button];
            [self.photoBrowser.view addSubview:toolView];
            [self.navigationController pushViewController:self.photoBrowser animated:YES];
            
        }
        else
        {
            //send picture
            if ([weakSelf.choosePhotosArray count] >0) {
                MBProgressHUD *HUD = [[MBProgressHUD alloc] initWithView:weakSelf.view];
                [weakSelf.view addSubview:HUD];
                
                HUD.dimBackground = YES;
                HUD.labelText = @"正在发送";
                
                [HUD showAnimated:YES whileExecutingBlock:^{
                    for (int i = 0; i<[weakSelf.choosePhotosArray count]; i++) {
                        BTPhoto *photo = [BTPhoto new];
                        ALAsset *asset = [weakSelf.choosePhotosArray objectAtIndex:i];
                        ALAssetRepresentation* representation = [asset defaultRepresentation];
                        NSURL* url = [representation url];
                        photo.localPath=url.absoluteString;
                        UIImage *image = nil;
                        if (representation == nil) {
                            CGImageRef thum = [asset aspectRatioThumbnail];
                            image = [[UIImage alloc]initWithCGImage:thum];
                        }else
                        {
                            image =[[UIImage alloc]initWithCGImage:[[asset defaultRepresentation] fullScreenImage]];
                        }
                        NSString *keyName = [[BTPhotosCache sharedPhotoCache] generateCacheKeyWithCurrentTime];
                        
                        photo.localPath=keyName;
                        [[BTChattingMainViewController shareInstance] sendImageMessage:photo Image:image];
                    }
                    
                } completionBlock:^{
                    [HUD removeFromSuperview];
                    [weakSelf.navigationController popToViewController:[BTChattingMainViewController shareInstance] animated:YES];
                }];
            }
        }
    };
    [self.view addSubview:self.bar];
    // Do any additional setup after loading the view.
    [self.gridView scrollToItemAtIndex:[self.assetsArray count] atScrollPosition:AQGridViewScrollPositionBottom animated:NO];

}
- (NSString *)photoBrowser:(MWPhotoBrowser *)photoBrowser titleForPhotoAtIndex:(NSUInteger)index{
    return [NSString stringWithFormat:@"%ld/%ld",index+1,[self.photos count]];
}
- (BOOL)photoBrowser:(MWPhotoBrowser *)photoBrowser isPhotoSelectedAtIndex:(NSUInteger)index {
    return [[_selections objectAtIndex:index] boolValue];
}

- (void)photoBrowser:(MWPhotoBrowser *)photoBrowser photoAtIndex:(NSUInteger)index selectedChanged:(BOOL)selected {
    [_selections replaceObjectAtIndex:index withObject:[NSNumber numberWithBool:selected]];
    [self setSendButtonTitle];
}
     
- (NSUInteger)numberOfPhotosInPhotoBrowser:(MWPhotoBrowser *)photoBrowser {
    return _photos.count;
}

- (id <MWPhoto>)photoBrowser:(MWPhotoBrowser *)photoBrowser photoAtIndex:(NSUInteger)index {
    if (index < _photos.count)
        return [_photos objectAtIndex:index];
    return nil;
}

-(void)setSendButtonTitle
{
    __block int j =0;
    [self.selections enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
        if ([obj boolValue]) {
            j++;
        }
    }];
    [self.button setTitle:[NSString stringWithFormat:@"发送(%d)",j] forState:UIControlStateNormal];
}
     
   
- (void)dealloc
{
    self.choosePhotosArray =nil;
    self.gridView=nil;
    self.assetsArray=nil;
    self.bar= nil;
}

-(void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
        [self.tabBarController.tabBar setHidden:YES];
}
-(void)backToRoot
{
}
- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}
- (NSUInteger) numberOfItemsInGridView: (AQGridView *) gridView
{
    return  [self.assetsArray count];
}
- (AQGridViewCell *) gridView: (AQGridView *) aGridView cellForItemAtIndex: (NSUInteger) index
{
    static NSString * PlainCellIdentifier = @"PlainCellIdentifier";
    
    BTImageGridViewCell * cell = (BTImageGridViewCell *)[self.gridView dequeueReusableCellWithIdentifier: PlainCellIdentifier];
	if ( cell == nil )
	{
        cell = [[BTImageGridViewCell alloc] initWithFrame: CGRectMake(0.0, 0.0, 75.0, 75.0) reuseIdentifier: PlainCellIdentifier];
        
	}
    cell.isShowSelect=YES;
    cell.selectionGlowColor=[UIColor clearColor];
    ALAsset *asset = [self.assetsArray objectAtIndex:index];
    
    CGImageRef thum = [asset thumbnail];
    UIImage* ti = [UIImage imageWithCGImage:thum];
	cell.image = ti;
    cell.tag=index;
    if ([self.choosePhotosArray containsObject:asset]) {
        [cell setCellIsToHighlight:YES];
    }else
    {
        [cell setCellIsToHighlight:NO];
    }
    return cell ;
}

-(void)gridView:(AQGridView *) gridView didSelectItemAtIndex:(NSUInteger) index
{
    [gridView deselectItemAtIndex:index animated:YES];
    
    ALAsset *asset = [self.assetsArray objectAtIndex:index];
    BTImageGridViewCell *cell = (BTImageGridViewCell *) [self.gridView cellForItemAtIndex:index];
    if ([self.choosePhotosArray containsObject:asset])
    {
        [cell setCellIsToHighlight:NO];
        [self.choosePhotosArray removeObject:asset];
    }
    else
    {
        if ([self.choosePhotosArray count] == 10)
        {
            return;
        }
        [cell setCellIsToHighlight:YES];
        [self.choosePhotosArray addObject:asset];
    }
    [self.bar setSendButtonTitle:[self.choosePhotosArray count]];
}

-(CGSize)portraitGridCellSizeForGridView: (AQGridView *) gridView
{
    return CGSizeMake(75, 80);
}

-(void)sendPhotos:(id)sender
{
    BTLog(@"prepare to send image");
    UIButton *button = (UIButton *)sender;
    [button setEnabled:NO];
    MBProgressHUD *hud = [[MBProgressHUD alloc] initWithView:self.view];
    [self.photoBrowser.view addSubview:hud];
    
    hud.dimBackground = YES;
    hud.labelText = @"正在发送";
    
    [hud showAnimated:YES whileExecutingBlock:^{
        if ([self.photos count] > 0) {
            NSMutableArray *tmp = [NSMutableArray new];
            [self.selections enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
                if ([obj boolValue])
                {
                    [tmp addObject:@(idx)];
                }
            }];
            [tmp enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
                NSInteger index = [obj integerValue];
                MWPhoto *newPhoto = [self.photos objectAtIndex:index];
                BTPhoto *photo = [BTPhoto new];
                NSString *keyName = [[BTPhotosCache sharedPhotoCache] generateCacheKeyWithCurrentTime];
                NSData *photoData = UIImagePNGRepresentation(newPhoto.image);
                [[BTPhotosCache sharedPhotoCache] storePhoto:photoData forKey:keyName toDisk:YES];
                photo.localPath = keyName;
                photo.image = newPhoto.image;
                [[BTChattingMainViewController shareInstance] sendImageMessage:photo Image:photo.image];
            }];
        }
         [button setEnabled:YES];
    } completionBlock:^{
        [hud removeFromSuperview];
        [self.navigationController popToViewController:[BTChattingMainViewController shareInstance] animated:YES];
         [button setEnabled:YES];
    }];
}
@end
