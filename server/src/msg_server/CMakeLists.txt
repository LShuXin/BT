cmake_minimum_required(VERSION 2.6)
PROJECT(msg_server)

AUX_SOURCE_DIRECTORY(./ ROOT_SRC_LIST)

SET(EXECUTABLE_OUTPUT_PATH  ./)
SET(PB_LIB_DIR ../base/pb/lib/linux)
SET(SECURITY_LIB_DIR ../base/security/lib/linux)

ADD_DEFINITIONS(-g -W -Wall -D_REENTRANT -D_FILE_OFFSET_BITS=64 -DAC_HAS_INFO
                -DAC_HAS_WARNING -DAC_HAS_ERROR -DAC_HAS_CRITICAL -DTIXML_USE_STL
                -DAC_HAS_DEBUG -DLINUX_DAEMON -std=c++17)

INCLUDE_DIRECTORIES(./ ../base ../base/pb/protocol ../base/pb/ ../base/security/)

LINK_DIRECTORIES(./ ../base ${PB_LIB_DIR} ../base/slog/lib ${SECURITY_LIB_DIR})

ADD_EXECUTABLE(msg_server ${ROOT_SRC_LIST} )

TARGET_LINK_LIBRARIES(msg_server base protobuf-lite slog crypto pthread)