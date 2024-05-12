//
//  BTSearchModule.h
//

#import <Foundation/Foundation.h>

typedef void(^SearchCompletion)(NSArray* result, NSError* error);

@interface BTSearchModule : NSObject
+(instancetype)instance;
-(void)searchContent:(NSString *)content completion:(SearchCompletion)completion;
-(void)searchContent:(NSString *)content inRange:(NSArray *)ranges completion:(SearchCompletion)completion;
-(void)searchDepartment:(NSString *)content completion:(SearchCompletion)completion;
@end
