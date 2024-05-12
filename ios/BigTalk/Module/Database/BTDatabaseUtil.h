//
//  BTDatabaseUtil.h
//

#import <Foundation/Foundation.h>
#import <fmdb/FMDB.h>


@class BTDepartmentEntity;
@class BTMessageEntity;
@class BTGroupEntity;
@class BTSessionEntity;
@class BTUserEntity;


@interface BTDatabaseUtil : NSObject
@property(strong)NSString *recentSession;
@property(nonatomic, readonly)dispatch_queue_t databaseMessageQueue;
+(instancetype)instance;
-(void)openCurrentUserDb;
@end


typedef void(^LoadMessagesInSessionCompletion)(NSArray *messages, NSError *error);
typedef void(^GetMessageCountCompletion)(NSInteger count);
typedef void(^DeleteSessionCompletion)(BOOL success);
typedef void(^GetLastestMessageCompletionWithError)(BTMessageEntity *message, NSError *error);
typedef void(^UpdateMessageCompletion)(BOOL result);


@interface BTDatabaseUtil(Message)

-(void)loadMessagesBySessionId:(NSString *)sessionId
                         limit:(int)limit
                        offset:(NSInteger)index
                    completion:(LoadMessagesInSessionCompletion)completion;

-(void)loadMessagesBySessionId:(NSString *)sessionId
                    afterMessage:(BTMessageEntity *)message
                      completion:(LoadMessagesInSessionCompletion)completion;

-(void)getLastestMessageBySessionId:(NSString *)sessionId
                        completion:(GetLastestMessageCompletionWithError)completion;

-(void)getMessageCountBySessionId:(NSString *)sessionId
                       completion:(GetMessageCountCompletion)completion;

/**
 *  需要用户必须在线，避免插入离线时阅读的消息
 */
-(void)insertMessages:(NSArray *)messages
               success:(void(^)())success
               failure:(void(^)(NSString *error))failure;

-(void)deleteMessagesUseSessionId:(NSString *)sessionId
                    completion:(DeleteSessionCompletion)completion;

-(void)updateMessage:(BTMessageEntity *)message
                    completion:(UpdateMessageCompletion)completion;
@end


typedef void(^LoadRecentContactsComplection)(NSArray *contacts, NSError *error);
typedef void(^LoadAllContactsComplection)(NSArray *contacts, NSError *error);
typedef void(^LoadAllSessionsComplection)(NSArray *session, NSError *error);
typedef void(^UpdateRecentContactsComplection)(NSError *error);
typedef void(^InsertRecentContactsComplection)(NSError *error);


@interface BTDatabaseUtil(Users)
-(void)loadContactsCompletion:(LoadRecentContactsComplection)completion;
-(void)updateContacts:(NSArray *)contacts completion:(UpdateRecentContactsComplection)completion;
-(void)insertContacts:(NSArray *)contacts completion:(InsertRecentContactsComplection)completion;
-(void)insertDepartments:(NSArray *)departments completion:(InsertRecentContactsComplection)completion;
-(void)getDepartmentByDepartmentId:(NSString *)departmentId completion:(void(^)(DepartInfo *department))completion;
-(void)insertContacts:(NSArray *)contacts completion:(InsertRecentContactsComplection)completion;
-(void)getAllContacts:(LoadAllContactsComplection)completion;
-(void)getContactByUserId:(NSString *)userId completion:(void(^)(BTUserEntity *user))completion;
-(void)updateRecentGroup:(BTGroupEntity *)group completion:(InsertRecentContactsComplection)completion;
-(void)updateSession:(BTSessionEntity *)session completion:(InsertRecentContactsComplection)completion;
-(void)loadAllGroupsCompletion:(LoadRecentContactsComplection)completion;
-(void)loadAllSessionsCompletion:(LoadAllSessionsComplection)completion;
-(void)removeSession:(NSString *)sessionId;
-(void)deleteMesages:(BTMessageEntity *)message completion:(DeleteSessionCompletion)completion;
-(void)loadGroupByGroupIdCompletion:(NSString *)groupId completion:(LoadRecentContactsComplection)completion;
// TODO
-(void)getAllDepartmentsCompletion:(LoadAllContactsComplection)completion;
-(void)getDepartmentTitleByDepartmentId:(NSInteger)departmentId completion:(void(^)(NSString *title))completion;
@end
