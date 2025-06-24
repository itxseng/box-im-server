-- ----------------------------
-- Chat2DB export data , export time: 2025-06-10 10:56:59
-- ----------------------------
SET
FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for table chat_see_permission
-- ----------------------------
DROP TABLE IF EXISTS `chat_see_permission`;
CREATE TABLE `chat_see_permission`
(
    `user_id`         bigint NOT NULL COMMENT '用户ID',
    `permission_type` int    DEFAULT NULL COMMENT '权限类型：1 不让谁看 2 不看谁',
    `target_id`       bigint DEFAULT NULL COMMENT '目标用户ID'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='屏蔽统一权限对象表';

-- ----------------------------
-- Records of chat_see_permission
-- ----------------------------
-- ----------------------------
-- Table structure for table chat_topic
-- ----------------------------
DROP TABLE IF EXISTS `chat_topic`;
CREATE TABLE `chat_topic`
(
    `id`          bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     bigint                                                         DEFAULT NULL COMMENT '用户id',
    `topic_type`  int                                                            DEFAULT NULL COMMENT '类型（1文字/表情  2图片/拍照  3视频）',
    `content`     varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '内容',
    `create_time` datetime                                                       DEFAULT NULL COMMENT '创建时间',
    `location`    varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  DEFAULT NULL COMMENT '经纬度',
    `latitude`    varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  DEFAULT NULL COMMENT '纬度',
    `longitude`   varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  DEFAULT NULL COMMENT '经度',
    `address`     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  DEFAULT NULL COMMENT '地址',
    `open_type`   int                                                            DEFAULT NULL COMMENT '查看类型（1开放，2私密，3部分可见，4不给谁看）',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='主题对象表';

-- ----------------------------
-- Records of chat_topic
-- ----------------------------
-- ----------------------------
-- Table structure for table chat_topic_black
-- ----------------------------
DROP TABLE IF EXISTS `chat_topic_black`;
CREATE TABLE `chat_topic_black`
(
    `id`       bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `topic_id` bigint DEFAULT NULL COMMENT '帖子id',
    `user_id`  bigint DEFAULT NULL COMMENT '用户id',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子不允许查看对象表';

-- ----------------------------
-- Records of chat_topic_black
-- ----------------------------
-- ----------------------------
-- Table structure for table chat_topic_like
-- ----------------------------
DROP TABLE IF EXISTS `chat_topic_like`;
CREATE TABLE `chat_topic_like`
(
    `id`       bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `topic_id` bigint                                                       DEFAULT NULL COMMENT '帖子id',
    `user_id`  bigint                                                       DEFAULT NULL COMMENT '用户id',
    `has_like` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '是否点赞(Y是  N否)',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子点赞对象表';

-- ----------------------------
-- Records of chat_topic_like
-- ----------------------------
-- ----------------------------
-- Table structure for table chat_topic_reply
-- ----------------------------
DROP TABLE IF EXISTS `chat_topic_reply`;
CREATE TABLE `chat_topic_reply`
(
    `reply_id`     bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `reply_type`   int                                                            DEFAULT NULL COMMENT '回复类型：1帖子  2用户',
    `reply_status` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci   DEFAULT NULL COMMENT '回复状态（Y是  N否）',
    `content`      varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '回复内容',
    `topic_id`     bigint                                                         DEFAULT NULL COMMENT '帖子id',
    `user_id`      bigint                                                         DEFAULT NULL COMMENT '用户id',
    `target_id`    bigint                                                         DEFAULT NULL COMMENT '目标id',
    `create_time`  datetime                                                       DEFAULT NULL COMMENT '回复时间',
    PRIMARY KEY (`reply_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子回复对象表';

-- ----------------------------
-- Records of chat_topic_reply
-- ----------------------------
-- ----------------------------
-- Table structure for table chat_topic_white
-- ----------------------------
DROP TABLE IF EXISTS `chat_topic_white`;
CREATE TABLE `chat_topic_white`
(
    `id`       bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `topic_id` bigint DEFAULT NULL COMMENT '帖子id',
    `user_id`  bigint DEFAULT NULL COMMENT '用户id',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子允许查看对象表';

-- ----------------------------
-- Records of chat_topic_white
-- ----------------------------
-- ----------------------------
-- Table structure for table chat_view_scope
-- ----------------------------
DROP TABLE IF EXISTS `chat_view_scope`;
CREATE TABLE `chat_view_scope`
(
    `user_id`   bigint NOT NULL COMMENT '用户表ID',
    `view_type` int DEFAULT NULL COMMENT '0-全部，1-私密，2-最近三天，3-最近7天',
    `is_friend` int DEFAULT NULL COMMENT '1 朋友 2 陌生人'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='朋友圈权限范围对象表';

-- ----------------------------
-- Records of chat_view_scope
-- ----------------------------
-- ----------------------------
-- Table structure for table im_file_info
-- ----------------------------
DROP TABLE IF EXISTS `im_file_info`;
CREATE TABLE `im_file_info`
(
    `id`              bigint       NOT NULL AUTO_INCREMENT COMMENT 'id',
    `file_name`       varchar(255) NOT NULL COMMENT '文件名',
    `file_path`       varchar(255) NOT NULL COMMENT '文件地址',
    `file_size`       int          NOT NULL COMMENT '文件大小',
    `file_type`       tinyint      NOT NULL COMMENT '0:普通文件 1:图片 2:视频',
    `compressed_path` varchar(255) DEFAULT NULL COMMENT '压缩文件路径',
    `cover_path`      varchar(255) DEFAULT NULL COMMENT '封面文件路径，仅视频文件有效',
    `upload_time`     datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    `is_permanent`    tinyint      DEFAULT '0' COMMENT '是否永久文件',
    `md5`             varchar(64)  NOT NULL COMMENT '文件md5',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_md5` (`md5`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文件';

-- ----------------------------
-- Records of im_file_info
-- ----------------------------
-- ----------------------------
-- Table structure for table im_friend
-- ----------------------------
DROP TABLE IF EXISTS `im_friend`;
CREATE TABLE `im_friend`
(
    `id`                  bigint       NOT NULL AUTO_INCREMENT COMMENT 'id',
    `user_id`             bigint       NOT NULL COMMENT '用户id',
    `friend_id`           bigint       NOT NULL COMMENT '好友id',
    `friend_nick_name`    varchar(255) NOT NULL COMMENT '好友昵称',
    `friend_head_image`   varchar(255) DEFAULT '' COMMENT '好友头像',
    `remark_nick_name`    varchar(255) DEFAULT '' COMMENT '备注昵称',
    `deleted`             tinyint      DEFAULT NULL COMMENT '删除标识  0：正常   1：已删除',
    `created_time`        datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `group_perm_status`   tinyint      DEFAULT '0' COMMENT '好友群权限控制状态（0：默认，1：白名单，2：黑名单）',
    `group_perm_yes_user` text COMMENT '允许该好友进入的群聊白名单',
    `group_perm_no_user`  text COMMENT '禁止该好友进入的群聊黑名单',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_friend` (`user_id`,`friend_id`),
    KEY                   `idx_user_id` (`user_id`),
    KEY                   `idx_friend_id` (`friend_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='好友';

-- ----------------------------
-- Records of im_friend
-- ----------------------------
-- ----------------------------
-- Table structure for table im_friend_request
-- ----------------------------
DROP TABLE IF EXISTS `im_friend_request`;
CREATE TABLE `im_friend_request`
(
    `id`              bigint       NOT NULL AUTO_INCREMENT COMMENT 'id',
    `send_id`         bigint       NOT NULL COMMENT '发起方用户ID',
    `send_nick_name`  varchar(255) NOT NULL COMMENT '发起方昵称，冗余字段',
    `send_head_image` varchar(255) DEFAULT NULL COMMENT '发起方头像，冗余字段',
    `recv_id`         bigint       NOT NULL COMMENT '接收方用户ID',
    `recv_nick_name`  varchar(255) NOT NULL COMMENT '接收方昵称，冗余字段',
    `recv_head_image` varchar(255) DEFAULT NULL COMMENT '接收方头像，冗余字段',
    `remark`          varchar(255) DEFAULT '' COMMENT '申请备注',
    `status`          tinyint      DEFAULT '1' COMMENT '状态  1:未处理 2:同意 3:拒绝 4:过期',
    `apply_time`      datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    PRIMARY KEY (`id`),
    KEY               `idx_send_id` (`send_id`),
    KEY               `idx_recv_id` (`recv_id`),
    KEY               `idx_apply_time` (`apply_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='好友申请列表';

-- ----------------------------
-- Records of im_friend_request
-- ----------------------------
-- ----------------------------
-- Table structure for table im_group
-- ----------------------------
DROP TABLE IF EXISTS `im_group`;
CREATE TABLE `im_group`
(
    `id`                bigint       NOT NULL AUTO_INCREMENT COMMENT 'id',
    `name`              varchar(255) NOT NULL COMMENT '群名字',
    `owner_id`          bigint       NOT NULL COMMENT '群主id',
    `head_image`        varchar(255)  DEFAULT '' COMMENT '群头像',
    `head_image_thumb`  varchar(255)  DEFAULT '' COMMENT '群头像缩略图',
    `notice`            varchar(1024) DEFAULT '' COMMENT '群公告',
    `top_message_id`    bigint        DEFAULT NULL COMMENT '置顶消息id',
    `is_muted`          tinyint(1) DEFAULT '0' COMMENT '是否开启全体禁言 0:否 1:是',
    `is_banned`         tinyint(1) DEFAULT '0' COMMENT '是否被封禁 0:否 1:是',
    `reason`            varchar(255)  DEFAULT '' COMMENT '被封禁原因',
    `dissolve`          tinyint(1) DEFAULT '0' COMMENT '是否已解散',
    `created_time`      datetime      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `interim_perm`      tinyint(1) DEFAULT NULL COMMENT '允许普通成员发起临时会话（false否  true是）',
    `query_member_perm` tinyint(1) DEFAULT NULL COMMENT '允许查看群成员（false否  true是）',
    `add_group_perm`    int           DEFAULT '1' COMMENT '加群权限（1不限制加入  2群成员可以拉人  3只能管理员拉人  4群成员拉人需要管理员验证）',
    `query_group_perm`  int           DEFAULT NULL COMMENT '查找方式（1允许查找  2不允许查找）',
    `room_group_perm`   tinyint(1) DEFAULT NULL COMMENT '是否关闭群语音/视频通话（false否  true是）',
    `top_message_ids`   longtext COMMENT '置顶消息ids',
    `update_time`       datetime      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='群';

-- ----------------------------
-- Records of im_group
-- ----------------------------
-- ----------------------------
-- Table structure for table im_group_member
-- ----------------------------
DROP TABLE IF EXISTS `im_group_member`;
CREATE TABLE `im_group_member`
(
    `id`                bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
    `group_id`          bigint NOT NULL COMMENT '群id',
    `user_id`           bigint NOT NULL COMMENT '用户id',
    `user_nick_name`    varchar(255) DEFAULT '' COMMENT '用户昵称',
    `remark_nick_name`  varchar(255) DEFAULT '' COMMENT '显示昵称备注',
    `head_image`        varchar(255) DEFAULT '' COMMENT '用户头像',
    `remark_group_name` varchar(255) DEFAULT '' COMMENT '显示群名备注',
    `is_manager`        tinyint(1) DEFAULT '0' COMMENT '是否管理员 0:否 1:是',
    `is_muted`          tinyint(1) DEFAULT '0' COMMENT '是否被禁言 0:否 1:是',
    `is_top_message`    tinyint(1) DEFAULT '0' COMMENT '是否显示置顶消息',
    `quit`              tinyint(1) DEFAULT '0' COMMENT '是否已退出',
    `quit_time`         datetime     DEFAULT NULL COMMENT '退出时间',
    `created_time`      datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY                 `idx_group_id` (`group_id`),
    KEY                 `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='群成员';

-- ----------------------------
-- Records of im_group_member
-- ----------------------------
-- ----------------------------
-- Table structure for table im_group_message
-- ----------------------------
DROP TABLE IF EXISTS `im_group_message`;
CREATE TABLE `im_group_message`
(
    `id`               bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
    `group_id`         bigint NOT NULL COMMENT '群id',
    `send_id`          bigint NOT NULL COMMENT '发送用户id',
    `send_nick_name`   varchar(255)  DEFAULT '' COMMENT '发送用户昵称',
    `recv_ids`         varchar(1024) DEFAULT '' COMMENT '接收用户id,逗号分隔，为空表示发给所有成员',
    `content`          text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '发送内容',
    `at_user_ids`      varchar(1024) DEFAULT NULL COMMENT '被@的用户id列表，逗号分隔',
    `receipt`          tinyint       DEFAULT '0' COMMENT '是否回执消息',
    `receipt_ok`       tinyint       DEFAULT '0' COMMENT '回执消息是否完成',
    `type`             tinyint(1) NOT NULL COMMENT '消息类型 0:文字 1:图片 2:文件 3:语音 4:视频 21:提示',
    `quote_message_id` bigint        DEFAULT NULL COMMENT '引用消息id',
    `status`           tinyint(1) DEFAULT '0' COMMENT '状态 0:未发出  2:撤回 ',
    `send_time`        datetime      DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    PRIMARY KEY (`id`),
    KEY                `idx_group_id` (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='群消息';

-- ----------------------------
-- Records of im_group_message
-- ----------------------------
-- ----------------------------
-- Table structure for table im_private_message
-- ----------------------------
DROP TABLE IF EXISTS `im_private_message`;
CREATE TABLE `im_private_message`
(
    `id`               bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
    `send_id`          bigint NOT NULL COMMENT '发送用户id',
    `recv_id`          bigint NOT NULL COMMENT '接收用户id',
    `content`          text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '发送内容',
    `type`             tinyint(1) NOT NULL COMMENT '消息类型 0:文字 1:图片 2:文件 3:语音 4:视频 21:提示',
    `quote_message_id` bigint   DEFAULT NULL COMMENT '引用消息id',
    `status`           tinyint(1) NOT NULL COMMENT '状态 0:未读 1:已发送 2:撤回 3:已读',
    `send_time`        datetime DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    `is_edited`        tinyint  DEFAULT '0' COMMENT '是否被编辑，0 否 1 是',
    `content_edit`     text COMMENT '编辑后的内容',
    PRIMARY KEY (`id`),
    KEY                `idx_send_id` (`send_id`),
    KEY                `idx_recv_id` (`recv_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='私聊消息';

-- ----------------------------
-- Records of im_private_message
-- ----------------------------
-- ----------------------------
-- Table structure for table im_sensitive_word
-- ----------------------------
DROP TABLE IF EXISTS `im_sensitive_word`;
CREATE TABLE `im_sensitive_word`
(
    `id`          bigint      NOT NULL AUTO_INCREMENT COMMENT 'id',
    `content`     varchar(64) NOT NULL COMMENT '敏感词内容',
    `enabled`     tinyint  DEFAULT '0' COMMENT '是否启用 0:未启用 1:启用',
    `creator`     bigint   DEFAULT NULL COMMENT '创建者',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='敏感词';

-- ----------------------------
-- Records of im_sensitive_word
-- ----------------------------
-- ----------------------------
-- Table structure for table im_session
-- ----------------------------
DROP TABLE IF EXISTS `im_session`;
CREATE TABLE `im_session`
(
    `id`           bigint  NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`      bigint  NOT NULL COMMENT '会话所属用户 ID',
    `session_type` tinyint NOT NULL COMMENT '会话类型：0=私聊，1=群聊',
    `target_id`    bigint  NOT NULL COMMENT '对方用户ID或群ID',
    `last_message` varchar(255) DEFAULT NULL COMMENT '最后一条消息内容',
    `last_time`    datetime     DEFAULT NULL COMMENT '最后一条消息时间',
    `unread_count` int          DEFAULT '0' COMMENT '未读消息数量',
    `top_flag`     tinyint      DEFAULT '0' COMMENT '是否置顶 0=否 1=是',
    `deleted`      tinyint      DEFAULT '0' COMMENT '是否删除 0=否 1=是',
    `create_time`  datetime     DEFAULT CURRENT_TIMESTAMP,
    `update_time`  datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_target` (`user_id`,`session_type`,`target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户会话表';

-- ----------------------------
-- Records of im_session
-- ----------------------------
-- ----------------------------
-- Table structure for table im_sm_push_task
-- ----------------------------
DROP TABLE IF EXISTS `im_sm_push_task`;
CREATE TABLE `im_sm_push_task`
(
    `id`          bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
    `message_id`  bigint NOT NULL COMMENT '系统消息id',
    `seq_no`      bigint        DEFAULT NULL COMMENT '发送序列号',
    `send_time`   datetime      DEFAULT NULL COMMENT '推送时间',
    `status`      tinyint       DEFAULT '1' COMMENT '状态 1:待发送 2:发送中 3:已发送 4:已取消',
    `send_to_all` tinyint       DEFAULT '1' COMMENT '是否发送给全体用户',
    `recv_ids`    varchar(1024) DEFAULT NULL COMMENT '接收用户id,逗号分隔,send_to_all为false时有效',
    `deleted`     tinyint       DEFAULT NULL COMMENT '删除标识  0：正常   1：已删除',
    `creator`     bigint        DEFAULT NULL COMMENT '创建者',
    `create_time` datetime      DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_seq_no` (`seq_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统消息推送任务';

-- ----------------------------
-- Records of im_sm_push_task
-- ----------------------------
-- ----------------------------
-- Table structure for table im_system_message
-- ----------------------------
DROP TABLE IF EXISTS `im_system_message`;
CREATE TABLE `im_system_message`
(
    `id`           bigint        NOT NULL AUTO_INCREMENT COMMENT 'id',
    `title`        varchar(64)   NOT NULL COMMENT '标题',
    `cover_url`    varchar(255) DEFAULT NULL COMMENT '封面图片',
    `intro`        varchar(1024) NOT NULL COMMENT '简介',
    `content_type` tinyint(1) DEFAULT '0' COMMENT '内容类型 0:富文本  1:外部链接',
    `rich_text`    text COMMENT '富文本内容，base64编码',
    `extern_link`  varchar(255) DEFAULT NULL COMMENT '外部链接',
    `deleted`      tinyint      DEFAULT '0' COMMENT '删除标识  0：正常   1：已删除',
    `creator`      bigint       DEFAULT NULL COMMENT '创建者',
    `create_time`  datetime     DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统消息';

-- ----------------------------
-- Records of im_system_message
-- ----------------------------
-- ----------------------------
-- Table structure for table im_user
-- ----------------------------
DROP TABLE IF EXISTS `im_user`;
CREATE TABLE `im_user`
(
    `id`                   bigint       NOT NULL AUTO_INCREMENT COMMENT 'id',
    `user_name`            varchar(255) NOT NULL COMMENT '用户名',
    `nick_name`            varchar(255) NOT NULL COMMENT '用户昵称',
    `head_image`           varchar(255)  DEFAULT '' COMMENT '用户头像',
    `head_image_thumb`     varchar(255)  DEFAULT '' COMMENT '用户头像缩略图',
    `password`             varchar(255) NOT NULL COMMENT '密码',
    `sex`                  tinyint(1) DEFAULT '0' COMMENT '性别 0:男 1:女',
    `phone`                varchar(16)   DEFAULT NULL COMMENT '手机号码',
    `email`                varchar(32)   DEFAULT NULL COMMENT '邮箱',
    `is_banned`            tinyint(1) DEFAULT '0' COMMENT '是否被封禁 0:否 1:是',
    `reason`               varchar(255)  DEFAULT '' COMMENT '被封禁原因',
    `type`                 smallint      DEFAULT '1' COMMENT '用户类型 1:普通用户 2:审核账户',
    `signature`            varchar(1024) DEFAULT '' COMMENT '个性签名',
    `is_manual_approve`    tinyint(1) DEFAULT '0' COMMENT '是否手动验证好友请求',
    `cid`                  varchar(255)  DEFAULT '' COMMENT '客户端id,用于uni-push推送',
    `status`               tinyint       DEFAULT '0' COMMENT '状态  0:正常  1:已注销',
    `last_login_time`      datetime      DEFAULT NULL COMMENT '最后登录时间',
    `created_time`         datetime      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `last_online_time`     datetime      DEFAULT NULL COMMENT '最后在线时间',
    `group_perm_status`    tinyint       DEFAULT '0' COMMENT '群权限控制状态（0：默认，1：白名单，2：黑名单）',
    `group_perm_yes_user`  text COMMENT '群聊白名单用户列表',
    `group_perm_no_user`   text COMMENT '群聊黑名单用户列表',
    `online_perm_status`   tinyint       DEFAULT '0' COMMENT '在线状态展示权限0:所有人 1:联系人 2:隐藏',
    `region_code`          varchar(20)   DEFAULT NULL COMMENT '用户所在地区代码（如 CN、US）',
    `is_modified_username` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否修改过用户名 0:未修改 1:已修改',
    `user_name_searchable` tinyint       DEFAULT '1' COMMENT '用户名搜索',
    `nick_name_searchable` tinyint       DEFAULT '1' COMMENT '昵称搜索',
    `phone_searchable`     tinyint       DEFAULT '1' COMMENT '手机号搜索',
    `email_searchable`     tinyint       DEFAULT '1' COMMENT '邮箱搜索',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_user_name` (`user_name`),
    UNIQUE KEY `idx_phone` (`phone`),
    UNIQUE KEY `idx_email` (`email`),
    KEY                    `idx_nick_name` (`nick_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户';

-- ----------------------------
-- Records of im_user
-- ----------------------------
-- ----------------------------
-- Table structure for table im_user_blacklist
-- ----------------------------
DROP TABLE IF EXISTS `im_user_blacklist`;
CREATE TABLE `im_user_blacklist`
(
    `id`           bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
    `from_user_id` bigint NOT NULL COMMENT '拉黑用户id',
    `to_user_id`   bigint NOT NULL COMMENT '被拉黑用户id',
    `create_time`  datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY            `idx_from_user_id` (`from_user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户黑名单';

-- ----------------------------
-- Records of im_user_blacklist
-- ----------------------------
-- ----------------------------
-- Table structure for table im_user_device
-- ----------------------------
DROP TABLE IF EXISTS `im_user_device`;
CREATE TABLE `im_user_device`
(
    `id`              bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`         bigint       NOT NULL COMMENT '用户ID',
    `device_id`       varchar(100) NOT NULL COMMENT '设备唯一ID（如UUID或设备码）',
    `device_name`     varchar(100) DEFAULT NULL COMMENT '设备名称（如 iPhone 14 Pro）',
    `platform`        varchar(50)  DEFAULT NULL COMMENT '设备平台（如 0-web 1-app 2-pc）',
    `last_login_time` datetime     DEFAULT NULL COMMENT '最后一次登录时间',
    `last_ip`         varchar(50)  DEFAULT NULL COMMENT '最后登录IP地址',
    `status`          tinyint      DEFAULT '1' COMMENT '状态（1：正常，0：禁用）',
    `create_time`     datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY               `idx_user_id` (`user_id`),
    KEY               `idx_device_id` (`device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户设备表';

-- ----------------------------
-- Records of im_user_device
-- ----------------------------
SET
FOREIGN_KEY_CHECKS=1;
