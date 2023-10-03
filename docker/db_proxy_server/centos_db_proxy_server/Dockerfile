FROM centos:centos7.9.2009 AS base
WORKDIR /teamtalk
EXPOSE 10600

RUN yum update -y &&\
    yum -y install wget tar bzip2 zip unzip openssl-devel net-tools make gcc gcc-c++ flex ncurses-devel curl-devel apr-devel apr-util-devel libuuid-devel
   

COPY docker/develop_server/gcc-8.5.0.tar.gz ./
COPY docker/develop_server/cmake-3.27.1.tar.gz ./
COPY docker/develop_server/mysql-5.6.45.tar.gz ./

RUN tar -zxvf cmake-3.27.1.tar.gz &&\
    cd cmake-3.27.1 &&\
    ./configure --prefix=/usr/local/cmake &&\
    gmake && gmake install &&\
    ln -s /usr/local/cmake/bin/cmake /usr/bin/cmake &&\
    echo "export CMAKE_HOME=/usr/local/cmake" >> /etc/profile &&\
    echo "export PATH=$PATH:$CMAKE_HOME/bin" >> /etc/profile &&\
    yum install -y boost-devel

RUN tar -xf gcc-8.5.0.tar.gz &&\
    cd gcc-releases-gcc-8.5.0 &&\
    ./contrib/download_prerequisites &&\
    ./configure --prefix=/usr -enable-checking=release -enable-languages=c,c++ -disable-multilib &&\
    make -j4 && make install

RUN tar -zxvf mysql-5.6.45.tar.gz &&\
    cd mysql-5.6.45 &&\
    cmake -DCMAKE_INSTALL_PREFIX=/usr/local/mysql -DEXTRA_CHARSETS=all -DDEFAULT_CHARSET=utf8 -DDEFAULT_COLLATION=utf8_general_ci -DWITH_READLINE=1 -DWITH_SSL=system -DWITH_ZLIB=system -DWITH_EMBEDDED_SERVER=1 -DENABLED_LOCAL_INFILE=1 &&\
    make -j2 && make install

RUN rm -rf cmake* &&\
    rm -rf gcc* &&\
    rm -rf mysql* &&\
    yum -y remove tar bzip2 zip unzip wget && \
    yum clean all


FROM 1569663570/centos_develop_server AS builder

FROM base AS runtime
COPY --from=builder /develop/im-server-1.0.0/db_proxy_server /teamtalk/db_proxy_server
COPY --from=builder /develop/im-server-1.0.0/daeml /teamtalk/daeml
COPY ./server/run/monitor.sh /teamtalk/db_proxy_server/monitor.sh
COPY ./server/run/restart.sh /teamtalk/restart.sh
COPY ./docker/db_proxy_server/main.sh main.sh
RUN chmod +x /teamtalk/main.sh
ENTRYPOINT ["/teamtalk/main.sh"]
CMD ["/bin/bash"]
