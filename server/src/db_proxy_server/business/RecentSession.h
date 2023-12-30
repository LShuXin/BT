/*================================================================
 *   Copyright (C) 2014 All rights reserved.
 *
 *   文件名称：FriendShip.h
 *   创 建 者：Zhang Yuanhao
 *   邮    箱：bluefoxah@gmail.com
 *   创建日期：2014年12月15日
 *   描    述：
 *
 ================================================================*/

#ifndef FRIEND_SHIP_H_
#define FRIEND_SHIP_H_

#include "ImPduBase.h"

namespace DB_PROXY
{
    /**
     *  获取指定时间以后发生变动的会话
     *
     *  @param pPdu      收到的packet包指针
     *  @param conn_uuid 该包过来的 socket 描述符
     */
    void getRecentSession(CImPdu* pPdu, uint32_t conn_uuid);


    /**
     *  删除会话接口
     *
     *  @param pPdu      收到的packet包指针
     *  @param conn_uuid 该包过来的socket 描述符
     */
    void deleteRecentSession(CImPdu* pPdu, uint32_t conn_uuid);
};


#endif /* FRIEND_SHIP_H_ */
