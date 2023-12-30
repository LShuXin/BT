cmake_minimum_required(VERSION 2.6)
PROJECT(routeserver)

AUX_SOURCE_DIRECTORY(./ SRC_LIST)

SET(EXECUTABLE_OUTPUT_PATH  ./)
if(APPLE)
    SET(PB_LIB_DIR ../base/pb/lib/mac)
elseif(UNIX)
    SET(PB_LIB_DIR ../base/pb/lib/linux)
elseif(MSVC)
    SET(PB_LIB_DIR ../lib)
    SET(EXECUTABLE_OUTPUT_PATH  ../bin)
endif()

if(MSVC)
    ADD_DEFINITIONS(-D_REENTRANT -D_FILE_OFFSET_BITS=64 -DAC_HAS_INFO
    -DAC_HAS_WARNING -DAC_HAS_ERROR -DAC_HAS_CRITICAL -DTIXML_USE_STL
    -DAC_HAS_DEBUG -DLINUX_DAEMON -D_HAS_STD_BYTE=0 /GL /Zc:auto /W1 /std:c++17)
else()
    ADD_DEFINITIONS( -g -W -Wall -D_REENTRANT -D_FILE_OFFSET_BITS=64 -DAC_HAS_INFO
    -DAC_HAS_WARNING -DAC_HAS_ERROR -DAC_HAS_CRITICAL -DTIXML_USE_STL
    -DAC_HAS_DEBUG -DLINUX_DAEMON -std=c++17 )
endif()

INCLUDE_DIRECTORIES(./ ../base ../base/pb/protocol ../base/jsoncpp/ ../base/pb/ ${PB_LIB_DIR})
LINK_DIRECTORIES(./ ../base ${PB_LIB_DIR} ../base/slog/lib)

ADD_EXECUTABLE(route_server ${SRC_LIST})

if(MSVC)
    message("PROJECT: ${CMAKE_GENERATOR}")
    TARGET_LINK_LIBRARIES(route_server slog base debug libprotobuf-lited optimized libprotobuf-lite debug pthreadVC3d optimized pthreadVC3 )
else()
    TARGET_LINK_LIBRARIES(route_server base slog protobuf-lite pthread)
endif()