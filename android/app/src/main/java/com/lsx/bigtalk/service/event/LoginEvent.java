package com.lsx.bigtalk.service.event;

public enum LoginEvent {
    NONE,
    LOGINING,
    // 网络登陆验证成功
    NORMAL_LOGIN_SUCCESS,
    LOGIN_INNER_FAILED,
    LOGIN_AUTH_FAILED,
    LOGIN_OUT,

    // 对于离线登陆
    // 如果在此时，网络登陆返回账号密码错误应该怎么处理? todo 强制退出
    // 登陆成功之后触发 REMOTE_LOGIN_SUCCESS
    LOCAL_LOGIN_SUCCESS,
    REMOTE_LOGIN_SUCCESS,


    PC_ONLINE,
    PC_OFFLINE,
    KICK_PC_SUCCESS,
    KICK_PC_FAILED
}