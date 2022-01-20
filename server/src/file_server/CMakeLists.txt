cmake_minimum_required(VERSION 2.6)
PROJECT(fileserver)

AUX_SOURCE_DIRECTORY(./ SRC_LIST1)
AUX_SOURCE_DIRECTORY(../base/pb/protocol SRC_LIST2)

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

if(MSVC)
    ADD_DEFINITIONS(-D_REENTRANT -D_FILE_OFFSET_BITS=64 -DAC_HAS_INFO
    -DAC_HAS_WARNING -DAC_HAS_ERROR -DAC_HAS_CRITICAL -DTIXML_USE_STL
    -DAC_HAS_DEBUG -DLINUX_DAEMON -D_HAS_STD_BYTE=0 /GL /Zc:auto /W1 /std:c++17)
else()
    ADD_DEFINITIONS( -g -W -Wall -D_REENTRANT -D_FILE_OFFSET_BITS=64 -DAC_HAS_INFO
    -DAC_HAS_WARNING -DAC_HAS_ERROR -DAC_HAS_CRITICAL -DTIXML_USE_STL
    -DAC_HAS_DEBUG -DLINUX_DAEMON -std=c++17 )
endif()

INCLUDE_DIRECTORIES(./ ../ ../base ../base/pb/protocol ../base/pb/)
LINK_DIRECTORIES(./ ../base ${PB_LIB_DIR} ../base/slog/lib)

#ADD_XXXX必须在TARGET_LINK_LIBRARIES前面，否则会报错
#ADD_LIBRARY(${PROJECTNAME} SHARED/STATIC ${SRC_LIST})
ADD_EXECUTABLE(file_server ${SRC_LIST1} ${SRC_LIST2})
if(MSVC)
    message("PROJECT: ${CMAKE_GENERATOR}")
    TARGET_LINK_LIBRARIES(file_server slog base debug libprotobuf-lited optimized libprotobuf-lite debug pthreadVC3d optimized pthreadVC3)
else()    
    TARGET_LINK_LIBRARIES(file_server pthread base protobuf-lite uuid slog)
endif()
 
