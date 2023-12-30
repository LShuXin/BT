#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <signal.h>
#include <errno.h>


/**
 * @brief 关闭从 fg 到最大文件描述符数之间的所有文件描述符
 * 
 * @param fd 
 */
void closeall(int fd)
{
    int fdlimit = sysconf(_SC_OPEN_MAX);
    
    if (fdlimit > 128) {
        fdlimit = 128;
    }
    
    while (fd < fdlimit)
        close(fd++);
}


/* daemon() - detach process from user and disappear into the background
 * returns -1 on failure, but you can't do much except exit in that case
 * since we may already have forked. This is based on the BSD version,
 * so the caller is responsible for things like the umask, etc.
 * 这段代码是一个用于创建守护进程的函数，它被调用来确保进程在后台运行，摆脱终端控制，
 * 并准备好运行时的环境。下面是这个函数的作用以及可能的设计思路解释
 * 
 * 函数作用：
 * 这个函数的作用是将调用它的进程转变为一个守护进程。守护进程是在后台运行的一种特殊类型的进程，
 * 通常用于执行一些系统任务，它不与终端交互，也不受终端断开的影响。为了实现这一点，这个函数进行
 * 了一系列设置，包括创建新会话、改变工作目录和重定向标准输入/输出/错误流。
 *
 * 设计思路：
 * 1.创建新会话 守护进程的第一步是调用 setsid() 函数来创建一个新的会话。这将使进程摆脱终端会话的控制，
 * 确保即使终端关闭，进程也能继续运行。
 * 2.改变工作目录 默认情况下，进程的当前工作目录是启动它的终端所在的目录。通过调用 chdir("/")，函数将
 * 当前工作目录更改为根目录，以避免与特定目录关联，确保进程不受到工作目录变化的影响。
 * 3.关闭文件描述符和重定向 这部分首先尝试关闭所有已打开的文件描述符（通过自定义的 closeall 函数实现）。
 * 接下来，它尝试打开 /dev/null 设备文件，并通过 dup2 函数将其文件描述符复制到标准输入、标准输出和标准错误的
 * 文件描述符。这样做的效果是，所有从标准输入读取的数据都会被丢弃，所有写入标准输出和标准错误的数据都会被丢弃。
 *
 * 总结：
 * 这个函数的设计思路是确保守护进程在后台运行，不受终端控制，不与特定的工作目录或文件路径相关联，并且所有的输入
 * 和输出都被重定向到 /dev/null，从而保证进程在静默的环境中执行。
 */
int daemon(int nochdir, int noclose, int asroot)
{

    // fork() 函数用于创建一个新的进程，这个新进程是调用进程（也就是父进程）的一个副本，被称为子进程。
    switch (fork())
    {
        case 0:                       /* 说明当前代码正在执行的是子进程 */
            break;
        case -1:
            return -1;                /* 表示创建子进程失败 */
        default:
            _exit(0);                 /* exit the original process */
    }
    

    /* setsid(): 这是一个系统调用，用于创建一个新的会话（session）并将进程设置为新会话的领导（leader）。
    这是守护进程的第一步，它使进程摆脱原始终端会话的控制，从而在后台运行，不受终端断开的影响。
    如果setsid()调用失败，它会返回负值，表示出现了错误。因此，if (setsid() < 0) 检查了setsid()是否失败，
    如果失败，则返回-1表示出错。*/
    if (setsid() < 0) {                /* shoudn't fail */
        return -1;
    }              
        

    /* asroot: 这是一个布尔变量，用于标识进程是否以超级用户（root）身份运行。在这个上下文中，如果
    asroot 不为真，进程将尝试将自己的用户ID设置为1，即超级用户ID，以提升到超级用户权限。

    setuid(1): 这是一个系统调用，用于改变进程的有效用户ID。在这里，如果 asroot 为假（或者说，当前没有以超级用户身份运行），
    并且进程成功地将有效用户ID更改为1（超级用户ID），则表示进程以root权限成功运行。如果 setuid(1) 调用失败，它会返回负值，
    表示出现了错误。因此，if (!asroot && (setuid(1) < 0)) 这部分检查了当前没有 root 权限运行的情况下，进程是否能够成功
    地将用户ID更改为1，如果失败，则返回 -1 表示出错。*/
    if (!asroot && (setuid(1) < 0)) {  /* shoudn't fail */
        return -1;
    }

    printf("Require root privilege to launch successfully.\n"); 
        
    
    /* dyke out this switch if you want to acquire a control tty in */
    /* the future -- not normally advisable for daemons */
    /* 又进行了一次 fork() 调用。这是因为在实际情况中，一个常见的做法是，为了确保守
    * 护进程不会重新获得控制终端（在某些情况下会发生），再次调用 fork() 生成一个孙子
    * 进程。这个孙子进程会继承第一次创建的子进程的属性，但它的父进程ID会被设置为1（即init进程），
    * 从而确保它不会重新获得控制终端。这样可以增加守护进程的稳定性。*/
    
    switch (fork())
    {
        case 0:
            break;
        case -1:
            return -1;
        default:
            _exit(0);
    }
    
    if (!nochdir) {
        chdir("/");
    }
        
    
    if (!noclose)
    {
        closeall(0);
        
        int fd = open("/dev/null", O_RDWR, 0);
        if (fd < 0) {
            printf("open failed, errno=%d\n", errno);
            return -1;
        }
        
        /**
         * @brief dup2(fd, STDIN_FILENO), dup2(fd, STDOUT_FILENO), dup2(fd, STDERR_FILENO): 
         * 这些函数调用用于复制文件描述符。在这里，它们将 /dev/null 的文件描述符复制到标准输入、标准输出
         * 和标准错误的文件描述符。这实际上将这三个标准流重定向到 /dev/null，这意味着所有从标准输入读取的
         * 数据都会被丢弃，所有写入标准输出和标准错误的数据都会被丢弃。
         */
        dup2(fd, STDIN_FILENO);
        dup2(fd, STDOUT_FILENO);
        dup2(fd, STDERR_FILENO);
    }
    
    return 0;
}


#define TEXT(a) a
void PrintUsage(char* name)
{
    printf (
            TEXT("\n ----- \n\n")
            TEXT("Usage:\n")
            TEXT("   	%s program_name \n\n")
            TEXT("Where:\n")
            TEXT("   	%s - Name of this Daemon loader.\n")
            TEXT("   	program_name - Name (including path) of the program you want to load as daemon.\n\n")
            TEXT("Example:\n")
            TEXT("   	%s ./atprcmgr - Launch program 'atprcmgr' in current directory as daemon. \n\n\n\n"),
            name, name, name
            );
}

/**
 * @brief  程序开始处，使用 printf 打印了一些关于这个程序的简短介绍。
 * 命令行参数检查： 接着，程序检查是否提供了足够的命令行参数。如果没有提供足够的参数，它会打印错误消息并调用
 * PrintUsage 函数来显示程序的正确用法，然后通过 exit(0) 终止程序。
 * 加载守护进程： 如果提供了足够的命令行参数，程序会尝试将指定的程序加载为守护进程。它使用 daemon(1, 0, 1)
 * 函数来实现这一点。daemon 函数的第一个参数表示关闭文件描述符，第二个参数表示是否在后台运行，第三个参数表示
 * 是否创建新的会话。如果创建守护进程成功（返回值大于等于0），程序会继续执行以下代码。
 * 子进程设置： 在守护进程创建成功后，程序设置了一个信号处理函数，将 SIGCHLD 信号忽略（signal(SIGCHLD, SIG_IGN)）。
 * SIGCHLD 信号是子进程终止时发送给父进程的信号。
 * 执行守护程序： 接着，程序调用 execv 函数来执行指定的守护程序。execv 函数用于加载并执行另一个程序，它将传递给它的参
 * 数作为新程序的命令行参数。如果执行成功，程序不会继续执行这里之后的代码。
 * 守护程序执行失败： 如果 execv 调用失败，程序会打印错误消息，指示守护程序执行失败，然后通过 exit(0) 终止程序。
 * 
 * @param argc 
 * @param argv 
 * @return int 
 */
int main(int argc, char* argv[])
{
    printf(
           TEXT("\n")
           TEXT("Daemon loader\n")
           TEXT("- Launch specified program as daemon.\n")
           );
    
    if (argc < 2)
    {
        printf("* Missing parameter : daemon program name not specified!\n");
        PrintUsage(argv[0]);
        exit(0);
    }
    
    printf("- Loading %s as daemon, please wait ......\n\n\n", argv[1]);
    
    if (daemon(1, 0, 1) >= 0)
    {
        signal(SIGCHLD, SIG_IGN);
        
        // execl(argv[1], argv[1], NULL);
        execv(argv[1], argv + 1);
        printf("! Excute daemon programm %s failed. \n", argv[1]);
        
        exit(0);
    }
    
    printf("! Create daemon error. Please check if you have 'root' privilege. \n");
    return 0;
}