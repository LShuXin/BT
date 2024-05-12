//
//  BTAlbumViewController.m
//

#import "BTAlbumViewController.h"
#import "BTAlbumDetailsViewController.h"
#import "BTPublicDefine.h"


@interface BTAlbumViewController()

@end


@implementation BTAlbumViewController

-(id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self)
    {

    }
    return self;
}

-(void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    [self.tabBarController.tabBar setHidden:YES];
}

-(void)viewDidLoad
{
    [super viewDidLoad];
    self.albumsArray = [NSMutableArray new];
    self.assetsLibrary =  [[ALAssetsLibrary alloc] init];
    UITableView *tableView = [[UITableView alloc] initWithFrame:self.view.frame];
    tableView.delegate = self;
    tableView.dataSource = self;
    [self.view addSubview:tableView];
    
    void(^assetsGroupsEnumerationBlock)(ALAssetsGroup *, BOOL *) = ^(ALAssetsGroup *assetsGroup, BOOL *stop) {
        [assetsGroup setAssetsFilter:[ALAssetsFilter allPhotos]];
        if (assetsGroup.numberOfAssets > 0)
        {
            [self.albumsArray addObject:assetsGroup];
        }
        if (stop)
        {
            [tableView reloadData];
        }
    };
    
    //查找相册失败block
    void(^assetsGroupsFailureBlock)(NSError *) = ^(NSError *error) {
        BTLog(@"Error: %@", [error localizedDescription]);
    };
    
    
    [self.assetsLibrary enumerateGroupsWithTypes:ALAssetsGroupAll
                                      usingBlock:assetsGroupsEnumerationBlock
                                    failureBlock:assetsGroupsFailureBlock];
}

-(void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

#pragma mark - UITableView DataSource
-(NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}

-(NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return [self.albumsArray count];
}

-(UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    static NSString *identifier = @"BTAlbumsCellIdentifier";
    UITableViewCell *cell = (UITableViewCell *)[tableView dequeueReusableCellWithIdentifier:identifier];
    if (!cell)
    {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:identifier];
    }
    NSString *name = [[self.albumsArray objectAtIndex:indexPath.row] valueForProperty:ALAssetsGroupPropertyName];
    cell.textLabel.text = [NSString stringWithFormat:@"%@  ( %ld )", name, (long)[[self.albumsArray objectAtIndex:indexPath.row] numberOfAssets]];
    [cell.textLabel setTextColor:RGB(145, 145, 145)];
    [cell.textLabel setHighlightedTextColor:[UIColor whiteColor]];
    [cell setAccessoryType:UITableViewCellAccessoryDisclosureIndicator];
    UIView *view = [[UIView alloc] initWithFrame:cell.frame];
    view.backgroundColor = RGB(246, 93, 137);
    cell.selectedBackgroundView = view;
    [cell.imageView setImage:[UIImage imageWithCGImage:[[self.albumsArray objectAtIndex:indexPath.row] posterImage]]] ;
    return cell;
}

-(CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return 60;
}

-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    BTAlbumDetailsViewController *details = [BTAlbumDetailsViewController new];
    details.assetsGroup = [self.albumsArray objectAtIndex:indexPath.row];
    [self.navigationController pushViewController:details animated:YES];
}
@end
