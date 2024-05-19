//
//  BTEditGroupViewController.m
//

#import "BTEditGroupViewController.h"
#import "BTEditGroupViewCell.h"
#import "BTEditContactsCell.h"
#import "BTAddMemberToGroupAPI.h"
#import "BTCreateGroupAPI.h"
#import "BTPublicDefine.h"
#import "BTRuntimeStatus.h"
#import "BTSearchModule.h"
#import "BTChattingMainViewController.h"
#import "MBProgressHUD.h"
#import "UIImageView+WebCache.h"
#import "BTGroupModule.h"
#import "BTContactsModule.h"
#import "BTUserModule.h"
#import "BTDeleteMemberFromGroupAPI.h"
#import "NSDictionary+BTSafe.h"
#import "BTSessionModule.h"
#import "BTDatabaseUtil.h"
#import "BTSpellLibrary.h"

@interface BTEditGroupViewController()
@property(weak)IBOutlet UITableView *tableView;
@property(weak)IBOutlet UISearchBar *searchBar;
@property(strong)NSDictionary *items;
@property(strong)NSMutableArray *backupArray;
@property(strong)NSMutableArray *editArray;
@property(strong)NSMutableArray *searchResult;
@property(strong)UISearchDisplayController *searchController;
@property(strong)BTContactsModule *model;
@property(strong)MBProgressHUD *hud;
@property(strong)NSMutableArray *addArray;
@end


@implementation BTEditGroupViewController

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

    if (BTIsIOS7)
    {
        // 默认值是UIRectEdgeAll, 意味着view会被拓展到整个屏幕（延伸到了navigation bar和status bar）
        // 如果把值设为UIRectEdgeNone, 就是不让view延伸到整个屏幕
        self.edgesForExtendedLayout = UIRectEdgeNone;
        // 根view在bar不透明情况下，是否允许延伸(YES：允许延伸)
        self.extendedLayoutIncludesOpaqueBars = NO;
    }
    self.title = @"选择联系人";
    self.searchResult = [NSMutableArray new];
    [self.users removeLastObject];
    self.backupArray = [NSMutableArray arrayWithArray:self.users];
    self.editArray = [NSMutableArray arrayWithArray:self.users];
    self.sessionId = self.editController.session.sessionId;
    self.items = [NSMutableDictionary new];
    self.model = [BTContactsModule new];
    self.addArray = [NSMutableArray new];
    UIBarButtonItem *item = [[UIBarButtonItem alloc] initWithTitle:@"确定"
                                                             style:UIBarButtonItemStylePlain
                                                            target:self
                                                            action:@selector(saveSelectItems)];
    [item setTitle:@"确定"];
    super.navigationItem.rightBarButtonItem = item;
    // self.searchBar.showsCancelButton = YES;
    self.searchController = [[UISearchDisplayController alloc] initWithSearchBar:self.searchBar
                                                              contentsController:self];
    self.searchController.delegate = self;
    self.searchController.searchResultsDataSource = self;
    self.searchController.searchResultsDelegate = self;

    NSArray *users = [[BTUserModule shareInstance] getAllMaintanceUser];
    self.items = [NSDictionary dictionaryWithDictionary:[self sortByContactFirstLetter:users]];
    // 右侧索引颜色透明
    self.tableView.sectionIndexBackgroundColor = [UIColor clearColor];
    [self.searchBar setBarTintColor:RGB(242, 242, 244)];
    [self.searchBar becomeFirstResponder];
    [self.searchBar setSearchFieldBackgroundImage:[UIImage imageNamed:@"search_bar"] forState:UIControlStateNormal];

    if ([[BTSpellLibrary instance] isEmpty])
    {
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
            [[[BTUserModule shareInstance] getAllMaintanceUser] enumerateObjectsUsingBlock:^(BTUserEntity *obj, NSUInteger idx, BOOL *stop) {
                [[BTSpellLibrary instance] addSpellForObject:obj];
                [[BTSpellLibrary instance] addDeparmentSpellForObject:obj];
            }];
            
        });
    }
    self.tableView.sectionIndexBackgroundColor = [UIColor clearColor];
    self.tableView.sectionIndexColor = RGB(102, 102, 102);
}

-(NSMutableDictionary *)sortByContactFirstLetter:(NSArray *)users
{
    NSMutableDictionary *dic = [NSMutableDictionary new];
    for (BTUserEntity *user in [[BTUserModule shareInstance] getAllMaintanceUser])
    {
        NSString *fl = [[user.pyname substringWithRange:NSMakeRange(0, 1)] uppercaseString];
        if ([dic safeObjectForKey:fl])
        {
            NSMutableArray *arr = [dic safeObjectForKey:fl];
            [arr addObject:user];
        }
        else
        {
            NSMutableArray *arr = [[NSMutableArray alloc] initWithArray:@[user]];
            [dic safeSetObject:arr forKey:fl];
        }
    }
    return dic;
}

-(void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
    [self.tabBarController.tabBar setHidden:YES];
    [self.editArray removeAllObjects];
}

-(void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
}

-(void)saveSelectItems
{
    if (self.isCreate)
    {
        [self createGroup];
    }
    else
    {
        if ([self.editArray count] != 0)
        {
           [self.editArray removeObjectsInArray:self.users];
           [self addUsersToGroup:self.editArray];
        }
        else
        {
            [BTRuntime showAlertView:@" " description:@"你没有选择要添加的联系人"];
        }
    }
}

-(BOOL)searchDisplayController:(UISearchDisplayController *)controller shouldReloadTableForSearchString:(NSString *)searchString
{
    if ([searchString isEqualToString:@""])
    {
        return NO;
    }
    
    [[BTSearchModule instance] searchContent:searchString completion:^(NSArray *result, NSError *error) {
        [self.searchResult removeAllObjects];
        [result enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
            if ([obj isKindOfClass:[BTUserEntity class]]) {
                [self.searchResult addObject:obj];
            }
        }];
        // TODO: why？
        [self.self.searchDisplayController.searchResultsTableView reloadData];
    }];
    
    return YES;
}

-(void)searchDisplayControllerDidEndSearch:(UISearchDisplayController *)controller
{
    [self.tableView reloadData];
}

-(BOOL)searchBarShouldBeginEditing:(UISearchBar *)searchBar
{
    if ([[self allKeys] count] == 0)
    {
        return NO;
    }
    // [self.searchBar setShowsCancelButton:YES animated:YES];
    [self.searchController setActive:YES animated:YES];

    return YES;
}

-(void)searchBarCancelButtonClicked:(UISearchBar *)searchBar
{
    [self.searchBar resignFirstResponder];
    // [self.searchBar setShowsCancelButton:NO animated:YES];
    [self.tableView reloadData];
}

-(void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

-(NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    if (tableView.tag == 100)
    {
        return [[self.items allKeys] count];
    }
    return 1;
}

-(NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
   
    if (tableView.tag == 100)
    {
        NSString *keyStr = [self allKeys][(NSUInteger)(section)];
        NSArray *arr = (self.items)[keyStr];
        return [arr count];
    }
    
    return [self.searchResult count];
}

-(UIView *)tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section
{
    UIView *sectionHeadView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, BTScreenWidth, 22)];
    [sectionHeadView setBackgroundColor:RGB(241, 240, 246)];
    UILabel *sectionHeaderLabel = [[UILabel alloc] initWithFrame:CGRectMake(10, 0, 100, 22)];
    [sectionHeaderLabel setText:[self allKeys][section]];
    [sectionHeadView addSubview:sectionHeaderLabel];
    return sectionHeadView;
}

-(NSArray *)sectionIndexTitlesForTableView:(UITableView *)tableView
{
    NSMutableArray *arr = [NSMutableArray new];
    [[self allKeys] enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
        char firstLetter = getFirstChar((NSString *)obj);
        [arr addObject:[NSString stringWithFormat:@"%c", firstLetter]];
    }];
    return arr;
}

-(NSArray *)allKeys
{
    return [[self.items allKeys] sortedArrayUsingComparator:^NSComparisonResult(id obj1, id obj2) {
        return [obj1 compare:obj2];
    }];
}

-(CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return 55.0;
}

-(UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    static NSString *cellIdentifier = @"contactsCell";
    BTEditContactsCell *cell = [tableView dequeueReusableCellWithIdentifier:cellIdentifier ];
    if (cell == nil)
    {
        cell = [[BTEditContactsCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:cellIdentifier];
    }
    cell.selectionStyle = UITableViewCellSelectionStyleNone;
    
    if (tableView.tag == 100)
    {
        // 分组首字母
        NSString *keyStr = [[self allKeys] objectAtIndex:indexPath.section];
        NSArray *userArray =[self.items objectForKey:keyStr];
        BTUserEntity *user = [userArray objectAtIndex:indexPath.row];
        if ([self.users containsObject:user])
        {
            cell.userInteractionEnabled = NO;
        }
        else
        {
            cell.userInteractionEnabled = YES;
        }
        
        if ([self.editArray containsObject:user])
        {
            [cell setCellToSelected:YES];
        }
        else
        {
            [cell setCellToSelected:NO];
        }
        
        [cell setCellContent:[user getAvatarUrl] name:user.nick cname:user.nick];
        
        return cell;
    }
    else
    {
        BTUserEntity *user = self.searchResult[indexPath.row];
        if ([self.editArray containsObject:user])
        {
           [cell setCellToSelected:YES];
        }
        else
        {
           [cell setCellToSelected:NO];
        }
        cell.nameLabel.text = user.nick;
        UIImage *placeholder = [UIImage imageNamed:@"user_placeholder"];
        [cell.avatar sd_setImageWithURL:[NSURL URLWithString:[user getAvatarUrl]] placeholderImage:placeholder];
        return cell;
    }
}

-(void)tableView:(UITableView *)tableView didDeselectRowAtIndexPath:(NSIndexPath *)indexPath
{
    BTEditContactsCell *oneCell = (BTEditContactsCell *)[tableView cellForRowAtIndexPath: indexPath];
    // 分组首字母
    NSString *keyStr = [[self allKeys] objectAtIndex:indexPath.section];
    NSArray *userArray =[self.items objectForKey:keyStr];
    BTUserEntity *user = [userArray objectAtIndex:indexPath.row];
    if (tableView.tag != 100)
    {
        user = self.searchResult[indexPath.row];
    }
    if ([self.editArray containsObject:user])
    {
        [oneCell setCellToSelected:YES];
    }
    else
    {
        [oneCell setCellToSelected:NO];
    }
}

-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    BTUserEntity *user;
    // 分组首字母
    NSString *keyStr = [[self allKeys] objectAtIndex:indexPath.section];
    NSArray *userArray =[self.items objectForKey:keyStr];
    user = [userArray objectAtIndex:indexPath.row];
    
    if (tableView.tag != 100)
    {
        user = self.searchResult[indexPath.row];
    }
    
    BTEditContactsCell *oneCell = (BTEditContactsCell *)[tableView cellForRowAtIndexPath: indexPath];

    if (![self.editArray containsObject:user])
    {
       [oneCell setCellToSelected:YES];
       [self.editArray addObject:user];
    }
    else
    {
        [oneCell setCellToSelected:NO];
        [self.editArray removeObject:user];
    }
}

-(void)addUsersToGroup:(NSMutableArray *)users
{
    BTAddMemberToGroupAPI *addMember = [[BTAddMemberToGroupAPI alloc] init];
    __block NSMutableArray *userIds = [NSMutableArray new];

    [users enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
        BTUserEntity *user = (BTUserEntity *)obj;
        if (user.objId)
        {
            [userIds addObject:user.objId];
        }
    }];
    
    [addMember requestWithObject:@[self.sessionId, userIds] completion:^(NSMutableArray *response, NSError *error) {
        if (response != nil)
        {
            self.editController.group.groupUserIds = response;
            [[BTDatabaseUtil instance] updateGroup: self.editController.group completion:^(NSError *error) {
                BTLog(@"update group error , group id is : %@", self.editController.group.objId);
            }];
            [self.navigationController popToViewController:self.editController animated:YES];
            [self.editArray addObjectsFromArray:self.users];
            [self.editController refreshUsers:self.editArray];
        }
        else
        {
            [self showAlert:error.domain?error.domain:@"未知错误"];
        }
    }];
}

-(void)createGroup
{
    UIAlertController *alertController = [UIAlertController alertControllerWithTitle:@"创建讨论组"
                                                                   message:nil
                                                            preferredStyle:UIAlertControllerStyleAlert];
    
    [alertController addTextFieldWithConfigurationHandler:^(UITextField * _Nonnull textField) {
        textField.placeholder = @"给讨论组起个名字吧";
    }];
    
    UIAlertAction *cancelAlertAction = [UIAlertAction actionWithTitle:@"取消"
                                                                style:UIAlertActionStyleCancel
                                                              handler:^(UIAlertAction * _Nonnull action) {
        BTLog(@"cancel group creation");
    }];
    
    UIAlertAction *confirmAlertAction = [UIAlertAction actionWithTitle:@"确定"
                                                                 style:UIAlertActionStyleDefault
                                                               handler:^(UIAlertAction * _Nonnull action) {
     
        UITextField *groupNameTextField = alertController.textFields.firstObject;
        if (groupNameTextField.text.length != 0)
        {
            BTCreateGroupAPI *createGroupAPI = [[BTCreateGroupAPI alloc] init];
            __block NSMutableArray *userIds = [NSMutableArray new];
            [userIds addObject:BTRuntime.user.objId];
            [self.editArray enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
                BTUserEntity *user = (BTUserEntity *)obj;
                if (user.objId)
                {
                    [userIds addObject:user.objId];
                }
            }];
            NSString *groupName = groupNameTextField.text.length != 0 ? groupNameTextField.text : [self generateDefaultGroupName];
            NSArray *array = @[groupName, @"", userIds];
            [createGroupAPI requestWithObject:array completion:^(BTGroupEntity *response, NSError *error) {
                if (response != nil)
                {
                    response.groupCreatorId = BTRuntime.user.objId;
                    [[BTGroupModule instance] addGroup:response];
                    [self.editController refreshUsers:self.editArray];
                    self.editController.group = response;
                    self.editController.session.sessionId = response.objId;
                    self.editController.session.sessionType = SessionType_SessionTypeGroup;
                    BTSessionEntity *session = [[BTSessionEntity alloc]
                                                initWithSessionId:response.objId
                                                sessionType:SessionType_SessionTypeGroup];
                    session.lastMsg = @" ";
                    [[BTDatabaseUtil instance] updateSession:session completion:^(NSError *error) {
                        
                    }];
                    [[BTDatabaseUtil instance] updateGroup:response completion:^(NSError *error) {
                        
                    }];
                    [[BTChattingMainViewController shareInstance] showChattingContentForSession:session];
                    // [[BTChattingMainViewController shareInstance].module.showingMessages removeAllObjects];
                    [BTChattingMainViewController shareInstance].title = response.name;
                    [self.navigationController popToViewController:[BTChattingMainViewController shareInstance] animated:YES];
                    [[BTSessionModule sharedInstance] addToSessionModel:session];
                    if ([BTSessionModule sharedInstance].delegate && [[BTSessionModule sharedInstance].delegate respondsToSelector:@selector(sessionUpdate:action:)])
                    {
                        [[BTSessionModule sharedInstance].delegate sessionUpdate:session action:ADD];
                    }
                    [[BTSpellLibrary instance] addSpellForObject:response];
                }
                else
                {
                    [self showAlert:error.domain ? error.domain : @"未知错误"];
                }
            }];
        }
    }];
    
    [alertController addAction:cancelAlertAction];
    [alertController addAction:confirmAlertAction];
    [self presentViewController:alertController animated:YES completion:nil];
}

-(void)showAlert:(NSString *)string
{
    UIAlertController *alertController = [UIAlertController alertControllerWithTitle:@""
                                                                             message:string
                                                                      preferredStyle:UIAlertControllerStyleAlert];
    
    [self presentViewController:alertController animated:YES completion:nil];
}

-(NSString *)generateDefaultGroupName
{
    NSMutableString *string = [NSMutableString new];
    [self.editArray enumerateObjectsUsingBlock:^(BTUserEntity *obj, NSUInteger idx, BOOL *stop) {
        [string appendFormat:@"%@,", obj.name];
        if (idx == 3)
        {
            *stop = YES;
        }
    }];

    return string;
}

@end
