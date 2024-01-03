# 服务器编程心得（四）—— 如何将socket设置为非阻塞模式

>  原文：https://balloonwj.blog.csdn.net/article/details/53033102

1. windows 平台上无论利用 socket() 函数还是 WSASocket() 函数创建的 socket 都是阻塞模式的：

   ```
   SOCKET WSAAPI socket(
     _In_ int af,
     _In_ int type,
     _In_ int protocol
   );
   
   SOCKET WSASocket(
     _In_ int                af,
     _In_ int                type,
     _In_ int                protocol,
     _In_ LPWSAPROTOCOL_INFO lpProtocolInfo,
     _In_ GROUP          g,
     _In_ DWORD         dwFlags
   );
   ```

   linux 平台上可以在利用 socket() 函数创建 socket 时指定创建的 socket 是异步的：

   ```
   int socket(int domain, int type, int protocol);
   ```

   在 type 的参数中设置 SOCK_NONBLOCK 标志即可，例如：

   ```
   int s = socket(AF_INET, SOCK_STREAM | SOCK_NONBLOCK, IPPROTO_TCP);
   ```

2. 另外，windows 和 linux 平台上 accept() 函数返回的 socekt 也是阻塞的，linux 另外提供了一个 accept4() 函数，可以直接将返回的 socket 设置为非阻塞模式：
  ```
  int accept(int sockfd, struct sockaddr *addr, socklen_t *addrlen);
  int accept4(int sockfd, struct sockaddr *addr, socklen_t *addrlen, int flags);
  ```

  只要将 accept4() 最后一个参数 flags 设置成 SOCK_NONBLOCK 即可。


3. 除了创建 socket 时，将 socket 设置成非阻塞模式，还可以通过以下API函数来设置：

   linux 平台上可以调用 fcntl() 或者 ioctl() 函数，实例如下：

   ```
   fcntl(sockfd, F_SETFL, fcntl(sockfd, F_GETFL, 0) | O_NONBLOCK);
   ioctl(sockfd, FIONBIO, 1);  //1:非阻塞 0:阻塞
   ```

   参考： http://blog.sina.com.cn/s/blog_9373fc760101i72a.html

但是网上也有文章说（文章链接：http://blog.csdn.net/haoyu_linux/article/details/44306993），linux下如果调用fcntl()设置socket为非阻塞模式，不仅要设置O_NONBLOCK模式，还需要在接收和发送数据时，需要使用MSG_DONTWAIT标志，即在recv，recvfrom和send，sendto数据时，将flag设置为MSG_DONTWAIT。是否有要进行这种双重设定的必要，笔者觉得没有这个必要。因为linux man手册上recv()函数的说明中关于MSG_DONTWAIT说明如下：

> Enables nonblocking operation; if the operation would block, the call fails with the error EAGAIN or EWOULDBLOCK (this can also be enabled using the O_NONBLOCK flag  with the F_SETFL fcntl(2)).

通过这段话我觉得要么通过设置 recv() 函数的 flags 标识位为 MSG_DONTWAIT，要么通过 fcntl() 函数设置 O_NONBLOCK 标识，而不是要同时设定。

windows 上可调用 ioctlsocket 函数：
```
int ioctlsocket(
  _In_    SOCKET s,
  _In_    long   cmd,
  _Inout_ u_long *argp
);
```

将 cmd 参数设置为 `FIONBIO`，`*argp = 0  `即设置成阻塞模式，而 `*argp` 非0即可设置成非阻塞模式。但是 windows 平台需要注意一个地方，如果你对一个socket 调用了 WSAAsyncSelect() 或 WSAEventSelect() 函数后，你再调用 ioctlsocket() 函数将该 socket 设置为非阻塞模式，则会失败，你必须先调用WSAAsyncSelect() 通过设置 lEvent 参数为0或调用 WSAEventSelect() 通过设置 lNetworkEvents 参数为0来分别禁用 WSAAsyncSelect() 或 WSAEventSelect()。再次调用 ioctlsocket( )将该 socket 设置成阻塞模式才会成功。因为调用 WSAAsyncSelect() 或 WSAEventSelect() 函数会自动将 socket 设置成非阻塞模式。msdn上的原话是：
```
The WSAAsyncSelect and WSAEventSelect functions automatically set a socket to nonblocking mode. If WSAAsyncSelect or WSAEventSelect has been issued on a socket, then any attempt to use ioctlsocket to set the socket back to blocking mode will fail with WSAEINVAL.

To set the socket back to blocking mode, an application must first disable WSAAsyncSelect by calling WSAAsyncSelect with the lEvent parameter equal to zero, or disable WSAEventSelect by calling WSAEventSelect with the lNetworkEvents parameter equal to zero.
```

网址：https://msdn.microsoft.com/en-us/library/windows/desktop/ms738573(v=vs.85).aspx

4. 在看实际项目中以前一些前辈留下来的代码中，通过在一个循环里面调用 fcntl() 或者 ioctlsocket() 函数来 socket 的非阻塞模式的，代码如下：

```
for (;;)
{
#ifdef UNIX
    on = 1;
    if (ioctlsocket(id, FIONBIO, (char *)&on) < 0)
#endif
			
#ifdef WIN32
    unsigned long on_windows = 1;
    if (ioctlsocket(id, FIONBIO, &on_windows) < 0)
#endif
			
			
#ifdef VOS
    int off = 0;
    if (ioctlsocket(id, FIONBIO, (char *)&off) <0)
#endif

    {
        if (GET_LAST_SOCK_ERROR() == EINTR)
        {
            continue;
        }
          
        RAISE_RUNTIME_ERROR("Can not set FIONBIO for socket");
        closesocket(id);
        return NULL;
    }
    break;
}
```

是否有必要这样做，有待考证。