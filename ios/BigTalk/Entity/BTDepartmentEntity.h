//
//  BTDepartmentEntity.h
//

#import <Foundation/Foundation.h>
#import "IMBaseDefine.pbobjc.h"


@interface BTDepartmentEntity : NSObject

@property(assign)NSInteger id;
@property(assign)NSInteger parentId;
@property(strong)NSString *title;
@property(strong)NSString *description;
@property(strong)NSString *leader;
@property(assign)NSInteger priority;
@property(assign)NSInteger count;

+(id)departmentFromDic:(DepartInfo*)dic;

@end
