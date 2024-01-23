# mariadb_server

直接使用官方镜像：[mariadb:10.7.1](https://hub.docker.com/layers/library/mariadb/10.7.1/images/sha256-1a09a9175c80d49ccbcbcc821472449bc4f83169d8393296736eaac9180d5484?context=explore)
镜像使用说明： https://hub.docker.com/_/mariadb、 https://github.com/docker-library/docs/blob/master/mariadb/README.md

注意⚠️：
本项目提供的 Docker 解决方案中，自定义 develop_server 镜像采用源码安装 mysql-5.6.45，安装目录为 `/usr/local/mysql`，而 docker-compose.yml 文件中使用的是 [mariadb:10.7.1](https://hub.docker.com/layers/library/mariadb/10.7.1/images/sha256-1a09a9175c80d49ccbcbcc821472449bc4f83169d8393296736eaac9180d5484?context=explore) 官方镜像