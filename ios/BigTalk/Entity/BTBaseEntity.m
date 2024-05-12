//
//  BTBaseEntity.m
//

#import "BTBaseEntity.h"


@implementation BTBaseEntity

-(NSUInteger)getPbId
{
    NSArray *array = [self.objId componentsSeparatedByString:@"_"];
    if (array[1])
    {
        return (unsigned long)array[1];
    }
    return 0;
}

@end
