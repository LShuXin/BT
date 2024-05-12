//
//  BTPhotosCache.m
//

#import "BTPhotosCache.h"
#import "SDImageCache.h"
#import "BTPublicDefine.h"
#import "NSString+BTAdditions.h"
#import <CommonCrypto/CommonDigest.h>
#import "BTSundriesCenter.h"


@interface BTPhotosCache()
@property(nonatomic, readonly)dispatch_queue_t ioQueue;
@property(nonatomic, retain)NSFileManager *fileManager;
@property(nonatomic, retain)NSCache *memoryCache;
@end


@implementation BTPhotosCache

+(void)calculatePhotoCacheCompletion:(void(^)(NSUInteger fileCount, NSUInteger totalSize))completion
{
    NSURL *diskCacheURL = [NSURL fileURLWithPath:BTPhotosMessageDir isDirectory:YES];
    
    [[BTSundriesCenter instance] pushTaskToSerialQueue:^{
        NSUInteger fileCount = 0;
        NSUInteger totalSize = 0;
        NSFileManager *fileManager = [[NSFileManager alloc] init];
        NSDirectoryEnumerator *fileEnumerator = [fileManager enumeratorAtURL:diskCacheURL
                                                  includingPropertiesForKeys:@[NSFileSize]
                                                                     options:NSDirectoryEnumerationSkipsHiddenFiles
                                                                errorHandler:NULL];
        
        for (NSURL *fileURL in fileEnumerator)
        {
            NSNumber *fileSize;
            [fileURL getResourceValue:&fileSize forKey:NSURLFileSizeKey error:NULL];
            totalSize += [fileSize unsignedIntegerValue];
            fileCount += 1;
        }
        
        if (completion)
        {
            dispatch_async(dispatch_get_main_queue(), ^{
                completion(fileCount, totalSize);
            });
        }
    }];
}

-(NSData *)photoCacheForKey:(NSString *)key
{
    // Firs, the the memory cache
    NSData *memoryPhotoData = [self memoryPhotoCacheForKey:key];
    if (memoryPhotoData)
    {
        return memoryPhotoData;
    }
    // Second check the disk cache...
    NSData *diskPhotoData = [self diskPhotoCacheForKey:key];
    // put it in memory is not exist
    if (diskPhotoData)
    {
        [self.memoryCache setObject:diskPhotoData forKey:key];
    }
    return diskPhotoData;
}

+(instancetype)sharedPhotoCache
{
    static dispatch_once_t onceToken;
    static BTPhotosCache *g_photosCache;
    dispatch_once(&onceToken, ^{
        g_photosCache = [self new];
    });
    return g_photosCache;
}

-(instancetype)init
{
    self = [super init];
    if (self)
    {
        _ioQueue = dispatch_queue_create("com.lsx.BTPhotosCache", DISPATCH_QUEUE_SERIAL);
        _memoryCache = [NSCache new];
        
        dispatch_sync(_ioQueue, ^{
            _fileManager = [NSFileManager new];
        });
    }
    return self;
}

-(void)storePhoto:(NSData *)data forKey:(NSString *)key
{
    [self storePhoto:data forKey:key toDisk:YES];
}

-(void)removePhotoCacheForKey:(NSString *)key
{
    [self.memoryCache removeObjectForKey:key];
    
    dispatch_async(self.ioQueue, ^{
        [_fileManager removeItemAtPath:[self defaultCachePathForKey:key] error:nil];
    });
}

-(void)removeMemoryPhotoCacheForKey:(NSString *)key
{
    [self.memoryCache removeObjectForKey:key];
}

-(NSUInteger)getSize
{
    __block NSUInteger size = 0;
    dispatch_sync(self.ioQueue, ^{
        NSDirectoryEnumerator *fileEnumerator = [_fileManager enumeratorAtPath:BTPhotosMessageDir];
        for (NSString *fileName in fileEnumerator)
        {
            NSString *filePath = [BTPhotosMessageDir stringByAppendingPathComponent:fileName];
            NSDictionary *attrs = [[NSFileManager defaultManager] attributesOfItemAtPath:filePath error:nil];
            size += [attrs fileSize];
        }
    });
    return size;
}

-(int)getCount
{
    __block int count = 0;
    dispatch_sync(self.ioQueue, ^{
        NSDirectoryEnumerator *fileEnumerator = [_fileManager enumeratorAtPath:BTPhotosMessageDir];
        for (__unused NSString *fileName in fileEnumerator)
        {
            count += 1;
        }
    });
    return count;
}

-(void)storePhoto:(NSData *)photo forKey:(NSString *)key toDisk:(BOOL)toDisk
{
    if (!photo || !key)
    {
        return;
    }
    [self.memoryCache setObject:photo forKey:key];
    
    if (toDisk)
    {
        dispatch_async(self.ioQueue, ^{
            if (photo)
            {
                if (![_fileManager fileExistsAtPath:BTPhotosMessageDir])
                {
                    [_fileManager createDirectoryAtPath:BTPhotosMessageDir
                            withIntermediateDirectories:YES
                                             attributes:nil
                                                  error:NULL];
                }
                
                [_fileManager createFileAtPath:[self defaultCachePathForKey:key] contents:photo attributes:nil];
            }
        });
    }
}


-(NSString *)defaultCachePathForKey:(NSString *)key
{
    return [self cachePathForKey:key inPath:BTPhotosMessageDir];
}

-(NSString *)cachePathForKey:(NSString *)key inPath:(NSString *)path
{
    NSString *filename = [self cachedFileNameForKey:key];
    return [path stringByAppendingPathComponent:filename];
}

-(NSString *)cachedFileNameForKey:(NSString *)key
{
    const char *str = [key UTF8String];
    if (str == NULL)
    {
        str = "";
    }
    unsigned char r[CC_MD5_DIGEST_LENGTH];
    CC_MD5(str, (CC_LONG)strlen(str), r);
    NSString *filename = [NSString stringWithFormat:@"%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x",
                          r[0], r[1], r[2], r[3], r[4], r[5], r[6], r[7], r[8], r[9], r[10], r[11], r[12], r[13], r[14], r[15]];
    
    return filename;
}

-(NSString *)generateCacheKeyWithCurrentTime
{
    NSDateFormatter *formatter = [[NSDateFormatter alloc ] init];
    [formatter setDateFormat:@"YYYYMMddhhmmssSSS"];
    NSString *dateString = [formatter stringFromDate:[NSDate date]];
    NSString *timeLocalString = [[NSString alloc] initWithFormat:@"%@", dateString];
    return [NSString stringWithFormat:@"%@", timeLocalString];
}

-(NSOperation *)queryDiskPhotoCacheForKey:(NSString *)key completion:(void(^)(NSData *data))completion
{
    NSOperation *operation = [NSOperation new];
    
    if (!completion)
    {
        return nil;
    }
    
    if (!key)
    {
        completion(nil);
        return nil;
    }
    
    // First check the in-memory cache...
    NSData *photo = [self memoryPhotoCacheForKey:key];
    if (photo)
    {
        completion(photo);
        return nil;
    }
    
    dispatch_async(self.ioQueue, ^{
        if (operation.isCancelled)
        {
            return;
        }
        
        @autoreleasepool
        {
            NSData *diskPhoto = [self diskPhotoCacheForKey:key];
            if (diskPhoto)
            {
                [self.memoryCache setObject:diskPhoto forKey:key];
            }
            
            dispatch_async(dispatch_get_main_queue(), ^{
                completion(diskPhoto);
            });
        }
    });
    
    return operation;
}

-(NSData *)memoryPhotoCacheForKey:(NSString *)key
{
    return [self.memoryCache objectForKey:key];
}

-(NSData *)diskPhotoCacheForKey:(NSString *)key
{
    NSData *data = [self diskPhotoDataBySearchingAllPathsForKey:key];
    if (data)
    {
        return data;
    }
    else
    {
        return nil;
    }
}

-(NSData *)diskPhotoDataBySearchingAllPathsForKey:(NSString *)key
{
    NSString *defaultPath = [self defaultCachePathForKey:key];
    NSData *data = [NSData dataWithContentsOfFile:defaultPath];
    if (data)
    {
        return data;
    }
    return nil;
}


-(NSMutableArray *)getCachedImagePathArray
{
    __block NSMutableArray *array = [NSMutableArray new];
    dispatch_sync(self.ioQueue, ^{
        NSDirectoryEnumerator *fileEnumerator = [_fileManager enumeratorAtPath:BTPhotosMessageDir];
        for (__unused NSString *fileName in fileEnumerator)
        {
            [array addObject:[NSString stringWithFormat:@"%@/%@", BTPhotosMessageDir, fileName]];
        }
    });
    return array;
}

-(void)clearPhotoCacheCompletion:(void(^)(bool isFinish))completion;
{
    [self.memoryCache removeAllObjects];
    NSArray *cachedImagePathArray = [self getCachedImagePathArray];
    
    if ([cachedImagePathArray count] == 0)
    {
        completion(YES);
    }

    [cachedImagePathArray enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
        NSString *path = (NSString *)obj;
        [_fileManager removeItemAtPath:path error:nil];
        if (idx == [cachedImagePathArray count] - 1)
        {
            completion(YES);
        }
    }];
}

@end
