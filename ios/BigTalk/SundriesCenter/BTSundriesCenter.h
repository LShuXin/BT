//
//  BTSundriesCenter.h
//

#import <Foundation/Foundation.h>


typedef void(^Task)();

@interface BTSundriesCenter : NSObject

@property(nonatomic,readonly)dispatch_queue_t serialQueue;
@property(nonatomic,readonly)dispatch_queue_t parallelQueue;

+(instancetype)instance;
-(void)pushTaskToSerialQueue:(Task)task;
-(void)pushTaskToParallelQueue:(Task)task;
-(void)pushTaskToSynchronizationSerialQueue:(Task)task;
@end
