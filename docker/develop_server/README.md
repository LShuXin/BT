# develop

> 在制作镜像时，自己的 Docker 实际已经配置了国内镜像源，但是在构建时仍然开着 VPN，在这种情况下构建时会发生下载依赖失败，关掉 VPN 后不再超时。
>
> 镜像提供 openeuler/centos 两个版本
>
> develop_base、develop_server 这种镜像安排使 develop_base 专注于提供开发环境、编译环境，develop_server 专注于以 develop_base 为基础镜像完成服务端代码构建，当修改服务端代码后再次编译服务端时，不需要在镜像中重新构建开发环境，节省了时间；此外 develop_base 也可以直接用作开发环境
>
> 在编写很长的 Dockerfile 时，可以先将 Dockerfile 写成多层镜像的构建方式，镜像构建成功之后再将可以合并的指令合并在一起，以减少镜像的层数，这种方式有助于快速定位构建镜像过程中出现的错误；当一开始就以减少镜像层数为目标将指令写在一起时，当镜像构建过程中发生错误时定位问题时可能会相对比较麻烦，因为有时候错误不明显，控制台只是提示某条构建指令执行失败了，而这里的“某条指令”可能是用 && 连接的一系列指令。此外，对于一些构建失败的情况，如果确定是某条构建指令失败了，但是并不知道具体的原因，此时可以先将从出问题的构建指令起到最后一条构建指令注释掉，先制作出一个能运行的镜像，运行该镜像，然后进入容器手动执行前面注释掉的构建指令，这样也可以加快排查错误。

## develop_base

构建此镜像的 Dockerfile 为 Dockerfile.base。 此镜像集成了 gcc-8.5.0、CMake2.7.1、libmcrypt-2.5.8、termcap-1.3.1、mhash-0.9.9.9、apache-log4cxx-0.12.1、protobuf-3.18.0、hiredis，本镜像可作为编译服务端的基础镜像，制作此镜像的目的为节省打包、开发环境配置时间，镜像已上传致 Docker Hub。可采用如下方式手动构建新镜像：

```shell
cd /centos
chmod +x ./build_base.sh && ./build_base.sh

```

## develop_server

develop_server 以 develop_base 为基础环境，可直接在 develop_server 镜像内进行服务端编译，镜像已上传致 Docker Hub。可采用如下方式手动构建新镜像：

```shell
cd /centos
chmod +x ./build_develop_server.sh && ./build_develop_server.sh
```




## openeuler/centos

- openeuler
https://www.openeuler.org/zh/
https://docs.openeuler.org/zh/
https://mirror.iscas.ac.cn/
https://docs.openeuler.org/zh/docs/20.03_LTS/docs/Releasenotes/%E6%B3%95%E5%BE%8B%E5%A3%B0%E6%98%8E.html

- centos
https://www.centos.org/

## 基础环境

```shell
这些软件包通常用于在Linux操作系统上进行软件开发、编译和构建的过程中，以及支持某些特定的库和工具。以下是这些软件包的简要说明：

cmake：CMake是一个用于管理项目构建的工具，它允许您定义和控制编译过程。

libuuid-devel：这是用于生成和操作UUID（通用唯一标识符）的库的开发包。

openssl-devel：OpenSSL是一个加密库，这个开发包包含了用于开发应用程序的头文件和静态库。

apr-devel：Apache Portable Runtime（APR）是一组库，它为Apache软件基金会的项目提供了跨平台的抽象层。apr-devel提供了APR库的开发支持。

apr-util-devel：类似于APR，但它提供了用于数据库访问等附加功能的库。apr-util-devel提供了APR Util库的开发支持。

curl-devel：cURL是一个用于进行网络数据传输的库，curl-devel包含了cURL库的开发文件。

mariadb-devel：MariaDB是一个开源的关系型数据库管理系统，mariadb-devel包含了用于开发MariaDB应用程序的头文件和库。

mariadb-common：这个软件包通常包含了MariaDB的共享配置文件和其他共享数据。

wget：wget是一个命令行工具，用于从网络上下载文件。

tar：tar是一个用于创建和解压tar存档文件的工具。

gdb.x86_64：GNU Debugger（GDB）是一个用于调试程序的工具，.x86_64表示64位版本。

make：make是一个用于自动构建和编译项目的工具，通常使用Makefile文件来定义构建规则。

gcc：GNU编译器集合，用于编译C程序。

gcc-c++：GCC的C++编译器，用于编译C++程序。

flex：Flex是用于生成词法分析器的工具，通常与Bison一起使用来构建编译器前端。

diffutils：这些工具用于比较文本文件和生成差异报告。

expat-devel：Expat是一个用于解析XML文档的库，expat-devel包含了Expat库的开发文件。

autoconf：Autoconf是一个用于自动配置软件包的工具，它可以生成配置脚本，以适应不同的系统。

automake：Automake是一个用于生成Makefile文件的工具，通常与Autoconf一起使用来构建跨平台的软件。

libtool：Libtool是一个用于管理共享库的工具，它可以确保库在不同的平台上具有良好的兼容性。

libnsl：这是一个Network Services Library，用于处理网络通信的库。

lbzip2: 是一个用于处理 bzip2 格式的压缩和解压缩工具

这些软件包通常在Linux系统上进行开发、编译和构建应用程序时需要安装，以满足开发和运行时的依赖关系。不同的项目可能需要不同的软件包，具体的依赖关系取决于项目的要求。


```

## 其他软件包

```shell
gcc-8.5.0.tar.gz：这是GNU Compiler Collection（GCC）的一个版本。GCC是一个广泛使用的编译器集合，用于编译多种编程语言，包括C、C++和Fortran。它在开发和构建应用程序时非常重要。

libmcrypt-2.5.8.tar.gz：libmcrypt是一个加密库，用于在应用程序中实现加密和解密功能。它为开发人员提供了加密算法的实现，用于数据安全和隐私保护。

termcap-1.3.1.tar.gz：Termcap是一个用于处理终端和终端模拟器的库。它允许应用程序与终端设备交互，从而能够处理屏幕显示、键盘输入等终端相关的功能。

mhash-0.9.9.9.tar.gz：mhash是一个哈希函数库，用于计算和管理哈希值。哈希函数在密码学、数据完整性检查和散列数据结构等方面很有用。

apache-log4cxx-0.12.1.tar.gz：Apache Log4cxx是一个C++版本的Apache Log4j库，用于日志记录。它允许应用程序开发人员将日志消息记录到不同的目标，并配置日志记录方式，以帮助调试和问题排查。

protobuf-3.18.0.tar.gz：Protocol Buffers（protobuf）是一种Google开发的数据序列化格式，用于将结构化数据序列化为二进制格式，以便在不同系统之间进行数据交换。它还具有代码生成工具，可用于生成多种编程语言的数据访问代码。

hiredis-master.zip：hiredis是一个C语言客户端库，用于与Redis数据库进行交互。Redis是一种内存数据库，hiredis允许开发人员通过C语言编写的应用程序与Redis服务器进行通信。

这些软件都是开源项目，可以根据需要进行编译和安装，以在您的系统上使用它们的功能。在编译和安装之前，通常需要按照每个软件包的文档提供的指南进行配置和构建。
```
