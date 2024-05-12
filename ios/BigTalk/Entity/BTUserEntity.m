//
//  BTUserEntity.m
//

#import "BTUserEntity.h"
#import "BTDatabaseUtil.h"
#import "NSDictionary+BTSafe.h"
#import "BTPublicProfileViewController.h"
#import "IMBuddy.pbobjc.h"

#define USER_PRE @"user_"

@implementation BTUserEntity

-(id)initWithUserId:(NSString *)userId
               name:(NSString *)name
               nick:(NSString *)nick
             avatar:(NSString *)avatar
           userRole:(NSInteger)userRole
        userUpdated:(NSUInteger)updated
{
    self = [super init];
    if (self)
    {
        self.objId = [userId copy];
        _name = [name copy];
        _nick = [nick copy];
        _avatar = [avatar copy];

        self.lastUpdateTime = updated;
    }
    return self;
}

//-(NSString *)avatar
//{
//    if (![_avatar hasSuffix:@"_100x100"])
//    {
//        return [NSString stringWithFormat:@"%@_100x100", _avatar];
//    }
//    return _avatar;
//}

+(NSMutableDictionary *)userToDic:(BTUserEntity *)user
{
    NSMutableDictionary *dic = [NSMutableDictionary new];
    [dic safeSetObject:user.objId forKey:@"userId"];
    [dic safeSetObject:user.name forKey:@"name"];
    [dic safeSetObject:user.nick forKey:@"nick"];
    [dic safeSetObject:user.avatar forKey:@"avatar"];
    [dic safeSetObject:@(user.departId) forKey:@"departId"];
    [dic safeSetObject:user.email forKey:@"email"];
    [dic safeSetObject:user.department forKey:@"department"];
    [dic safeSetObject:user.position forKey:@"position"];
    [dic safeSetObject:user.telphone forKey:@"telphone"];
    [dic safeSetObject:user.department forKey:@"departName"];
    [dic safeSetObject:[NSNumber numberWithInt:user.sex] forKey:@"sex"];
    [dic safeSetObject:[NSNumber numberWithInt:user.lastUpdateTime] forKey:@"lastUpdateTime"];
    return dic;
}

-(void)encodeWithCoder:(NSCoder *)encoder
{
    [encoder encodeObject:self.objId forKey:@"userId"];
    [encoder encodeObject:self.name forKey:@"name"];
    [encoder encodeObject:self.nick forKey:@"nick"];
    [encoder encodeObject:self.avatar forKey:@"avatar"];
    [encoder encodeObject:@(self.departId) forKey:@"departId"];
    [encoder encodeObject:self.email forKey:@"email"];
    [encoder encodeObject:self.department forKey:@"department"];
    [encoder encodeObject:self.position forKey:@"position"];
    [encoder encodeObject:self.telphone forKey:@"telphone"];
    [encoder encodeObject:[NSNumber numberWithInt:self.sex] forKey:@"sex"];
    [encoder encodeObject:[NSNumber numberWithInt:self.lastUpdateTime] forKey:@"lastUpdateTime"];
}

-(id)initWithCoder:(NSCoder *)aDecoder
{
    if ((self = [super init]))
    {
        self.objId = [aDecoder decodeObjectForKey:@"userId"];
        self.name = [aDecoder decodeObjectForKey:@"name"];
        self.nick = [aDecoder decodeObjectForKey:@"nickName"];
        self.avatar = [aDecoder decodeObjectForKey:@"avatar"];
        self.department = [aDecoder decodeObjectForKey:@"department"];
        self.departId = [[aDecoder decodeObjectForKey:@"departId"] integerValue];
        self.email = [aDecoder decodeObjectForKey:@"email"];
        self.position = [aDecoder decodeObjectForKey:@"position"];
        self.telphone = [aDecoder decodeObjectForKey:@"telphone"];

    }
    return self;
}

+(id)dicToUserEntity:(NSDictionary *)dic
{
    BTUserEntity *user = [BTUserEntity new];
    user.objId = [dic safeObjectForKey:@"userId"];
    user.name = [dic safeObjectForKey:@"name"];
    user.nick = [dic safeObjectForKey:@"nickName"] ? [dic safeObjectForKey:@"nickName"] : user.name;
    user.avatar = [dic safeObjectForKey:@"avatar"];
    user.department = [dic safeObjectForKey:@"department"];
    user.departId = [[dic safeObjectForKey:@"departId"] integerValue];
    user.email = [dic safeObjectForKey:@"email"];
    user.position = [dic safeObjectForKey:@"position"];
    user.telphone = [dic safeObjectForKey:@"telphone"];
    user.sex = [[dic safeObjectForKey:@"sex"] integerValue];
    user.lastUpdateTime = [[dic safeObjectForKey:@"lastUpdateTime"] integerValue];
    user.pyname = [dic safeObjectForKey:@"pyname"];
    
    return user;
}

-(void)sendEmail
{
    NSString *stringURL = [NSString stringWithFormat:@"mailto:%@", self.email];
    NSURL *url = [NSURL URLWithString:stringURL];
    [[UIApplication sharedApplication] openURL:url];
}

-(void)callPhoneNum
{
    NSString *string = [NSString stringWithFormat:@"tel:%@", self.telphone];
    [[UIApplication sharedApplication] openURL:[NSURL URLWithString:string]];
}

-(BOOL)isEqual:(id)other
{
    if (other == self)
    {
        return YES;
    }
    else if ([self class] != [other class])
    {
        return NO;
    }
    else
    {
        BTUserEntity *otherUser = (BTUserEntity*)other;
        if (![otherUser.objId isEqualToString:self.objId])
        {
            return NO;
        }
        if (![otherUser.name isEqualToString:self.name])
        {
            return NO;
        }
        if (![otherUser.nick isEqualToString:self.nick])
        {
            return NO;
        }
        if (![otherUser.pyname isEqualToString:self.pyname])
        {
            return NO;
        }
    }
    return YES;
}

-(NSUInteger)hash
{
    NSUInteger objIdHash = [self.objId hash];
    NSUInteger nameHash = [self.name hash];
    NSUInteger nickHash = [self.nick hash];
    NSUInteger pynameHash = [self.pyname hash];
    
    return objIdHash^nameHash^nickHash^pynameHash;
}

+(NSString *)pbUserIdToLocalUserId:(NSUInteger)userId
{
    return [NSString stringWithFormat:@"%@%ld", USER_PRE, userId];
}

+(UInt32)localUserIdToPbUserId:(NSString *)userId
{
    if (![userId hasPrefix:USER_PRE])
    {
        return 0;
    }
    return [[userId substringFromIndex:[USER_PRE length]] integerValue];
}

-(id)initWithPbData:(UserInfo *)pbUser
{
    self = [super init];
    if (self)
    {
        self.objId = [[self class] pbUserIdToLocalUserId:pbUser.userId];
        self.name = pbUser.userRealName;
        self.nick = pbUser.userNickName;
        self.avatar = pbUser.avatarURL;
        self.departId = pbUser.departmentId;
        self.telphone = pbUser.userTel;
        self.sex = pbUser.userGender;
        self.email = pbUser.email;
        self.pyname = pbUser.userDomain;
        self.userStatus = pbUser.status;
    }
    return self;
}

-(NSString *)getAvatarUrl
{
    return [NSString stringWithFormat:@"%@_100x100.jpg", self.avatar];
}

-(NSString *)getAvatarPreImageUrl
{
    return [NSString stringWithFormat:@"%@_640×999.jpg", self.avatar];
}

@end
