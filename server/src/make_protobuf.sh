#!/bin/bash
# author: luoning
# date: 03/25/2015

PROTOBUF_VERSION=3.18.0
PROTOBUF=protobuf-${PROTOBUF_VERSION}
PROTOBUF_ZIP=${PROTOBUF}.tar.gz
PROTOBUF_PATH=https://github.com/protocolbuffers/protobuf/archive/refs/tags/v${PROTOBUF_VERSION}.tar.gz
CUR_DIR=
download() {
    if [ -f "$1" ]; then
        echo "$1 existed."
    else
        echo "$1 not existed, begin to download..."
        wget -c $2 -O $PROTOBUF_ZIP
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

build_protobuf() {
    # 下载安装pb
    cd protobuf
    download $PROTOBUF_ZIP $PROTOBUF_PATH
    tar -xf $PROTOBUF_ZIP
    cd $PROTOBUF
    sh autogen.sh
    ./configure --prefix=/usr/local/protobuf
    make -j4 && make install

    # 拷贝 .a 和 .h 文件
    cd ..
    mkdir -p ../base/pb/lib/linux/
    cp /usr/local/protobuf/lib/libprotobuf-lite.a ../base/pb/lib/linux/
    cp -r /usr/local/protobuf/include/* ../base/pb/
}

check_user
get_cur_dir
build_protobuf