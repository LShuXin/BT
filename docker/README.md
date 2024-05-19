<!--
 * @LastEditors: liushuxin
 * @LastEditTime: 2024-05-19 12:35:02
 * @FilePath: /BT/docker/README.md
 * @Description: 
 * 
 * Copyright (c) 2024 by liushuxina@gmail.com All Rights Reserved. 
-->
# BigTalk Docker 版本

- 直接从已有镜像运行 BigTalk 容器

```Dockerfile
sudo docker-compose -f docker-compose-centos792009.yml up -d
```

- 本地构建 BigTalk 镜像，并使用本地构建的镜像运行容器

```Dockerfile
sudo docker-compose -f "docker-compose-build-centos792009.yml" up -d --build
```
