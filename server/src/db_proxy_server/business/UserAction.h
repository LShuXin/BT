/*================================================================
 *   Copyright (C) 2014 All rights reserved.
 *
 *   文件名称：UserAction.h
 *   创 建 者：Zhang Yuanhao
 *   邮    箱：bluefoxah@gmail.com
 *   创建日期：2014年12月15日
 *   描    述：
 *
 ================================================================*/

#ifndef __USER_ACTION_H__
#define __USER_ACTION_H__

#include "ImPduBase.h"

namespace DB_PROXY
{
    // 通过用户 id 列表获取用户信息列表
    void getUserInfo(CImPdu* pPdu, uint32_t conn_uuid);
    // 获取指定时间以后有信息更新的用户数组、最近一次用户信息更新发生的时间
    void getChangedUser(CImPdu* pPdu, uint32_t conn_uuid);
};


#endif /* __USER_ACTION_H__ */