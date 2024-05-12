//
//  BTDbManager.h
//

#import <Foundation/Foundation.h>
#import "BTDbHelper.h"
#import "BTUserEntity.h"
#import "BTPublicDefine.h"


enum
{
    USER,
    MESSAGE,
    SESSION,
    GROUP
} BTDbType;


@interface BTDbManager : NSObject
AS_SINGLETON(BTDbManager)

-(void)updateUser:(BTUserEntity *)user;
-(void)insertUsers:(NSArray *)array;
-(NSMutableArray *)getAllUsers;
-(void)setUsersVersion:(NSUInteger)version;
-(NSInteger)getUsersVersion;

@end
