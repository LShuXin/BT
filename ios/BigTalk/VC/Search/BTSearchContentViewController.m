//
//  BTSearchContentViewController.m
//

#import "BTSearchContentViewController.h"
#import "BTPublicDefine.h"
#import "BTSearchModule.h"
#import "BTContactsCell.h"
#import "BTUserEntity.h"
#import "BTPublicProfileViewController.h"
#import "BTSessionEntity.h"
#import "BTContactsViewController.h"
#import "BTAppDelegate.h"
#import "MBProgressHUD.h"
#import "BTContactsModule.h"
#import "BTDatabaseUtil.h"
#import "BTSpellLibrary.h"
#import "BTGroupModule.h"
#import "BTUserModule.h"
#import "BTSessionModule.h"
#import "BTChattingMainViewController.h"


@interface BTSearchContentViewController()
@property(strong)NSString *keyString;
@property(strong)NSMutableArray *searchResult;
@property(strong)NSMutableArray *groups;
@property(strong)NSMutableArray *department;
@property(strong)BTContactsViewController *contact;
@end


@implementation BTSearchContentViewController
-(instancetype)init
{
    self = [super init];
    if (self)
    {
        self.groups = [NSMutableArray new];
        self.searchResult = [NSMutableArray new];
        self.department = [NSMutableArray new];
        self.keyString = @"";
        self.dataSource = self;
        self.delegate = self;
        BTLog(@"come to BTSearchContentViewController");
        if ([[BTSpellLibrary instance] isEmpty])
        {
            BTLog(@"spelllibrary is empty");
            dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
                [[[BTUserModule shareInstance] getAllMaintanceUser] enumerateObjectsUsingBlock:^(BTUserEntity *obj, NSUInteger idx, BOOL *stop) {
                    [[BTSpellLibrary instance] addSpellForObject:obj];
                    [[BTSpellLibrary instance] addDeparmentSpellForObject:obj];
                }];
                NSArray *array = [[BTGroupModule instance] getAllGroups];
                [array enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
                    [[BTSpellLibrary instance] addSpellForObject:obj];
                }];
            });
        }
    }
    return self;
}





#pragma mark - UITableViewDataSource
-(NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    if (section == 0)
    {
        return [self.searchResult count];
    }
    else if (section == 1)
    {
        return [self.groups count];
    }
    else
    {
        return [self.department count];
    }
}

-(NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 3;
}

-(UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    static NSString *cellIdentifier = @"contactsCell";
    BTContactsCell *cell = [tableView dequeueReusableCellWithIdentifier:cellIdentifier];
    if (cell == nil)
    {
        cell = [[BTContactsCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:cellIdentifier];
    }
    BTUserEntity *user = nil;
    if (indexPath.section == 0)
    {
        user = [self.searchResult objectAtIndex:indexPath.row];
        [cell setCellContent:[user getAvatarUrl] name:user.nick cname:@""];
    }
    else if(indexPath.section == 1)
    {
        BTGroupEntity *group = [self.groups objectAtIndex:indexPath.row];
        [cell setCellContent:group.avatar name:group.name cname:@""];
    }
    else
    {
        NSString *string = [self.department objectAtIndex:indexPath.row];
        // TODO: why
        [cell setCellContent:[user getAvatarUrl] name:string cname:@""];
    }
    
    return cell;
}

-(NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section
{
    if (section == 0)
    {
        return [self.searchResult count] ? @"联系人": @"";
    }
    else if (section == 1)
    {
        return [self.groups count] ? @"群组" : @"";
    }
    else
    {
        return [self.department count ] ? @"部门" : @"";
    }
}





#pragma mark - UITableViewDelegate
-(void)scrollViewDidScroll:(UIScrollView *)scrollView
{
    
}

-(CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return 55;
}

-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (indexPath.section == 0)
    {
        BTUserEntity *user;
        user = self.searchResult[indexPath.row];
        BTPublicProfileViewController *public = [BTPublicProfileViewController new];
        public.user = user;
        [self.viewController.navigationController pushViewController:public animated:YES];
        return;
    }
    else if (indexPath.section == 1)
    {
        BTGroupEntity *group = [self.groups objectAtIndex:indexPath.row];
        BTSessionEntity *session;
        if (![[BTSessionModule sharedInstance] getSessionById:group.objId])
        {
            session = [[BTSessionEntity alloc] initWithSessionId:group.objId sessionName:group.name sessionType:SessionType_SessionTypeGroup];
        }
        else
        {
            session = [[BTSessionModule sharedInstance] getSessionById:group.objId];
        }
        
        [[BTChattingMainViewController shareInstance] showChattingContentForSession:session];
        [self.viewController.navigationController pushViewController:[BTChattingMainViewController shareInstance] animated:YES];
    }
    else
    {
        NSString *string = [self.department objectAtIndex:indexPath.row];
        BTContactsViewController *contact = [BTContactsViewController new];
        contact.sectionTitle = string;
        contact.isSearchResult = YES;
        [self.viewController.navigationController pushViewController:contact animated:YES];
    }
}





# pragma mark - UISearchBarDelegate
-(void)searchBarSearchButtonClicked:(UISearchBar *)searchBar {
   
}

-(void)searchBarCancelButtonClicked:(UISearchBar *)searchBar {

    [self.searchResult removeAllObjects];
    [self.groups removeAllObjects];
    [self.department removeAllObjects];
    [self reloadData];
}





# pragma mark - Custom Methods
-(void)searchTextDidChanged:(NSString *)searchText Block:(void(^)(bool done)) block
{
    if ([searchText isEqualToString:@""]) {
        return ;
    }

    MBProgressHUD *HUD = [[MBProgressHUD alloc] initWithView:self];
    [self addSubview:HUD];
    [HUD show:YES];
    HUD.dimBackground = YES;
    HUD.labelText = @"正在搜索";
    [[BTSearchModule instance] searchDepartment:searchText completion:^(NSArray *result, NSError *error) {
        if ([result count] >0) {
            [self.department removeAllObjects];
            [result enumerateObjectsUsingBlock:^(BTUserEntity *obj, NSUInteger idx, BOOL *stop) {
                if (![self.department containsObject:obj.department]) {
                    [self.department addObject:obj.department];
                }
            }];
            
            block(YES);
        }
        [HUD removeFromSuperview];
    }
     ];
    [[BTSearchModule instance] searchContent:searchText completion:^(NSArray *result, NSError *error) {
        self.keyString=searchText;
        if ([result count] >0) {
            [self.searchResult removeAllObjects];
            [self.groups removeAllObjects];
            [result enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
                if ([obj isKindOfClass:[BTUserEntity class]]) {
                    [self.searchResult addObject:obj];
                }else if ([obj isKindOfClass:[BTGroupEntity class]])
                {
                    [self.groups addObject:obj];
                }
             
            }];
           block(YES);
        }
        [HUD removeFromSuperview];
    }];
    
}

- (void)searchTextDidChanged:(NSString *)searchText block:(void (^__strong)(bool))block {
}

@end
