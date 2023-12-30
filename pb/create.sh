#!/bin/sh


#https://github.com/protocolbuffers/protobuf/releases/tag/v3.18.0




SRC_DIR=./
DST_DIR=./gen

#C++，用于服务端
mkdir -p $DST_DIR/cpp
protoc -I=$SRC_DIR --cpp_out=$DST_DIR/cpp/ $SRC_DIR/*.proto


#JAVA，主要用于客户端（后续也可能用于 Java 服务端）
mkdir -p $DST_DIR/java
protoc -I=$SRC_DIR --java_out=lite:$DST_DIR/java/ $SRC_DIR/*.proto


#PYTHON（后续也可能用于 Python 服务端，其他语言类似，不再赘述）
#mkdir -p $DST_DIR/python
#protoc -I=$SRC_DIR --python_out=$DST_DIR/python/ $SRC_DIR/*.proto



#Objective-C

#OC 不适用于此方案
#如果是 protoc 2.x 的版本，需要配合插件来使用 protoc 命令来进行 OC pb 文件生成
#本项目采用protoc 3.x 版本（3.18），在 Mac 上安装 OC 版本的 protoc 命令后可以用下面的命令生成
#mkdir -p $DST_DIR/objc
#protoc -I=$SRC_DIR --objc_out=$DST_DIR/objc/ $SRC_DIR/*.proto