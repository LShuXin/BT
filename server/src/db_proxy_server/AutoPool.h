/*================================================================
*     Copyright (c) 2015年 lanhu. All rights reserved.
*   
*   文件名称：AutoPool.h
*   创 建 者：Zhang Yuanhao
*   邮    箱：bluefoxah@gmail.com
*   创建日期：2015年03月18日
*   描    述：用于获取对指定 redis/mysql 数据库包装连接对象的指针
*
#pragma once
================================================================*/
#ifndef __AUTOPOOL_H__
#define __AUTOPOOL_H__

class CDBConn;
class CacheConn;

/**
 * mysql
 * */
class CAutoDB
{
public:
    /**
     * @param pDBConn 数据库标识符
     * @param pDBConn 包装连接对象指针的指针，供外部获取包装连接对象
     * */
    CAutoDB(const char* pDBName, CDBConn** pDBConn);
    ~CAutoDB();
private:
    CDBConn* m_pDBConn;
};

/**
 * redis
 * */
class CAutoCache
{
    /**
     * @param pDBConn 数据库标识符
     * @param pDBConn 包装连接对象指针的指针，供外部获取包装连接对象
     * */
    CAutoCache(const char* pCacheName, CacheConn** pCacheConn);
    ~CAutoCache();
private:
    CacheConn* m_pCacheConn;
};
#endif
