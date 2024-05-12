//
//  BTAnalysisImage.h
//

#import <Foundation/Foundation.h>
#import "BTMessageEntity.h"


@interface BTAnalysisImage : NSObject
+(void)analysisImage:(BTMessageEntity *)message completion:(void(^)(NSMutableArray *array))completion;
@end
