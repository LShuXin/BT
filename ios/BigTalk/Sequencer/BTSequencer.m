//
//  BTSequencer.m
//

#import "BTSequencer.h"


@interface BTSequencer()

@property (nonatomic, strong)NSMutableArray *steps;

@end


@implementation BTSequencer

-(NSMutableArray *)steps
{
    if (!_steps)
    {
        _steps = [NSMutableArray new];
    }
    return _steps;
}

-(void)run
{
    [self runNextStepWithResult:nil];
}

-(void)enqueueStep:(SequencerStep)step
{
    [self.steps addObject:[step copy]];
}

-(SequencerStep)dequeueNextStep
{
    SequencerStep step = [self.steps objectAtIndex:0];
    [self.steps removeObjectAtIndex:0];
    return step;
}

-(void)runNextStepWithResult:(id)result
{
    if ([self.steps count] <= 0)
    {
        return;
    }
    
    SequencerStep step = [self dequeueNextStep];
    
    step(result, ^(id nextRresult) {
        [self runNextStepWithResult:nextRresult];
    });
}

@end
