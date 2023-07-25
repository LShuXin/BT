

# TeamTalk部署问题及解决方案

[TOC]

### 1、部分源下载地址

gmp

```
wget ftp://ftp.gnu.org/gnu/gmp/gmp-5.1.3.tar.gz
```

mpfr

```
wget ftp://ftp.gnu.org/gnu/mpfr/mpfr-3.1.2.tar.gz
```

mpc

```
wget http://www.multiprecision.org/downloads/mpc-1.0.tar.gz
```

php

```
wget http://mirrors.sohu.com/php/php-5.3.28.tar.gz
```

nginx

```
wget http://mirrors.sohu.com/nginx/nginx-1.6.0.tar.gz
```

apache-log4cxx-0.10.0

```
https://archive.apache.org/dist/logging/log4cxx/0.10.0/apache-log4cxx-0.10.0.tar.gz
```

MariaDB

```
//mariadb:make_mariadb.sh  原下载地址已不再使用所以替换下 

#这个名称需要根据不同的系统版本进行修改并查看确认下载服务其上是否存在对应的文件
MARIADB_DEVEL=MariaDB-10.0.17-centos7-x86_64-devel             
MARIADB_DEVEL_DOWNLOAD_PATH=https://archive.mariadb.org/mariadb-10.0.17/yum/centos7-amd64/rpms/$MARIADB_DEVEL.rpm

MARIADB_COMMON=MariaDB-10.0.17-centos7-x86_64-common
MARIADB_COMMON_DOWNLOAD_PATH=https://archive.mariadb.org/mariadb-10.0.17/yum/centos7-amd64/rpms/$MARIADB_COMMON.rpm

MARIADB_COMPAT=MariaDB-10.0.17-centos7-x86_64-compat
MARIADB_COMPAT_DOWNLOAD_PATH=https://archive.mariadb.org/mariadb-10.0.17/yum/centos7-amd64/rpms/$MARIADB_COMPAT.rpm
```

### 2、编译安装libiconv报错

编译安装libiconv报错：

```
./stdio.h:1010:1: error: ‘gets’ undeclared here (not in a function)
```

解决方法：
```
vi libiconv-1.14/srclib/stdio.in.h
```

将698行的代码：

```
_GL_WARN_ON_USE (gets, “gets is a security hole - use fgets instead”);
```

替换为：

```
#if defined(**GLIBC**) && !defined(**UCLIBC**) && !__GLIBC_PREREQ(2, 16)
_GL_WARN_ON_USE (gets, “gets is a security hole - use fgets instead”);
#endif
```



### 3、找不到tt

部署说明中，有提示。
im_web 与TeamTalk web管理相关的部署，包含了PHP的配置以及php所需nginx相关配置。需要将php目录更名为tt并打包压缩放到此目录下,否则会报如下错误:

```
unzip: cannot find or open tt.zip, tt.zip.zip or tt.zip.ZIP。
```



### 4、编译im-server

到/server/src目录下
```
./build.sh version 1
```

特别注意，如果系统gcc版本不是4.8的要升级一下，要不然会报编译错误。
编译成功后，在server目录下会生成 im-server-1.tar.gz文件

### 5、缺少daeml

到server/src/tools/执行

```
g++ -Wall -o daeml daeml.cpp 
```

将daeml移动到im-server-1（根据编译后的具体名称找到对应目录）下

```
cp /root/TeamTalk/server/src/tools/daeml  /root/TeamTalk/auto_setup/im_server/im-server-1/
```

### 6、找不到mysql.h

居然说没有mysql.h这个文件，可是我确实安装了mysql了
原来是缺少libmysqlclient-dev于是安装一下即可
ubuntu下 ：

```
audo apt-get install libmysqlclient-dev
```

centos下 : 

```
yum install mysql-devel
```



### 7、centos7 mini 安装后无法连接到网络

环境：vm虚拟机,桥接网络模式

```
cd /etc/sysconfig/network-scripts
vi ifcfg-ens33
将ONBOOT=yes

sudo vi /etc/sysconfig/network-scripts/ifcfg-ens33

//
TYPE=Ethernet
PROXY_METHOD=none
BROWSER_ONLY=no
BOOTPROTO=dhcp
DEFROUTE=yes
IPV4_FAILURE_FATAL=no
IPV6INIT=yes
IPV6_AUTOCONF=yes
IPV6_DEFROUTE=yes
IPV6_FAILURE_FATAL=no
IPV6_ADDR_GEN_MODE=stable-privacy
NAME=ens33
UUID=abd0aaff-b087-4ecc-a46a-91e71baae414
DEVICE=ens33
ONBOOT=yes
//
```

重启网络服务 `sudo service network restart` 即可

### 8、使用mwget提高下载速度

安装mwget

```
wget http://jaist.dl.sourceforge.net/project/kmphpfm/mwget/0.1/mwget_0.1.0.orig.tar.bz2
tar -xjvf mwget_0.1.0.orig.tar.bz2
cd mwget_0.1.0.orig
./configure
make
make install
```

### 9、nginx: [emerg] unknown log format "access" in错误解决方法

今天想打开nginx的日志，在vhost的配置如下：
```
access_log /data/logs/a.com_access.log access;
error_log /data/logs/a.com_error.log warn;
```

但是在执行nginx -t的时候一直报以下错误：
```
nginx: [emerg] unknown log format “access” in /usr/local/nginx/conf/vhost/a.conf:4
```

在网上反复搜索了下，原来是log_format没有打开的原因，只需要在主配置文件nginx.conf上加入以下代码就OK了，切记一定是要在include vhost/*.conf;之前加上：

```
log_format  access  '$remote_addr - $remote_user [$time_local] "$request" '
             '$status $body_bytes_sent "$http_referer" '
             '"$http_user_agent" $http_x_forwarded_for "$upstream_addr" "$upstream_response_time" $request_time $content_length';
```

### 10、PHP报错

错误提示如下：

```
Unable to connect to your database server using the provided settings.  
Filename: core/Loader.php  
Line Number: 346
```

修改为了 `$db[‘default’][‘hostname’] = ‘127.0.0.1’;`访问通过了
如果不能解决可以参考以下链接进行排查
https://stackoverflow.com/questions/7254049/codeigniter-unable-to-connect-to-your-database-server-using-the-provided-settin

