//
//  BTChattingEditModule.m
//

#import "BTChattingEditModule.h"
#import "BTCreateGroupAPI.h"


@interface BTChattingEditModule(Private)
@property(nonatomic, strong)NSMutableArray *group;
@end


@implementation BTChattingEditModule

-(instancetype)initChattingEditModel:(NSString *)sessionId
{
    self = [super init];
    if (self)
    {
        
    }
    return self;
}

-(void)removePersonFromGroup:(NSArray *)userIds completion:(void(^)(BOOL success))completion
{
    BTLog(@"remove persion from group need to be develop");
}

@end
