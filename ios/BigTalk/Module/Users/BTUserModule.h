//
//  BTUserModule.h
//

#import <Foundation/Foundation.h>
#import "BTUserEntity.h"


typedef void(^LoadRecentUsersCompletion)();


@interface BTUserModule : NSObject

@property(nonatomic, strong)NSString *currentUserId;
@property(nonatomic, strong)NSMutableDictionary *recentUsers;

+(instancetype)shareInstance;
-(void)addMaintanceUser:(BTUserEntity *)user;
-(void)getUserByUserId:(NSString *)userId completion:(void(^)(BTUserEntity *user))completion;
-(void)addRecentUser:(BTUserEntity *)user;
// 加载本地最近联系人
-(void)loadAllRecentUsers:(LoadRecentUsersCompletion)completion;
-(void)clearRecentUser;
-(NSArray *)getAllMaintanceUser;

@end
