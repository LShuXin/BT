# Protocol Buffers

## 一、使用说明

### 1.1 版本

版本：3.18.0

下载链接： https://github.com/protocolbuffers/protobuf/releases/tag/v3.18.0

我们选择源码进行安装，具体的安装步骤可以查看就 `server/src/make_protobuf.sh`

### 1.2 服务端与 Android 端如何进行 pb 文件生成

- 首先确保你的终端中能执行 protoc 命令，比如

  ```
  [root@localhost pb]# protoc --version
  libprotoc 3.18.0
  ```

- 运行 `./creash.sh` 命令从而生成 cpp、java 等版本的 pb 文件，其中 cpp 用于服务端，java 用于 Android 端
- 运行 `./sync.sh`  命令将生成的 pb 文件（cpp 版本）拷贝到服务端指定的源码目录
- 手动将生成的 java 版本的 pb 文件拷贝到客户端

### 1.3 OC 如何进行 pb 文件生成

上述生成 pb 的流程对 OC 不适用，对于 protoc 3.x 生成 OC pb 的方式如下：

1. 安装 OC 版本的 protoc

   在 mac 下载 [protoc-3.18.0-osx-x86_64.zip](https://github.com/protocolbuffers/protobuf/releases/download/v3.18.0/protoc-3.18.0-osx-x86_64.zip)，假如将解压后的文件放入以下目录：

   ```
   apples-Mac-mini-1243:protoc-3.18.0-osx-x86_64 apple$ pwd
   /Users/apple/IDE/protoc-3.18.0-osx-x86_64
   apples-Mac-mini-1243:protoc-3.18.0-osx-x86_64 apple$ ls
   bin		include		readme.txt
   ```

   将` /Users/apple/IDE/protoc-3.18.0-osx-x86_64/bin` 加入 PATH 环境变量后即可在任意位置运行 protoc 命令（第一次运行时可能需要在“隐私&安全”授权）

2. 

### 1.4 注意事项

- 客户端、服务端的 pb 版本要一致，具体来说服务端可能需要编译指定版本的 pb、客户端可能需要修改 pb 插件/库版本 
- OC pb 文件的生成是不一样的，protoc 2.x 需要配合插件进行代码生成，protoc 3.x 需要使用 OC 版本的 protoc 进行文件生成
- 从 OC 使用 pb 的角度 2.x 与 3.x 的不同在与 3.x 没有了builder、一些命名规则发生了变化、Cocoapods 依赖发生了变化、parseFromData 方法提供了 error 回调
- OC 使用 pb 时需要在 Build Phase 中将 pb 文件的 compile flag 设置为 -fno-objc-arc



## 二、协议设计

### 2.1 模块划分

所有协议并非完全一致，有些协议不知道具体放在哪个文件合适，就挑选了一个相对合适的文件放入。

- IM.BaseDefine.proto

该文件定义了 service_id, command_id 以及一些基础数据结构，如用户，部门等

- IM.Buddy.proto

该文件定义了与最近联系人，会话相关的协议。

- IM.File.proto

该文件定义了文件传输相关的协议，但是暂未使用。

- IM.Group.proto

该文件定义了与群组相关的协议。

- IM.Login.proto

该文件定义了与登录相关的协议。

- IM.Message.proto

该文件定义了与消息相关的协议

- IM.Other.proto

该文件目前只有一个心跳协议。

- IM.Server.proto

该文件定义了服务端之间专属的协议

- IM.SwitchService.proto

该文件定义了用户之间的P2P消息，比如正在输入等，服务端不关心具体的协议内容，只做转发，客户端互相之间知道协议的含义。



### 2.2 协议说明

这里简单说下各个协议的含义，不会具体到每个字段标识什么意思，一般的协议定义的时候，就可以见名知意，不过有些协议还是会导致一些误导，在介绍这些协议的时候，我会尽量说清楚。
介绍顺序以数据结构，协议在文件出现的先后顺序来进行。



#### （1）基础数据结构定义

基础数据结构的定义在IM.BaseDefine.proto中。

- ServiceID

服务号，对不同的协议进行归类，便于后面针对模块进行分类。

- LoginCmdID

登陆相关的命令。

- BuddyListCmdID

最近联系人，会话等相关命令。

- MessageCmdID

消息相关命令。

- GroupCmdID

群组相关命令。

- FileCmdID

文件传输相关命令。

- SwitchServiceCmdID

定义了P2P命令。

- OtherCmdID

定义了一些其他的命令，目前只使用了心跳。

- ResultType

这里定义了登陆返回错误码。

- KickReasonType

这里定义了用户被踢的原因。 

- OnlineListType

o(╯□╰)o

- UserStatType

用户状态定义。

- SessionType

会话类型，群组，还是单聊。

- MsgType

消息类型，单聊文字，单聊语音，群聊文字，群聊语音。

- ClientType

客户端类型，包含win，mac， Android， iOS

- GroupType

群组类型，临时群，固定群。

- GroupModifyType

群成员更改类型，增加成员，删除成员。

- XFileType

文件传输类型，在线传输，离线传输。

- ClientFileState

文件传输状态定义。

- ClientFileRole

文件传输角色定义。

- FileServerError

文件传输错误码。

- SessionStatusType

最近联系人(会话)状态定义，删除，正常。

- DepartmentStatusType

部门状态定义。

- IpAddr

一组服务的唯一标识：IP + port定义

- UserInfo

用户信息数据结构。

- ContactSessionInfo

最近联系人(会话)数据结构

- UserStat

用户状态数据结构

- ServerUserStat

服务端用户状态数据结构，比UserStat多了一个用户所在端。

- UnreadInfo

未读消息数据结构，其中seession_id定义的有点歧义，在这里标识对方id(可以理解为peer_id)，如果是单聊，表示对方id，如果是群组，表示群id

- MsgInfo

消息数据结构，session_id 同上。

- GroupVersionInfo

群组版本信息数据结构，为增量推送群组信息考虑。

- GroupInfo

群组数据结构。

- UserTokenInfo

推送用户token数据结构。

- PushResult

推送结果定义。

- ShieldStatus

群消息推送屏蔽状态

- OfflineFileInfo

离线文件信息。

- DepartInfo

部门信息数据结构。



#### （2）最近会话相关协议

该系列协议定义在IM.Buddy.proto中，主要定义了最近联系人(会话)相关的协议。

- IMRecentContactSessionReq

最近联系人会话请求。

- IMRecentContactSessionRsp

最近联系人会话应答。

- IMUserStatNotify

用户状态通知。

- IMUsersInfoReq

用户信息请求

- IMUsersInfoRsp

用户信息应答。

- IMRemoveSessionReq

删除最近会话请求

- IMRemoveSessionRsp

删除最近会话应答。

- IMAllUserReq

所有用户请求，其中带了一个latest_update_time字段，请求应答会返回latest_update_time时间之后发生变化的用户回来。

- IMAllUserRsp

所有用户应答，同事携带一个新的latest_update_time本次最新的用户变化时间，下次请求带上这个字段即可。

- IMUsersStatReq

用户状态请求。

- IMUsersStatRsp

用户状态应答。

- IMChangeAvatarReq

更改头像请求(暂时未使用)

- IMChangeAvatarRsp

更改头像应答(暂时未使用)

- IMPCLoginStatusNotify

PC登陆后通知移动端。

- IMRemoveSessionNotify

删除会话后的通知，用于多端同步。

- IMDepartmentReq

部门信息请求。

- IMDepartmentRsp

部门信息应答。

文件传输协议协议定义在IM.File.proto中，因为暂时未使用，暂不做说明。



#### （3）群组相关协议

该系列协议定义在IM.Group.proto中，定义了与群组相关的协议。

- IMNormalGroupListReq

用户所在固定群组请求。

- IMNormalGroupListRsp

用户所在固定群组应答。

- IMGroupInfoListReq

群组信息请求。

- IMGroupInfoListRsp

群组信息应答。

- IMGroupCreateReq

创建群组请求。

- IMGroupCreateRsp

创建群组应答。

- IMGroupChangeMemberReq

群组成员变更请求。

- IMGroupChangeMemberRsp

群组成员变更应答。

- IMGroupShieldReq

屏蔽群组请求。

- IMGroupShieldRsp

屏蔽群组应答。

- IMGroupChangeMemberNotify

群组成员变更通知。



#### （4）登陆相关协议

该系列协议定义在IM.Login.proto文件中，定义了一系列与登陆相关的协议。

- IMMsgServReq

msg_server地址请求(已经废弃，改用http请求)

- IMMsgServRsp

msg_server地址应答(已经废弃)

- IMLoginReq

登陆请求

- IMLoginRes

登陆应答。

- IMLogoutReq

登出请求。

- IMLogoutRsp

登出应答。

- IMKickUser

踢用户。

- IMDeviceTokenReq

设备token汇报请求(用于推送)

- IMDeviceTokenRsp

设备token汇报应答

- IMKickPCClientReq

移动端踢PC端请求

- IMKickPCClientRsp

移动端踢PC端应答。



#### （5）消息相关协议

该系列协议定义在IM.Message.proto文件中，定义了一系列与消息相关的协议。

- IMMsgData

发送消息协议。

- IMMsgDataAck

消息收到回复。

- IMMsgDataReadAck

消息已读回复。

- IMMsgDataReadNotify

消息已读通知，用于多端同步。

- IMClientTimeReq

服务器时间请求。

- IMClientTimeRsp

服务器时间回复。

- IMUnreadMsgCntReq

未读消息计数请求。

- IMUnreadMsgCntRsp

未读消息计数回复

- IMGetMsgListReq

获取消息请求。

- IMGetMsgListRsp

获取消息回复。
对于群而言，如果消息数目返回的数值小于请求的cnt,则表示群的消息能拉取的到头了，更早的消息没有权限拉取。如果msg_cnt 和 msg_id_begin计算得到的最早消息id与实际返回的最早消息id不一致，说明服务器消息有缺失，需客户端做一个缺失标记，避免下次再次拉取。

- IMGetLatestMsgIdReq

获取某个会话最新msg_id请求

- IMGetLatestMsgIdRsp

获取某个会话最新msg_id回复

- IMGetMsgByIdReq

通过msg_id获取消息请求

- IMGetMsgByIdRsp

通过msg_id获取消息回复



#### （6） 其他协议

该系列协议定义在IM.Other.proto文件中，目前只定义了心跳协议。

- IMHeartBeat

心跳协议



#### （7） 服务器端之间专属协议

	该系列协议定义在IM.Server.proto文件中，定义了服务端之间专属的相关协议。主要用于msg_server与db_proxy之间的通信。

- IMStopReceivePacket

由db_proxy发给其他服务端，用于通知其他服务端本端停止接收包，主要在重启的过程中用到，目前做的比较ugly。

- IMValidateReq

客户端登陆认证请求。

- IMValidateRsp

客户端登陆认证回复。

- IMGetDeviceTokenReq

获取某个用户的设备token请求，主要用于推送。

- IMGetDeviceTokenRsp

获取某个用户设备token回复。

- IMRoleSet

服务端主从角色变换，用于route_server。

- IMOnlineUserInfo

在线用户信息。

- IMMsgServInfo

msg_server信息，主要用于msg_server向login_server汇报用。

- IMUserStatusUpdate

用户状态变更。

- IMUserCntUpdate

用户数量变化。

- IMServerKickUser

服务端踢人。

- IMServerPCLoginStatusNotify

PC登陆通知。

- IMPushToUserReq

发送push通知请求。

- IMPushToUserRsp

发送push通知回复。

- IMGroupGetShieldReq

获取用户屏蔽群设置请求。

- IMGroupGetShieldRsp

获取用户屏蔽群设置回复。

- IMFileTransferReq

文件传输请求

- IMFileTransferRsp

文件传输回复。

- IMFileServerIPReq

文件服务器信息请求

- IMFileServerIPRsp

文件服务器信息回复。



#### （8） P2P协议

该系列协议定义在IM.SwitchService.proto中，主要定义了客户端之间的协议，服务端只做转发。

- IMP2PCmdMsg

客户端之间的协议，服务端不认识，只做转发。例如“正在输入”这些。

