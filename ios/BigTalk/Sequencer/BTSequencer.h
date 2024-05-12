//
//  BTSequencer.h

#import <Foundation/Foundation.h>


typedef void(^SequencerCompletion)(id result);
typedef void(^SequencerStep)(id result, SequencerCompletion completion);


@interface BTSequencer : NSObject {}

-(void)run;
-(void)enqueueStep:(SequencerStep)step;

@end
