//
//  BTPhotosCache.h
//

#import <Foundation/Foundation.h>


typedef void(^cacheblock)(BOOL isFinished);

@interface BTPhotosCache : NSObject

// cache statics: number of images + memery usage
+(void)calculatePhotoCacheCompletion:(void(^)(NSUInteger fileCount, NSUInteger totalSize))completion;
+(BTPhotosCache *)sharedPhotoCache;
// retrieve image cache
-(NSData *)photoCacheForKey:(NSString *)key;
// delete image cache
-(void)removePhotoCacheForKey:(NSString *)key;
// calculate cache path with cache key
-(NSString *)defaultCachePathForKey:(NSString *)key;
// retrieve memery usage of image cache
-(NSUInteger)getSize;
// retrieve the number of images in the cache
-(int)getCount;
// cache image
-(void)storePhoto:(NSData *)photos forKey:(NSString *)key toDisk:(BOOL)toDisk;
// remove image cache from memery
-(void)removeMemoryPhotoCacheForKey:(NSString *)key;
// search for image cache on disk
-(NSOperation *)queryDiskPhotoCacheForKey:(NSString *)key completion:(void(^)(NSData *data))completion;
// generate cache key with time information
-(NSString *)generateCacheKeyWithCurrentTime;
// clear image cache
-(void)clearPhotoCacheCompletion:(void(^)(bool ok))completion;;

@end
