# 服务器端编程心得（七）——开源一款即时通讯软件的源码

> 原文：https://balloonwj.blog.csdn.net/article/details/69481542


在我的《服务器端编程心得》这个系列的第一篇至第六篇都是讲了一些零散的不成体系的网络编程细节。今天，在这篇文章中，我将介绍一款我自主开发的即时通讯软件 flamingo（中文：火烈鸟），并开源其服务器和 pc 客户端代码。以此来对前几篇文章中说到的理论进行实践。

代码在 github 和码云上各上传了一份：

```
github地址：https://github.com/baloonwj/flamingo

码云：https://gitee.com/balloonwj/flamingo
```

csdn 上代码可能不是最新的，但是 github 上的代码是不断维护的，包括一些新功能的增加和 bug 的修复。如果你想关注 flamingo 的最新功能，请关注 github 上的更新。如果你只想研究下网络通信程序的基本原理和编码技巧，csdn 上的代码就足够了。



目前即时通讯软件实现了如下功能（这里只列举网络相关的功能，其他客户端已经实现的功能不统计在列，请自行发现）：

- 注册
- 登录
- 查找好友、查找群
- 添加好友、添加群
- 好友列表、群列表、最近会话
- 单人聊天功能（包括发文字、表情、窗口抖动、离线文件）
- 群聊功能（包括发文字、表情）
- 修改密码
- 修改个人信息（自定义昵称、签名、个性头像等个人信息）
- 自动升级功能




下面是pc版本的一些截图：





















下面是安卓版本的一些截图：











客户端还有很多细节功能，比如头像有三种显示模式、好友上线动画、聊天记录、聊天自动回复功能等，有兴趣的同学可以自己探索尝试一下吧，这里就不截图了。



**服务器代码编译与运行环境：**

flamingo 服务器端代码使用 cmake + makefile 编译，使用了纯 C++11 开发，运行于 linux 系统下（我的系统是CentOS7.0），为了支持 C++11，你的 gcc 版本至少要大于 4.7，我的版本是 4.8.5。另外，使用了 mysql 数据库，我的数据库版本是 5.7.17。我实际安装的是 mysql 的开源分支 mariadb，安装方法如下：

```
[root@yl-web yl]# yum install mariadb-server mariadb mariadb-devel
```

mariadb 数据库的相关命令是：

```
systemctl start mariadb  #启动MariaDB

systemctl stop mariadb  #停止MariaDB

systemctl restart mariadb  #重启MariaDB

systemctl enable mariadb  #设置开机启动
```

所以先启动数据库

```
[root@yl-web yl]# systemctl start mariadb
```

然后就可以正常使用mysql了：

```
[root@yl-web yl]# mysql -u root -p
Enter password: 
Welcome to the MariaDB monitor.  Commands end with ; or \g.
Your MariaDB connection id is 3
Server version: 5.5.41-MariaDB MariaDB Server

Copyright (c) 2000, 2014, Oracle, MariaDB Corporation Ab and others.

Type 'help;' or '\h' for help. Type '\c' to clear the current input statement.

MariaDB [(none)]> show databases;
+--------------------+
| Database           |
+--------------------+
| information_schema |
| mysql              |
| performance_schema |
| test               |
+--------------------+
4 rows in set (0.00 sec)

MariaDB [(none)]> 
```


注意：如果你使用的是低版本的 CentOS 系统，或者其他版本的 linux 系统，你安装的 mysql 不是 mariadb 的话，需要安装的分别是：mysql-server mysql 和mysql-devel。

服务器代码不仅是一款即时通讯软件的服务器代码，同时也是一款通用的 C++11 服务器框架。



**服务器代码使用方法：**

**编译方法**

1. 进入程序目录，输入cmake . (注意有一个点号，表示当前目录)

2. 没有错误，输入make

3. 最终会产生三个可执行程序：

   - 聊天服务器 chatserver

   - 文件服务器 filesever

   - 图片服务器 imgserver

**部署方法**

在配置文件中 etc/chatserver.conf 中，配置的 mysql 数据库的用户名为 root，密码为 123456，请根据你自己的需要修改相应的用户名和密码。

chatserver 是聊天服务器，fileserver 是文件服务器，文件服务器负责上传和下载聊天中发送的文件，imgserver 负责上传和下载聊天中的图片。三个服务相互独立，互不影响。聊天服务器监听端口是20000，文件服务器端口是20001，图片服务器端口号是20002，这三个端口供客户端连接，其中聊天端口和客户端是长连接，文件端口和图片可选择长连接或短连接。

第一次运行 chatserver 时，如果能顺利连上 mysql，chatserver 会自动检测是否存在名为 flamingo 的数据库，如果不存在则创建之，并新建三张信息表，分别是用户信息表：t_user, 好友关系表 t_user_relationship 和聊天消息记录表 t_chatmsg。第一次启动 fileserver 时会创建 filecache 目录，这个目录用来存储聊天中的离线文件以及客户端升级包。第一次启动 imgserver 时，会创建 imgcache 目录，这个用于存储聊天过程中的聊天图片和用户头像文件。

为了方便查看代码，我用 Visual Studio 来管理代码，可使用 VS 打开 myserver.sln 查看和管理代码。（VS版本必须是VS2013或以上版本）



**pc 客户端代码使用方法**

1.用VS2013打开程序目录下的：Flamingo.sln，你可以使用其他的VS版本，但是至少不低于VS2013，因为客户端代码也使用了大量C++11语法和库，VS2013及以上版本才能较好的支持C++11的语法。

2. 打开的解决方案包括三个项目：Flamingo是即时通讯主程序，CatchScreen是聊天中使用的截图工具，iUpdateAuto是升级功能中用到的解压工具。

3. 用VS2013编译整个解决方法即可，编译成功以后将在Bin目录下生成对应的程序。启动Flamingo.exe注册一个账号就可以开始使用flamingo了。

 

**Android客户端编译方法**

使用 Android Studio 打开对应的 flamingo 安卓项目编译，生成 apk 文件安装到手机上即可使用。

 

**测试服务器地址可以在登录界面的网络设置里面进行设置（登录界面右上角最小化按钮左边的一个按钮）。**