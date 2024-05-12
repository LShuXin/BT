//
//  BTPhoto.m
//

#import "BTPhoto.h"


@implementation BTPhoto

-(instancetype)init
{
    self = [super init];
    if (self)
    {
        self.localPath = nil;
        self.resultUrl = nil;
        self.imageRef = nil;
        self.image = nil;
    }
    return self;
}

-(void)dealloc
{
    self.localPath = nil;
    self.resultUrl = nil;
    CGImageRelease(self.imageRef);
    self.image = nil;
}

@end
