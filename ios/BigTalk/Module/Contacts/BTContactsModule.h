//
//  BTContactsModule.h
//

#import <Foundation/Foundation.h>
#import "BTUserEntity.h"


@interface BTContactsModule : NSObject

@property(strong)NSMutableArray *groups;
@property(strong)NSMutableDictionary *department;
@property(assign)int contactsCount;

-(NSMutableDictionary *)sortByContactFirstLetter;
-(NSMutableDictionary *)sortByDepartment;
+(void)favContact:(BTUserEntity *)user;
+(NSArray *)getFavContact;
-(BOOL)isInFavContactList:(BTUserEntity *)user;
+(void)getDepartmentData:(void(^)(id response))completion;

@end
