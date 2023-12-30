/*
 * DBPool.h
 *
 *  Created on: 2014年7月22日
 *      Author: ziteng
 *  Modify By ZhangYuanhao
 *  2015-01-12
 *  enable config the max connection of every instance
 */

#ifndef DBPOOL_H_
#define DBPOOL_H_

#include "../base/util.h"
#include "ThreadPool.h"
//#include <mysql/mysql.h>
#include <mysql.h>

#define MAX_ESCAPE_STRING_LEN	10240

/**
 * mysql 查询结果包装对象
 * */
class CResultSet {
public:
	CResultSet(MYSQL_RES* res);
	virtual ~CResultSet();

	bool Next();
	int GetInt(const char* key);
	char* GetString(const char* key);
private:
	int _GetIndex(const char* key);

	MYSQL_RES* 			m_res;
	MYSQL_ROW			m_row;
	map<string, int>	m_key_map;
};

/*
 * TODO:
 * 用MySQL的prepare statement接口来防止SQL注入
 * 暂时只用于插入IMMessage表，因为只有那里有SQL注入的风险，
 * 以后可以把全部接口用这个来实现替换
 */
class CPrepareStatement {
public:
	CPrepareStatement();
	virtual ~CPrepareStatement();

	bool Init(MYSQL* mysql, string& sql);

	void SetParam(uint32_t index, int& value);
	void SetParam(uint32_t index, uint32_t& value);
    void SetParam(uint32_t index, string& value);
    void SetParam(uint32_t index, const string& value);

	bool ExecuteUpdate();
	uint32_t GetInsertId();
private:
	MYSQL_STMT*	m_stmt;
	MYSQL_BIND*	m_param_bind;
	uint32_t	m_param_cnt;
};

class CDBPool;

/**
 * mysql 包装连接对象
 * */
class CDBConn {
public:
	CDBConn(CDBPool* pDBPool);
	virtual ~CDBConn();
	int Init();

	CResultSet* ExecuteQuery(const char* sql_query);
	bool ExecuteUpdate(const char* sql_query);
	char* EscapeString(const char* content, uint32_t content_len);

	uint32_t GetInsertId();

	const char* GetPoolName();
	MYSQL* GetMysql() { return m_mysql; }
private:
    /**
     * 本连接所属的 mysql 连接池，可以获取具体的 mysql 信息
     * */
	CDBPool* 	m_pDBPool;
	MYSQL* 		m_mysql;
	//MYSQL_RES*	m_res;
	char		m_escape_string[MAX_ESCAPE_STRING_LEN + 1];
};

/**
 * 数据库连接池
 * */
class CDBPool {
public:
	CDBPool(const char* pool_name, const char* db_server_ip, uint16_t db_server_port,
			const char* username, const char* password, const char* db_name, int max_conn_cnt);
	virtual ~CDBPool();

	int Init();
	CDBConn* GetDBConn();
	void RelDBConn(CDBConn* pConn);

	const char* GetPoolName() { return m_pool_name.c_str(); }
	const char* GetDBServerIP() { return m_db_server_ip.c_str(); }
	uint16_t GetDBServerPort() { return m_db_server_port; }
	const char* GetUsername() { return m_username.c_str(); }
	const char* GetPasswrod() { return m_password.c_str(); }
	const char* GetDBName() { return m_db_name.c_str(); }
private:
	string 		m_pool_name;
	string 		m_db_server_ip;
	uint16_t	m_db_server_port;
	string 		m_username;
	string 		m_password;
	string 		m_db_name;
	int			m_db_cur_conn_cnt;
	int 		m_db_max_conn_cnt;
	list<CDBConn*>	m_free_list;
	CThreadNotify	m_free_notify;
};

/**
 * 数据库连接池管理者（单例）
 * 在实现上：
 * 1. 管理着多个数据库连接池（CDBPool）
 * 2. 每个 CDBPool 只连接一个 mysql 数据库
 * 3. 每个 CDBPool 有着对同一个 mysql 数据库的多个"包装连接对象（CDBConn）"
 * 4. 实例提供 CDBConn* GetDBConn(const char* dbpool_name) 方法，用于获取对指定数据库的包装连接对象指针，
 * 可以通过该指针对数据库进行读写
 * 5. 提供 void RelDBConn(CDBConn* pConn) 方法，用于将使用完的包装连接对象放回数据库连接池
 * 6. 提供 int Init() 方法，用于根据配置文件初始化对多个（主、从）数据库的连接池
 * 7. 关于主、从数据库，master 用于写，slave 用于读（目前这两个数据库其实是连接的同一个 mysql 实例，但在编码上严格遵守了master 用于写，slave 用于读）
 * */
class CDBManager {
public:
	virtual ~CDBManager();

	static CDBManager* getInstance();

    // 用于根据配置文件初始化对多个（主、从）数据库的连接池
	int Init();

	// 用于获取对指定数据库的包装连接对象指针，可以通过该指针对数据库进行读写
	CDBConn* GetDBConn(const char* dbpool_name);

	// 用于将使用完的包装连接对象放回数据库连接池
	void RelDBConn(CDBConn* pConn);
private:
	CDBManager();

private:
	static CDBManager*		s_db_manager;
	map<string, CDBPool*>	m_dbpool_map;
};


#endif /* DBPOOL_H_ */
