//
//  BTContactsModule.m
//

#import "BTContactsModule.h"
#import "BTPublicDefine.h"
#import "NSDictionary+BTSafe.h"
#import "BTDepartmentAPI.h"
#import "BTFixedGroupAPI.h"
#import "BTDatabaseUtil.h"
#import "BTGroupModule.h"
#import "BTRuntimeStatus.h"
#import "BTUserModule.h"
#import "BTGroupEntity.h"
#import "BTSpellLibrary.h"
#import "IMBaseDefine.pbobjc.h"
#import "BTGetDepartment.h"


@implementation BTContactsModule

-(instancetype)init
{
    self = [super init];
    if (self)
    {
        self.groups = [NSMutableArray new];
        self.department = [NSMutableDictionary new];
        [self p_loadAllDeptCompletion:^{
            BTLog(@"contacts module load departments completion");
        }];
    }
    return self;
}

-(void)addContact:(BTUserEntity *)user
{
    
}

-(NSMutableDictionary *)sortByContactFirstLetter
{
    NSMutableDictionary *dic = [NSMutableDictionary new];
    for (BTUserEntity *user in [[BTUserModule shareInstance] getAllMaintanceUser])
    {
        NSString *fl = [user.pyname substringWithRange:NSMakeRange(0, 1)];
        if ([dic safeObjectForKey:fl])
        {
            NSMutableArray *arr = [dic safeObjectForKey:fl];
            [arr addObject:user];
        }
        else
        {
            NSMutableArray *arr = [[NSMutableArray alloc] initWithArray:@[user]];
            [dic setObject:arr forKey:fl];
        }
    }
    return dic;
}

-(NSMutableDictionary *)sortByDepartment
{
    NSMutableDictionary *dic = [NSMutableDictionary new];
    NSArray *allMaintanceUsers = [[BTUserModule shareInstance] getAllMaintanceUser];
    for (BTUserEntity *user in allMaintanceUsers)
    {
        if ([dic safeObjectForKey:[NSString stringWithFormat:@"%d", (int)user.departId]])
        {
            NSMutableArray *arr = [dic safeObjectForKey:[NSString stringWithFormat:@"%d", (int)user.departId]];
            [arr addObject:user];
        }
        else
        {
            NSMutableArray *arr = [[NSMutableArray alloc] initWithArray:@[user]];
            [dic safeSetObject:arr forKey:[NSString stringWithFormat:@"%d", (int)user.departId]];
        }
    }
    return dic;
}

+(NSArray *)getFavContact
{
    NSUserDefaults *userDefaults = [NSUserDefaults standardUserDefaults];
    NSArray *arr = [userDefaults objectForKey:@"favUsers"];
    NSMutableArray *contacts = [NSMutableArray new];
    [arr enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
        [contacts addObject:[BTUserEntity dicToUserEntity:(NSDictionary *)obj]] ;
    }];
    return contacts;
}

+(void)favContact:(BTUserEntity *)user
{
    NSUserDefaults *userDefaults = [NSUserDefaults standardUserDefaults];
    if ([userDefaults objectForKey:@"favUsers"] == nil)
    {
        [userDefaults setObject:@[[BTUserEntity userToDic:user]] forKey:@"favUsers"];
    }
    else
    {
        NSMutableArray *arr = [NSMutableArray arrayWithArray:[userDefaults objectForKey:@"favUsers"]];
        if ([arr count] == 0)
        {
            [arr addObject:[BTUserEntity userToDic:user]];
            [userDefaults setObject:arr forKey:@"favUsers"];
            return;
        }
        
        for (int i = 0; i < [arr count]; i++)
        {
            NSDictionary *dic = [arr objectAtIndex:i];
            if ([[dic objectForKey:@"userId"] isEqualToString: user.objId])
            {
                [arr removeObject:dic];
                [userDefaults setObject:arr forKey:@"favUsers"];
                return;
            }
            else
            {
                // 实现 push
                if ([[arr objectAtIndex:i] isEqualToDictionary:[arr lastObject]])
                {
                    [arr addObject:[BTUserEntity userToDic:user]];
                    [userDefaults setObject:arr forKey:@"favUsers"];
                    return;
                }
            }
        }
    }
}

-(BOOL)isInFavContactList:(BTUserEntity *)user
{
    NSUserDefaults *userDefaults = [NSUserDefaults standardUserDefaults];
    NSMutableArray *arr = [NSMutableArray arrayWithArray:[userDefaults objectForKey:@"favUsers"]];
    for (int i = 0; i < [arr count]; i++)
    {
        NSDictionary *dic = [arr objectAtIndex:i];
        if ([[dic objectForKey:@"userId"] integerValue] == user.objId)
        {
            return YES;
        }
    }
    return NO;
}


+(void)getDepartmentData:(void(^)(id response))completion
{
    BTDepartmentAPI *api = [[BTDepartmentAPI alloc] init];
    [api requestWithObject:nil completion:^(id response, NSError *error) {
        if (!error)
        {
            BTLog(@"contacts module get department data success");
            if (response)
            {
                completion(response);
            }
            else
            {
                completion(nil);
            }
        }
        else
        {
            BTLog(@"contacts module get department data success error: %@", [error domain]);
            completion(nil);
        }
    }];
}

-(void)p_loadAllDeptCompletion:(void(^)())completion
{
    __block NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    __block NSInteger version = [[defaults objectForKey:@"allDeptLatestUpdateTime"] integerValue];
    
    [[BTDatabaseUtil instance] getAllDepartmentsCompletion:^(NSArray *departments, NSError *error) {
        if ([departments count] != 0)
        {
            [departments enumerateObjectsUsingBlock:^(DepartInfo *obj, NSUInteger idx, BOOL *stop) {
                [self.department setObject:obj.deptName forKey:[NSString stringWithFormat:@"%d", obj.deptId]];
            }];
        }
        else
        {
            version = 0;
            BTGetDepartment *api = [[BTGetDepartment alloc] init];
            [api requestWithObject:@[@(version)] completion:^(id response, NSError *error) {
                if (!error)
                {
                    // TODO: rename
                    NSUInteger responseVersion = [response[@"allDeplastupdatetime"] integerValue];
                    if (responseVersion == version && responseVersion != 0)
                    {
                        return;
                    }
                    [defaults setObject:@(responseVersion) forKey:@"allDeptLatestUpdateTime"];
                    NSMutableArray *array = response[@"deplist"];
                    [[BTDatabaseUtil instance] insertDepartments:array completion:^(NSError *error) {
                        BTLog(@"insert departments completion");
                    }];
                    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
                        [array enumerateObjectsUsingBlock:^(DepartInfo *obj, NSUInteger idx, BOOL *stop) {
                             [self.department setObject:obj.deptName forKey:[NSString stringWithFormat:@"%d", obj.deptId]];
                        }];
                    });
                }
            }];
        }
    }];
    
    // for update
    BTGetDepartment *api = [[BTGetDepartment alloc] init];
    [api requestWithObject:@[@(version)] completion:^(id response, NSError *error) {
        if (!error)
        {
            // TODO: rename
            NSUInteger responseVersion = [response[@"allDeplastupdatetime"] integerValue];
            if (responseVersion == version && responseVersion != 0)
            {
                // no updates since last sync
                return;
            }
            [defaults setObject:@(responseVersion) forKey:@"allDeptLatestUpdateTime"];
            NSMutableArray *array = response[@"deplist"];
            [[BTDatabaseUtil instance] insertDepartments:array completion:^(NSError *error) {
                BTLog(@"insert departments completion");
            }];
            dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
                [array enumerateObjectsUsingBlock:^(DepartInfo *obj, NSUInteger idx, BOOL *stop) {
                    [self.department setObject:obj.deptName forKey:[NSString stringWithFormat:@"%d", obj.deptId]];
                }];
            });
        }
    }];
}

@end
