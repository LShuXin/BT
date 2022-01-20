
chcp 65001
@ECHO off
SETLOCAL enabledelayedexpansion
cls
COLOR 1f

ECHO.
ECHO.
ECHO   ##############################################################
ECHO   #               欢迎使用 TeamTalk 工程生成器                 #
ECHO   #                   version 2.0                              #
ECHO   ##############################################################
ECHO.
ECHO.

mkdir solution
cd solution
cmake -G "Visual Studio 16 2019" ../src
pause