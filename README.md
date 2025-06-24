
###  **盒子IM**
1. 盒子IM是一个仿微信实现的网页版聊天软件，不依赖任何第三方收费组件。
1. 支持私聊、群聊、离线消息、发送语音、图片、文件、已读未读、群@等功能
1. 支持单人、多人音视频通话(基于原生webrtc实现,需要ssl证书)
1. uniapp端兼容app、h5、微信小程序,可与web端同时在线，并保持消息同步
1. 后端采用springboot+netty实现，网页端使用vue，移动端使用uniapp
1. 服务器支持集群化部署，每个im-server仅处理自身连接用户的消息

详细文档：https://www.yuque.com/u1475064/mufu2a


#### 在线体验

账号：张三/123456 李四/123456,也可以在网页端自行注册账号

网页端：https://www.boxim.online

移动安卓端：https://www.boxim.online/download/boxim.apk

移动H5端: https://www.boxim.online/h5/ ,或扫码：

![输入图片说明](%E6%88%AA%E5%9B%BE/h5%E4%BA%8C%E7%BB%B4%E7%A0%81.png)

由于微信小程序每次发布审核过于严苛和繁琐，暂时不再提供体验环境，但uniapp端依然会继续兼容小程序


#### 项目结构
| 模块                     | 功能                               |
|------------------------|----------------------------------|
| im-platform            | 业务平台服务，负责处理来自用户的业务请求(http)       |
| im-server              | 消息推送服务，不依赖业务,负责将消息推送给用户(ws)      |
| im-client              | 消息推送sdk, 其他服务可集成此sdk与im-server通信 |
| im-common              | 公共包,后端服务均依赖此包                    |
| im-web                 | web页面                            |
| im-uniapp              | uniapp页面,可打包成app、h5、微信小程序        |
| im-webview-private-rtc | uniapp单人音视频webview组件源码           |
| im-webview-group-rtc   | uniapp多人音视频webview组件源码                  |


#### 本地启动
1.安装运行环境
- 安装node:v18.19.0
- 安装jdk:17
- 安装maven:3.9.6
- 安装mysql:8.0,账号密码分别为root/root,创建名为im_platform的数据库，运行db/im_platfrom.sql脚本
- 安装redis:6.2
- 安装minio:RELEASE.2024-xx,使用默认账号、密码、端口

2.启动后端服务
```
mvn clean package
java -jar ./im-platform/target/im-platform.jar
java -jar ./im-server/target/im-server.jar
```

3.启动前端web
```
cd im-web
npm install
npm run serve
```
访问 http://localhost:8080

4.启动uniapp-h5
将im-uniapp目录导入HBuilderX,点击菜单"运行"->"开发环境-h5"
访问 http://localhost:5173




# 📦 Flyway 数据库版本管理清单

本文件用于记录 `im_platform` 数据库在使用 Flyway 进行结构迁移时的所有版本更新历史，确保版本变更可追踪、可还原、可审核。

---

## ✅ 当前版本清单

| 版本号 | 文件名                          | 说明                     | 作者   | 日期         |
|--------|----------------------------------|--------------------------|--------|--------------|
| V1     | V1__init_schema.sql              | 初始化用户、好友、群、消息等表结构 | echo   | 2025-06-10   |
| V2     | V2__add_group_announcement.sql   | 群组表添加公告字段       | echo   | 2025-06-11   |
| V3     | V3__create_index_on_message.sql  | 为 message 表创建 sender_id 和 group_id 索引 | echo | 2025-06-11   |

---

## 🗂 脚本说明细则

### V1__init_schema.sql

- 初始化创建以下表：
    - `user`
    - `friend`
    - `group`
    - `group_member`
    - `message`
- 使用 `utf8mb4` 字符集
- 所有主键为 `BIGINT AUTO_INCREMENT`

---

### V2__add_group_announcement.sql

- 修改 `group` 表，新增字段：
  ```sql
  ALTER TABLE `group` ADD COLUMN announcement TEXT;
