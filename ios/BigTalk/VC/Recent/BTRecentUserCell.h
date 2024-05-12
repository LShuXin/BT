//
//  BTRecentUserCell.h
//

#import <UIKit/UIKit.h>
#import "BTGroupEntity.h"
#import "BTUserEntity.h"


@class BTSessionEntity;

@interface BTRecentUserCell : UITableViewCell

@property(weak) IBOutlet UIImageView *avatarImageView;
@property(weak) IBOutlet UILabel *nameLabel;
@property(weak) IBOutlet UILabel *dateLabel;
@property(weak) IBOutlet UILabel *lastmessageLabel;
@property(weak) IBOutlet UILabel *unreadMessageCountLabel;
@property(assign) NSInteger time_sort;

-(void)setName:(NSString *)name;
-(void)setTimeStamp:(NSUInteger)timeStamp;
-(void)setLastMessage:(NSString *)message;
-(void)setAvatar:(NSString *)avatar;
-(void)setUnreadMessageCount:(NSUInteger)messageCount;
-(void)setShowSession:(BTSessionEntity *)session;

@end
