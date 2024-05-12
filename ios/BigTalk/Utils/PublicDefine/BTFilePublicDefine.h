//
//  BTFilePublicDefine.h
//  tt_ios
//
//  Created by LShuXin on 2023/7/15.
//

#ifndef BTFilePublicDefine_h
#define BTFilePublicDefine_h


#define BTTempPath              NSTemporaryDirectory()
#define BTDocumentPath          [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) firstObject]
#define BTCachePath             [NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES) firstObject]
#define BTFileManager           ([NSFileManager defaultManager])
#define BTTheUserDefaults       ([NSUserDefaults standardUserDefaults])
#define BTVoiceMessageDir       ([[NSString documentPath] stringByAppendingPathComponent:@"/VoiceMessageDir/"])
#define BTPhotosMessageDir      ([[NSString documentPath] stringByAppendingPathComponent:@"/PhotosMessageDir/"])
#define BlacklistDir            ([[NSString documentPath] stringByAppendingPathComponent:@"/BlacklistDir/"])
#define BTDepartmentlist        ([[NSString documentPath] stringByAppendingPathComponent:@"/department.plist"])
#define BTFixedList             ([[NSString documentPath] stringByAppendingPathComponent:@"/fixed.plist"])
#define BTShieldingList         ([[NSString documentPath] stringByAppendingPathComponent:@"/shieldingArray.plist"])


#endif /* BTFilePublicDefine_h */
