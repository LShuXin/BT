# 服务器编程心得（五）—— 如何编写高性能日志

> 原文：https://balloonwj.blog.csdn.net/article/details/60757391


## 一、服务器端日志与客户端日志的区别

在正式讲解之前，我们先来看一个日志类的实现方法，这个日志类也是代表着大多数客户端日志的主流写法：

	/**
	 *@desc:    程序运行log类,log.h
	 *@author:  zhangyl
	 *@date:    2017.01.17
	 **/
	#ifndef __LOG_H__
	#define __LOG_H__
	
	#ifdef _ZYL_LOG_
	#define LogInfo(...)     Log::GetInstance().AddLog("INFO", __FILE__, __LINE__, __FUNCSIG__, __VA_ARGS__)
	#define LogWarning(...)  Log::GetInstance().AddLog("WARNING", __FILE__, __LINE__, __FUNCSIG__, __VA_ARGS__)
	#define LogError(...)    Log::GetInstance().AddLog("ERROR", __FILE__, __LINE__, __FUNCSIG__, __VA_ARGS__)
	#else
	#define LogInfo(...) (void(0))
	#define LogError(...) (void(0))
	#endif
	
	class Log
	{
	public:
	    static Log& GetInstance();
	    bool AddLog(const char* pszLevel, const char* pszFile, int lineNo, const char* pszFuncSig, char* pszFmt, ...);
	
	private:
	    Log();
	    ~Log();
	    Log(const Log&);
	    Log& operator=(const Log&);
	
	private:
		FILE*	m_file;
	};
	
	#endif //!__LOG_H__

```
/**
 *@desc:    程序运行log类,log.cpp
 *@author:  zhangyl
 *@date:    2017.01.17
 **/

#include <time.h>
#include <stdio.h>
#include <stdarg.h>
#include "Log.h"

Log& Log::GetInstance()
{
    static Log log;
    return log;
}

bool Log::AddLog(const char* pszLevel, const char* pszFile, int lineNo, const char* pszFuncSig, char* pszFmt, ...)
{
    if (m_file == NULL)
    {
        return false;
    }

    char tmp[8192*10] = { 0 };
    va_list va;					                          // 定义一个va_list型的变量,这个变量是指向参数的指针.
    va_start(va, pszFmt);			                    // 用va_start宏初始化变量,这个宏的第二个参数是第一个可变参数的前一个参数,是一个固定的参数
    _vsnprintf(tmp, ARRAYSIZE(tmp), pszFmt, va);  // 注意,不要漏掉前面的_
    va_end(va);
 
    time_t now = time(NULL);
    struct tm* tmstr = localtime(&now);
    char content[8192 * 10 + 256] = {0};
    sprintf_s(content, ARRAYSIZE(content), "[%04d-%02d-%02d %02d:%02d:%02d][%s][0x%04x][%s:%d %s]%s\r\n",
          tmstr->tm_year + 1900,
          tmstr->tm_mon + 1,
          tmstr->tm_mday,
          tmstr->tm_hour,
          tmstr->tm_min,
          tmstr->tm_sec,
          pszLevel,
          GetCurrentThreadId(),
          pszFile,
          lineNo,
          pszFuncSig,
          tmp);

    if (fwrite(content, strlen(content), 1, m_file) != 1)
    {
        return false;
    }
      
    fflush(m_file);

    return true;
}

Log::Log()
{
    time_t now = time(NULL);
    struct tm* tmstr = localtime(&now);
    char filename[256];
    sprintf_s(filename, ARRAYSIZE(filename), "%04d%02d%02d%02d%02d%02d.runlog", 
          tmstr->tm_year + 1900, 
          tmstr->tm_mon + 1, 
          tmstr->tm_mday, 
          tmstr->tm_hour, 
          tmstr->tm_min, 
          tmstr->tm_sec);
    m_file = fopen(filename, "at+");
}

Log::~Log()
{
    if (m_file != NULL)
    {
        fclose(m_file);
    }
}
```

这个 Log 类的定义和实现代码节选自我的一款12306刷票软件，如果需要使用这个类的话包含 Log.h 头文件，然后使用宏：LogInfo/LogWarning/LogError 这三个宏就可以了。示例如下：

    string strResponse;
    string strCookie = "Cookie: ";
    strCookie += m_strCookies;
    if (!HttpRequest(osURL.str().c_str(), strResponse, true, strCookie.c_str(), NULL, false, 10))
    {
        LogError("QueryTickets2 failed");
        return false;
    }

这个日志类，每次输出一行，一行中输出时间、日志级别、线程id、文件名、行号、函数签名和自定义的错误信息，演示如下：
```
[2017-02-16 17:30:08][INFO][0x0e7c][f:\mycode\hack12306\12306demo\client12306.cpp:1401 bool __thiscall Client12306::HttpRequest(const char *,class std::basic_string<char,struct std::char_traits<char>,class std::allocator<char> > &,bool,const char *,const char *,bool,int)]http response: {"validateMessagesShowId":"_validatorMessage","status":true,"httpstatus":200,"data":{"loginAddress":"10.1.232.219","otherMsg":"","loginCheck":"Y"},"messages":[],"validateMessages":{}}

[2017-02-16 17:30:08][INFO][0x0e7c][f:\mycode\hack12306\12306demo\client12306.cpp:1379 bool __thiscall Client12306::HttpRequest(const char *,class std::basic_string<char,struct std::char_traits<char>,class std::allocator<char> > &,bool,const char *,const char *,bool,int)]http post: url=https://kyfw.12306.cn:443/otn/login/userLogin, headers=Cookie: JSESSIONID=0A01D965C45FE88A1FB289F288BD96C255E3547783; BIGipServerotn=1708720394.50210.0000; , postdata=_json_att=

[2017-02-16 17:30:08][INFO][0x0e7c][f:\mycode\hack12306\12306demo\client12306.cpp:1401 bool __thiscall Client12306::HttpRequest(const char *,class std::basic_string<char,struct std::char_traits<char>,class std::allocator<char> > &,bool,const char *,const char *,bool,int)]http response: 

[2017-02-16 17:30:08][INFO][0x0e7c][f:\mycode\hack12306\12306demo\client12306.cpp:1379 bool __thiscall Client12306::HttpRequest(const char *,class std::basic_string<char,struct std::char_traits<char>,class std::allocator<char> > &,bool,const char *,const char *,bool,int)]http post: url=https://kyfw.12306.cn:443/otn/index/initMy12306, headers=Cookie: JSESSIONID=0A01D965C45FE88A1FB289F288BD96C255E3547783; BIGipServerotn=1708720394.50210.0000; , postdata=
```

上文中也说了，以上示例是我曾经写的一款客户端程序的日志，注意 “客户端” 这个重要的关键字。因为上述日志的实现虽然通用，但其局限性也只能用于客户端这样对性能和效率要求不高的程序（这里的性能和效率是相对于高并发高性能的服务器程序来说的，也就是说上述日志实现可用于大多数客户端程序，但不能用于高性能高并发的服务器程序）。那么上述程序存在什么问题？问题是效率低！

不知道读者有没有注意上，上述日志类实现，是在调用者线程中直接进行IO操作，相比较于高速的CPU，IO磁盘操作是很慢的，直接在某些工作线程（包括UI线程）写文件，程序执行速度太慢，尤其是当日志数据比较多的时候。

这也就是服务器端日志和客户端日志的区别之一，客户端程序日志一般可以在直接在所在的工作线程写日志，因为这点性能和时间损失对大多数客户端程序来说，是可以忽略的，但对于要求高并发（例如并发量达百万级乃至千万级的系统）的服务器程序来说，单位时间内耗在磁盘写操作上的时间就相当可观了。我目前的做法是参考陈硕的 muduo 库的做法，使用一个队列，需要写日志时，将日志加入队列中，另外一个专门的日志线程来写日志，我给出下我的具体实现代码，如果需要查看 muduo 库的做法，请参考陈硕的书《Linux多线程服务端编程：使用muduo C++网络库》关于日志章节。注意：以下是纯C++11代码：

    /** 
     * 日志类头文件, Logger.h
     * zhangyl 2017.02.28
     **/
    
    #ifndef __LOGGER_H__
    #define __LOGGER_H__
    
    #include <string>
    #include <memory>
    #include <thread>
    #include <mutex>
    #include <condition_variable>
    #include <list>
    
    //struct FILE;
    
    #define LogInfo(...)        Logger::GetInstance().AddToQueue("INFO", __FILE__, __LINE__, __PRETTY_FUNCTION__, __VA_ARGS__)
    #define LogWarning(...)     Logger::GetInstance().AddToQueue("WARNING", __FILE__, __LINE__, __PRETTY_FUNCTION__, __VA_ARGS__)
    #define LogError(...)       Logger::GetInstance().AddToQueue("ERROR", __FILE__, __LINE__, __PRETTY_FUNCTION__, __VA_ARGS__)
    
    class Logger
    {
    public:
        static Logger& GetInstance();
        void SetFileName(const char* filename);
        bool Start();
        void Stop();
        void AddToQueue(const char* pszLevel, const char* pszFile, int lineNo, const char* pszFuncSig, char* pszFmt, ...);
    private:
        Logger() = default;
        Logger(const Logger& rhs) = delete;
        Logger& operator =(Logger& rhs) = delete;
        void threadfunc();
        private:
        std::string                     filename_;
        FILE*                           fp_{};
        std::shared_ptr<std::thread>    spthread_;
        std::mutex                      mutex_;
        std::condition_variable         cv_;            // 有新的日志到来的标识
        bool                            exit_{false};
        std::list<std::string>          queue_;
    };
    
    #endif //!__LOGGER_H__

```
/**

 * 日志类实现文件, Logger.cpp
 * zhangyl 2017.02.28
   **/

#include "Logger.h"
#include <time.h>
#include <stdio.h>
#include <memory>
#include <stdarg.h>

Logger& Logger::GetInstance()
{
    static Logger logger;
    return logger;
}

void Logger::SetFileName(const char* filename)
{
    filename_ = filename;
}

bool Logger::Start()
{
    if (filename_.empty())
    {
        time_t now = time(NULL);
        struct tm* t = localtime(&now);
        char timestr[64] = { 0 };
        sprintf(timestr, "%04d%02d%02d%02d%02d%02d.imserver.log",
                t->tm_year + 1900,
                t->tm_mon + 1,
                t->tm_mday,
                t->tm_hour,
                t->tm_min,
                t->tm_sec);

        filename_ = timestr;
    }
    
    fp_ = fopen(filename_.c_str(), "wt+");
    if (fp_ == NULL)
    {
        return false;
    }
        
    spthread_.reset(new std::thread(std::bind(&Logger::threadfunc, this)));

    return true;
}

void Logger::Stop()
{
    exit_ = true;
    cv_.notify_one();
    // 等待时间线程结束
    spthread_->join();
}

void Logger::AddToQueue(const char* pszLevel, const char* pszFile, int lineNo, const char* pszFuncSig, char* pszFmt, ...)
{
    char msg[256] = { 0 };
    va_list vArgList;                            
    va_start(vArgList, pszFmt);
    vsnprintf(msg, 256, pszFmt, vArgList);
    va_end(vArgList);

    time_t now = time(NULL);
    struct tm* tmstr = localtime(&now);
    char content[512] = { 0 };
    sprintf(content, "[%04d-%02d-%02d %02d:%02d:%02d][%s][0x%04x][%s:%d %s]%s\n",
                tmstr->tm_year + 1900,
                tmstr->tm_mon + 1,
                tmstr->tm_mday,
                tmstr->tm_hour,
                tmstr->tm_min,
                tmstr->tm_sec,
                pszLevel,
                std::this_thread::get_id(),
                pszFile,
                lineNo,
                pszFuncSig,
                msg);
 
    {
        std::lock_guard<std::mutex> guard(mutex_);
        queue_.emplace_back(content);
    }

    cv_.notify_one();
}

void Logger::threadfunc()
{
    if (fp_ == NULL)
    {
        return;
    }
        
    while (!exit_)
    {
        //写日志
        std::unique_lock<std::mutex> guard(mutex_);
        while (queue_.empty())
        {
            if (exit_)
            {
                return;
            }
            cv_.wait(guard);
        }

        // 写日志
        const std::string& str = queue_.front();
        fwrite((void*)str.c_str(), str.length(), 1, fp_);
        fflush(fp_);
        queue_.pop_front();
    }
}
```

以上代码只是个简化版的实现，使用 std::list 来作为队列，使用条件变量来作为新日志到来的触发条件。当然，由于使用了两个固定长度的数组，大小是256和512，如果日志数据太长，会导致数组溢出，这个可以根据实际需求增大缓冲区或者改用动态长度的string类型。使用这两个文件只要包含Logger.h，然后使用如下一行代码启动日志线程就可以了：

```
Logger::GetInstance().Start();
```

生成日志，使用头文件里面定义的三个宏 LogInfo、LogWarning、LogError，当然你也可以扩展自己的日志级别。



## 二、日志里面应该写些什么？

我开始在试着去写日志的时候，也走了不少弯路，无论是客户端还是服务器端，日志写的内容倒是不少，但都是些废话，虽然也报出故障，但对解决实际问题时毫无作用。尤其是在服务器上生产环境以后，出现很多问题，问题也暴露出来了，但是由于日志含有的当时现场的环境信息太少，只能看到错误，却没法追踪问题，更别说解决问题了。我们来看两个具体的例子：

```
CIULog::Log(LOG_WARNING, __FUNCSIG__, _T("Be cautious! Unhandled net data! req_ans_command=%d."), header.cmd);
```

这条日志记录，只打印出一条警告信息和命令号（cmd），对具体产生这个警告的输入参数和当时的环境也没进行任何记录，即使产生问题，事后也无法追踪。再看一条

    if (!HttpRequest(osURL.str().c_str(), strResponse, true, strCookie.c_str(), NULL, false, 10))
    {
        LogError("QueryTickets1 failed");
        return false;
    }

这条日志，因为http请求报了个简单的错误，至于产生错误的参数和原因一概没有交待，这种日志如果在生产环境上出现如何去排查呢？出错原因可能是设置的参数非法，这是外部原因，可以解决的，甚至是交互双方的一端传过来的，需要对方去纠正；也可能是当时的网络故障，这个也可以解决，也不算是程序的bug，不需要解决；也可能是的bug引起的，这个需要程序作者去解决。另外，如果是服务器程序，甚至应该在错误中交待下产生日志的用户id、操作类型等信息，这样事后才能便于定位位置，进行重现等。 

总结起来，日志记录应该尽量详细，能反映出当时出错的现场情节、产生的环境等。比如一个注册请求失败，至少要描述出当时注册的用户名、密码、用户状态（比如是否已经注册）、请求的注册地址等等。因为日志报错不一定是程序bug，可能是用户非法请求。日志详细了，请不用担心服务器的磁盘空间，因为相比较定位错误，这点磁盘空间还是值得的，实在不行可以定期清理日志嘛。

另外一点是，可以将错误日志、运行状态日志等分开，甚至可以将程序记录日志与业务本身日志分开，这样排查故障时优先查看是否有错误日志文件产生，再去错误日志里面去找，而不用在一堆日志中筛选错误日志。我的很多项目在生产环境也是这么做的。
