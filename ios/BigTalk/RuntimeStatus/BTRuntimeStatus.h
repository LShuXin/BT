//
//  BTRuntimeStatus.h
//

#import <Foundation/Foundation.h>
#import "BTUserEntity.h"
#import "BTPublicDefine.h"


#define BTRuntime [BTRuntimeStatus instance]


@interface BTRuntimeStatus : NSObject

@property(strong)BTUserEntity *user;
@property(strong)NSMutableArray *isFixedArray;
@property(strong)NSString *sessionId;
@property(assign)int groupCount;
@property(copy)NSString *msfs;
@property(copy)NSString *token;
@property(copy)NSString *userId;
@property(strong)NSString *username;
@property(copy)NSString *pushToken;

+(instancetype)instance;

-(void)insertToFixedTop:(NSString *)idString;
-(void)removeFromFixedTop:(NSString *)idString;
-(BOOL)isInFixedTop:(NSString *)idString;
-(NSUInteger)getFixedTopCount;
-(BOOL)isInShielding:(NSString *)idString;
-(void)removeFromShieldingById:(NSString *)idString;
-(void)addToShielding:(NSString *)idString;
-(void)showAlertView:(NSString *)title description:(NSString *)content;
-(void)updateData;
-(NSUInteger)convertLocalIdToPbId:(NSString *)sessionId;
-(NSString *)convertPbIdToLocalId:(NSUInteger)pbId sessionType:(SessionType)sessionType;

// TODO: rename to LOCAL_MSG_END_ID ?
#define LOCAL_MSG_BEGIN_ID 1000000

@end
