cmake_minimum_required(VERSION 2.6)
PROJECT(websocket_server)

AUX_SOURCE_DIRECTORY(./ ROOT_SRC_LIST)
AUX_SOURCE_DIRECTORY(../msg_server MSG_SERVER_SRC_LIST)

SET(EXECUTABLE_OUTPUT_PATH  ./)
################# set your openssl lib & incluce path #################
SET(OPENSSL_INCLUDE_DIR ./)
SET(OPENSSL_LIB_DIR ./)
################# set your openssl lib & incluce path #################
SET(PB_LIB_DIR ../base/pb/lib/linux)
SET(SECURITY_LIB_DIR ../base/security/lib/linux)

ADD_DEFINITIONS(-g -W -Wall -D_REENTRANT -D_FILE_OFFSET_BITS=64 -DAC_HAS_INFO
                -DAC_HAS_WARNING -DAC_HAS_ERROR -DAC_HAS_CRITICAL -DTIXML_USE_STL
                -DAC_HAS_DEBUG -DLINUX_DAEMON -std=c++11 -Wno-deprecated)

INCLUDE_DIRECTORIES(./
                    ../base
                    ../base/jsoncpp
                    ../base/pb/protocol
                    ../base/security/
                    ../msg_server
                    ./websocket
                    ${OPENSSL_INCLUDE_DIR})

LINK_DIRECTORIES(./
                 ../base
                 ../base/slog/lib
                 ../slog
                 ./websocket
                 ${PB_LIB_DIR}
                 ${SECURITY_LIB_DIR}
                 ${OPENSSL_LIB_DIR})

## why
LIST(REMOVE_ITEM MSG_SERVER_SRC_LIST ../msg_server/msg_server.cpp)
LIST(REMOVE_ITEM MSG_SERVER_SRC_LIST ../msg_server/client_connect_impl.cpp)

ADD_EXECUTABLE(websocket_server ${ROOT_SRC_LIST} ${MSG_SERVER_SRC_LIST})

TARGET_LINK_LIBRARIES(websocket_server base websocket protobuf-lite slog crypto ssl pthread)
