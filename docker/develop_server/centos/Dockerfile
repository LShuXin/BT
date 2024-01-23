FROM 1569663570/centos792009_develop_base:latest

VOLUME ["/develop"]

WORKDIR /develop
COPY ./server server
COPY ./pb pb
ENV PATH=$PATH:/usr/local/protobuf/bin
ENV LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/usr/local/protobuf/lib
ENV CPLUS_INCLUDE_PATH=$CPLUS_INCLUDE_PATH:/usr/local/protobuf/include

RUN cd server/src &&\
    cp -rf /usr/local/log4cxx/include slog/ &&\
    mkdir -p slog/lib/ &&\
    cp -f /usr/local/log4cxx/lib64/liblog4cxx.so* slog/lib/

RUN cd server/src && mkdir -p base/pb/lib/linux/ &&\
    cp -rf /usr/local/protobuf/lib/libprotobuf-lite.a base/pb/lib/linux/ &&\
    cp -rf /usr/local/protobuf/include/* base/pb/

RUN cp -a /usr/local/hiredis/* server/src/db_proxy_server

RUN cd pb && ./create.sh && ./sync.sh

# 编译服务端
RUN rm -rf server/im-server-1.0.0.tar.gz &&\
    cd server/src &&\
    sh build.sh version 1.0.0

# 复制开机运行sh
COPY ./docker/develop_server/main.sh /opt/main.sh
RUN chmod +x /opt/main.sh &&\
    tar -zxvf server/im-server-1.0.0.tar.gz &&\
    cd im-server-1.0.0 && chmod +x ./sync_lib_for_zip.sh && ./sync_lib_for_zip.sh

VOLUME ["/develop" ]
ENTRYPOINT ["/opt/main.sh"]
CMD ["/bin/bash"]