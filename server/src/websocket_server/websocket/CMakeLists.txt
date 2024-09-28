cmake_minimum_required(VERSION 2.6)
PROJECT(custom_websocket)

AUX_SOURCE_DIRECTORY(./ ROOT_SRC_LIST)
AUX_SOURCE_DIRECTORY(./base64 BASE64_SRC_LIST)
AUX_SOURCE_DIRECTORY(./md5 MD5_SRC_LIST)
AUX_SOURCE_DIRECTORY(./sha1 SH1_SRC_LIST)

SET(LIBRARY_OUTPUT_PATH ./)

ADD_DEFINITIONS(-g -W -Wall -D_REENTRANT -D_FILE_OFFSET_BITS=64 -DAC_HAS_INFO
                -DAC_HAS_WARNING -DAC_HAS_ERROR -DAC_HAS_CRITICAL -DTIXML_USE_STL
                -DAC_HAS_DEBUG -DLINUX_DAEMON)

INCLUDE_DIRECTORIES(./
                    ./base64
                    ./md5
                    ./sha1)

LINK_DIRECTORIES(./
                 ./base64
                 ./md5
                 ./sha1)

ADD_LIBRARY(websocket STATIC ${ROOT_SRC_LIST}  ${BASE64_SRC_LIST}  ${MD5_SRC_LIST}  ${SH1_SRC_LIST})

TARGET_LINK_LIBRARIES(websocket)