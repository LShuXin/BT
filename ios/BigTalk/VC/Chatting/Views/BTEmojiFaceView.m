//
//  BTEmojiFaceView.m
//

#import "BTEmojiFaceView.h"
#import "BTEmotionsModule.h"


#define EMOTIONS_COUNT_PERPAGE                           19
#define EMOTIONS_PERROW                                  4
#define EMPTIONS_ROWS                                    2


@implementation BTEmojiFaceView

-(id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self)
    {

    }
    return self;
}

-(void)loadFacialView:(int)page limit:(CGSize)size
{
	//row number
    NSArray *emotions = [[BTEmotionsModule shareInstance].emotionUnicodeDic allValues];
	for (int y = 0; y < EMPTIONS_ROWS; y++)
    {
		for (int x = 0; x < EMOTIONS_PERROW; x++)
        {
			UIButton *button = [UIButton buttonWithType:UIButtonTypeCustom];
            [button setBackgroundColor:[UIColor clearColor]];
            [button setFrame:CGRectMake(x * size.width, y * size.height, size.width, size.height)];
            if ((y * EMOTIONS_PERROW + x + page * EMOTIONS_COUNT_PERPAGE) > [emotions count])
            {
                return;
            }
            else
            {
                if (
                    y * EMOTIONS_PERROW + x == EMOTIONS_COUNT_PERPAGE
                    ||
                    (y * EMOTIONS_PERROW + x + page * EMOTIONS_COUNT_PERPAGE) == [emotions count]
                   )
                {
                    [button setImage:[UIImage imageNamed:@"dd_emoji_delete"] forState:UIControlStateNormal];
                    button.tag = 10000;
                    [button addTarget:self action:@selector(selected:) forControlEvents:UIControlEventTouchUpInside];
                    [self addSubview:button];
                }
                else
                {
                    [button setImage:[UIImage imageNamed:[emotions objectAtIndex: y * EMOTIONS_PERROW + x + (page * EMOTIONS_COUNT_PERPAGE)]]
                            forState:UIControlStateNormal];
                    button.tag = y * EMOTIONS_PERROW + x + (page * EMOTIONS_COUNT_PERPAGE);
                    [button addTarget:self action:@selector(selected:) forControlEvents:UIControlEventTouchUpInside];
                    [self addSubview:button];
                }
            }
		}
	}
}

-(void)selected:(UIButton *)button
{
    NSArray *emotions = [BTEmotionsModule shareInstance].emotions;
    if (button.tag == 10000)
    {
        [self.delegate selectedFacialView:@"delete"];
    }
    else
    {
        NSString *str = [emotions objectAtIndex:button.tag];
        [self.delegate selectedFacialView:str];
    }
}
@end
