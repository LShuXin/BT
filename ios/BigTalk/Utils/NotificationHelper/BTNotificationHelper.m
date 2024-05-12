//
//  BTNotificationHelper.m
//  TeamTalk
//
//  Created by apple on 2024/4/14.
//  Copyright © 2024 Michael Hu. All rights reserved.
//


#import "BTNotificationHelper.h"


@implementation BTNotificationHelper

+(void)postNotification:(NSString *)notification
               userInfo:(NSDictionary *)userInfo
                 object:(id)object
{
    dispatch_async(dispatch_get_main_queue(), ^{
        [[NSNotificationCenter defaultCenter] postNotificationName:notification object:object userInfo:userInfo];
    });
}

@end
