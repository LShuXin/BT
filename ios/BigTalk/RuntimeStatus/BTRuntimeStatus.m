//
//  BTRuntimeStatus.m
//

#import "NSString+BTAdditions.h"
#import "BTRuntimeStatus.h"
#import "BTUserEntity.h"
#import "BTGroupModule.h"
#import "BTPublicDefine.h"
#import "BTMessageModule.h"
#import "BTClientState.h"
#import "BTClientStateMaintenanceManager.h"
#import "BTLoginViewController.h"
#import "BTAppDelegate.h"
#import "BTReceiveKickoffAPI.h"
#import "BTLogoutMsgServerAPI.h"
#import "IMLogin.pbobjc.h"

#define SHIELDINGKEY @"shieldingkey"


@interface BTRuntimeStatus()
@property(strong)NSMutableArray *fixedTopArray;
@property(strong)NSMutableArray *shieldingArray;
@end


@implementation BTRuntimeStatus

+(instancetype)instance
{
    static BTRuntimeStatus *g_runtimeState;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        g_runtimeState = [[BTRuntimeStatus alloc] init];
    });
    return g_runtimeState;
}

-(instancetype)init
{
    self = [super init];
    if (self)
    {
        self.user = [BTUserEntity new];
        self.fixedTopArray = [NSMutableArray arrayWithContentsOfFile:BTFixedList];
        self.shieldingArray = [NSMutableArray arrayWithContentsOfFile:BTShieldingList];
        [self registerAPI];
    }
    return self;
}

-(void)registerAPI
{
    //接收踢出
    BTReceiveKickoffAPI *receiveKick = [BTReceiveKickoffAPI new];
    [receiveKick registerAPIInAPIScheduleReceiveData:^(id object, NSError *error) {
        KickReasonType type = [object integerValue];
        // TODO: use const
        [[NSNotificationCenter defaultCenter] postNotificationName:@"KickOffUser" object:@(type)];
    }];
}

-(void)updateData
{
    [BTMessageModule shareInstance];
    [BTClientStateMaintenanceManager shareInstance];
    [BTGroupModule instance];
}

-(void)insertToFixedTop:(NSString *)idString
{
    if (self.fixedTopArray == nil || [self.fixedTopArray count] == 0)
    {
        self.fixedTopArray = [NSMutableArray new];
        [self.fixedTopArray addObject:idString];
    }
    else
    {
        if (![self.fixedTopArray containsObject:idString])
        {
            [self.fixedTopArray addObject:idString];
        }
    }
    [self.fixedTopArray writeToFile:BTFixedList atomically:YES];
}

-(void)removeFromFixedTop:(NSString *)idString
{
    
    if (self.fixedTopArray != nil)
    {
        [self.fixedTopArray removeObject:idString];
    }

    [self.fixedTopArray writeToFile:BTFixedList atomically:YES];
    // [self.userDefaults synchronize];
}

-(BOOL)isInFixedTop:(NSString *)idString
{
    if (self.fixedTopArray == nil)
    {
        return NO;
    }
    else
    {
        if (![self.fixedTopArray containsObject:idString])
        {
            return NO;
        }
        else
        {
            return YES;
        }
    }
    return NO;
}

-(NSUInteger)getFixedTopCount
{
    return [self.fixedTopArray count];
}

-(void)addToShielding:(NSString *)idString
{
    if (self.shieldingArray == nil || [self.shieldingArray count] == 0)
    {
        self.shieldingArray = [NSMutableArray new];
        [self.shieldingArray addObject:idString];
    }
    else
    {
        if (![self.shieldingArray containsObject:idString])
        {
            [self.shieldingArray addObject:idString];
        }
    }
    [self.shieldingArray writeToFile:BTShieldingList atomically:YES];
}

-(void)removeFromShieldingById:(NSString *)idString
{
    if (self.shieldingArray != nil)
    {
        [self.shieldingArray removeObject:idString];
    }
    [self.shieldingArray writeToFile:BTShieldingList atomically:YES];
}

-(BOOL)isInShielding:(NSString *)idString
{
    if (self.shieldingArray == nil)
    {
        return NO;
    }
    else
    {
        if (![self.shieldingArray containsObject:idString])
        {
            return NO;
        }
        else
        {
            return YES;
        }
    }
    return NO;
}

-(void)showAlertView:(NSString *)title description:(NSString *)content
{
    UIAlertView *alert = [[UIAlertView alloc] initWithTitle:title
                                                    message:content
                                                   delegate:self
                                          cancelButtonTitle:@"确定"
                                          otherButtonTitles:nil, nil];
    [alert show];
}

-(NSInteger)convertLocalIdToPbId:(NSString *)sessionId
{
    NSArray *array = [sessionId componentsSeparatedByString:@"_"];
    if (array[1])
    {
        return [array[1] integerValue];
    }
    return 0;
}

-(NSString *)convertPbIdToLocalId:(NSUInteger)pbId sessionType:(SessionType)sessionType
{
    if (sessionType == SessionType_SessionTypeSingle)
    {
        return [BTUserEntity pbUserIdToLocalUserId:pbId];
    }
    return [BTGroupEntity pbGroupIdToLocalGroupId:pbId];
}

@end
