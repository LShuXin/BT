cmake_minimum_required(VERSION 2.6)
PROJECT(db_proxy_server)

AUX_SOURCE_DIRECTORY(./ ROOT_SRC_LIST)
AUX_SOURCE_DIRECTORY(./business BUSINESS_SRC_LIST)

SET(EXECUTABLE_OUTPUT_PATH ./)
SET(PB_LIB_DIR ../base/pb/lib/linux)
SET(MYSQL_INCLUDE_DIR /usr/local/mysql/include)
SET(MYSQL_LIB /usr/local/mysql/lib)

ADD_DEFINITIONS(-g -W -Wall -D_REENTRANT -D_FILE_OFFSET_BITS=64 -DAC_HAS_INFO
                -DAC_HAS_WARNING -DAC_HAS_ERROR -DAC_HAS_CRITICAL -DTIXML_USE_STL
                -DAC_HAS_DEBUG -DLINUX_DAEMON -std=c++17 -DENCRYPT)

INCLUDE_DIRECTORIES(
                    ../base
                    ../base/jsoncpp 
                    ../base/pb/protocol 
                    ../base/pb/ 
                    ./
                    ./businiess 
                    ${MYSQL_INCLUDE_DIR})

LINK_DIRECTORIES(./
                ../base
                ${PB_LIB_DIR}
                ${MYSQL_LIB}
                ../base/slog/lib)

ADD_EXECUTABLE(db_proxy_server ${ROOT_SRC_LIST} ${BUSINESS_SRC_LIST})

TARGET_LINK_LIBRARIES(db_proxy_server pthread base protobuf-lite mysqlclient_r hiredis curl slog crypto)