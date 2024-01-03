# TeamTalk WinClient编译问题及解决方案

[TOC]

### 一、<hash_map> is deprecated and will be REMOVED.

进入到源码下的win-client\solution目录,打开.sln解决方案，升级为vs2017项目，执行第一次编译，提示如下错误：

```
fatal error C1189: #error:  <hash_map> is deprecated and will be REMOVED. 
Please use <unordered_map>. You can define _SILENCE_STDEXT_HASH_DEPRECATION_WARNINGS to acknowledge that you have received this warning.
```

解决方法：
在源码中搜索：#include <hash_map>，然后在其前面插入如下一行

```
#define _SILENCE_STDEXT_HASH_DEPRECATION_WARNINGS 1
```

### 二、error C2440: “初始化”: 无法从“int”转换为“mbstate_t”

```
mbstate_t in_state = 0;
```

改为如下代码：

```
mbstate_t in_state = {0};
```

### 三、无法打开文件“mfcs120ud.lib”

错误提示如下：

```
1>已完成生成项目“GifSmiley2003.vcxproj”的操作。
2>LINK : fatal error LNK1104: 无法打开文件“mfcs120ud.lib”
```

分别修改工程utility、Modules的属性
Properties->Linker->Input->Additional Dependencies
将mfcs120ud.lib修改为mfcs140ud.lib或者你电脑上的对应版本

### 四、error LNK2038: 检测到“_MSC_VER”的不匹配项: 值“1800”不匹配值“1900”(IM.BaseDefine.pb.obj 中)

1. 解压win-client\3rdParty目录下的protobuf-2.6.1压缩包,打开protobuf-2.6.1/vsprojects/protobuf.sln
   重新生成libprotobuf和libprotobuf-lite两个工程，完成编译后在protobuf目录下的Debug目录中会对应生成两个lib库，将其拷贝到win-client/lib/Debug目录下
2. 在命令行里cd到teamtalk/server/src/libsecurity/win/目录下运行build.bat批处理文件
   会提示“else 不是内部或外部命令，也不是可运行的程序或批处理文件。”这时候修改批处理文件else跟随到右括号)，如下所示：

```
set "command=%1"
if "%command%" == "clean" (
    echo "clean all build..."
    rm -rf CMakeFiles
    rm -rf Makefile
    rm -rf CMakeCache.txt
    rm -rf cmake_install.cmake
    rm -rf libsecurity.a
    )else ( cmake ../src )
```

再次执行build.bat即可编译出security.sln等相关工程文件

1. error C2371: “int8_t”: 重定义；不同的基类型
   直接注释掉对应的定义即可
   //typedef char int8_t;

再次编译，通过后将lib库其拷贝到win-client/lib/Debug目录。