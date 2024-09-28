<!--
 * @LastEditors: liushuxin
 * @LastEditTime: 2024-09-26 21:22:03
 * @FilePath: /web_client/README.md
 * @Description: 
 * 
 * Copyright (c) 2024 by liushuxina@gmail.com All Rights Reserved. 
-->
# web_client

> 说明：此镜像提供 nginx(1.21.5) 与 php(5.6.6) 支持，基于基础镜像 openeuler/openeuler:20.03 构建。镜像 `1569663570/openeuler_nginx` 已上传到 Docker Hub。

## 快速使用

### 运行系统
```
docker run -p 9880:80 -d -tid --name openeuler_nginx --privileged=true 1569663570/openeuler_nginx /sbin/init
```

### 进入系统

```
docker exec -it 1569663570/openeuler_nginx /bin/bash
```

nginx 版本 1.21.5 php-5.6.6
快速使用
运行系统
docker run -p 9880:80 -d -tid --name openeuler_nginx --privileged=true lsqtzj/openeuler_nginx /sbin/init
http://localhost:9880/
进入系统
docker exec -it openeuler_nginx /bin/bash