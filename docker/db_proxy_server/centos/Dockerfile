FROM centos:centos7.9.2009 AS base
WORKDIR /teamtalk
EXPOSE 10600
RUN yum update -y &&\
    yum install -y wget tar bzip2 zip unzip openssl-devel net-tools cmake make gcc gcc-c++ flex ncurses-devel curl-devel apr-devel apr-util-devel libuuid-devel
COPY docker/develop_server/mysql-5.6.45.tar.gz ./
RUN tar -zxvf mysql-5.6.45.tar.gz &&\
    cd mysql-5.6.45 &&\
    cmake -DCMAKE_INSTALL_PREFIX=/usr/local/mysql -DEXTRA_CHARSETS=all -DDEFAULT_CHARSET=utf8 -DDEFAULT_COLLATION=utf8_general_ci -DWITH_READLINE=1 -DWITH_SSL=system -DWITH_ZLIB=system -DWITH_EMBEDDED_SERVER=1 -DENABLED_LOCAL_INFILE=1 &&\
    make -j4 && make install
RUN rm -rf mysql* &&\
    yum clean all


FROM 1569663570/centos792009_develop_server AS builder


FROM base AS runtime
COPY --from=builder /lib64/libstdc++.so.6 /lib64/libstdc++.so.6
COPY --from=builder /develop/im-server-1.0.0/db_proxy_server /teamtalk/db_proxy_server
COPY --from=builder /develop/im-server-1.0.0/daeml /teamtalk/daeml
COPY ./server/run/monitor.sh /teamtalk/db_proxy_server/monitor.sh
COPY ./server/run/restart.sh /teamtalk/restart.sh
COPY ./docker/db_proxy_server/main.sh main.sh
RUN chmod +x /teamtalk/main.sh
ENTRYPOINT ["/teamtalk/main.sh"]
CMD ["/bin/bash"]
