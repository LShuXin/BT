FROM centos:centos7.9.2009
EXPOSE 6379
WORKDIR /teamtalk
COPY docker/redis_server/redis-2.8.19.tar.gz ./

ARG BIND=0.0.0.0
RUN yum update -y && \
    yum -y install tar make gcc && \
    yum clean all && \
    tar -zxvf redis-2.8.19.tar.gz && \
    cd redis-2.8.19 && \
    make PREFIX=/usr/local/redis install && \
    mkdir -p /usr/local/redis/etc/ && \
    cp redis.conf /usr/local/redis/etc/redis.conf && \
    sed -i "s/# bind 127\.0\.0\.1/bind $BIND/g" /usr/local/redis/etc/redis.conf && \
    cd .. && \
    rpm -e --nodeps tar make gcc && \
    yum clean all

CMD ["/usr/local/redis/bin/redis-server", "/usr/local/redis/etc/redis.conf"]