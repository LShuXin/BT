//
//  BTThreadPublicDefine.h
//  tt_ios
//
//  Created by LShuXin on 2023/7/15.
//

#ifndef BTThreadPublicDefine_h
#define BTThreadPublicDefine_h


#define BTDispatchMainSyncSafe(block) \
if ([NSThread isMainThread]) { \
block(); \
} else { \
dispatch_sync(dispatch_get_main_queue(), block); \
}

#define BTDispatchAsyncMain(block) \
dispatch_async(dispatch_get_main_queue(), block)


#endif /* BTThreadPublicDefine_h */
