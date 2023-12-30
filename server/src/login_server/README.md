# login_server

## login_server 的作用
- 负载均衡，将连接数未满并且接收连接最少的 msg_server 的 msg_server、msfs_server 信息返回给客户端，供客户端连接
- 本服务可以查看在线用户数量

## 整体工作流程
- 根据配置文件分别在指定端口监听客户端发起的 http 请求和 msg_server 发起的长连接请求
- 各个 msg_server 需要先用长连接连接此服务进行服务注册，将自己的信息注册到 login_server, 当用户在 msg_server 连接成功、连接关闭时要通知 login_server
- msg_server 注册完成后，客户端才能通过 http 请求让 login_server 为自己分配 msg_server
- 客户端连接到分配到的 msg_server