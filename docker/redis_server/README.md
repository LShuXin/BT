# redis_server

提供 `openeuler_redis_server`、`centos_redis_server` 两个版本，分别基于 `openeuler/openeuler:20.03`、 `centos:centos7.9.2009`

## 快速使用

### 运行系统

```shell
docker run -d -tid --name openeuler_redis_server --privileged=true --ENV BIND=0.0.0.0 1569663570/openeuler_redis_server /sbin/init

或

docker run -d -tid --name centos_redis_server --privileged=true --ENV BIND=0.0.0.0 1569663570/centos_redis_server /sbin/init
```

### 进入系统

```shell
docker exec -it openeuler_redis_server /bin/bash

或

docker exec -it centos_redis_server /bin/bash
```

### 环境变量

```shell
BIND
```

### 构建自定义镜像

```shell
cd docker/redis_server/openeuler_redis_server
chmod +x ./build.sh
./build.sh

或

cd docker/redis_server/centos_redis_server
chmod +x ./build.sh
./build.sh
```
