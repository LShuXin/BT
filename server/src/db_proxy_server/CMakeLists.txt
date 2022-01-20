cmake_minimum_required(VERSION 2.6)
PROJECT(db_proxy_server)

AUX_SOURCE_DIRECTORY(./ SRC_LIST)
AUX_SOURCE_DIRECTORY(./business SRC_LIST1)

#SET(LIBRARY_OUTPUT_PATH ./)
SET(EXECUTABLE_OUTPUT_PATH  ./)
if(APPLE)
    SET(PB_LIB_DIR ../base/pb/lib/mac)
elseif(UNIX)
    SET(PB_LIB_DIR ../base/pb/lib/linux)
elseif(MSVC)
    SET(PB_LIB_DIR ../lib)
    SET(EXECUTABLE_OUTPUT_PATH  ../bin)
endif()

if(UNIX)
SET(MYSQL_INCLUDE_DIR /usr/local/mysql/include)
SET(MYSQL_LIB /usr/local/mysql/lib)
endif()

if(MSVC)
    ADD_DEFINITIONS(-D_REENTRANT -D_FILE_OFFSET_BITS=64 -DAC_HAS_INFO
    -DAC_HAS_WARNING -DAC_HAS_ERROR -DAC_HAS_CRITICAL -DTIXML_USE_STL
    -DAC_HAS_DEBUG -DLINUX_DAEMON -DENCRYPT -D_HAS_STD_BYTE=0  /GL /Zc:auto /W1 /std:c++17)
else()
    ADD_DEFINITIONS( -g -W -Wall -D_REENTRANT -D_FILE_OFFSET_BITS=64 -DAC_HAS_INFO
    -DAC_HAS_WARNING -DAC_HAS_ERROR -DAC_HAS_CRITICAL -DTIXML_USE_STL
    -DAC_HAS_DEBUG -DLINUX_DAEMON -std=c++17 -DENCRYPT)
endif()



INCLUDE_DIRECTORIES(./ ./businiess ../base ../base/jsoncpp ../base/pb/protocol
../base/pb/ ${MYSQL_INCLUDE_DIR})
LINK_DIRECTORIES(./ ../base ${PB_LIB_DIR} ${MYSQL_LIB} ../base/slog/lib)

#ADD_XXXX必须在TARGET_LINK_LIBRARIES前面，否则会报错
#ADD_LIBRARY(${PROJECTNAME} SHARED/STATIC ${SRC_LIST})
ADD_EXECUTABLE(db_proxy_server ${SRC_LIST} ${SRC_LIST1})

 
if(MSVC)
    message("PROJECT: ${CMAKE_GENERATOR}")
    TARGET_LINK_LIBRARIES(db_proxy_server slog base hiredis libcrypto libmariadb debug libcurl-d optimized libcurl debug libprotobuf-lited optimized libprotobuf-lite debug pthreadVC3d optimized pthreadVC3 )
else()
    TARGET_LINK_LIBRARIES(db_proxy_server pthread base protobuf-lite mysqlclient_r hiredis curl slog crypto)
endif()