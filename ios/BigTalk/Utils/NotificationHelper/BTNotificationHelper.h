//
//  BTNotificationHelper.h
//  TeamTalk
//
//  Created by apple on 2024/4/14.
//  Copyright © 2024 Michael Hu. All rights reserved.
//

#ifndef BTNotificationHelper_h
#define BTNotificationHelper_h


@interface BTNotificationHelper : NSObject
+(void)postNotification:(NSString *)notification userInfo:(NSDictionary *)userInfo object:(id)object;
@end


#endif /* BTNotificationHelper_h */
