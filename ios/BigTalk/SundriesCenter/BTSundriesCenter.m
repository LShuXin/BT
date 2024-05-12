//
//  BTSundriesCenter.m
//

#import "BTSundriesCenter.h"


@implementation BTSundriesCenter

+(instancetype)instance
{
    static BTSundriesCenter *g_SundriesCenter;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        g_SundriesCenter = [[BTSundriesCenter alloc] init];
    });
    return g_SundriesCenter;
}

-(id)init
{
    self = [super init];
    if (self)
    {
        _serialQueue = dispatch_queue_create("com.lsx.bigtalk.SundriesSerial", NULL);
        _parallelQueue = dispatch_queue_create("com.lsx.bigtalk.SundriesParallel", NULL);
    }
    return self;
}

-(void)pushTaskToSerialQueue:(Task)task
{
    dispatch_async(self.serialQueue, ^{
        task();
    });
}

-(void)pushTaskToParallelQueue:(Task)task
{
    dispatch_async(self.parallelQueue, ^{
        task();
    });
}

-(void)pushTaskToSynchronizationSerialQueue:(Task)task
{
    dispatch_sync(self.serialQueue, ^{
        task();
    });
}

@end
