//
//  BTPromptCell.h
//

#import <UIKit/UIKit.h>

@interface BTPromptCell : UITableViewCell
{
    UILabel *_promptLabel;
}

-(void)setprompt:(NSString *)prompt;
@end
