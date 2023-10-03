# TeamTalk Docker 版本

> 说明：对于所有的自定义镜像有 lsqtzj/xxx 版本与 1569663570/xxx 版本，前者是开源作者基于 openeuler/openeuler:20.03 制作的镜像，后者是本人根据前者基于 CentOS17 制作的镜像。
> 如果出现 b'i/o timeout' 问题，重复执行 docker-compose xxx 命令就可以解决。

## 直接从已有镜像运行 TeamTalk 容器

```Dockerfile
docker-compose up -f docker-compose-openeuler.yml -d

或

docker-compose up -f docker-compose-centos.yml -d
```

## 本地构建 TeamTalk 镜像，然后再运行 TeamTalk 容器

```Dockerfile
docker-compose -f "docker-compose-build-openeuler.yml" up -d --build

或

docker-compose -f "docker-compose-build-centos.yml" up -d --build
```
