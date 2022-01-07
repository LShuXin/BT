
chcp 65001
@ECHO off
SETLOCAL enabledelayedexpansion
cls
COLOR 1f

ECHO.
ECHO.
ECHO   ##############################################################
ECHO   #               欢迎使用 TeamTalk 工程配置向导               #
ECHO   #                   version 2.0                              #
ECHO   ##############################################################
ECHO.
ECHO.

set SRC_DIR=%~dp0
set protoc_dir=%~dp0\\
set CPP_DIR=%~dp0\\..\\server\\src\\base\\pb\\protocol
set CPP_CLIENT_DIR=%~dp0\\..\\win-cliient\\include\\ProtocolBuffer
set DST_DIR=%~dp0\\gen
rem 生成PB协议的C++版本
mkdir %DST_DIR%\\cpp
%protoc_dir%\\protoc -I=%SRC_DIR% --cpp_out=%DST_DIR%\\cpp\\ %SRC_DIR%\\*.proto


rem 拷贝PB协议文件
ECHO 拷贝PB协议文件到服务端目录
copy /V /Y %DST_DIR%\\cpp\\* %CPP_DIR%\\
ECHO 拷贝PB协议文件到客户端目录
copy /V /Y %DST_DIR%\\cpp\\* %CPP_CLIENT_DIR%\\
ECHO 删除临时文件
rmdir /S /Q %DST_DIR%
ECHO PB协议配置完成
pause