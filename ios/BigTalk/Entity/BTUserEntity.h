//
//  BTUserEntity.h
//

#import <Foundation/Foundation.h>
#import "BTBaseEntity.h"
#import "BTDepartmentEntity.h"
#import "IMBaseDefine.pbobjc.h"

@interface BTUserEntity : BTBaseEntity

@property(nonatomic, strong)NSString *name;         // 用户名
@property(nonatomic, strong)NSString *nick;         // 用户昵称
@property(nonatomic, strong)NSString *avatar;       // 用户头像
@property(nonatomic, strong)NSString *department;   // 用户部门
@property(strong)NSString *position;
@property(assign)NSInteger sex;
@property(assign)NSInteger departId;
@property(strong)NSString *telphone;
@property(strong)NSString *email;
@property(strong)NSString *pyname;
@property(assign)NSInteger userStatus;

-(id)initWithUserId:(NSString *)userId
               name:(NSString *)name
               nick:(NSString *)nick
             avatar:(NSString *)avatar
           userRole:(NSInteger)userRole
        userUpdated:(NSUInteger)updated;

+(id)dicToUserEntity:(NSDictionary *)dic;
+(NSMutableDictionary *)userToDic:(BTUserEntity *)user;
-(void)sendEmail;
-(void)callPhoneNum;
-(NSString *)getAvatarUrl;
-(NSString *)getAvatarPreImageUrl;
-(id)initWithPbData:(UserInfo *)pbUser;
+(UInt32)localUserIdToPbUserId:(NSString *)localId;
+(NSString *)pbUserIdToLocalUserId:(NSUInteger)pbId;
-(void)updateLastUpdateTimeToDb;

@end
