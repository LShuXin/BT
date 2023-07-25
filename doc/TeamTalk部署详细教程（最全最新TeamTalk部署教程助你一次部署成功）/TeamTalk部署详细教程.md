### TeamTalk部署详细教程

[TOC]



### 背景

TeamTalk是一款蘑菇街开源的企业内部即时通讯软件，目前支持pc、安卓、IOS、Mac和web多个终端。这是各个版本的代码和部署脚本。
最近在部署TeamTalk服务端的过程中，绕了很多弯路。尝试过使用官网提供的默认的一键配置，尝试过所谓的TeamTalk安装部署手册，后面遇到了各种各样的问题导致部署失败。

现在将最后实践有效的部署过程，结合网友的资料，在此记录与分享一下。
文章中已经替换了原文中已经失效的源，修改了之前网友整理该文档时的一些命令的排版问题，同时修改了部署过程中遇到的一些其他问题。

> 安装环境：腾讯云服务器 1核 2GB 1Mbps
> 操作系统：centos7.8
> 源码下载：https://[github](https://so.csdn.net/so/search?q=github&spm=1001.2101.3001.7020).com/mogujie/TeamTalk

### 1、更新操作系统

更新操作系统:
CentOS 使用如下命令:

```
yum update
```

Ubuntu 使用如下命令:

```
apt-get update
```

### 2、删除已经安装的软件

为了减少一些不必要的麻烦，我们需要先卸载系统自带的一些软件，譬如mysql，nginx，php，执行以下命令:
CentOS 执行如下命令:

```
yum -y remove httpd* php* mysql-server mysql mysql-libs php-mysql
```

Ubuntu 使用如下命令:

```
apt-get autoremove -yapt-get -fy installapt-get install -y build-essential gcc g++ makeapt-get install -y --force-yes wget vim git texinfo patch build-essential gcc g++ make cmake automake autoconf re2c wget cron bzip2 libzip-dev libc6-dev file rcconf flex vim nano bison m4 gawk less make cpp binutils diffutils unzip tar bzip2 libbz2-dev unrar p7zip libncurses5-dev libncurses5 libncurses5-dev libncurses5-dev libtool libevent-dev libpcre3 libpcre3-dev libpcrecpp0  libssl-dev zlibc openssl libsasl2-dev libltdl3-dev libltdl-dev libmcrypt-dev zlib1g zlib1g-dev libbz2-1.0 libbz2-dev libglib2.0-0 libglib2.0-dev libpng3 libjpeg62 libjpeg62-dev libjpeg-dev libpng-dev libpng12-0 libpng12-dev curl libcurl3 libmhash2 libmhash-dev libpq-dev libpq5 gettext libncurses5-dev libcurl4-gnutls-dev libjpeg-dev libpng12-dev libxml2-dev zlib1g-dev libfreetype6 libfreetype6-dev libssl-dev libcurl3 libcurl4-openssl-dev libcurl4-gnutls-dev mcrypt libcap-dev diffutils ca-certificates debian-keyring debian-archive-keyring;apt-get -fy installapt-get -y autoremove
```

### 3、安装必要的依赖软件

如果CentOS是最小化安装，系统中很多软件是没有安装的，需要进行手动安装。
执行如下命令安装一些依赖软件:
CentOS 使用如下命令:

```
yum -y install wget vim git texinfo patch make cmake gcc gcc-c++ gcc-g77 flex bison file libtool libtool-libs autoconf kernel-devel libjpeg libjpeg-devel libpng libpng-devel libpng10 libpng10-devel gd gd-devel freetype freetype-devel libxml2 libxml2-devel zlib zlib-devel glib2 glib2-devel bzip2 bzip2-devel libevent libevent-devel ncurses ncurses-devel curl curl-devel e2fsprogs e2fsprogs-devel krb5 krb5-devel libidn libidn-devel openssl openssl-devel vim-minimal nano fonts-chinese gettext gettext-devel ncurses-devel gmp-devel pspell-devel unzip libcap diffutils
```

ubuntu 使用如下命令:

```
apt-get autoremove -yapt-get -fy installapt-get install -y build-essential gcc g++ makeapt-get install -y --force-yes wget vim git texinfo patch build-essential gcc g++ make cmake automake autoconf re2c wget cron bzip2 libzip-dev libc6-dev file rcconf flex vim nano bison m4 gawk less make cpp binutils diffutils unzip tar bzip2 libbz2-dev unrar p7zip libncurses5-dev libncurses5 libncurses5-dev libncurses5-dev libtool libevent-dev libpcre3 libpcre3-dev libpcrecpp0  libssl-dev zlibc openssl libsasl2-dev libltdl3-dev libltdl-dev libmcrypt-dev zlib1g zlib1g-dev libbz2-1.0 libbz2-dev libglib2.0-0 libglib2.0-dev libpng3 libjpeg62 libjpeg62-dev libjpeg-dev libpng-dev libpng12-0 libpng12-dev curl libcurl3 libmhash2 libmhash-dev libpq-dev libpq5 gettext libncurses5-dev libcurl4-gnutls-dev libjpeg-dev libpng12-dev libxml2-dev zlib1g-dev libfreetype6 libfreetype6-dev libssl-dev libcurl3 libcurl4-openssl-dev libcurl4-gnutls-dev mcrypt libcap-dev diffutils ca-certificates debian-keyring debian-archive-keyring;apt-get -fy installapt-get -y autoremove
```

### 4、安装mysql

#### 4.1 下载

```
wget http://mirrors.sohu.com/mysql/MySQL-5.6/mysql-5.6.45.tar.gz
```

#### 4.2 解压编译

执行如下命令:

```
tar -zxvf mysql-5.6.45.tar.gz 
cd mysql-5.6.45 
cmake -DCMAKE_INSTALL_PREFIX=/usr/local/mysql -DEXTRA_CHARSETS=all -DDEFAULT_CHARSET=utf8 -DDEFAULT_COLLATION=utf8_general_ci -DWITH_READLINE=1 -DWITH_SSL=system -DWITH_ZLIB=system -DWITH_EMBEDDED_SERVER=1 -DENABLED_LOCAL_INFILE=1
make -j 2 && make install
```

编译将是一个漫长得过程。。。不同的机器性能等待时间不同。
make的-j参数可以使make进行并行编译编译。cpu的个数是2，所以指定为2.

#### 4.3 添加mysql用户

```
groupadd mysql
useradd -s /sbin/nologin -M -g mysql mysql
```

#### 4.4 修改配置文件

```
vim /etc/my.cnf
```

下面给出一份参考配置(只是测试用，如果要用于生产环境，请根据需求自行调配):

```
# Example MySQL config file for medium systems.

# The following options will be passed to all MySQL clients
[client]
#password   = your_password
port        = 3306
socket      = /tmp/mysql.sock
default-character-set=utf8mb4

# Here follows entries for some specific programs

# The MySQL server
[mysqld]
bind-address=127.0.0.1
port        = 3306
socket      = /tmp/mysql.sock
datadir = /usr/local/mysql/var
collation-server     = utf8mb4_general_ci
character-set-server = utf8mb4
skip-external-locking
key_buffer_size = 16M
max_allowed_packet = 1M
table_open_cache = 64
sort_buffer_size = 512K
net_buffer_length = 8K
read_buffer_size = 256K
read_rnd_buffer_size = 512K
myisam_sort_buffer_size = 8M

# Replication Master Server (default)
# binary logging is required for replication
log-bin=mysql-bin

# binary logging format - mixed recommended
binlog_format=mixed

# required unique id between 1 and 2^32 - 1
# defaults to 1 if master-host is not set
# but will not function as a master if omittedserver-id   = 1

# Uncomment the following if you are using InnoDB tablesinnodb_data_home_dir = /usr/local/mysql/varinnodb_data_file_path = ibdata1:10M:autoextendinnodb_log_group_home_dir = /usr/local/mysql/var
# You can set .._buffer_pool_size up to 50 - 80 %
# of RAM but beware of setting memory usage too highinnodb_buffer_pool_size = 16Minnodb_additional_mem_pool_size = 2M
# Set .._log_file_size to 25 % of buffer pool sizeinnodb_log_file_size = 5Minnodb_log_buffer_size = 8Minnodb_flush_log_at_trx_commit = 1
innodb_lock_wait_timeout = 50

[mysqldump]
quickmax_allowed_packet = 16M

[mysql]
no-auto-rehash
# Remove the next comment character if you are not familiar with SQL
#safe-updatesdefault-character-set=utf8mb4

[myisamchk]
key_buffer_size = 20Ms
ort_buffer_size = 20M
read_buffer = 2M
write_buffer = 2M

[mysqlhotcopy]
interactive-timeout
```

#### 4.5 初始化mysql

```
/usr/local/mysql/scripts/mysql_install_db --defaults-file=/etc/my.cnf --basedir=/usr/local/mysql --datadir=/usr/local/mysql/var --user=mysql

chown -R mysql /usr/local/mysql/var
chgrp -R mysql /usr/local/mysql/.
cp support-files/mysql.server /etc/init.d/mysql
chmod 755 /etc/init.d/mysql

vim /etc/ld.so.conf.d/mysql.conf
```

在该文件中输入如下内容：

```
/usr/local/mysql/lib
/usr/local/lib

ldconfig
```

#### 4.6 启动mysql

```
/etc/init.d/mysql start
```

#### 4.7 查看到mysql进程，安装成功

```
ps -ef|grep mysql
```

#### 4.8 后期配置

```
ln -s /usr/local/mysql/lib/mysql /usr/lib/mysql
ln -s /usr/local/mysql/include/mysql /usr/include/mysql
ln -s /usr/local/mysql/bin/mysql /usr/bin/mysql
ln -s /usr/local/mysql/bin/mysqldump /usr/bin/mysqldump
ln -s /usr/local/mysql/bin/myisamchk /usr/bin/myisamchk
ln -s /usr/local/mysql/bin/mysqld_safe /usr/bin/mysqld_safe
```

登陆mysql:

```
mysql -uroot -p
```

修改密码(假定密码为:test123，这里根据需要自己设置，但是后面还会用到这个密码，自己记一下):
下面指令中的mysqlrootpwd改为自己的密码再执行。

```
use mysql;
update user set password=password('$mysqlrootpwd') where user='root';
flush privileges;
```

退出，重新登陆:

```
mysql -uroot -p
```

#### 4.9 结束

至此，mysql 已经安装结束。退出到上一层目录

```
cd ../
```

### 5、安装PHP

本次安装的PHP是php 5.3.28，选择从搜狐源下载。

#### 5.1 下载PHP

```
wget http://mirrors.sohu.com/php/php-5.3.28.tar.gz
```

#### 5.2 安装依赖

##### 5.2.1 libiconv

```
wget http://ftp.gnu.org/pub/gnu/libiconv/libiconv-1.14.tar.gz
tar -zxvf libiconv-1.14.tar.gz
cd libiconv-1.14
./configure
make -j 2&& make install
cd ..
```

##### 5.2.2 libmcrypt

```
wget https://sourceforge.net/projects/mcrypt/files/Libmcrypt/2.5.8/libmcrypt-2.5.8.tar.gz
tar zxvf libmcrypt-2.5.8.tar.gz
cd libmcrypt-2.5.8/
./configure
make
make install
/sbin/ldconfig
cd libltdl/
./configure --enable-ltdl-install
make
make install
cd ../../
```

##### 5.2.3 mhash

```
wget https://sourceforge.net/projects/mhash/files/latest/download/mhash-0.9.9.9.tar.gz
tar -zxvf mhash-0.9.9.9.tar.gz
cd mhash-0.9.9.9
./configure
make -j 2 && make install
cd ../
```

#### 5.3 解压编译

```
tar -zxvf php-5.3.28.tar.gz
cd php-5.3.28
./configure --prefix=/usr/local/php --with-config-file-path=/usr/local/php/etc --enable-fpm --with-fpm-user=www --with-fpm-group=www --with-mysql=mysqlnd --with-mysqli=mysqlnd --with-pdo-mysql=mysqlnd --with-iconv-dir --with-freetype-dir --with-jpeg-dir --with-png-dir --with-zlib --with-libxml-dir=/usr --enable-xml --disable-rpath --enable-magic-quotes --enable-safe-mode --enable-bcmath --enable-shmop --enable-sysvsem --enable-inline-optimization --with-curl --enable-mbregex --enable-mbstring --with-mcrypt --enable-ftp --with-gd --enable-gd-native-ttf --with-openssl --with-mhash --enable-pcntl --enable-sockets --with-xmlrpc --enable-zip --enable-soap --without-pear --with-gettext --disable-fileinfo
make -j 2 ZEND_EXTRA_LIBS='-liconv' && make install
```

#### 5.4 配置php

```
cp php.ini-production /usr/local/php/etc/php.ini
sed -i 's/post_max_size = 8M/post_max_size = 50M/g' /usr/local/php/etc/php.ini
sed -i 's/upload_max_filesize = 2M/upload_max_filesize = 50M/g' /usr/local/php/etc/php.ini
sed -i 's/;date.timezone =/date.timezone = PRC/g' /usr/local/php/etc/php.ini
sed -i 's/short_open_tag = Off/short_open_tag = On/g' /usr/local/php/etc/php.ini
sed -i 's/; cgi.fix_pathinfo=1/cgi.fix_pathinfo=0/g' /usr/local/php/etc/php.ini
sed -i 's/; cgi.fix_pathinfo=0/cgi.fix_pathinfo=0/g' /usr/local/php/etc/php.ini
sed -i 's/;cgi.fix_pathinfo=1/cgi.fix_pathinfo=0/g' /usr/local/php/etc/php.ini
sed -i 's/max_execution_time = 30/max_execution_time = 300/g' /usr/local/php/etc/php.ini
sed -i 's/register_long_arrays = On/;register_long_arrays = On/g' /usr/local/php/etc/php.ini
sed -i 's/magic_quotes_gpc = On/;magic_quotes_gpc = On/g' /usr/local/php/etc/php.ini
sed -i 's/disable_functions =.*/disable_functions = passthru,exec,system,chroot,scandir,chgrp,chown,shell_exec,proc_open,proc_get_status,ini_alter,ini_restore,dl,openlog,syslog,readlink,symlink,popepassthru,stream_socket_server/g' /usr/local/php/etc/php.ini
```

#### 5.5 后期配置

```
ln -s /usr/local/php/bin/php /usr/bin/php
ln -s /usr/local/php/bin/phpize /usr/bin/phpize
ln -s /usr/local/php/sbin/php-fpm /usr/bin/php-fpm

cd ..
```

#### 5.6 安装ZendGuardLoader

```
mkdir -p /usr/local/zend/
wget http://downloads.zend.com/guard/5.5.0/ZendGuardLoader-php-5.3-linux-glibc23-x86_64.tar.gz
tar -zxvf ZendGuardLoader-php-5.3-linux-glibc23-x86_64.tar.gz
cp ZendGuardLoader-php-5.3-linux-glibc23-x86_64/php-5.3.x/ZendGuardLoader.so /usr/local/zend/

vi  /usr/local/php/etc/php.ini

;eaccelerator;ionCube[Zend Optimizer]
zend_extension=/usr/local/zend/ZendGuardLoader.so
zend_loader.enable=1
zend_loader.disable_licensing=0
zend_loader.obfuscation_level_support=3
zend_loader.license_path=
EOF
```

#### 5.7 修改php-fpm配置文件

```
vim /usr/local/php/etc/php-fpm.conf

[global]
pid = /usr/local/php/var/run/php-fpm.pid
error_log = /usr/local/php/var/log/php-fpm.log
log_level = notice

[www]
listen = /tmp/php-cgi.sock
listen.backlog = -1
listen.allowed_clients = 127.0.0.1
listen.owner = www
listen.group = www
listen.mode = 0666user = www
group = www
pm = dynamic
pm.max_children = 10
pm.start_servers = 2
pm.min_spare_servers = 1
pm.max_spare_servers = 6
request_terminate_timeout = 100
request_slowlog_timeout = 0
slowlog = var/log/slow.log
```

5.8 创建php-fpm启动脚本

```
vim /etc/init.d/php-fpm
chmod +x /etc/init.d/php-fpm
```

以下是一份参考:

```
#! /bin/sh

### BEGIN INIT INFO
# Provides:          php-fpm
# Required-Start:    $remote_fs $network
# Required-Stop:     $remote_fs $network
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Short-Description: starts php-fpm
# Description:       starts the PHP FastCGI Process Manager daemon
### END INIT INFO
prefix=/usr/local/php
exec_prefix=${prefix}
php_fpm_BIN=${exec_prefix}/sbin/php-fpm
php_fpm_CONF=${prefix}/etc/php-fpm.conf
php_fpm_PID=${prefix}/var/run/php-fpm.pid
php_opts="--fpm-config $php_fpm_CONF --pid $php_fpm_PID"

wait_for_pid () {
        try=0
        while test $try -lt 35 ; do
                case "$1" in
                        'created')
                        if [ -f "$2" ] ; then
                                try=''
                                break
                        fi
                        ;;
                        'removed')
                        if [ ! -f "$2" ] ; then
                                try=''
                                break
                        fi
                        ;;
                esac
                echo -n .
                try=`expr $try + 1`
                sleep 1
        done
}

case "$1" in
        start)
                echo -n "Starting php-fpm "
                $php_fpm_BIN --daemonize $php_opts
                if [ "$?" != 0 ] ; then
                        echo " failed"
                        exit 1
                fi
                wait_for_pid created $php_fpm_PID

                if [ -n "$try" ] ; then
                        echo " failed"
                        exit 1
                else
                        echo " done"
                fi
        ;;
        stop)
                echo -n "Gracefully shutting down php-fpm "

                if [ ! -r $php_fpm_PID ] ; then
                        echo "warning, no pid file found - php-fpm is not running ?"
                        exit 1
                fi

                kill -QUIT `cat $php_fpm_PID`

                wait_for_pid removed $php_fpm_PID

                if [ -n "$try" ] ; then
                        echo " failed. Use force-quit"
                        exit 1
                else
                        echo " done"
                fi
        ;;

        force-quit)
                echo -n "Terminating php-fpm "

                if [ ! -r $php_fpm_PID ] ; then
                        echo "warning, no pid file found - php-fpm is not running ?"
                        exit 1
                fi
                kill -TERM `cat $php_fpm_PID`
                wait_for_pid removed $php_fpm_PID

                if [ -n "$try" ] ; then
                        echo " failed"
                        exit 1
                else
                        echo " done"
                fi
        ;;
        restart)
                $0 stop
                $0 start
        ;;
        reload)
                echo -n "Reload service php-fpm "
                if [ ! -r $php_fpm_PID ] ; then
                        echo "warning, no pid file found - php-fpm is not running ?"
                        exit 1
                fi
                kill -USR2 `cat $php_fpm_PID`

                echo " done"
        ;;
        *)
                echo "Usage: $0 {start|stop|force-quit|restart|reload}"
                exit 1
        ;;

esac
```

5.9 启动php-fpm

```
groupadd www
useradd -s /sbin/nologin -g www www
/etc/init.d/php-fpm start
```

### 6、 安装nginx

#### 6.1 下载nginx

```
wget http://mirrors.sohu.com/nginx/nginx-1.6.0.tar.gz
```

#### 6.2 安装依赖

##### 6.2.1 pcre

```
wget https://sourceforge.net/projects/pcre/files/pcre/8.39/pcre-8.39.tar.bz2
tar -jxvf pcre-8.39.tar.bz2
cd pcre-8.39
./configure
make -j 2 && make install 
cd ..
```

#### 6.3 解压编译nginx

```
tar -zxvf nginx-1.6.0.tar.gz
cd nginx-1.6.0./configure --user=www --group=www --prefix=/usr/local/nginx --with-http_stub_status_module --with-http_ssl_module --with-http_gzip_static_module --with-ipv6
make -j 2 && make install
cd ..
ln -s /usr/local/nginx/sbin/nginx /usr/bin/nginx
```

#### 6.4 配置nginx

```
/usr/local/nginx/conf/nginx.conf
```

下面是一份参考配置:

```
user  www www;
worker_processes auto;
error_log  /home/wwwlogs/nginx_error.log  crit;
pid        /usr/local/nginx/logs/nginx.pid;
#Specifies the value for maximum file descriptors that can be opened by this process.
worker_rlimit_nofile 51200;
events
    {
        use epoll;
        worker_connections 51200;
        multi_accept on;
    }
http
    {
        include       mime.types;
        default_type  application/octet-stream;
        server_names_hash_bucket_size 128;
        client_header_buffer_size 32k;
        large_client_header_buffers 4 32k;
        client_max_body_size 50m;
        sendfile on;
        tcp_nopush     on;
        keepalive_timeout 60;
        tcp_nodelay on;
        fastcgi_connect_timeout 300;
        fastcgi_send_timeout 300;
        fastcgi_read_timeout 300;
        fastcgi_buffer_size 64k;
        fastcgi_buffers 4 64k;
        fastcgi_busy_buffers_size 128k;
        fastcgi_temp_file_write_size 256k;
        gzip on;
        gzip_min_length  1k;
        gzip_buffers     4 16k;
        gzip_http_version 1.0;
        gzip_comp_level 2;
        gzip_types       text/plain application/x-javascript text/css application/xml;
        gzip_vary on;
        gzip_proxied        expired no-cache no-store private auth;
        gzip_disable        "MSIE [1-6]\.";
        server_tokens off;
        #log_format  access  '$remote_addr - $remote_user [$time_local] "$request" ''$status $body_bytes_sent "$http_referer" ''"$http_user_agent" $http_x_forwarded_for';
        log_format  access  '$remote_addr - $remote_user [$time_local] "$request" '
             '$status $body_bytes_sent "$http_referer" '
             '"$http_user_agent" $http_x_forwarded_for "$upstream_addr" "$upstream_response_time" $request_time $content_length';
    server
    {
            listen       80;
            server_name localhost;
            index index.html index.htm index.php default.html default.htm default.php;
            root        /home/wwwroot/default;

            location ~ \.php($|/) {
                fastcgi_pass   unix:/tmp/php-cgi.sock;
                fastcgi_index  index.php;
                fastcgi_split_path_info ^(.+\.php)(.*)$;
                fastcgi_param   PATH_INFO $fastcgi_path_info;
                fastcgi_param  SCRIPT_FILENAME  $document_root$fastcgi_script_name;
                include        fastcgi_params;
            }

            location ~ .*\.(gif|jpg|jpeg|png|bmp|swf)$
                    {
                            expires      30d;
                    }

            location ~ .*\.(js|css)?$
                    {
                            expires      12h;
                    }
            if (!-e $request_filename) {
                rewrite ^/(.*)$ /index.php/$1 last;
                break;
            }
    }
}
```

#### 6.5 后期配置

```
mkdir -p /home/wwwroot/default
chmod +w /home/wwwroot/default
mkdir -p /home/wwwlogs
chmod 777 /home/wwwlogs

chown -R www:www /home/wwwroot/default
```

6.6 编写nginx启动脚本

```
vim /etc/init.d/nginx
chmod +x /etc/init.d/nginx
```

下面是一份参考配置:

```
#!/bin/sh
# chkconfig: 2345 55 25
# Description: Startup script for nginx webserver on Debian. Place in /etc/init.d and
# run 'update-rc.d -f nginx defaults', or use the appropriate command on your
# distro. For CentOS/Redhat run: 'chkconfig --add nginx'
### BEGIN INIT INFO# Provides:          nginx
# Required-Start:    $all
# Required-Stop:     $all
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Short-Description: starts the nginx web server# Description:       starts nginx using start-stop-daemon
### END INIT INFO
# Author:   licess
# website:  http://lnmp.org

PATH=/usr/local/sbin:/usr/local/bin:/sbin:/bin:/usr/sbin:/usr/bin
NAME=nginx
NGINX_BIN=/usr/local/nginx/sbin/$NAME
CONFIGFILE=/usr/local/nginx/conf/$NAME.conf
PIDFILE=/usr/local/nginx/logs/$NAME.pid
SCRIPTNAME=/etc/init.d/$NAME
case "$1" in
    start)
        echo -n "Starting $NAME... "
        if netstat -tnpl | grep -q nginx;
        then
        echo "$NAME (pid `pidof $NAME`) already running."
        exit 1
        fi
        $NGINX_BIN -c $CONFIGFILE
        if [ "$?" != 0 ] ; 
        then
        echo " failed"
        exit 1
        else
        echo " done"
        fi
    ;;

    stop)
        echo -n "Stoping $NAME... "
        if ! netstat -tnpl | grep -q nginx; 
        then
        echo "$NAME is not running."
        exit 1
        fi
        $NGINX_BIN -s stop

        if [ "$?" != 0 ] ; then
        echo " failed. Use force-quit"
        exit 1
        else
    echo " done"
        fi
    ;;

    status)
        if netstat -tnpl | grep -q nginx; then
            PID=`pidof nginx`
            echo "$NAME (pid $PID) is running..."
            else
            echo "$NAME is stopped"
            exit 0
            fi
    ;;

    force-quit)
        echo -n "Terminating $NAME... "
        if ! netstat -tnpl | grep -q nginx; 
        then
        echo "$NAME is not running."
        exit 1
        fi

        kill `pidof $NAME`

        if [ "$?" != 0 ] ; 
        then
        echo " failed"
        xit 1
        else
        echo " done"
        fi
    ;;

    restart)
        $SCRIPTNAME stop
        sleep 1$SCRIPTNAME start
    ;;

    reload)

        echo -n "Reload service $NAME... "
        if netstat -tnpl | grep -q nginx; 
        then $NGINX_BIN -s reload
            echo " done"elseecho "$NAME is not running, can't reload."
            exit 1
            fi
    ;;

    configtest)

        echo -n "Test $NAME configure files... "$NGINX_BIN -t
    ;;

    *)
        echo "Usage: $SCRIPTNAME {start|stop|force-quit|restart|reload|status|configtest}"
        exit 1
    ;;
esac
```

#### 6.6 测试nginx

##### 6.6.1 写php测试代码

```
cat >/home/wwwroot/default/index.php<<EOF
<?
phpinfo();
?>
EOF
```

##### 6.6.2启动nginx

```
/etc/init.d/nginx startps -ef|grep nginx
```

如果你开启了selinux，请关闭，否则访问不了:

```
sed -i 's/SELINUX=enforcing/SELINUX=disabled/g' /etc/selinux/config
```

临时关闭selinux:

```
setenforce 0
```

关闭防火墙:

```
service iptables stop
```

#### 6.6.3 设置开机启动

```
chkconfig --level 345 php-fpm on
chkconfig --level 345 nginx on
chkconfig --level 345 mysql on
```

### 7、 安装redis

#### 7.1 下载redis

```
wget http://download.redis.io/releases/redis-2.8.19.tar.gz
```

#### 7.2 解压编译redis

```
tar -zxvf redis-2.8.19.tar.gz
cd redis-2.8.19
make PREFIX=/usr/local/redis install
```

#### 7.3 配置redis

```
mkdir -p /usr/local/redis/etc/
cp redis.conf  /usr/local/redis/etc/
sed -i 's/daemonize no/daemonize yes/g' /usr/local/redis/etc/redis.conf
cd ..
```

#### 7.4 编写redis启动脚本

```
vim /etc/init.d/redis
chmod +x /etc/init.d/redis
```

下面是一份参考配置:

```
#! /bin/bash
## redis - this script starts and stops the redis-server daemon
## chkconfig:    2345 80 90
# description:  Redis is a persistent key-value database##
## BEGIN INIT INFO
# Provides:          redis
# Required-Start:    $syslog
# Required-Stop:     $syslog# Should-Start:        $local_fs# Should-Stop:        $local_fs
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Short-Description:    redis-server daemon
# Description:        redis-server daemon
### END INIT INFO

REDISPORT=6379
EXEC=/usr/local/redis/bin/redis-server
REDIS_CLI=/usr/local/redis/bin/redis-cli

PIDFILE=/var/run/redis.pid
CONF="/usr/local/redis/etc/redis.conf"
case "$1" in
    start)
        if [ -f $PIDFILE ]
        then
        echo "$PIDFILE exists, process is already running or crashed"
        else
        echo "Starting Redis server..."
        $EXEC $CONF
        fi
        if [ "$?"="0" ]
        then
        echo "Redis is running..."
        fi
        ;;
    stop)
        if [ ! -f $PIDFILE ]
        then
        echo "$PIDFILE does not exist, process is not running"
        else
                PID=$(cat $PIDFILE)
                echo "Stopping ..."$REDIS_CLI -p $REDISPORT shutdown
                while [ -x ${PIDFILE} ]
                do
                echo "Waiting for Redis to shutdown ..."
                    sleep 1
                    done
                    echo "Redis stopped"
                    fi
        ;;
   restart)
        ${0} stop
        ${0} start
        ;;
  *)  
    echo "Usage: /etc/init.d/redis {start|stop|restart}" >&2
    exit 1
esac
```

#### 7.5 启动redis

```
/etc/init.d/redis start
```

查看redis是否启动

```
ps -ef|grep redis
```

### 8、 升级gcc，gdb等

(非常漫长,如果系统中自带的g++支持C++11，可跳过此步骤)

#### 8.1 下载gcc4.9.2

```
wget http://ftp.tsukuba.wide.ad.jp/software/gcc/releases/gcc-4.9.2/gcc-4.9.2.tar.gz
```

#### 8.2 解压编译gcc4.9.2

```
tar -zxvf gcc-4.9.2.tar.gz
cd gcc-4.9.2
./contrib/download_prerequisitesmkdir gcc-build-4.9.2cd gcc-build-4.9.2../configure --prefix=/usr -enable-checking=release -enable-languages=c,c++ -disable-multilib
make -j 2 && make install
cd ../../
```

#### 8.3 下载termcap

```
wget https://mirrors.sjtug.sjtu.edu.cn/gnu/termcap/termcap-1.3.1.tar.gz
```

#### 8.4 解压编译termcap

```
tar -zxvf termcap-1.3.1.tar.gz
cd termcap-1.3.1./configure --prefix=/usr
make -j 2 && make install
```

#### 8.5 下载gdb

```
wget http://ftp.gnu.org/gnu/gdb/gdb-7.9.tar.gz
```

#### 8.6 解压编译gdb

```
tar -zxvf gdb-7.9.tar.gz
cd gdb-7.9
./configure --prefix=/usr
make -j 2 && make install
```

### 9、 重启电脑

```
shutdown -r now
```

### 10、 安装PB

#### 10.1 下载pb

```
wget https://github.com/google/protobuf/releases/download/v2.6.1/protobuf-2.6.1.tar.gz
```

如果上面的下载不了，可以使用下面的链接，下载后改下名字即可

```
wget https://launchpad.net/ubuntu/+archive/primary/+sourcefiles/protobuf/2.6.1-1.3/protobuf_2.6.1.orig.tar.gz
mv protobuf_2.6.1.orig.tar.gz protobuf_2.6.1.tar.gz
```

#### 10.2 解压编译pb

```
tar -zxvf protobuf-2.6.1
cd protobuf-2.6.1
./configure --prefix=/usr/local/protobuf
make -j 2 && make install
```

### 11、 下载TeamTalk代码

```
git clone https://github.com/mogujie/TeamTalk.git
```

### 12、 生成pb文件

#### 12.1 拷贝pb相关文件

拷贝pb的库、头文件到TeamTalk相关目录中:

```
mkdir -p /root/TeamTalk/server/src/base/pb/lib/linux/
cp /usr/local/protobuf/lib/libprotobuf-lite.a /root/TeamTalk/server/src/base/pb/lib/linux/
cp  -r /usr/local/protobuf/include/* /root/TeamTalk/server/src/base/pb/
```

#### 12.2 生成pb协议

```
cd /root/TeamTalk/pb
```

执行:

```
export PATH=$PATH:/usr/local/protobuf/bin
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/usr/local/protobuf/lib

sh create.sh
```

生成协议相关源码文件。

再执行:

```
sh sync.sh
```

将相关文件拷贝到server 目录下。

### 13、 安装依赖

```
cd /root/TeamTalk/server/src
sh make_log4cxx.sh
sh make_hiredis.sh
```

### 14、 编译server

由于我们是源码安装mysql的，所以对db_proxy_server中的CMakeList做一定的修改.
原来:

```
SET(MYSQL_INCLUDE_DIR /usr/include/mysql)
SET(MYSQL_LIB /usr/lib64/mysql)
```

修改为:

```
SET(MYSQL_INCLUDE_DIR /usr/local/mysql/include)
SET(MYSQL_LIB /usr/local/mysql/lib)
```

在server/src目录下，执行:

```
sh build.sh version 1.0.0
```

这时候会在src的上级目录server目录下构建出im-server-1.0.0.tar.gz的文件，将其拷贝到auto_setup目录下解压，并进入该目录。
然后执行下面的命令：

```
sh sync_lib_for_zip.sh
```

然后就可以配置并启动各个服务程序了。

### 15、 配置server(这里单独开一篇说明一下各个服务地址的配置，请看下面的链接)

https://blog.csdn.net/siyacaodeai/article/details/114981583

### 16、更新

#### 16.1 导入mysql

登陆mysql:

```
mysql -uroot -p
```

输入密码:test123
创建TeamTalk数据库:

```
create database teamtalk
```

创建成功显示如下内容：

```
mysql> create database teamtalk;
Query OK, 1 row affected (0.00 sec)
```

创建teamtalk用户并给teamtalk用户授权teamtalk的操作:

```
grant select,insert,update,delete on teamtalk.* to 'teamtalk'@'%' identified by 'test@123';
flush privileges;
```

导入数据库.

```
use teamtalk;
source /root/TeamTalk/auto_setup/mariadb/conf/ttopen.sql;
show tables;
```

成功后会如下内容:

```
mysql> show tables;
+--------------------+| Tables_in_teamtalk |+--------------------+
| IMAdmin          || IMAudio            || IMDepart           || IMDiscovery        || IMGroup            || IMGroupMember      || IMGroupMessage_0   || IMGroupMessage_1   || IMGroupMessage_2   || IMGroupMessage_3   || IMGroupMessage_4   || IMGroupMessage_5   || IMGroupMessage_6   || IMGroupMessage_7   || IMMessage_0        || IMMessage_1        || IMMessage_2        || IMMessage_3        || IMMessage_4        || IMMessage_5        || IMMessage_6        || IMMessage_7        || IMRecentSession    || IMRelationShip     || IMUser             |
+--------------------+
25 rows in set (0.00 sec)mysql>
```

#### 16.2 修改php

执行如下命令:

```
cd /home/wwwroot/default
cp -r /root/TeamTalk/php/* /home/wwwroot/default
```

修改config.php:

```
vim application/config/config.php
```

修改第18-19行:

```
$config['msfs_url'] = 'http://192.168.1.150:8700/';
$config['http_url'] = 'http://192.168.1.150:8400';
```

修改database.php

```
vim application/config/database.php
```

修改52-54行:

```
$db['default']['hostname'] = '192.168.1.150';
$db['default']['username'] = 'tamtalk';
$db['default']['password'] = 'test@123';
$db['default']['database'] = 'teamtalk';
```

这里需要注意安装自己的username和password进行修改

### 17、测试

直接在浏览器输入ip地址即可看到php页面，这时候就可以在后台添加test用户了

如果页面中出现如下错误，

```
Unable to connect to your database server using the provided settings.  
Filename: core/Loader.php  
Line Number: 346
```

可以参考后面的链接查找问题，我的修改 $db[‘default’][‘hostname’] = ‘127.0.0.1’;访问通过

[TeamTalk部署问题及解决方案](https://blog.csdn.net/siyacaodeai/article/details/114984901)

### 18、 运行服务

```
./restart.sh login_server
./restart.sh route_server
./restart.sh msg_server
./restart.sh file_server
./restart.sh msfs
./restart.sh http_msg_server
./restart.sh push_server
./restart.sh db_proxy_server
ps -ef|grep server
```

如果看到如下:

```
[root@zhyh ~]# ps -ef|grep server
root      1653     1  0 22:13 ?        00:00:05 /usr/local/redis/bin/redis-server *:6379root      1658     1  1 22:13 ?        00:00:21 ./db_proxy_server
root      1717     1  0 22:13 ?        00:00:02 ./http_msg_server
root      1729     1  0 22:13 ?        00:00:02 ./route_server
root      1737     1  0 22:14 ?        00:00:02 ./login_server
root      1757     1  0 22:15 ?        00:00:02 ./msg_server
root      1788  1774  0 22:34 pts/2    00:00:00 grep server 
```

如果没有发现:db_proxy_server, http_msg_server,route_server,login_server,msg_server的进程，请执行如下命令启动:

```
cd /usr/local/teamtalk
cd xxxx
../daeml xxxx
```

xxx代表相应的程序名。通过查看:xxxx/log/default.log 查看程序错误。

### 19、 redis,php,nginx,mysql的启动，停止与重启

```
/etc/init.d/redis {start|stop|restart}
/etc/init.d/php-fpm {start|stop|force-quit|restart|reload}
/etc/init.d/nginx {start|stop|force-quit|restart|reload|status|configtest}
/etc/init.d/mysql {start|stop|restart|reload|force-reload|status}  [ MySQL server options ]
```

### 相关文章

[TeamTalk部署详细教程（最全最新TeamTalk部署教程助你一次部署成功）](https://blog.csdn.net/siyacaodeai/article/details/114982897?spm=1001.2014.3001.5501)
[TeamTalk部署问题及解决方案](https://blog.csdn.net/siyacaodeai/article/details/114984901?spm=1001.2014.3001.5501)
[TeamTalk各个服务的IP配置方案](https://blog.csdn.net/siyacaodeai/article/details/114981583?spm=1001.2014.3001.5501)
[TeamTalk WinClient编译问题及解决方案](https://blog.csdn.net/siyacaodeai/article/details/115359898)