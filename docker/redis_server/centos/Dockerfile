# 第一阶段：构建阶段
FROM centos:centos7.9.2009 as builder
WORKDIR /build

# 安装编译所需的软件包
RUN yum update -y &&\
    yum install -y tar make gcc &&\
    yum clean all &&\
    rm -rf /var/cache/yum

# 复制 Redis 源码并编译安装
COPY docker/redis_server/redis-2.8.19.tar.gz ./
RUN tar -zxvf redis-2.8.19.tar.gz &&\
    cd redis-2.8.19 &&\
    make PREFIX=/usr/local/redis install &&\
    mkdir -p /usr/local/redis/etc/ &&\
    cp redis.conf /usr/local/redis/etc/

# 第二阶段：运行阶段
FROM centos:centos7.9.2009
EXPOSE 6379
WORKDIR /usr/local/redis

# 从构建阶段复制编译好的 Redis 和配置文件
COPY --from=builder /usr/local/redis /usr/local/redis

# 设置环境变量并更新 Redis 配置
ARG BIND=0.0.0.0
RUN sed -i "s/# bind 127\.0\.0\.1/bind $BIND/g" etc/redis.conf

CMD ["/usr/local/redis/bin/redis-server", "/usr/local/redis/etc/redis.conf"]
