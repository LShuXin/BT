//
//  CONSTANT.h
//  IOSDuoduo
//
//  Created by 独嘉 on 14-5-23.
//  Copyright (c) 2014年 dujia. All rights reserved.
//

#ifndef TEAMTALK_CONSTANT_H
#define TEAMTALK_CONSTANT_H

/**
 *  Debug模式和Release模式不同的宏定义
 */

//-------------------打印--------------------
#ifdef DEBUG
#define NEED_OUTPUT_LOG             1
#define IS_CAN_SWITCH_SERVER        1
#else
#define NEED_OUTPUT_LOG             0
#define IS_CAN_SWITCH_SERVER        0
#endif


/**
 * 这是一个宏定义，用于自定义日志输出
 * #define：这是C和Objective-C中用于创建宏定义的关键字，允许你定义一个符号，该符号在代码中被替换为指定的内容。
  * DDLog：这是你定义的宏的名称。你可以在代码中使用DDLog来代替后面的内容。
 * (xx, ...)：这是宏的参数列表。在这个宏中，xx 是一个占位符，而 ... 是一个省略号，表示宏可以接受可变数量的参数。
 * NSLog(@"%s(%d): " xx, __PRETTY_FUNCTION__, __LINE__, ##__VA_ARGS__)：这是宏的实际替换内容。宏定义会将代码中的DDLog(xx, ...)替换为这个字符串。
 * NSLog：这是一个用于打印日志消息的函数。
 * @"%s(%d): " xx：这部分是一个格式化字符串，用于输出日志信息。%s 会被替换为函数的名称，%d 会被替换为代码的行号，而 xx 会被替换为传递给宏的可变参数。
 * __PRETTY_FUNCTION__：这是一个预定义的宏，在编译时会被替换为包含当前函数名称的字符串。
 * __LINE__：这是另一个预定义的宏，会被替换为当前代码所在的行号。
 * ##__VA_ARGS__：这部分用于将宏接受的可变参数插入到格式化字符串中。## 是一个预处理操作符，用于将参数连接在一起。
  * 因此，当你在代码中使用DDLog(xx, ...)时，宏会将它替换为一个包含函数名称、行号和传递给宏的可变参数的格式化字符串，然后使用NSLog来打印这个字符串，
 * 实现自定义的日志输出格式。
 *
 * e.g.1 假设有以下代码片段：
 *
 * DDLog(@"This is a log message");
 * 在这个例子中，宏 DDLog 将被替换为以下内容：
 * NSLog(@"%s(%d): " @"This is a log message",  __PRETTY_FUNCTION__,  __LINE__);
 * 然后，这个替换后的代码将在控制台中打印如下日志消息：
 * [YourClassName yourMethodName](42): This is a log message
 *
 * e.g.2 假设有以下代码片段：
 *
 * DDLog(@"User %@ performed action: %@", username, action);
 * 在这个例子中，宏 DDLog 将被替换为以下内容：
 * NSLog(@"%s(%d): " @"User %@ performed action: %@",  __PRETTY_FUNCTION__, __LINE__, username, action)
 * 然后，这个替换后的代码将在控制台中打印如下日志消息：
 * -[YourClassName yourMethodName](42): User johndoe performed action: clicked_button
 *
*/
#define DDLog(xx, ...)              NSLog(@"%s(%d): " xx, __PRETTY_FUNCTION__, __LINE__, ##__VA_ARGS__)


#define IM_PDU_HEADER_LEN   16
#define IM_PDU_VERSION      1

//-------------------本地化--------------------
// 在所有显示在界面上的字符串进行本地化处理
#define _(x)                                NSLocalizedString(x, @"")
#endif


