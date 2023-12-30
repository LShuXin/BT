# TeamTalk

## 项目中用到的一些库
- [level-db](https://github.com/google/leveldb/tree/main) 用于持久化 KV 存储，类似的有 MMKV
- [SDWebImage](https://github.com/SDWebImage) 主流的图片库，可以方便的处理图片的加载、缓存、占位图
- [FMDB](https://github.com/ccgus/fmdb) SQLite 数据库的封装库
- [AFNetworking](https://github.com/AFNetworking/AFNetworking/fork) OJ 网络请求库（已废弃，推荐迁移到[Alamofire](https://github.com/Alamofire/Alamofire)）
- [DACircularProgress](https://github.com/danielamitay/DACircularProgress) 进度条控件
- [MBProgressHUD](https://github.com/jdg/MBProgressHUD) toast 控件
- [PSTCollectionView](https://github.com/steipete/PSTCollectionView) 
- [HPGrowingTextView](https://github.com/adonoho/HPGrowingTextView) 基于 [GrowingTextView](https://github.com/hanspinckaers/GrowingTextView)
- [ProtocolBuffers](https://github.com/protocolbuffers/protobuf)
- [SCLAlertView-Objective-C](https://github.com/dogo/SCLAlertView) 
- [MWPhotoBrowser](https://github.com/mwaterfall/MWPhotoBrowser)

## 登录
- 通过 httpserver 取得 tcpServer 的 ip 和 port、discoverUrl、msfsUrl
- 登录 tcpServer
- 登录 msgServer
- 将用户名、密码保存到 NSUserDefaults 和内存
- 将本地用户状态标记为在线
- 将用户信息保存到 TheRuntime.user
- 广播用户登录成功通知
- 更新所有用户信息（用户信息有版本，如果请求到的版本没有变，则不更新，版本号其实就是最后一次用户信息更改的时间）
