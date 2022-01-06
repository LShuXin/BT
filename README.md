# TeamTalk
TeamTalk is a solution for enterprise IM
具体文档见doc目录下,安装之前请仔细阅读相关文档。
# 安装调试参考 
https://blog.csdn.net/lsqtzj/article/details/119456161</br>
# 客户端
## Android
2022年1月6日 项目更新到 Android Studio 2020.3.1.26 版本</br>
Android Gradle Plugin   7.0.4</br>
Gradle                  7.0.2
## 命令行转换
下载https://sourceforge.net/projects/dos2unix/</br>

命令行运行</br>
for /R %G in (*.c *.cc *.h *.mk *.cpp) do unix2dos "%G" 
# linux换行问题
yum install dos2unix</br>
find ./ -type f -print0 | xargs -0 dos2unix --