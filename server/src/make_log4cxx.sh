#!/bin/bash
# author: luoning
# date: 03/24/2015


APR_VERSION=1.7.0
APR=apr-${APR_VERSION}
APR_ZIP=${APR}.tar.gz
APR_PATH=https://mirrors.tuna.tsinghua.edu.cn/apache/apr/${APR_ZIP}

APR_UTIL_VERSION=1.6.1
APR_UTIL=apr-util-${APR_UTIL_VERSION}
APR_UTIL_ZIP=${APR_UTIL}.tar.gz
APR_UTIL_PATH=https://mirrors.tuna.tsinghua.edu.cn/apache/apr/${APR_UTIL_ZIP}

LOG4CXX_VERSION=0.12.1
LOG4CXX=apache-log4cxx-${LOG4CXX_VERSION}
LOG4CXX_ZIP=${LOG4CXX}.tar.gz
LOG4CXX_PATH=https://archive.apache.org/dist/logging/log4cxx/${LOG4CXX_VERSION}/${LOG4CXX_ZIP}
CUR_DIR=
download() {
    if [ -f "$1" ]; then
        echo "$1 existed."
    else
        echo "$1 not existed, begin to download..."
        wget -c $2
        if [ $? -eq 0 ]; then
            echo "download $1 successed";
        else
            echo "Error: download $1 failed";
            return 1;
        fi
    fi
    return 0
}

check_user() {
    if [ $(id -u) != "0" ]; then
        echo "Error: You must be root to run this script, please use root to install im"
        exit 1
    fi
}

get_cur_dir() {
    # Get the fully qualified path to the script
    case $0 in
        /*)
            SCRIPT="$0"
            ;;
        *)
            PWD_DIR=$(pwd);
            SCRIPT="${PWD_DIR}/$0"
            ;;
    esac
    # Resolve the true real path without any sym links.
    CHANGED=true
    while [ "X$CHANGED" != "X" ]
    do
        # Change spaces to ":" so the tokens can be parsed.
        SAFESCRIPT=`echo $SCRIPT | sed -e 's; ;:;g'`
        # Get the real path to this script, resolving any symbolic links
        TOKENS=`echo $SAFESCRIPT | sed -e 's;/; ;g'`
        REALPATH=
        for C in $TOKENS; do
            # Change any ":" in the token back to a space.
            C=`echo $C | sed -e 's;:; ;g'`
            REALPATH="$REALPATH/$C"
            # If REALPATH is a sym link, resolve it.  Loop for nested links.
            while [ -h "$REALPATH" ] ; do
                LS="`ls -ld "$REALPATH"`"
                LINK="`expr "$LS" : '.*-> \(.*\)$'`"
                if expr "$LINK" : '/.*' > /dev/null; then
                    # LINK is absolute.
                    REALPATH="$LINK"
                else
                    # LINK is relative.
                    REALPATH="`dirname "$REALPATH"`""/$LINK"
                fi
            done
        done

        if [ "$REALPATH" = "$SCRIPT" ]
        then
            CHANGED=""
        else
            SCRIPT="$REALPATH"
        fi
    done
    # Change the current directory to the location of the script
    CUR_DIR=$(dirname "${REALPATH}")
}

build_apr() {
    
    download $APR_ZIP $APR_PATH
    tar -xf $APR_ZIP
    cd $APR
    # rm: cannot remove 'libtoolT': No such file or directory
    sed -i "s/RM='\$RM'/RM='\$RM -f'/g" configure
    ./configure --prefix=/usr/local/apr
    make -j4 && make install
    cd ..
}

build_apr_util() {
    download $APR_UTIL_ZIP $APR_UTIL_PATH
    tar -xf $APR_UTIL_ZIP
    cd $APR_UTIL
    ./configure --prefix=/usr/local/apr --with-apr=/usr/local/apr/bin/apr-1-config
    make -j4 && make install
    cd ..
}

build_log4cxx() {
    cd log4cxx
    # yum -y update
    # cut -f 2 -d '='：一旦找到包含 "VERSION_ID" 的行，cut 命令被用来根据等号(=)分隔符提取出等号后面的部分，也就是版本号。
    # -f 2 选项指定了要提取的字段编号，这里是第二个字段，即等号后面的部分。
    local VERSION_ID=`grep "VERSION_ID" /etc/os-release | cut -f 2 -d '='`
    # <<< "$VERSION_ID"：这是 Bash 的 "here string" 语法，它将 $VERSION_ID 的内容作为输入传递给下一个命令。
    # cut -f2：与第一行相同，这个命令提取 $VERSION_ID 中的第二个字段，也就是版本号。
    local NumOnly=$(cut -f2 <<< "$VERSION_ID")
    if [ NumOnly>7 ]; then
        yum -y install apr-devel
        yum -y install apr-util-devel
    else
        # centos:7 需要编译安装
        yum -y uninstall apr-devel
        yum -y uninstall apr-util-devel
        build_apr    
        build_apr_util
    fi

    download $LOG4CXX_ZIP $LOG4CXX_PATH
    tar -xf $LOG4CXX_ZIP
    cd $LOG4CXX
    # ./configure --prefix=$CUR_DIR/log4cxx --with-apr=/usr --with-apr-util=/usr

    # 将 CMake 项目的安装目录设置为当前工作目录下的 log4cxx 子目录
    # :PATH 是CMake中的一种类型提示，它告诉CMake将变量解释为一个路径（文件夹）
    cmake -DCMAKE_INSTALL_PREFIX:PATH=$CUR_DIR/log4cxx .
    /bin/cp -rf ../inputstreamreader.cpp ./src/main/cpp/
    /bin/cp -rf ../socketoutputstream.cpp ./src/main/cpp/
    /bin/cp -rf ../console.cpp ./src/examples/cpp/
    make -j4 && make install
    cd ../../
    cp -rf log4cxx/include slog/
    mkdir -p slog/lib/
    cp -f log4cxx/lib64/liblog4cxx.so* slog/lib/
}

check_user
get_cur_dir
build_log4cxx
