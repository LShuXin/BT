[toc]

> 数据库代理服务是本 IM 系统的核心之一，所有的业务逻辑都封装在该模块中

# 一、编译此服务前的准备
- 在 `/usr/local/mysql/` 安装 mysql 数据库 
- 将 `/usr/local/mysql/bin` 加入 $PATH 环境变量
- 安装 redis
```
	git clone https://github.com/redis/hiredis.git
	cd hiredis
	make 
	sudo make install
```



# 二、整体架构

## 2.1 服务启动流程

- 创建缓存池管理者单例
- 创建数据库连接池管理者单例
- 创建各种业务处理模块单例，包括：
  - CAudioModel
  - CGroupMessageModel
  - CGroupModel
  - CMessageModel
  - CSessionModel
  - CRelationModel
  - CUserModel
  - CFileModel
- 为 CAudioModel 初始化文件服务器地址
- 初始化 curl
- 注册消息处理器
- 初始化线程池
- 开启消息回复工作循环、开启长链接心跳工作循环
- 初始化同步中心，开始同步数据
- 开启网络监听，接受并处理到来的数据包



## 2.2 请求处理流程

- 连接到来后 new 一个 ProxyConn 对象，将此 ProxyConn 对象放到：（1）以 socket handle 为key的map中，后续可以发送心跳

  （2）以 uuid 为key的map中（由于 socket 复用，socket handle无法标识每个进入的请求）

- 连接数据数据可读时，调用 ProxyConn 对象的 OnRead 方法，一次性将数据读完

- 读完后根据数据显示的消息类型找到 handler，并新建一个任务，将任务的工作函数设置为 handler，将此任务仍进线程池

- 任务完成后，如果此消息需要回复，则将回复数据包放入主线程的回复消息队列



## 2.3 数据库连接池管理

#### **CDBManager** 

```
/**
 * 数据库连接池管理者（单例）
 * 在实现上：
 * 1. 管理着多个数据库连接池（CDBPool）
 * 2. 每个 CDBPool 只连接一个 mysql 数据库
 * 3. 每个 CDBPool 有着对同一个 mysql 数据库的多个"包装连接对象（CDBConn）"
 * 4. 实例提供 CDBConn* GetDBConn(const char* dbpool_name) 方法，用于获取对指定数据库的包装连接对象指针，
 * 可以通过该指针对数据库进行读写
 * 5. 提供 void RelDBConn(CDBConn* pConn) 方法，用于将使用完的包装连接对象放回数据库连接池
 * 6. 提供 int Init() 方法，用于根据配置文件对开对多个（主、从）数据库的连接池
 * 7. 关于主、从数据库，master 用于写，slave 用于读（目前这两个数据库其实是连接的同一个 mysql 实例，但在编码上严格遵守了master 用于写，slave 用于读）
 * */
```



## 2.4 缓存连接池管理

#### CacheManager

```
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
```



## 2.3 表设计

数据采用 mysql 存储，目前在设计上区分了主、从数据库，主数据库只用于写，从数据库只用于读。在实现上，代码中严格区分了主从数据库，但是目前主、从数据库连接的其实都是同一个数据库实例。

### IMDepart

```
CREATE TABLE `IMDepart` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '部门id',
  `departName` varchar(64) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '部门名称',
  `priority` int(11) unsigned NOT NULL DEFAULT '0' COMMENT '显示优先级',
  `parentId` int(11) unsigned NOT NULL COMMENT '上级部门id',
  `status` int(11) unsigned NOT NULL DEFAULT '0' COMMENT '状态',
  `created` int(11) unsigned NOT NULL COMMENT '创建时间',
  `updated` int(11) unsigned NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_departName` (`departName`),
  KEY `idx_priority_status` (`priority`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
```



### IMDiscovery

```
CREATE TABLE `IMDiscovery` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `itemName` varchar(64) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '名称',
  `itemUrl` varchar(64) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT 'URL',
  `itemPriority` int(11) unsigned NOT NULL COMMENT '显示优先级',
  `status` int(11) unsigned NOT NULL DEFAULT '0' COMMENT '状态',
  `created` int(11) unsigned NOT NULL COMMENT '创建时间',
  `updated` int(11) unsigned NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_itemName` (`itemName`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
```



### IMGroupMember

```
CREATE TABLE `IMGroupMember` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `groupId` int(11) unsigned NOT NULL COMMENT '群Id',
  `userId` int(11) unsigned NOT NULL COMMENT '用户id',
  `status` tinyint(4) unsigned NOT NULL DEFAULT '1' COMMENT '是否退出群，0-正常，1-已退出',
  `created` int(11) unsigned NOT NULL DEFAULT '0' COMMENT '创建时间',
  `updated` int(11) unsigned NOT NULL DEFAULT '0' COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_groupId_userId_status` (`groupId`,`userId`,`status`),
  KEY `idx_userId_status_updated` (`userId`,`status`,`updated`),
  KEY `idx_groupId_updated` (`groupId`,`updated`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户和群的关系表';
```



### IMGroupMessage_0

群消息表分了8个,计算方式为：IMGroupMessage_${nGroupId % 8}

```
CREATE TABLE `IMGroupMessage_0` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `groupId` int(11) unsigned NOT NULL COMMENT '用户的关系id',
  `userId` int(11) unsigned NOT NULL COMMENT '发送用户的id',
  `msgId` int(11) unsigned NOT NULL COMMENT '消息ID',
  `content` varchar(4096) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '消息内容',
  `type` tinyint(3) unsigned NOT NULL DEFAULT '2' COMMENT '群消息类型,101为群语音,2为文本',
  `status` int(11) unsigned NOT NULL DEFAULT '0' COMMENT '消息状态',
  `updated` int(11) unsigned NOT NULL DEFAULT '0' COMMENT '更新时间',
  `created` int(11) unsigned NOT NULL DEFAULT '0' COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_groupId_status_created` (`groupId`,`status`,`created`),
  KEY `idx_groupId_msgId_status_created` (`groupId`,`msgId`,`status`,`created`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='IM群消息表';
```



### IMMessage_0

单聊消息表分了8个,计算方式为：IMGroupMessage_${nRelateId % 8}

```
CREATE TABLE `IMMessage_0` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `relateId` int(11) unsigned NOT NULL COMMENT '用户的关系id',
  `fromId` int(11) unsigned NOT NULL COMMENT '发送用户的id',
  `toId` int(11) unsigned NOT NULL COMMENT '接收用户的id',
  `msgId` int(11) unsigned NOT NULL COMMENT '消息ID',
  `content` varchar(4096) COLLATE utf8mb4_bin DEFAULT '' COMMENT '消息内容',
  `type` tinyint(2) unsigned NOT NULL DEFAULT '1' COMMENT '消息类型',
  `status` tinyint(1) unsigned NOT NULL DEFAULT '0' COMMENT '0正常 1被删除',
  `updated` int(11) unsigned NOT NULL COMMENT '更新时间',
  `created` int(11) unsigned NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_relateId_status_created` (`relateId`,`status`,`created`),
  KEY `idx_relateId_status_msgId_created` (`relateId`,`status`,`msgId`,`created`),
  KEY `idx_fromId_toId_created` (`fromId`,`toId`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
```



### IMRecentSession

```
CREATE TABLE `IMRecentSession` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `userId` int(11) unsigned NOT NULL COMMENT '用户id',
  `peerId` int(11) unsigned NOT NULL COMMENT '对方id',
  `type` tinyint(1) unsigned DEFAULT '0' COMMENT '类型，1-用户,2-群组',
  `status` tinyint(1) unsigned DEFAULT '0' COMMENT '用户:0-正常, 1-用户A删除,群组:0-正常, 1-被删除',
  `created` int(11) unsigned NOT NULL DEFAULT '0' COMMENT '创建时间',
  `updated` int(11) unsigned NOT NULL DEFAULT '0' COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_userId_peerId_status_updated` (`userId`,`peerId`,`status`,`updated`),
  KEY `idx_userId_peerId_type` (`userId`,`peerId`,`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
```



### IMRelationShip

```
CREATE TABLE `IMRelationShip` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `smallId` int(11) unsigned NOT NULL COMMENT '用户A的id',
  `bigId` int(11) unsigned NOT NULL COMMENT '用户B的id',
  `status` tinyint(1) unsigned DEFAULT '0' COMMENT '用户:0-正常, 1-用户A删除,群组:0-正常, 1-被删除',
  `created` int(11) unsigned NOT NULL DEFAULT '0' COMMENT '创建时间',
  `updated` int(11) unsigned NOT NULL DEFAULT '0' COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_smallId_bigId_status_updated` (`smallId`,`bigId`,`status`,`updated`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
```



### IMUser

```
CREATE TABLE `IMUser` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '用户id',
  `sex` tinyint(1) unsigned NOT NULL DEFAULT '0' COMMENT '1男2女0未知',
  `name` varchar(32) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '用户名',
  `domain` varchar(32) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '拼音',
  `nick` varchar(32) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '花名,绰号等',
  `password` varchar(32) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '密码',
  `salt` varchar(4) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '混淆码',
  `phone` varchar(11) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '手机号码',
  `email` varchar(64) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT 'email',
  `avatar` varchar(255) COLLATE utf8mb4_bin DEFAULT '' COMMENT '自定义用户头像',
  `departId` int(11) unsigned NOT NULL COMMENT '所属部门Id',
  `status` tinyint(2) unsigned DEFAULT '0' COMMENT '1. 试用期 2. 正式 3. 离职 4.实习',
  `created` int(11) unsigned NOT NULL COMMENT '创建时间',
  `updated` int(11) unsigned NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_domain` (`domain`),
  KEY `idx_name` (`name`),
  KEY `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
```




## 2.3 缓存key设计

### 2.3.1 缓存设计概述
本服务一共需要如下 5 个 redis 数据库
- unread
  用于缓存未读消息

- group_set
  用于缓存用户群组设置、目前只有推送

- token
  用于缓存用户token

- sync

  同步中心使用，缓存上一次群组同步的时间

- group_member
用于缓存各个群的群成员、群成员入群时间

### 2.3.2 缓存设计详情

- unread: 未读消息缓存
```
// 单聊用户未读消息数
redis key: unread_userIdA
redis value type: hash    
hash field: userIdN         // 某用户N的用户id
hash value: nUnread         // 用户N向用户A发送的、用户A看到的未读数
```

- group_set： 群组设置缓存
```
redis value type: hash
redis key: group_set_${nGroupId}
hash key: uUserId_${nType}
hash value: ${nStatus}
desc: uUserId 在 nGroupId 中设置类型为 nType 的状态，0: 推送
```

- token： 用户 token

- sync：



- group_member： 群组信息
```
redis key: group_member_nGroupId
redis value type: hash
hash field: uUserId 用户id
hash value: nJoinTime 用户加入此群的时间
```



### 

```
群聊用户未读消息数
```






```
${nGroupId}_im_group_msg
```



```
${nUserId}_${nGroupId}_im_user_group
```





# 四、模块划分、各模块指责

在模块划分上可以分为基础模块和业务处理模块

## 4.1 SyncCenter

SyncCenter 不直接处理用户的业务处理请求，但是他会在服务启动之后没隔5秒执行一次会话同步, 所谓的会话同步就是：

- 从 redis 中找出在上次群组同步之后，又有新聊天消息的群组，然后更新这些群组中用户的会话

- 修改 redis 中最近一次同步群组会话的时间

## 4.2 Login

- 登录并返回登录信息

## 4.3 Session

- 获取指定时间以后发生变动的会话
- 删除会话

## 4.4 User

- 通过用户 id 列表获取用户信息列表
- 获取指定时间以后有信息更新的用户数组、最近一次用户信息更新发生的时间
- 获取指定时间以后有信息更新的部门数组、最近一次部门信息发生更新的时间

## 4.5 message

- 向某人发送某种类型的消息
- 根据会话 id、起始消息 id、limit 拉取指定特定会话指定数量的消息
- 获取未读消息列表，包括是哪个会话，未读数是多少，该会话最新的消息 id 是多少、最后一条消息类型是什么、内容是什么、是谁发送的

- 清除未读消息数，包括群聊和单聊
- 通过消息 id 列表查询对应的消息内容
- 获取指定会话的最新消息

## 4.6 token 

- 设置 token
- 获取 token

## 4.7 groupSetting

- 获取群推送设置
- 设置群推送状态

## 4.8 group

- 获取正式群列表
- 通过群 id 列表获取群信息列表

- 创建群聊
- 编辑群成员
