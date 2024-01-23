FROM centos:centos7.9.2009 AS base
WORKDIR /teamtalk
EXPOSE 80

RUN yum update -y &&\
    yum install -y wget tar bzip2 zip unzip openssl-devel net-tools cmake make gcc gcc-c++ flex ncurses-devel curl-devel apr-devel apr-util-devel libuuid-devel libjpeg libjpeg-devel libpng libpng-devel libpng10 libpng10-devel gd gd-devel freetype freetype-devel libxml2 libxml2-devel

COPY docker/web_server/libiconv-1.14.modified.tar.gz ./
RUN tar -zxvf libiconv-1.14.modified.tar.gz &&\ 
    cd libiconv-1.14 &&\
    ./configure &&\
    make -j2 && make install &&\
    cd /teamtalk && rm -rf libiconv-1.14*

COPY docker/web_server/libmcrypt-2.5.8.tar.gz ./
RUN tar -zxvf libmcrypt-2.5.8.tar.gz &&\
    cd libmcrypt-2.5.8/ &&\
    ./configure &&\
    make -j2 && make install &&\
    ldconfig &&\
    cd libltdl/ &&\
    ./configure --enable-ltdl-install &&\
    make -j2 && make install &&\
    cd /teamtalk && rm -rf libmcrypt-2.5.8*

COPY docker/web_server/mhash-0.9.9.9.tar.gz ./
RUN tar -xf mhash-0.9.9.9.tar.gz &&\
    cd mhash-0.9.9.9 &&\
    ./configure &&\
    make -j2 && make install &&\
    cd /teamtalk && rm -rf mhash-0.9.9.9*

COPY docker/web_server/php-5.3.28.tar.gz ./
RUN tar -zxvf php-5.3.28.tar.gz &&\
    cd php-5.3.28 &&\
    ./configure --prefix=/usr/local/php --with-config-file-path=/usr/local/php/etc --enable-fpm --with-fpm-user=www --with-fpm-group=www --with-mysql=mysqlnd --with-mysqli=mysqlnd --with-pdo-mysql=mysqlnd --with-iconv-dir --with-freetype-dir --with-jpeg-dir --with-png-dir --with-zlib --with-libxml-dir=/usr --enable-xml --disable-rpath --enable-magic-quotes --enable-safe-mode --enable-bcmath --enable-shmop --enable-sysvsem --enable-inline-optimization --with-curl --enable-mbregex --enable-mbstring --with-mcrypt --enable-ftp --with-gd --enable-gd-native-ttf --with-openssl --with-mhash --enable-pcntl --enable-sockets --with-xmlrpc --enable-zip --enable-soap --without-pear --with-gettext --disable-fileinfo &&\
    make -j2 ZEND_EXTRA_LIBS='-liconv' && make install &&\
    cp php.ini-production /usr/local/php/etc/php.ini &&\
    sed -i 's/post_max_size = 8M/post_max_size = 50M/g' /usr/local/php/etc/php.ini &&\
    sed -i 's/upload_max_filesize = 2M/upload_max_filesize = 50M/g' /usr/local/php/etc/php.ini &&\
    sed -i 's/;date.timezone =/date.timezone = PRC/g' /usr/local/php/etc/php.ini &&\
    sed -i 's/short_open_tag = Off/short_open_tag = On/g' /usr/local/php/etc/php.ini &&\
    sed -i 's/; cgi.fix_pathinfo=1/cgi.fix_pathinfo=0/g' /usr/local/php/etc/php.ini &&\
    sed -i 's/; cgi.fix_pathinfo=0/cgi.fix_pathinfo=0/g' /usr/local/php/etc/php.ini &&\
    sed -i 's/;cgi.fix_pathinfo=1/cgi.fix_pathinfo=0/g' /usr/local/php/etc/php.ini &&\
    sed -i 's/max_execution_time = 30/max_execution_time = 300/g' /usr/local/php/etc/php.ini &&\
    sed -i 's/register_long_arrays = On/;register_long_arrays = On/g' /usr/local/php/etc/php.ini &&\
    sed -i 's/magic_quotes_gpc = On/;magic_quotes_gpc = On/g' /usr/local/php/etc/php.ini &&\
    sed -i 's/disable_functions =.*/disable_functions = passthru,exec,system,chroot,scandir,chgrp,chown,shell_exec,proc_open,proc_get_status,ini_alter,ini_restore,dl,openlog,syslog,readlink,symlink,popepassthru,stream_socket_server/g' /usr/local/php/etc/php.ini &&\
    ln -s /usr/local/php/bin/php /usr/bin/php &&\
    ln -s /usr/local/php/bin/phpize /usr/bin/phpize &&\
    ln -s /usr/local/php/sbin/php-fpm /usr/bin/php-fpm &&\
    cd /teamtalk && rm -rf php-5.3.28*

COPY docker/web_server/ZendGuardLoader-php-5.3-linux-glibc23-x86_64.tar.gz ./
RUN tar -zxvf ZendGuardLoader-php-5.3-linux-glibc23-x86_64.tar.gz &&\
    mkdir /usr/local/zend/ &&\
    cp ZendGuardLoader-php-5.3-linux-glibc23-x86_64/php-5.3.x/ZendGuardLoader.so /usr/local/zend/ &&\
    cd /teamtalk && rm -rf ZendGuardLoader-php-5.3-linux-glibc23-x86_64*

RUN echo "[Zend Optimizer]" >> /usr/local/php/etc/php.ini &&\
    echo "zend_extension=/usr/local/zend/ZendGuardLoader.so" >> /usr/local/php/etc/php.ini &&\
    echo "zend_loader.enable=1" >> /usr/local/php/etc/php.ini &&\
    echo "zend_loader.disable_licensing=0" >> /usr/local/php/etc/php.ini &&\
    echo "zend_loader.obfuscation_level_support=3" >> /usr/local/php/etc/php.ini &&\
    echo "zend_loader.license_path="

COPY docker/web_server/php-fpm.conf /usr/local/php/etc/php-fpm.conf

COPY docker/web_server/php-fpm /etc/init.d/php-fpm

RUN chmod +x /etc/init.d/php-fpm &&\
    groupadd www &&\
    useradd -s /sbin/nologin -g www www

COPY docker/web_server/pcre-8.39.tar.bz2 ./
RUN tar -jxvf pcre-8.39.tar.bz2 &&\
    cd pcre-8.39 &&\
    ./configure &&\
    make -j2 && make install &&\
    cd /teamtalk && rm -rf pcre-8.39*

COPY docker/web_server/nginx-1.6.0.tar.gz ./
RUN tar -zxvf nginx-1.6.0.tar.gz &&\
    cd nginx-1.6.0 &&\
    ./configure --user=www --group=www --prefix=/usr/local/nginx --with-http_stub_status_module --with-http_ssl_module --with-http_gzip_static_module --with-ipv6 &&\
    make -j2 && make install &&\
    ln -s /usr/local/nginx/sbin/nginx /usr/bin/nginx &&\
    cd /teamtalk && rm -rf nginx-1.6.0*

COPY docker/web_server/nginx.conf /usr/local/nginx/conf/nginx.conf

RUN mkdir -p /www/wwwroot/default &&\
    chmod +w /www/wwwroot/default &&\
    mkdir -p /www/wwwlogs &&\
    chmod 777 /www/wwwlogs &&\
    chown -R www:www /www/wwwroot/default

COPY docker/web_server/nginx /etc/init.d/nginx

RUN chmod +x /etc/init.d/nginx
RUN yum clean all && rm -rf /var/cache/yum

COPY php /www/wwwroot/default/
COPY docker/web_server/setupweb.sh main.sh
RUN chmod +x /teamtalk/main.sh
ENTRYPOINT ["/teamtalk/main.sh"]
CMD ["/bin/bash"]