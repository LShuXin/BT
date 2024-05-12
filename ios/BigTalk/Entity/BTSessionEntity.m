//
//  BTSessionEntity.m
//

#import "BTSessionEntity.h"
#import "BTUserModule.h"
#import "BTDatabaseUtil.h"
#import "BTGroupEntity.h"
#import "BTGroupModule.h"
#import "BTMessageModule.h"
#import "BTUserEntity.h"
#import "BTGroupEntity.h"


@implementation BTSessionEntity
// 在 Objective-C 中，@synthesize 指令用于在类实现文件中生成实例变量的存取方法（getter 和 setter）。
// 它可以简化属性的声明和使用，并使代码更易读。
@synthesize name;
@synthesize timeInterval;

-(void)setSessionId:(NSString *)sessionId
{
    _sessionId = [sessionId copy];
    name = nil;
    timeInterval = 0;
}

-(void)setSessionType:(SessionType)sessionType
{
    // 在 Objective-C 中，为属性赋值时使用 copy 方法可以带来以下好处：
    // 1. 维护数据完整性和线程安全性：
    //
    // **数据完整性：** 当多个线程访问共享对象时，如果一个线程在另一个线程读取对象时对其进行修改，则存在数据损坏的风险。使用 copy 可确保每个线程都操作其自身数据副本，从而防止数据损坏。
    //
    // **线程安全性：** 通过使用 copy，您可以创建对象的单独实例，使其与其他线程所做的修改隔离。这使得代码更加线程安全，并降低了并发问题风险。
    //
    // 2. 内存管理和所有权：
    //
    // **内存管理：**当您直接将对象分配给实例变量时，实例变量现在拥有该对象。如果对象是可变的，则通过实例变量进行的修改也会影响原始对象。
    // 使用 copy 会创建对象的全新实例，并且实例变量仅拥有其副本。这有助于管理对象生命周期并避免内存泄漏。
    //
    // **所有权清晰：**当您使用 copy 时，您明确指示实例变量拥有原始对象的副本。这使得代码更具可读性和易于理解，尤其是在处理可变对象时。
    //
    // 3. 不可变性和一致性：
    //
    // **不可变性：** 如果 sessionType 对象是不可变的（无法修改），则使用 copy 可确保实例变量始终引用同一不可变对象。这有助于维护数据一致性并防止意外更改。
    //
    // **一致性：** 通过使用 copy，您可以确保实例变量始终对数据保持一致引用，即使原始对象在其他地方被修改。这促进了数据一致性，并降低了由于共享可变对象而导致不一致的风险。
    //
    // 总而言之，在为属性赋值时使用 copy 是一种良好实践，可用于维护数据完整性、线程安全性、内存管理、所有权清晰性、不可变性和数据一致性，尤其是在多线程环境中处理共享对象时。
    //
    // 请注意，并非所有情况下都需要使用 copy。例如，如果属性是原始类型（如 int 或 float）或不可变对象，则无需使用 copy。但是，对于可变对象或可能被多个线程共享的对象，使用 copy 通常是一种更安全和更可靠的做法。
    _sessionType = sessionType;
    name = nil;
    timeInterval = 0;
}

-(NSString *)name
{
    if (!name)
    {
        switch (self.sessionType)
        {
            case SessionType_GPBUnrecognizedEnumeratorValue:
            {
                return nil;
                break;
            }
            case SessionType_SessionTypeNone: 
            {
                return nil;
                break;
            }
            case SessionType_SessionTypeSingle:
            {
                [[BTUserModule shareInstance] getUserByUserId:_sessionId completion:^(BTUserEntity *user) {
                    if ([user.nick length] > 0)
                    {
                        name = user.nick;
                    }
                    else
                    {
                        name = user.name;
                    }

                }];
                break;
            }
            case SessionType_SessionTypeGroup:
            {
                BTGroupEntity *group = [[BTGroupModule instance] getGroupByGroupId:_sessionId];
                if (!group)
                {
                    [[BTGroupModule instance] getGroupInfoByGroupId:_sessionId completion:^(BTGroupEntity *group) {
                        name = group.name;
                    }];
                }
                else
                {
                     name = group.name;
                }
                break;
            }
        }
    }
    return name;
}

-(void)setSessionName:(NSString*)sessionName
{
    name = sessionName;
}

-(NSUInteger)timeInterval
{
    if (timeInterval == 0)
    {
        switch (_sessionType)
        {
            case SessionType_GPBUnrecognizedEnumeratorValue:
            {
                return 0;
                break;
            }
            case SessionType_SessionTypeNone: 
            {
                return 0;
                break;
            }
            case SessionType_SessionTypeSingle:
            {
                [[BTUserModule shareInstance] getUserByUserId:_sessionId completion:^(BTUserEntity *user) {
                    timeInterval = user.lastUpdateTime;
                }];
                break;
            }
            case SessionType_SessionTypeGroup: {
                return 0;
                break;
            }
        }
    }
    return timeInterval;
}


#pragma mark Public API
-(id)initWithSessionId:(NSString *)sessionId sessionName:(NSString *)sessionName sessionType:(SessionType)sessionType
{
    BTSessionEntity *session = [self initWithSessionId:sessionId sessionType:sessionType];
    [session setSessionName:name];
    return session;
}

-(id)initWithSessionId:(NSString *)sessionId sessionType:(SessionType)sessionType
{
    self = [super init];
    if (self)
    {
        self.sessionId = sessionId;
        self.sessionType = sessionType;
        self.unReadMsgCount =  0;
        self.lastMsg = @"";
        self.lastMsgId = 0;
        self.timeInterval = [[NSDate date] timeIntervalSince1970];
    }
    return self;
}

-(void)updateUpdateTime:(NSUInteger)date
{
    timeInterval = date;
    self.timeInterval = timeInterval;
    [[BTDatabaseUtil instance] updateSession:self completion:^(NSError *error) {
                    
    }];
  }

-(NSArray *)sessionUsers
{
    if (SessionType_SessionTypeGroup == self.sessionType)
    {
        BTGroupEntity *group = [[BTGroupModule instance] getGroupByGroupId:_sessionId];
        return group.groupUserIds;
    }
    
    return nil;
}

-(NSString *)getSessionGroupId
{
    return _sessionId;
}

-(BOOL)isGroup
{
    if (SessionType_SessionTypeGroup == self.sessionType)
    {
        return YES;
    }
    return NO;
}

-(id)initByUser:(BTUserEntity *)userEntity
{
    BTSessionEntity *session = [self initWithSessionId:userEntity.objId sessionType:SessionType_SessionTypeSingle];
    [session setSessionName:userEntity.name];
    return session;
}

-(id)initByGroup:(BTGroupEntity *)group
{
    SessionType sessionType = SessionType_SessionTypeGroup;
    BTSessionEntity *session = [self initWithSessionId:group.objId sessionType:sessionType];
    [session setSessionName:group.name];
    
    return session;
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
        BTSessionEntity *otherSession = (BTSessionEntity *)other;
        if (![self.sessionId isEqualToString:otherSession.sessionId])
        {
            return NO;
        }
        if (self.sessionType != otherSession.sessionType)
        {
            return NO;
        }
    }
    return YES;
}

-(NSUInteger)hash
{
    NSUInteger sessionIdHash = [self.sessionId hash];
    return sessionIdHash^self.sessionType;
}

-(void)setSessionUsers:(NSArray *)array
{
}

-(id)dicToGroup:(NSDictionary *)dic
{
    return nil;
}

@end
