/*
 * CachePool.h
 *
 *  Created on: 2014年7月24日
 *      Author: ziteng
 *  Modify By ZhangYuanhao 
 *  2015-01-13
 *  Add some redis command
 */

#ifndef CACHEPOOL_H_
#define CACHEPOOL_H_

#include <vector>
#include "../base/util.h"
#include "ThreadPool.h"
#include "hiredis.h"

class CachePool;

/**
 * 包装连接对象，隐藏对 redis 数据库的真实连接
 * 实例代表对特定 redis 数据库发起的一个包装连接
 * */
class CacheConn {
public:
    /**
     * 构造函数中，将当前包装连接对象所属的缓存池指针传入，将内部真正连接对象置空，将上次更新真正连接对象的时间置空
     * */
	CacheConn(CachePool* pCachePool);

	/**
	 * 析构函数中，释放内部对 redis 数据库的真实连接
	 * */
	virtual ~CacheConn();

	/**
	 * 指定时间内不允许重复调用，默认为4s；初始化时的主要工作是根据构造函数中传入的缓存池指针，找到 redis 服务 ip、port、数据库编号，
	 * 然后建立真正的连接，并选择指定编号的 redis 数据库
	 * */
	int Init();

	const char* GetPoolName();

	string get(string key);
	string setex(string key, int timeout, string value);
    string set(string key, string& value);
    
    // 批量获取
    bool mget(const vector<string>& keys, map<string, string>& ret_value);
    // 判断一个key是否存在
    bool isExists(string &key);

	// Redis hash structure
	long hdel(string key, string field);
	string hget(string key, string field);
	bool hgetAll(string key, map<string, string>& ret_value);
	long hset(string key, string field, string value);
	string hmset(string key, map<string, string>& hash);
	bool hmget(string key, list<string>& fields, list<string>& ret_value);
    long hincrBy(string key, string field, long value);

    long incrBy(string key, long value);
    
    // 原子加减1
    long incr(string key);
    long decr(string key);

	// Redis list structure
	long lpush(string key, string value);
	long rpush(string key, string value);
	long llen(string key);
	bool lrange(string key, long start, long end, list<string>& ret_value);

private:
    /**
     * 本连接所属的缓存池
     * */
	CachePool* 		m_pCachePool;

	/**
	 * 真正的连接对象（内部）
	 * */
	redisContext* 	m_pContext;

	/**
	 * 上次更新内部连接对象的时间
	 * */
	uint64_t		m_last_connect_time;
};

/**
 * 缓存池， 对应着一个 redis 数据库
 * */
class CachePool {
public:
	CachePool(const char* pool_name, const char* server_ip, int server_port, int db_num, int max_conn_cnt);

    /**
     * 析构函数中释放对当前 redis 数据库的所有连接
     * */
	virtual ~CachePool();

	/**
	 * 初始化时，直接根据 MIN_CACHE_CONN_CNT 提前打开指定数量的对当前 redis 数据库的连接
	 * */
	int Init();

	/**
	 * 获取一个针对当前 redis 数据库的包装连接对象，可以用获取到的包装连接对象操作数据库
	 * 一个缓存池中多个对当前 redis 数据库的包装连接对象组成了一个队列，从队首取一个即可
	 * */
	CacheConn* GetCacheConn();

	/**
	 * 当包装连接对象使用完毕之后，将其再次添加到包装连接对象队列，供后续取用
	 * */
	void RelCacheConn(CacheConn* pCacheConn);

	const char* GetPoolName() { return m_pool_name.c_str(); }
	const char* GetServerIP() { return m_server_ip.c_str(); }
	int GetServerPort() { return m_server_port; }
	int GetDBNum() { return m_db_num; }
private:
    /**
     * 用于唯一标识每个 redis 数据库的字符串
     * */
	string 		m_pool_name;

	/**
	 * redis 数据库所在机器ip
	 * */
	string		m_server_ip;

	/**
	 * redis 服务所在端口
	 * */
	int			m_server_port;

	/**
	 * redis 数据库编号
	 * */
	int			m_db_num;

	/**
	 * 当前缓存池打开的连接对象的个数
	 * */
	int			m_cur_conn_cnt;

	/**
	 * 当前缓存池最多允许打开的连接对象的个数
	 * */
	int 		m_max_conn_cnt;

	/**
	 * 当前空闲的连接对象
	 * */
	list<CacheConn*>	m_free_list;

	/**
	 * 即有锁的功能，又有条件变量的功能
	 * */
	CThreadNotify		m_free_notify;
};

/**
 * 缓存连接池管理者（单例）
 * 在实现上：
 * 1. 管理着多个缓存连接池（CachePool）
 * 2. 每个 CachePool 只连接一个 redis 数据库
 * 3. 每个 CachePool 有着对同一个 redis 数据库的多个"包装连接对象（CacheConn）"
 * 4. 提供 int Init() 方法，可以按照配置文件初始化指定的缓存池
 * 5. 提供 CacheConn* GetCacheConn(const char* pool_name) 方法，获取对指定 redis 的包装连接对象指针（CacheConn*），
 * 可以通过该指针对 redis 数据库进行读写
 * 6. 提供 void RelCacheConn(CacheConn* pCacheConn) 方法，将使用完成的包装连接对象重新放入连接池，供后续取用
 * */
class CacheManager {
public:
	virtual ~CacheManager();

	static CacheManager* getInstance();

	// 根据配置文件初始化缓存池
	int Init();

	/**
	 * 获取对指定 redis 的包装连接对象指针（CacheConn*）
	 * 可以通过该指针对 redis 数据库进行读写
	 * */
	CacheConn* GetCacheConn(const char* pool_name);

	// 将使用完成的包装连接对象重新放入连接池，供后续取用
	void RelCacheConn(CacheConn* pCacheConn);
private:
	CacheManager();

private:
	static CacheManager* 	s_cache_manager;

	/**
	 * 对应着多个 redis 数据库实例
	 * */
	map<string, CachePool*>	m_cache_pool_map;
};

#endif /* CACHEPOOL_H_ */
