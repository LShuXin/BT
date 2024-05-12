//
//  BTDepartmentEntity.m
//

#import "BTDepartmentEntity.h"


@implementation BTDepartmentEntity

-(instancetype)init
{
    self = [super init];
    if (self)
    {
        self.id = 0;
        self.parentId = 0;
        self.title = @"";
        self.description = @"";
        self.leader = @"";
        self.count = 0;

    }
    return self;
}

+(id)departmentFromDic:(DepartInfo*)info
{
    return info;
}

@end
