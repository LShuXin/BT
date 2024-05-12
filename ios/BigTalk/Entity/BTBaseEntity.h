//
//  BTBaseEntity.h
//

#import <Foundation/Foundation.h>


@interface BTBaseEntity : NSObject

@property(assign)long lastUpdateTime;
@property(copy)NSString *objId;
@property(assign)NSInteger objectVersion;

-(NSUInteger)getPbId;

@end
