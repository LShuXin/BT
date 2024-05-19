//  BTDatabaseUtil.m
//

#import "BTDatabaseUtil.h"
#import "BTMessageEntity.h"
#import "BTUserEntity.h"
#import "BTUserModule.h"
#import "BTGroupEntity.h"
#import "NSString+BTPath.h"
#import "NSDictionary+BTSafe.h"
#import "BTSessionEntity.h"

#define DB_FILE_NAME                    @"bt.sqlite"
#define TABLE_MESSAGES                  @"messages"
#define TABLE_CONTACTS                  @"contacts"
#define TABLE_DEPARTMENTS               @"departments"
#define TABLE_GROUPS                    @"groups"
#define TABLE_SESSIONS                  @"sessions"

#define SQL_CREATE_MESSAGES_TABLE              [NSString stringWithFormat:@"CREATE TABLE IF NOT EXISTS %@ (msg_id integer, session_id text, from_user_id text, to_user_id text, content text, status integer, msg_time real, session_type integer, msg_content_type integer, msg_type integer, info text, reserve1 integer, reserve2 text, primary key (msg_id, session_id))", TABLE_MESSAGES]

#define SQL_CREATE_MESSAGES_TABLE_INDEX        [NSString stringWithFormat:@"CREATE INDEX idx_msg_id on %@(msg_id)", TABLE_MESSAGES]


#define SQL_CREATE_DEPARTMENTS_TABLE           [NSString stringWithFormat:@"CREATE TABLE IF NOT EXISTS %@ (ID integer UNIQUE, parent_id integer, title text, status integer, priority integer)", TABLE_DEPARTMENTS]


#define SQL_CREATE_CONTACTS_TABLE              [NSString stringWithFormat:@"CREATE TABLE IF NOT EXISTS %@ (ID text UNIQUE, name text, nick text, avatar text, department text, dept_id text, email text, postion text, telphone text, sex integer, updated real, pyname text)", TABLE_CONTACTS]

#define SQL_CREATE_CONTACTS_TABLE_INDEX        [NSString stringWithFormat:@"CREATE UNIQUE idx_ID on %@(ID)", TABLE_CONTACTS]

#define SQL_CREATE_GROUPS_TABLE                [NSString stringWithFormat:@"CREATE TABLE IF NOT EXISTS %@ (ID text UNIQUE, avatar text, group_type integer, name text, creator_id text, users text, last_msg text, updated real, is_shield integer, version integer)", TABLE_GROUPS]

#define SQL_CREATE_SESSIONS_TABLE              [NSString stringWithFormat:@"CREATE TABLE IF NOT EXISTS %@ (ID text UNIQUE, avatar text, type integer, name text, updated real, is_shield integer, users text, unread_count integer, last_msg text, last_msg_id integer)", TABLE_SESSIONS]


@implementation BTDatabaseUtil
{
    FMDatabase *_database;
    FMDatabaseQueue *_dataBaseQueue;
}

+(instancetype)instance
{
    static BTDatabaseUtil *g_databaseUtil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        g_databaseUtil = [[BTDatabaseUtil alloc] init];
    });
    return g_databaseUtil;
}

-(void)reOpenNewDb
{
    [self openCurrentUserDb];
}

-(id)init
{
    self = [super init];
    if (self)
    {
        [self openCurrentUserDb];
    }
    return self;
}

-(void)openCurrentUserDb
{
    if (_database)
    {
        [_database close];
        _database = nil;
    }
    _dataBaseQueue = [FMDatabaseQueue databaseQueueWithPath:[BTDatabaseUtil dbFilePath]];
    _database = [FMDatabase databaseWithPath:[BTDatabaseUtil dbFilePath]];
    
    if (![_database open])
    {
        BTLog(@"open datebase faild");
    }
    else
    {
        [_dataBaseQueue inDatabase:^(FMDatabase *db) {
            if (![_database tableExists:TABLE_MESSAGES])
            {
                [self createTable:SQL_CREATE_MESSAGES_TABLE];
            }
            if (![_database tableExists:TABLE_DEPARTMENTS])
            {
                [self createTable:SQL_CREATE_DEPARTMENTS_TABLE];
            }
            if (![_database tableExists:TABLE_CONTACTS])
            {
                [self createTable:SQL_CREATE_CONTACTS_TABLE];
            }
            if (![_database tableExists:TABLE_GROUPS])
            {
                [self createTable:SQL_CREATE_GROUPS_TABLE];
            }
            if (![_database tableExists:TABLE_SESSIONS])
            {
                [self createTable:SQL_CREATE_SESSIONS_TABLE];
            }
        }];
    }
}

+(NSString *)dbFilePath
{
    NSString *directorPath = [NSString userExclusiveDirectoryPath];
    NSFileManager *fileManager = [NSFileManager defaultManager];
    
    BOOL isDirectory = NO;
    // 数据库文件是否存在
    BOOL isExiting = [fileManager fileExistsAtPath:directorPath isDirectory:&isDirectory];
    
    if (!(isExiting && isDirectory))
    {
        BOOL createDirectorySuccess = [fileManager createDirectoryAtPath:directorPath
                                             withIntermediateDirectories:YES
                                                              attributes:nil
                                                                   error:nil];
        if (!createDirectorySuccess)
        {
            BTLog(@"创建DB目录失败");
        }
    }
    
    
    NSString *dbPath = [directorPath stringByAppendingPathComponent:[NSString stringWithFormat:@"%@_%@", BTRuntime.user.objId, DB_FILE_NAME]];
    return dbPath;
}

-(BOOL)createTable:(NSString *)sql
{
    BOOL result = NO;
    [_database setShouldCacheStatements:YES];
    NSString *tempSql = [NSString stringWithFormat:@"%@", sql];
    result = [_database executeUpdate:tempSql];
    return result;
}

-(BOOL)clearTable:(NSString *)tableName
{
    BOOL result = NO;
    [_database setShouldCacheStatements:YES];
    NSString *tempSql = [NSString stringWithFormat:@"DELETE FROM %@", tableName];
    result = [_database executeUpdate:tempSql];
    return result;
}

-(BTMessageEntity *)messageFromResult:(FMResultSet *)resultSet
{
    
    NSString *sessionId = [resultSet stringForColumn:@"session_id"];
    NSString *fromUserId = [resultSet stringForColumn:@"from_user_id"];
    NSString *toUserId = [resultSet stringForColumn:@"to_user_id"];
    NSString *content = [resultSet stringForColumn:@"content"];
    NSTimeInterval msgTime = [resultSet doubleForColumn:@"msg_time"];
    MsgType messageType = [resultSet intForColumn:@"msg_type"];
    NSUInteger messageContentType = [resultSet intForColumn:@"msg_content_type"];
    NSUInteger messageId = [resultSet intForColumn:@"msg_id"];
    NSUInteger messageState = [resultSet intForColumn:@"status"];
    NSString *infoString = [resultSet stringForColumn:@"info"];
    
    BTMessageEntity *messageEntity = [[BTMessageEntity alloc] initWithMsgId:messageId
                                                                    msgType:messageType
                                                                    msgTime:msgTime
                                                                  sessionId:sessionId
                                                                   senderId:fromUserId
                                                                 msgContent:content
                                                                   toUserId:toUserId];
    messageEntity.state = messageState;
    messageEntity.msgContentType = messageContentType;
    
    if (infoString)
    {
        NSData *infoData = [infoString dataUsingEncoding:NSUTF8StringEncoding];
        NSDictionary *info = [NSJSONSerialization JSONObjectWithData:infoData options:0 error:nil];
        NSMutableDictionary *mutalInfo = [NSMutableDictionary dictionaryWithDictionary:info];
        messageEntity.info = mutalInfo;
    }
    return messageEntity;
}

-(BTUserEntity *)userFromResult:(FMResultSet *)resultSet
{
    NSMutableDictionary *dic = [NSMutableDictionary new];
    [dic safeSetObject:[resultSet stringForColumn:@"name"] forKey:@"name"];
    [dic safeSetObject:[resultSet stringForColumn:@"nick"] forKey:@"nickName"];
    [dic safeSetObject:[resultSet stringForColumn:@"ID"] forKey:@"userId"];
    [dic safeSetObject:[resultSet stringForColumn:@"department"] forKey:@"department"];
    [dic safeSetObject:[resultSet stringForColumn:@"postion"] forKey:@"position"];
    [dic safeSetObject:[NSNumber numberWithInt:[resultSet intForColumn:@"sex"]] forKey:@"sex"];
    [dic safeSetObject:[resultSet stringForColumn:@"dept_id"] forKey:@"departId"];
    [dic safeSetObject:[resultSet stringForColumn:@"telphone"] forKey:@"telphone"];
    [dic safeSetObject:[resultSet stringForColumn:@"avatar"] forKey:@"avatar"];
    [dic safeSetObject:[resultSet stringForColumn:@"email"] forKey:@"email"];
    [dic safeSetObject:@([resultSet longForColumn:@"updated"]) forKey:@"lastUpdateTime"];
    [dic safeSetObject:[resultSet stringForColumn:@"pyname"] forKey:@"pyname"];
    BTUserEntity *user = [BTUserEntity dicToUserEntity:dic];
    
    return user;
}

-(BTGroupEntity *)groupFromResult:(FMResultSet *)resultSet
{
    NSMutableDictionary *dic = [NSMutableDictionary new];
    [dic safeSetObject:[resultSet stringForColumn:@"name"] forKey:@"name"];
    [dic safeSetObject:[resultSet stringForColumn:@"ID"] forKey:@"groupId"];
    [dic safeSetObject:[resultSet stringForColumn:@"avatar"] forKey:@"avatar"];
    [dic safeSetObject:[NSNumber numberWithInt:[resultSet intForColumn:@"group_type"]] forKey:@"groupType"];
    [dic safeSetObject:@([resultSet longForColumn:@"updated"]) forKey:@"lastUpdateTime"];
    [dic safeSetObject:[resultSet stringForColumn:@"creator_id"] forKey:@"creatorId"];
    [dic safeSetObject:[resultSet stringForColumn:@"users"] forKey:@"users"];
    [dic safeSetObject:[resultSet stringForColumn:@"last_msg"] forKey:@"lastMessage"];
    [dic safeSetObject:[NSNumber numberWithInt:[resultSet intForColumn:@"is_shield"]] forKey:@"isShield"];
    [dic safeSetObject:[NSNumber numberWithInt:[resultSet intForColumn:@"version"]] forKey:@"version"];
    BTGroupEntity *group = [BTGroupEntity dicToGroupEntity:dic];
    
    return group;
}

-(DepartInfo *)departmentFromResult:(FMResultSet *)resultSet
{
    DepartInfo *deaprtment = [[DepartInfo alloc] init];
    [deaprtment setDeptId:[resultSet intForColumn:@"ID"]];
    [deaprtment setParentDeptId:[resultSet intForColumn:@"parent_id"]];
    [deaprtment setPriority:[resultSet intForColumn:@"priority"]];
    [deaprtment setDeptName:[resultSet stringForColumn:@"title"]];
    [deaprtment setDeptStatus:[resultSet intForColumn:@"status"]];
    
    return deaprtment;
}

-(BTSessionEntity *)sessionFromResult:(FMResultSet *)resultSet
{
    BTSessionEntity *session = [[BTSessionEntity alloc] initWithSessionId:[resultSet stringForColumn:@"ID"]
                                                              sessionName:[resultSet stringForColumn:@"name"]
                                                              sessionType:(SessionType)[resultSet intForColumn:@"type"]];
    session.avatar = [resultSet stringForColumn:@"avatar"];
    session.timeInterval = [resultSet longForColumn:@"updated"];
    session.lastMsg = [resultSet stringForColumn:@"last_msg"];
    session.lastMsgId = [resultSet longForColumn:@"last_msg_id"];
    session.unReadMsgCount = [resultSet longForColumn:@"unread_count"];
    
    return session;
}


#pragma mark Message
-(void)loadMessagesBySessionId:(NSString *)sessionId limit:(int)limit offset:(NSInteger)offset completion:(LoadMessagesInSessionCompletion)completion
{
    [_dataBaseQueue inDatabase:^(FMDatabase *db) {
        NSMutableArray *array = [[NSMutableArray alloc] init];
        
        if ([_database tableExists:TABLE_MESSAGES])
        {
            [_database setShouldCacheStatements:YES];
    
            NSString *sqlString = [NSString stringWithFormat:@"SELECT * FROM %@ where session_id = ? ORDER BY msg_time DESC limit ?, ?", TABLE_MESSAGES];
            FMResultSet *result = [_database executeQuery:sqlString, sessionId, [NSNumber numberWithInteger:offset], [NSNumber numberWithInteger:limit]];
            while ([result next])
            {
                BTMessageEntity *message = [self messageFromResult:result];
                [array addObject:message];
            }
            dispatch_async(dispatch_get_main_queue(), ^{
                completion(array, nil);
            });
        }
    }];
}

-(void)loadMessagesBySessionId:(NSString *)sessionId afterMessage:(BTMessageEntity *)message completion:(LoadMessagesInSessionCompletion)completion
{
    [_dataBaseQueue inDatabase:^(FMDatabase *db) {
        NSMutableArray *array = [[NSMutableArray alloc] init];
        if ([_database tableExists:TABLE_MESSAGES])
        {
            [_database setShouldCacheStatements:YES];
            
            NSString *sqlString = [NSString stringWithFormat:@"select * from %@ where session_id = ? AND msg_id >= ? order by msg_time DESC, row_id DESC", TABLE_MESSAGES];
            FMResultSet *result = [_database executeQuery:sqlString, sessionId, message.msgId];
            while ([result next])
            {
                BTMessageEntity *message = [self messageFromResult:result];
                [array addObject:message];
            }
            dispatch_async(dispatch_get_main_queue(), ^{
                completion(array, nil);
            });
        }
    }];
}

-(void)getLastestMessageBySessionId:(NSString *)sessionId completion:(GetLastestMessageCompletionWithError)completion
{
    [_dataBaseQueue inDatabase:^(FMDatabase *db) {
        if ([_database tableExists:TABLE_MESSAGES])
        {
            [_database setShouldCacheStatements:YES];
            
            NSString *sqlString = [NSString stringWithFormat:@"SELECT * FROM %@ where session_id = ? and status = 2 ORDER BY msg_id DESC limit 0, 1", TABLE_MESSAGES];
            FMResultSet *result = [_database executeQuery:sqlString, sessionId];
            BTMessageEntity *message = nil;
            while ([result next])
            {
                message = [self messageFromResult:result];
                dispatch_async(dispatch_get_main_queue(), ^{
                    completion(message, nil);
                });
                
                break;
            }
            if (message == nil)
            {
                completion(message, nil);
            }
        }
    }];
}

-(void)getMessageCountBySessionId:(NSString *)sessionId completion:(GetMessageCountCompletion)completion
{
    [_dataBaseQueue inDatabase:^(FMDatabase *db) {
        if ([_database tableExists:TABLE_MESSAGES])
        {
            [_database setShouldCacheStatements:YES];
            
            NSString *sqlString = [NSString stringWithFormat:@"SELECT COUNT(*) FROM %@ where session_id = ?", TABLE_MESSAGES];
            FMResultSet *result = [_database executeQuery:sqlString, sessionId];
            int count = 0;
            while ([result next])
            {
                count = [result intForColumnIndex:0];
            }
            dispatch_async(dispatch_get_main_queue(), ^{
                completion(count);
            });
        }
    }];
}

-(void)insertMessages:(NSArray *)messages
              success:(void(^)())success
              failure:(void(^)(NSString *error))failure
{
    [_dataBaseQueue inDatabase:^(FMDatabase *db) {
        [_database beginTransaction];
        __block BOOL isRollBack = NO;
        @try
        {
            [messages enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
                BTMessageEntity *message = (BTMessageEntity *)obj;
                NSString *sql = [NSString stringWithFormat:@"INSERT OR REPLACE INTO %@ VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", TABLE_MESSAGES];
                NSData *infoJsonData = [NSJSONSerialization dataWithJSONObject:message.info options:NSJSONWritingPrettyPrinted error:nil];
                NSString *json = [[NSString alloc] initWithData:infoJsonData encoding:NSUTF8StringEncoding];
                
                BOOL result = [_database executeUpdate:sql,
                                                       @(message.msgId),
                                                       message.sessionId,
                                                       message.senderId,
                                                       message.toUserId,
                                                       message.msgContent,
                                                       @(message.state),
                                                       @(message.msgTime),
                                                       @(1),
                                                       @(message.msgContentType),
                                                       @(message.msgType),
                                                       json,
                                                       @(0),
                                                       @""];
                                        
                if (!result)
                {
                    isRollBack = YES;
                    *stop = YES;
                }
            }];
        }
        @catch (NSException *exception)
        {
            [_database rollback];
            failure(@"插入数据失败");
        }
        @finally
        {
            if (isRollBack)
            {
                [_database rollback];
                BTLog(@"insert to database failure content");
                failure(@"插入数据失败");
            }
            else
            {
                [_database commit];
                success();
            }
        }
    }];
}

-(void)deleteMessagesBySessionId:(NSString *)sessionId completion:(DeleteSessionCompletion)completion
{
    [_dataBaseQueue inDatabase:^(FMDatabase *db) {
        NSString *sqlString = [NSString stringWithFormat:@"DELETE FROM %@ WHERE session_id = ?", TABLE_MESSAGES];
        BOOL result = [_database executeUpdate:sqlString, sessionId];
        dispatch_async(dispatch_get_main_queue(), ^{
            completion(result);
        });
    }];
}

-(void)deleteMessages:(BTMessageEntity *)message completion:(DeleteSessionCompletion)completion
{
    [_dataBaseQueue inDatabase:^(FMDatabase *db) {
        NSString *sqlString = [NSString stringWithFormat:@"DELETE FROM %@ WHERE msg_id = ?", TABLE_MESSAGES];
        BOOL result = [_database executeUpdate:sqlString, @(message.msgId)];
        dispatch_async(dispatch_get_main_queue(), ^{
            completion(result);
        });
    }];
}

-(void)updateMessage:(BTMessageEntity *)message completion:(UpdateMessageCompletion)completion
{
    [_dataBaseQueue inDatabase:^(FMDatabase *db) {
        NSString *sqlString = [NSString stringWithFormat:@"UPDATE %@ set session_id = ?, from_user_id = ?, to_user_id = ?, content = ?, status = ?, msg_time = ?, session_type = ?, msg_type = ?, msg_content_type = ?, info = ? where msg_id = ?", TABLE_MESSAGES];
        
        NSData *infoJsonData = [NSJSONSerialization dataWithJSONObject:message.info options:NSJSONWritingPrettyPrinted error:nil];
        NSString *json = [[NSString alloc] initWithData:infoJsonData encoding:NSUTF8StringEncoding];
        BOOL result = [_database executeUpdate:sqlString,
                                               message.sessionId,
                                               message.senderId,
                                               message.toUserId,
                                               message.msgContent,
                                               @(message.state),
                                               @(message.msgTime),
                                               @(message.sessionType),
                                               @(message.msgType),
                                               @(message.msgContentType),
                                               json,
                                               @(message.msgId)];
        
        dispatch_async(dispatch_get_main_queue(), ^{
            completion(result);
        });
    }];
}

-(void)insertDepartments:(NSArray *)departments completion:(void(^)(NSError *error))completion
{
    [_dataBaseQueue inDatabase:^(FMDatabase *db) {
        [_database beginTransaction];
        __block BOOL isRollBack = NO;
        @try
        {
            [departments enumerateObjectsUsingBlock:^(DepartInfo *obj, NSUInteger idx, BOOL *stop) {
                NSString *sqlString = [NSString stringWithFormat:@"INSERT OR REPLACE INTO %@ VALUES(?, ?, ?, ?, ?)", TABLE_DEPARTMENTS];
                BOOL result = [_database executeUpdate:sqlString,
                                                       @(obj.deptId),
                                                       @(obj.parentDeptId),
                                                       obj.deptName,
                                                       @(obj.deptStatus),
                                                       @(obj.priority)];
                if (!result)
                {
                    isRollBack = YES;
                    *stop = YES;
                }
            }];
        }
        @catch (NSException *exception)
        {
            [_database rollback];
        }
        @finally
        {
            if (isRollBack)
            {
                [_database rollback];
                BTLog(@"insert department faild");
                NSError *error = [NSError errorWithDomain:@"insert department faild" code:0 userInfo:nil];
                completion(error);
            }
            else
            {
                [_database commit];
                completion(nil);
            }
        }
    }];
}

-(void)getDepartmentByDepartmentId:(NSString *)departmentId completion:(void(^)(DepartInfo *department))completion
{
    [_dataBaseQueue inDatabase:^(FMDatabase *db) {
        if ([_database tableExists:TABLE_DEPARTMENTS])
        {
            [_database setShouldCacheStatements:YES];
            
            NSString *sqlString = [NSString stringWithFormat:@"SELECT * FROM %@ where ID = ?", TABLE_DEPARTMENTS];
            FMResultSet *result = [_database executeQuery:sqlString, departmentId];
            DepartInfo *department = nil;
            while ([result next])
            {
                department = [self departmentFromResult:result];
            }
            dispatch_async(dispatch_get_main_queue(), ^{
                completion(department);
            });
        }
    }];
}

-(void)insertContacts:(NSArray *)contacts completion:(void(^)(NSError *error))completion
{
    BTLog(@"prepare to insert contacts into db");
    [_dataBaseQueue inDatabase:^(FMDatabase *db) {
        [_database beginTransaction];
        __block BOOL isRollBack = NO;
        @try
        {
            [contacts enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
                BTUserEntity *user = (BTUserEntity *)obj;
                BTLog(@"insert contact into db: %@", user.name);
                user.department= @" ";
                user.position = @" ";
                NSString *sqlString = [NSString stringWithFormat:@"INSERT OR REPLACE INTO %@ VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", TABLE_CONTACTS];
                BOOL result = [_database executeUpdate:sqlString,
                                                       user.objId,
                                                       user.name,
                                                       user.nick,
                                                       user.avatar,
                                                       user.department,
                                                       @(user.departId),
                                                       user.email,
                                                       user.position,
                                                       user.telphone,
                                                       @(user.sex),
                                                       user.lastUpdateTime,
                                                       user.pyname];
                    
                    if (!result)
                    {
                        isRollBack = YES;
                        *stop = YES;
                        BTLog(@"insert contact failed");
                    }
            }];
            
        }
        @catch (NSException *exception)
        {
            [_database rollback];
        }
        @finally
        {
            if (isRollBack)
            {
                [_database rollback];
                BTLog(@"insert contacts faild");
                NSError *error = [NSError errorWithDomain:@"insert contacts faild" code:0 userInfo:nil];
                completion(error);
            }
            else
            {
                [_database commit];
                completion(nil);
            }
        }
    }];
}

-(void)getAllContacts:(LoadAllContactsComplection)completion
{
    BTLog(@"prepare to get all contacts from db");
    [_dataBaseQueue inDatabase:^(FMDatabase *db) {
        if ([_database tableExists:TABLE_CONTACTS])
        {
            [_database setShouldCacheStatements:YES];
            NSMutableArray *array = [[NSMutableArray alloc] init];
            NSString *sqlString = [NSString stringWithFormat:@"SELECT * FROM %@", TABLE_CONTACTS];
            FMResultSet *result = [_database executeQuery:sqlString];
            BTUserEntity *user = nil;
            while ([result next])
            {
                BTLog(@"find contact record in db");
                user = [self userFromResult:result];
                // TODO: what does 3 mean
                if (user.userStatus != 3)
                {
                    [array addObject:user];
                }
            }
            dispatch_async(dispatch_get_main_queue(), ^{
                BTLog(@"get all contacts from db completion, currently %d contacts", (unsigned int)[array count]);
                completion(array, nil);
            });
        }
    }];
}

-(void)getContactByUserId:(NSString *)userId completion:(void(^)(BTUserEntity *user))completion
{
    [_dataBaseQueue inDatabase:^(FMDatabase *db) {
        if ([_database tableExists:TABLE_CONTACTS])
        {
            [_database setShouldCacheStatements:YES];
            
            NSString *sqlString = [NSString stringWithFormat:@"SELECT * FROM %@ where ID = ?", TABLE_CONTACTS];
            FMResultSet *result = [_database executeQuery:sqlString, userId];
            BTUserEntity *user = nil;
            while ([result next])
            {
                user = [self userFromResult:result];
            }
            dispatch_async(dispatch_get_main_queue(), ^{
                completion(user);
            });
        }
    }];
}

-(void)loadGroupByGroupIdCompletion:(NSString *)groupId completion:(void(^)(NSArray *array, NSError *error))completion
{
    [_dataBaseQueue inDatabase:^(FMDatabase *db) {
        NSMutableArray *array = [[NSMutableArray alloc] init];
        if ([_database tableExists:TABLE_GROUPS])
        {
            [_database setShouldCacheStatements:YES];
            
            NSString *sqlString = [NSString stringWithFormat:@"SELECT * FROM %@ where ID = ?", TABLE_GROUPS];
            FMResultSet *result = [_database executeQuery:sqlString, groupId];
            while ([result next])
            {
                BTGroupEntity *group = [self groupFromResult:result];
                [array addObject:group];
            }
            dispatch_async(dispatch_get_main_queue(), ^{
                completion(array, nil);
            });
        }
    }];
}

-(void)loadAllGroupsCompletion:(void(^)(NSArray *array, NSError *error))completion
{
    [_dataBaseQueue inDatabase:^(FMDatabase *db) {
        NSMutableArray *array = [[NSMutableArray alloc] init];
        if ([_database tableExists:TABLE_GROUPS])
        {
            [_database setShouldCacheStatements:YES];
            
            NSString *sqlString = [NSString stringWithFormat:@"SELECT * FROM %@", TABLE_GROUPS];
            FMResultSet *result = [_database executeQuery:sqlString];
            while ([result next])
            {
                BTGroupEntity *group = [self groupFromResult:result];
                [array addObject:group];
            }
            dispatch_async(dispatch_get_main_queue(), ^{
                completion(array, nil);
            });
        }
    }];
}

-(void)updateGroup:(BTGroupEntity *)group completion:(void(^)(NSError *error))completion
{
    [_dataBaseQueue inDatabase:^(FMDatabase *db) {
        [_database beginTransaction];
        __block BOOL isRollBack = NO;
        @try
        {
            NSString *sqlString = [NSString stringWithFormat:@"INSERT OR REPLACE INTO %@ VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", TABLE_GROUPS];
            NSString *users = @"";
            if ([group.groupUserIds count] > 0)
            {
                users = [group.groupUserIds componentsJoinedByString:@"-"];
            }
            
            BOOL result = [_database executeUpdate:sqlString,
                                                   group.objId,
                                                   group.avatar,
                                                   @(group.groupType),
                                                   group.name,
                                                   group.groupCreatorId,
                                                   users,
                                                   group.lastMsg,
                                                   @(group.lastUpdateTime),
                                                   @(group.isShield),
                                                   @(group.objectVersion)];
            if (!result)
            {
                isRollBack = YES;
            }
        }
        @catch (NSException *exception)
        {
            [_database rollback];
        }
        @finally
        {
            if (isRollBack)
            {
                [_database rollback];
                BTLog(@"update group failed");
                NSError *error = [NSError errorWithDomain:@"update group failed" code:0 userInfo:nil];
                completion(error);
            }
            else
            {
                [_database commit];
                completion(nil);
            }
        }
    }];
}

-(void)updateSession:(BTSessionEntity *)session completion:(void(^)(NSError *error))completion
{
    [_dataBaseQueue inDatabase:^(FMDatabase *db) {
        [_database beginTransaction];
        __block BOOL isRollBack = NO;
        @try
        {
            NSString *sqlString = [NSString stringWithFormat:@"INSERT OR REPLACE INTO %@ VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", TABLE_SESSIONS];
            NSString *users = @"";
            if ([session.sessionUsers count] > 0)
            {
                users = [session.sessionUsers componentsJoinedByString:@"-"];
            }
            
            BOOL result = [_database executeUpdate:sqlString,
                                                   session.sessionId,
                                                   session.avatar,
                                                   @(session.sessionType),
                                                   session.name,
                                                   @(session.timeInterval),
                                                   @(session.isShield),
                                                   users,
                                                   @(session.unReadMsgCount),
                                                   session.lastMsg,
                                                   @(session.lastMsgId)];
            if (!result)
            {
                isRollBack = YES;
            }
        }
        @catch (NSException *exception)
        {
            [_database rollback];
        }
        @finally
        {
            if (isRollBack)
            {
                [_database rollback];
                BTLog(@"insert to database failure content");
                NSError *error = [NSError errorWithDomain:@"failed to update session" code:0 userInfo:nil];
                completion(error);
            }
            else
            {
                [_database commit];
                completion(nil);
            }
        }
    }];
}

-(void)loadAllSessionsCompletion:(LoadAllSessionsComplection)completion
{
    [_dataBaseQueue inDatabase:^(FMDatabase *db) {
        NSMutableArray *array = [[NSMutableArray alloc] init];
        if ([_database tableExists:TABLE_SESSIONS])
        {
            [_database setShouldCacheStatements:YES];
            
            NSString *sqlString = [NSString stringWithFormat:@"SELECT * FROM %@ order BY updated DESC", TABLE_SESSIONS];
            FMResultSet *result = [_database executeQuery:sqlString];
            while ([result next])
            {
                BTSessionEntity *session = [self sessionFromResult:result];
                [array addObject:session];
            }
            dispatch_async(dispatch_get_main_queue(), ^{
                completion(array, nil);
            });
        }
    }];
}

-(void)removeSessionBySessionId:(NSString *)sessionId
{
    [_dataBaseQueue inDatabase:^(FMDatabase *db) {
        NSString *sqlString = [NSString stringWithFormat:@"DELETE FROM %@ WHERE ID = ?", TABLE_SESSIONS];
        BOOL result = [_database executeUpdate:sqlString, sessionId];
        if (result)
        {
            NSString *sqlString = [NSString stringWithFormat:@"DELETE FROM %@ WHERE session_id = ?", TABLE_MESSAGES];
            BOOL result = [_database executeUpdate:sqlString, sessionId];
            BTLog(@"%@", [NSNumber numberWithBool:result]);
        }
    }];
}

-(void)getAllDepartmentsCompletion:(LoadAllContactsComplection)completion
{
    [_dataBaseQueue inDatabase:^(FMDatabase *db) {
        if ([_database tableExists:TABLE_DEPARTMENTS])
        {
            [_database setShouldCacheStatements:YES];
            
            NSMutableArray *array = [[NSMutableArray alloc] init];
            NSString *sqlString = [NSString stringWithFormat:@"SELECT * FROM %@", TABLE_DEPARTMENTS];
            FMResultSet *result = [_database executeQuery:sqlString];
            DepartInfo *department = nil;
            while ([result next])
            {
                department = [self departmentFromResult:result];
                [array addObject:department];
            }
            dispatch_async(dispatch_get_main_queue(), ^{
                completion(array, nil);
            });
        }
    }];
}

-(void)getDepartmentTitleByDepartmentId:(NSInteger)departmentId completion:(void(^)(NSString *title))completion
{
    [_dataBaseQueue inDatabase:^(FMDatabase *db) {
        if ([_database tableExists:TABLE_DEPARTMENTS])
        {
            [_database setShouldCacheStatements:YES];
            
            NSString *sqlString = [NSString stringWithFormat:@"SELECT * FROM %@ where ID = ?", TABLE_DEPARTMENTS];
            FMResultSet *result = [_database executeQuery:sqlString, @(departmentId)];
            DepartInfo *department = nil;
            while ([result next])
            {
                department = [self departmentFromResult:result];
            }
            dispatch_async(dispatch_get_main_queue(), ^{
                completion(department.deptName);
            });
        }
    }];
}
@end
