//
//  BTChattingEditModule.h
//

#import <Foundation/Foundation.h>

@interface BTChattingEditModule : NSObject
-(void)removePersonFromGroup:(NSArray *)userIds completion:(void(^)(BOOL success))completion;
-(instancetype)initChattingEditModel:(NSString *)sessionId;
@end
