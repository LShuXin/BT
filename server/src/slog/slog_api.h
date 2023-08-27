//
//  slog_api.h
//  slog
//
//  Created by luoning on 15-1-6.
//  Copyright (c) 2015年 luoning. All rights reserved.
//

#ifndef __slog__slog_api__
#define __slog__slog_api__

#include <stdio.h>

#define WATCH_DELAY_TIME     10 * 1000

// 这是一个前向声明（forward declaration）。它告诉编译器 CSLogObject 是一个类，
// 但不提供其详细定义。这样可以在 CSLog 类中使用 CSLogObject* m_log;，而不需要完全了解 CSLogObject 类的实现细节。
class CSLogObject;

class CSLog
{
public:
    CSLog(const char* module_name, int delay = WATCH_DELAY_TIME);
    virtual ~CSLog();
    
    void Trace(const char* format, ...);
    void Debug(const char* format, ...);
    void Info(const char* format, ...);
    void Warn(const char* format, ...);
    void Error(const char* format, ...);
    void Fatal(const char* format, ...);
private:
    CSLogObject* m_log;
};



#endif /* defined(__slog__slog_api__) */
