//
//  BTDbManager.m
//

#import "BTDbManager.h"
#import "NSString+BTAdditions.h"
#import "BTUserEntity.h"
#import "BTPublicDefine.h"


@interface BTDbManager()
@property(strong)BTDbHelper *bigTalkUserDb;
@property(strong)BTDbHelper *bigTalkDb;
@end


@implementation BTDbManager
DEF_SINGLETON(BTDbManager)

-(instancetype)init
{
    self = [super init];
    if (self)
    {
        BOOL isExist = [[NSFileManager defaultManager] fileExistsAtPath:[self bigTalkDbPath]];
        if (!isExist)
        {
            self.bigTalkUserDb = [[BTDbHelper alloc] initWithPath:[self bigTalkUserDbPath]];
            self.bigTalkDb = [[BTDbHelper alloc] initWithPath:[self bigTalkDbPath]];
        }
    }
    return self;
}

-(NSString *)bigTalkDbPath
{
    return [[NSString documentPath] stringByAppendingPathComponent:@"bigtalk_db"];
}

-(NSString *)bigTalkUserDbPath
{
    return [[NSString documentPath] stringByAppendingPathComponent:@"bigtalk_user_db"];
}

#pragma mark - User Operation
-(NSMutableArray *)getAllUsers
{
    // In Objective-C, the __block keyword is used in the context of blocks (closures)
    // to indicate that a variable should be mutable inside the block, even if it was
    // declared as a non-mutable (normal) variable outside the block
    __block NSMutableArray *users = [NSMutableArray new];
    [self.bigTalkUserDb enumerateKeys:^(NSString *key, BOOL *stop) {
        BTUserEntity *user = [self getUserById:key];
        [users addObject:user];
    }];
   return users;
}

-(void)insertUser:(BTUserEntity *)user
{
    NSDictionary* dic = [BTUserEntity userToDic:user];
    [self.bigTalkUserDb setObject:dic forKey:user.objId];
}

-(void)insertUsers:(NSArray *)array
{
    [array enumerateObjectsUsingBlock:^(BTUserEntity *obj, NSUInteger idx, BOOL *stop) {
        [self insertUser:obj];
    }];
}

-(void)updateUser:(BTUserEntity *)user
{
    [self removeUser:user];
    [self insertUser:user];
}

-(void)removeUser:(BTUserEntity *)user
{
    [self removeUserById:user.objId];
}

-(void)removeUserById:(NSString *)userId
{
    [self.bigTalkUserDb removeValueForKey:userId];
}

-(BTUserEntity *)getUserById:(NSString *)userId
{
    return [self.bigTalkUserDb objectForKey:userId];
}

-(BOOL)userVersionIsChanged:(NSString *)userId version:(NSUInteger)version
{
    BTUserEntity *user = [self getUserById:userId];
    if (user.objectVersion == version)
    {
        return NO;
    }
    return YES;
}

-(void)setUsersVersion:(NSUInteger)version
{
    [self.bigTalkDb setInt:version forKey:@"usersVersion"];
}

-(NSInteger)getUsersVersion
{
    return  [self.bigTalkDb intForKey:@"usersVersion"];
}

@end
