//
//  BTChattingEditViewController.m
//

#import "BTChattingEditViewController.h"
#import "BTChattingEditModule.h"
#import "BTUserModule.h"
#import "BTCreateGroupAPI.h"
#import "BTRuntimeStatus.h"
#import "BTGroupModule.h"
#import "BTEditGroupViewController.h"
#import "BTPublicProfileViewController.h"
#import "BTPersonEditCollectionCell.h"
#import "BTShieldGroupMessageAPI.h"
#import "BTEditGroupMemberCell.h"
#import "BTPublicDefine.h"
#import "BTDeleteMemberFromGroupAPI.h"
#import "MBProgressHUD.h"
#import "BTDatabaseUtil.h"


@interface BTChattingEditViewController()
@property(nonatomic, strong)BTChattingEditModule *model;
@property(nonatomic, strong)NSMutableArray *temp;
@property(nonatomic, strong)BTUserEntity *edit;
@property(nonatomic, strong)BTUserEntity *delete;
@property(strong)NSMutableArray *willDeleteItems;
@property(strong)MBProgressHUD *hud;
@property(strong)UISwitch *shieldingOn;
@property(strong)UISwitch *topOn;
@property(strong)UITableView *tableView;
@property(strong)IBOutlet UICollectionView *collectionView;
@property(assign)BOOL isShowEdit;
@property(strong)UIButton *btn;
@end

@implementation BTChattingEditViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
  
    self.title = @"聊天详情";
    self.willDeleteItems = [NSMutableArray new];
    if (self.session.sessionType != SessionType_SessionTypeSingle)
    {
        [self.collectionView.layer setBorderWidth:0.5];
        [self.collectionView.layer setBorderColor:RGB(199, 199, 196).CGColor];
    }

    self.edit = [BTUserEntity new];
    self.edit.avatar = @"tt_group_manager_add_user";
    self.edit.position = @"99999";
    self.edit.nick = @"  ";
    
    self.delete = [BTUserEntity new];
    self.delete.avatar = @"tt_group_manager_delete_user";
    self.delete.position = @"00000";
    self.delete.nick = @"  ";
    self.items = [NSMutableArray new];
    self.temp = [NSMutableArray arrayWithArray:@[self.edit, self.delete]];

    self.groupName = @"";
    [self.items addObjectsFromArray:self.temp];
    self.tableView = [[UITableView alloc] initWithFrame:CGRectMake(0, 304, BTFullWidth, 188) style:UITableViewStyleGrouped];
    self.tableView.delegate = self;
    self.tableView.scrollEnabled = NO;
    self.tableView.dataSource = self;
    [self.tableView setBackgroundColor:[UIColor clearColor]];
    [self.view addSubview:self.tableView];
    [self.collectionView registerClass:[BTPersonEditCollectionCell class] forCellWithReuseIdentifier:@"PersonCollectionCell"];
    [self loadGroupUsers];
    [self.view setBackgroundColor:[UIColor whiteColor]];
    [self addHiddenDelete];
    if (self.session.sessionType == SessionType_SessionTypeSingle)
    {
        self.collectionView.frame = CGRectMake(0, 0, BTFullWidth, BTFullHeight);
    }
    self.collectionView.contentInset = UIEdgeInsetsMake(0, 0, -55, 0);
    self.tableView.separatorColor = [UIColor clearColor];
    self.hud = [[MBProgressHUD alloc] initWithView:self.view];
    [self.view addSubview:self.hud];
    self.hud.dimBackground = YES;
    self.hud.labelText = @"正在删除...";
}
-(void)addHiddenDelete
{
    UITapGestureRecognizer *hiddenDelete = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(hiddenAlldelete)];
    UIView *bgview = [[UIView alloc] initWithFrame:CGRectMake(0, 0, self.collectionView.contentSize.width, self.collectionView.contentSize.height)];
     [bgview addGestureRecognizer:hiddenDelete];
    [self.collectionView setBackgroundView:bgview];
   
}
-(void)hiddenAlldelete
{
     self.isShowEdit=NO;
    [self.collectionView reloadData];
    
}

-(NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section
{
    return [self.items count];
}

- (CGFloat)collectionView:(UICollectionView *)collectionView layout:(UICollectionViewLayout*)collectionViewLayout minimumInteritemSpacingForSectionAtIndex:(NSInteger)section {
    return 5;
}
-(UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath
{
    
    BTPersonEditCollectionCell *cell =[collectionView dequeueReusableCellWithReuseIdentifier:@"PersonCollectionCell" forIndexPath:indexPath];
    BTUserEntity *user = [self.items objectAtIndex:indexPath.row];
    UILongPressGestureRecognizer * longPressGr = [[UILongPressGestureRecognizer alloc] initWithTarget:self action:@selector(longPressToDo:)];
    longPressGr.minimumPressDuration = 1.0;
    [cell addGestureRecognizer:longPressGr];
    if (self.isShowEdit) {
        [cell.delImg setHidden:NO];
    }else{
        [cell.delImg setHidden:YES];
    }
    cell.delImg.tag=indexPath.row;
    [cell.delImg addTarget:self action:@selector(clieckDeleteUser:) forControlEvents:UIControlEventTouchUpInside];
    
     if (![user.position isEqualToString:@"99999"] && ![user.position isEqualToString:@"00000"]) {
         if ([user.objId isEqualToString:BTRuntime.user.objId]) {
             [cell.delImg setHidden:YES];
         }
         [cell setName:user.nick avatarImage:[user getAvatarUrl]];
     }else{
         if (self.group.groupType == GROUP_TYPE_FIXED) {
             [cell setName:@"  " avatarImage:@"  "];
             [cell.personIcon  setHidden:YES];
             [cell setUserInteractionEnabled:NO];
             [cell.delImg setHidden:YES];
         }else
         {
            [cell setName:user.nick avatarImage:[user getAvatarUrl]];
            [cell.delImg setHidden:YES];
         }
     }
    
    return cell ;
}

-(IBAction)clieckDeleteUser:(id)sender
{
    [self.hud show:YES];
    UIButton *btn = (UIButton *)sender;
    BTUserEntity *user = [self.items objectAtIndex:btn.tag];
    BTDeleteMemberFromGroupAPI* deleteMemberAPI = [[BTDeleteMemberFromGroupAPI alloc] init];
    [deleteMemberAPI requestWithObject:@[self.session.sessionId, user.objId] completion:^(BTGroupEntity *response, NSError *error) {
        [self.hud hide:YES afterDelay:1];
        if (error) {
            [BTRuntime showAlertView:@" " description:error.domain?error.domain:@"未知错误"];
            return ;
        }
        if (response)
        {
            [self.items removeObject:user];
            [self.collectionView reloadData];
            self.group=response;
            //[[BTGroupModule instance] addGroup:response];
            [[BTDatabaseUtil instance] updateGroup:response completion:^(NSError *error) {
                
            }];
        }
    }];
}
-(void)longPressToDo:(UILongPressGestureRecognizer *)gesture
{
    if(gesture.state == UIGestureRecognizerStateBegan)
    {
        if ([self.group.groupCreatorId isEqual:BTRuntime.user.objId]) {
            self.isShowEdit=YES;
            [self.collectionView reloadData];
        }
    }
}
-(void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    [self.tabBarController.tabBar setHidden:YES];
    BTUserEntity *user = [self.items lastObject];

    if (![user.position isEqualToString:@"99999"]) {
        [self.items removeObject:self.edit];
        [self.items addObject:self.edit];
    }

}
-(void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    [self.tabBarController.tabBar setHidden:YES];
}

-(void)loadGroupUsers
{
    
    if([self.items count] > 2)
    {
        [self.items removeObjectsInRange:NSMakeRange(0, [self.items count] - 2)];
    }
    if (self.session.sessionType == SessionType_SessionTypeGroup)
    {
        self.group = [[BTGroupModule instance] getGroupByGroupId:self.session.sessionId];
        self.groupName = self.group.name;
        if (!self.group)
        {
            BTSessionEntity* session = self.session;
            [[BTGroupModule instance] getGroupInfoByGroupId:session.sessionId completion:^(BTGroupEntity *group) {
                
                self.group = group;
                self.groupName = self.group.name;
                [self loadUserToView:self.group.groupUserIds];
            }];
        }
        else
        {
            [self loadUserToView:self.group.groupUserIds];
        }
    }
    else
    {
        //加载对方的头像上去
        [self loadUserToView:@[self.session.sessionId]];
    }
   
}
-(void)loadUserToView:(NSArray *)users
{
    if ([users count] > 0)
    {
        [users enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
            NSString *userId = (NSString *)obj;
            [[BTUserModule shareInstance] getUserByUserId:userId completion:^(BTUserEntity *user) {
                if (user)
                {
                    [self.items insertObject:user atIndex:0];
                }
            }];
            
        }];
        [self.collectionView reloadData];
        [self.tableView reloadData];
    }
    
}

-(void)refreshUsers:(NSMutableArray *)array
{
    [self.items removeAllObjects];
    [self.items addObjectsFromArray:array];
    [self.items addObject:self.edit];
    [self.collectionView reloadData];
    [self.tableView reloadData];
}

- (void)collectionView:(UICollectionView *)collectionView didSelectItemAtIndexPath:(NSIndexPath *)indexPath
{
    if (self.isShowEdit)
    {
        [self hiddenAlldelete];
        return;
    }
    BTUserEntity *user = self.items[indexPath.row];
    if ([user.position isEqualToString:@"99999"])
    {
        //添加联系人
        BTEditGroupViewController *newEdit = [BTEditGroupViewController new];
        newEdit.session = self.session;
        newEdit.group = self.group;
        newEdit.isCreate = self.group.objId ? NO : YES;
        newEdit.users = self.items;
        newEdit.editController = self;
        [self.navigationController pushViewController:newEdit animated:YES];
    }
    else if ([user.position isEqualToString:@"00000"])
    {
        if ([self.group.groupCreatorId isEqual:BTRuntime.user.objId])
        {
            self.isShowEdit = YES;
            [self.collectionView reloadData];
        }
    }
    else if (user)
    {
        BTPublicProfileViewController *public = [BTPublicProfileViewController new];
        public.user = user;
        [self.navigationController pushViewController:public animated:YES];
    }
}

-(void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

-(NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    if (self.session.sessionType == SessionType_SessionTypeGroup)
    {
        return 2;
    }
    else
    {
        return 0;
    }
   
}

-(UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    static NSString *cellIdentifier = @"ChatEditCellIdentifier";
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:cellIdentifier ];
    if (cell == nil)
    {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:cellIdentifier];
        UIView *line = [[UIView alloc] initWithFrame:CGRectMake(0, 44, tableView.frame.size.width, 0.5)];
        [line setBackgroundColor:[UIColor lightGrayColor]];
        [cell addSubview:line];
    }
   
    if (self.session.sessionType == SessionType_SessionTypeGroup)
    {
        if (indexPath.row == 0)
        {
            [cell.textLabel setText:@"群聊名称"];
            [cell.textLabel setTextColor:RGB(164, 165, 169)];
            [cell.textLabel setFont:systemFont(15)];
            CGSize size = [self.session.name sizeWithFont:[UIFont systemFontOfSize:17] constrainedToSize:CGSizeMake(MAXFLOAT, 17)];
            UILabel *label = [[UILabel alloc] initWithFrame:CGRectMake(BTScreenWidth-150-15, 14, 150, 17)];
            [label setTextAlignment:NSTextAlignmentRight];
            [label setTextColor:[UIColor blackColor]];
            [label setText:self.session.name];
            [cell.contentView addSubview:label];
        }
        if (indexPath.row == 1)
        {
            [cell.textLabel setText:@"接收但不通知"];
            [cell.textLabel setTextColor:RGB(164, 165, 169)];
            [cell.textLabel setFont:systemFont(15)];
            if (!self.shieldingOn)
            {
                self.shieldingOn = [[UISwitch alloc] initWithFrame:CGRectMake(BTScreenWidth-50-15, 7, 50, 30)];
                [self.shieldingOn setOn:self.group.isShield];
                [self.shieldingOn addTarget:self action:@selector(switchIsAddShielding:) forControlEvents:UIControlEventValueChanged];
                [cell.contentView addSubview:self.shieldingOn];
            }
        }
    }
    return cell;

}

-(void)setToTop:(id)sender
{
    
}

-(IBAction)switchIsAddShielding:(id)sender
{
    BTShieldGroupMessageAPI *request = [BTShieldGroupMessageAPI new];
    NSMutableArray *array = [NSMutableArray new];
    [array addObject:self.session.sessionId];
    UISwitch *switchButton = (UISwitch *)sender;
    if (switchButton.on) {
        [array addObject:@(1)];
    }else
    {
        [array addObject:@(0)];
    }
    [request requestWithObject:array completion:^(id response, NSError *error) {
        if (error) {
            self.group.isShield=NO;
            [switchButton setOn:!switchButton.on animated:YES];
            SCLAlertView *alert = [[SCLAlertView alloc] init];
            [alert showError:self title:@"网络问题" subTitle:@"操作失败了亲,要不待会再试试 ?" closeButtonTitle:nil duration:2];
            return ;
        }
        self.group.isShield=!self.group.isShield;
        [[BTDatabaseUtil instance] updateGroup:self.group completion:^(NSError *error) {
            
        }];
    }];

}


@end
