cmake_minimum_required(VERSION 2.6)
PROJECT(slog)

# 将当前目录下的所有资源放入 SRC_LIST
AUX_SOURCE_DIRECTORY(./ SRC_LIST)

if(MSVC)
	SET(LIBRARY_OUTPUT_PATH ../lib)
	ADD_DEFINITIONS(-D_REENTRANT -D_FILE_OFFSET_BITS=64 -DAC_HAS_INFO
	-DAC_HAS_WARNING -DAC_HAS_ERROR -DAC_HAS_CRITICAL -DTIXML_USE_STL
	-DAC_HAS_DEBUG -DLINUX_DAEMON -D_HAS_STD_BYTE=0  /GL /Zc:auto /W1 /std:c++17)
else()
	SET(CMAKE_MACOSX_RPATH 1)
	SET(LIBRARY_OUTPUT_PATH .)
	ADD_DEFINITIONS( -g -W -Wall -D_REENTRANT -D_FILE_OFFSET_BITS=64 -DAC_HAS_INFO
	-DAC_HAS_WARNING -DAC_HAS_ERROR -DAC_HAS_CRITICAL -DTIXML_USE_STL
	-DAC_HAS_DEBUG -std=c++17 -D _GLIBCXX_USE_CXX11_ABI=1 -DLINUX_DAEMON)
endif()

# _REENTRAN： 用于支持多线程编程宏定义
# _FILE_OFFSET_BITS=64：设置文件偏移量使用 64 位，用于支持大文件
# AC_HAS_INFO、AC_HAS_WARNING、AC_HAS_ERROR、AC_HAS_CRITICAL：
# 用于表示不同级别的日志信息
# TIXML_USE_STL：用于选择是否在编译时使用 STL（标准模板库）来
# 解析 XML，取决于是否定义了这个宏。
# AC_HAS_DEBUG：用于启用调试相关的功能。
# LINUX_DAEMON：用于在 Linux 上将程序设置为守护进程（后台运行）的宏定义。
# _HAS_STD_BYTE=0：用于控制是否启用 C++17 中的 std::byte 类型。
# /GL /Zc:auto /W1 /std:c++17: 这部分是编译器的编译选项。涉及到链接器优
# 化 (/GL)、C++17 标准 (/std:c++17)，以及其他一些编译器警告等级 (/W1)。
# 在 CMake 构建脚本中，SET(CMAKE_MACOSX_RPATH 1) 是用于配置 macOS 平台上的运行时搜索路径的设置。
# 在 macOS 上，可执行文件和共享库（动态链接库）在运行时需要知道如何查找其依赖的其他共享库。为了解决这
# 个问题，macOS 提供了运行时搜索路径（rpath）的概念。rpath 是一个用于指定共享库搜索路径的机制，使得
# 程序可以在运行时自动查找所需的共享库。
# CMAKE_MACOSX_RPATH 是一个 CMake 变量，用于控制是否在生成的可执行文件和共享库中设置 rpath。具体
# 地说，SET(CMAKE_MACOSX_RPATH 1) 将这个变量设置为 1，表示要在生成的可执行文件和共享库中包含 rpath
# 信息。
# 通常情况下，如果你的项目依赖于一些共享库，并且这些共享库在不同的路径中，你可以设置 CMAKE_MACOSX_RPATH
# 以确保生成的可执行文件或共享库能够正确地找到这些依赖库。当你运行生成的可执行文件时，系统会根据 rpath 信
# 息来查找所需的共享库，从而避免出现动态链接库找不到的问题。
# -g: 这是一个编译选项，表示在生成的可执行文件中包含调试信息，以便在调试过程中可以进行源代码级别的调试。
# -W -Wall: 这也是编译选项，用于开启编译器的警告信息。-W 表示开启警告，而 -Wall 表示开启所有警告。编译器
# 会在编译过程中检查代码，并在可能存在问题的地方产生警告信息。
# -std=c++17: 这是一个编译选项，指定使用 C++17 标准进行编译。
# -D_GLIBCXX_USE_CXX11_ABI=1: 这个宏定义用于指定在 C++11 标准 ABI（应用二进制接口）模式下编译。
# -DLINUX_DAEMON: 这个宏定义可能用于在 Linux 上将程序设置为守护进程（后台运行）。

# TODO：这里没什么作用？
INCLUDE_DIRECTORIES(./ ./include ./include/log4cxx)
LINK_DIRECTORIES(./ ./lib)

#ADD_XXXX必须在TARGET_LINK_LIBRARIES前面，否则会报错
#ADD_LIBRARY(${PROJECTNAME} SHARED/STATIC ${SRC_LIST})
#ADD_EXECUTABLE(slog ${SRC_LIST})

# 构建目标
if(MSVC)
    ADD_LIBRARY(slog ${SRC_LIST})
else()
    ADD_LIBRARY(slog SHARED ${SRC_LIST})
endif()
TARGET_LINK_LIBRARIES(slog log4cxx)
