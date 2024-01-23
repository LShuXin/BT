# redis_server

提供 `1569663570/openeuler2003_redis_server`、`1569663570/centos792009_redis_server` 两个版本，分别基于 `openeuler/openeuler:20.03`、 `centos:centos7.9.2009` 基础镜像构建

## 快速使用

```shell
apples-Mac-mini-1243:redis_server apple$ cd centos/
apples-Mac-mini-1243:centos apple$ chmod +x run.sh 
apples-Mac-mini-1243:centos apple$ ./run.sh 
188142135d33de2d47374947ab036cda0fe0b7f1f6fb7b02d74f97f77bd6a562
apples-Mac-mini-1243:centos apple$ 
```

## 自定义镜像

```shell
apples-Mac-mini-1243:redis_server apple$ cd centos/
apples-Mac-mini-1243:centos apple$ chmod +x build.sh 
apples-Mac-mini-1243:centos apple$ ./build.sh 
```

## 环境变量

```shell
BIND
```
