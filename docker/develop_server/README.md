# develop_server 说明
Dockerfile.base 是develop_server Dockerfile 的基础环境，集成TeamTalk的开发环境，拆成量个文件是为了避免 Dockerfile 修改后需要长时间的重复编译。
# 编译方式
在 docker\develop_server 目录下运行 build_base.bat 