CREATE TABLE `im_user` (
  `id` BIGINT NOT NULL auto_increment PRIMARY key comment 'id',
  `user_name` VARCHAR(255) NOT NULL comment '用户名',
  `nick_name` VARCHAR(255) NOT NULL comment '用户昵称',
  `head_image` VARCHAR(255) DEFAULT '' comment '用户头像',
  `head_image_thumb` VARCHAR(255) DEFAULT '' comment '用户头像缩略图',
  `password` VARCHAR(255) NOT NULL comment '密码',
  `sex` tinyint (1) DEFAULT 0 comment '性别 0:男 1:女',
  `phone` VARCHAR(16) DEFAULT NULL comment '手机号码',
  `email` VARCHAR(32) DEFAULT NULL comment '邮箱',
  `is_banned` tinyint (1) DEFAULT 0 comment '是否被封禁 0:否 1:是',
  `reason` VARCHAR(255) DEFAULT '' comment '被封禁原因',
  `type` SMALLINT DEFAULT 1 comment '用户类型 1:普通用户 2:审核账户',
  `signature` VARCHAR(1024) DEFAULT '' comment '个性签名',
  `is_manual_approve` tinyint (1) DEFAULT 0 comment '是否手动验证好友请求',
  `cid` VARCHAR(255) DEFAULT '' comment '客户端id,用于uni-push推送',
  `status` tinyint DEFAULT 0 comment '状态  0:正常  1:已注销',
  `last_login_time` datetime DEFAULT NULL comment '最后登录时间',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP comment '创建时间',
  UNIQUE key `idx_user_name` (user_name),
  UNIQUE key `idx_phone` (phone),
  UNIQUE key `idx_email` (email),
  key `idx_nick_name` (nick_name)
) ENGINE = InnoDB CHARSET = utf8mb4 comment '用户';

CREATE TABLE `im_friend` (
  `id` BIGINT NOT NULL auto_increment PRIMARY key comment 'id',
  `user_id` BIGINT NOT NULL comment '用户id',
  `friend_id` BIGINT NOT NULL comment '好友id',
  `friend_nick_name` VARCHAR(255) NOT NULL comment '好友昵称',
  `friend_head_image` VARCHAR(255) DEFAULT '' comment '好友头像',
  `remark_nick_name` VARCHAR(255) DEFAULT '' comment '备注昵称',
  `deleted` tinyint comment '删除标识  0：正常   1：已删除',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP comment '创建时间',
  key `idx_user_id` (`user_id`),
  key `idx_friend_id` (`friend_id`)
) ENGINE = InnoDB CHARSET = utf8mb4 comment '好友';


CREATE TABLE `im_friend_request` (
  `id` BIGINT NOT NULL auto_increment PRIMARY key comment 'id',
  `send_id` BIGINT NOT NULL comment '发起方用户ID',
  `send_nick_name` VARCHAR(255) NOT NULL comment '发起方昵称，冗余字段',
  `send_head_image` VARCHAR(255) DEFAULT NULL comment '发起方头像，冗余字段',
  `recv_id` BIGINT NOT NULL comment '接收方用户ID',
  `recv_nick_name` VARCHAR(255) NOT NULL comment '接收方昵称，冗余字段',
  `recv_head_image` VARCHAR(255) DEFAULT NULL comment '接收方头像，冗余字段',
  `remark`  VARCHAR(255) DEFAULT '' comment '申请备注',
  `status` tinyint DEFAULT 1 comment '状态  1:未处理 2:同意 3:拒绝 4:过期',
  `apply_time` datetime DEFAULT CURRENT_TIMESTAMP comment '申请时间',
  key `idx_send_id` (`send_id`),
  key `idx_recv_id` (`recv_id`),
  key `idx_apply_time` (`apply_time`)
) ENGINE = InnoDB CHARSET = utf8mb4 comment '好友申请列表';


CREATE TABLE `im_private_message` (
  `id` BIGINT NOT NULL auto_increment PRIMARY key comment 'id',
  `send_id` BIGINT NOT NULL comment '发送用户id',
  `recv_id` BIGINT NOT NULL comment '接收用户id',
  `content` text CHARACTER SET utf8mb4 comment '发送内容',
  `type` tinyint (1) NOT NULL comment '消息类型 0:文字 1:图片 2:文件 3:语音 4:视频 21:提示',
  `quote_message_id`  BIGINT DEFAULT NULL comment '引用消息id',
  `status` tinyint (1) NOT NULL comment '状态 0:未读 1:已发送 2:撤回 3:已读',
  `send_time` datetime DEFAULT CURRENT_TIMESTAMP comment '发送时间',
  key `idx_send_id` (`send_id`),
  key `idx_recv_id` (`recv_id`)
) ENGINE = InnoDB CHARSET = utf8mb4 comment '私聊消息';

CREATE TABLE `im_group` (
  `id` BIGINT NOT NULL auto_increment PRIMARY key comment 'id',
  `name` VARCHAR(255) NOT NULL comment '群名字',
  `owner_id` BIGINT NOT NULL comment '群主id',
  `head_image` VARCHAR(255) DEFAULT '' comment '群头像',
  `head_image_thumb` VARCHAR(255) DEFAULT '' comment '群头像缩略图',
  `notice` VARCHAR(1024) DEFAULT '' comment '群公告',
  `top_message_id` BIGINT DEFAULT NULL  comment '置顶消息id',
  `is_muted` tinyint (1) DEFAULT 0 comment '是否开启全体禁言 0:否 1:是',
  `is_banned` tinyint (1) DEFAULT 0 comment '是否被封禁 0:否 1:是',
  `reason` VARCHAR(255) DEFAULT '' comment '被封禁原因',
  `dissolve` tinyint (1) DEFAULT 0 comment '是否已解散',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP comment '创建时间'
) ENGINE = InnoDB CHARSET = utf8mb4 comment '群';

CREATE TABLE `im_group_member` (
  `id` BIGINT NOT NULL auto_increment PRIMARY key comment 'id',
  `group_id` BIGINT NOT NULL comment '群id',
  `user_id` BIGINT NOT NULL comment '用户id',
  `user_nick_name` VARCHAR(255) DEFAULT '' comment '用户昵称',
  `remark_nick_name` VARCHAR(255) DEFAULT '' comment '显示昵称备注',
  `head_image` VARCHAR(255) DEFAULT '' comment '用户头像',
  `remark_group_name` VARCHAR(255) DEFAULT '' comment '显示群名备注',
  `is_manager` tinyint (1) DEFAULT 0 comment '是否管理员 0:否 1:是',
  `is_muted` tinyint (1) DEFAULT 0 comment '是否被禁言 0:否 1:是',
  `is_top_message` tinyint (1) DEFAULT 0 comment '是否显示置顶消息',
  `quit` tinyint (1) DEFAULT 0 comment '是否已退出',
  `quit_time` datetime DEFAULT NULL comment '退出时间',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP comment '创建时间',
  key `idx_group_id` (`group_id`),
  key `idx_user_id` (`user_id`)
) ENGINE = InnoDB CHARSET = utf8mb4 comment '群成员';

CREATE TABLE `im_group_message` (
  `id` BIGINT NOT NULL auto_increment PRIMARY key comment 'id',
  `group_id` BIGINT NOT NULL comment '群id',
  `send_id` BIGINT NOT NULL comment '发送用户id',
  `send_nick_name` VARCHAR(255) DEFAULT '' comment '发送用户昵称',
  `recv_ids` VARCHAR(1024) DEFAULT '' comment '接收用户id,逗号分隔，为空表示发给所有成员',
  `content` text CHARACTER SET utf8mb4 COMMENT '发送内容',
  `at_user_ids` VARCHAR(1024) comment '被@的用户id列表，逗号分隔',
  `receipt` tinyint DEFAULT 0 comment '是否回执消息',
  `receipt_ok` tinyint DEFAULT 0 comment '回执消息是否完成',
  `type` tinyint (1) NOT NULL comment '消息类型 0:文字 1:图片 2:文件 3:语音 4:视频 21:提示',
  `quote_message_id`  BIGINT DEFAULT NULL comment '引用消息id',
  `status` tinyint (1) DEFAULT 0 comment '状态 0:未发出  2:撤回 ',
  `send_time` datetime DEFAULT CURRENT_TIMESTAMP comment '发送时间',
  key `idx_group_id` (group_id)
) ENGINE = InnoDB CHARSET = utf8mb4 comment '群消息';

CREATE TABLE `im_system_message` (
  `id` BIGINT NOT NULL auto_increment PRIMARY key comment 'id',
  `title` VARCHAR(64) NOT NULL comment '标题',
  `cover_url` VARCHAR(255) comment '封面图片',
  `intro` VARCHAR(1024) NOT NULL comment '简介',
  `content_type` tinyint (1) DEFAULT 0 comment '内容类型 0:富文本  1:外部链接',
  `rich_text` text comment '富文本内容，base64编码',
  `extern_link` VARCHAR(255) comment '外部链接',
  `deleted` tinyint DEFAULT 0 comment '删除标识  0：正常   1：已删除',
  `creator` BIGINT comment '创建者',
  `create_time` datetime comment '创建时间'
) ENGINE = InnoDB CHARSET = utf8mb4 comment '系统消息';

CREATE TABLE `im_sm_push_task` (
  `id` BIGINT NOT NULL auto_increment PRIMARY key comment 'id',
  `message_id` BIGINT NOT NULL comment '系统消息id',
  `seq_no` BIGINT comment '发送序列号',
  `send_time` datetime comment '推送时间',
  `status` tinyint DEFAULT 1 comment '状态 1:待发送 2:发送中 3:已发送 4:已取消',
  `send_to_all` tinyint DEFAULT 1 comment '是否发送给全体用户',
  `recv_ids` VARCHAR(1024) comment '接收用户id,逗号分隔,send_to_all为false时有效',
  `deleted` tinyint comment '删除标识  0：正常   1：已删除',
  `creator` BIGINT comment '创建者',
  `create_time` datetime comment '创建时间',
  UNIQUE KEY `idx_seq_no` (seq_no)
) ENGINE = InnoDB CHARSET = utf8mb4 comment '系统消息推送任务';

CREATE TABLE `im_sensitive_word` (
  `id` BIGINT NOT NULL auto_increment PRIMARY key comment 'id',
  `content` VARCHAR(64) NOT NULL comment '敏感词内容',
  `enabled` tinyint DEFAULT 0 comment '是否启用 0:未启用 1:启用',
  `creator` BIGINT DEFAULT NULL comment '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP comment '创建时间'
) ENGINE = InnoDB CHARSET = utf8mb4 comment '敏感词';

CREATE TABLE `im_user_blacklist` (
  `id` BIGINT NOT NULL auto_increment PRIMARY key comment 'id',
  `from_user_id` BIGINT NOT NULL comment '拉黑用户id',
  `to_user_id` BIGINT NOT NULL comment '被拉黑用户id',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP comment '创建时间',
  key `idx_from_user_id` (from_user_id)
) ENGINE = InnoDB CHARSET = utf8mb4 comment '用户黑名单';

CREATE TABLE `im_file_info` (
  `id` BIGINT NOT NULL auto_increment PRIMARY key comment 'id',
  `file_name` VARCHAR(255) NOT NULL comment '文件名',
  `file_path` VARCHAR(255) NOT NULL comment '文件地址',
  `file_size` INTEGER NOT NULL comment '文件大小',
  `file_type` tinyint NOT NULL comment '0:普通文件 1:图片 2:视频',
  `compressed_path` VARCHAR(255) DEFAULT NULL comment '压缩文件路径',
  `cover_path` VARCHAR(255) DEFAULT NULL comment '封面文件路径，仅视频文件有效',
  `upload_time` datetime DEFAULT CURRENT_TIMESTAMP comment '上传时间',
  `is_permanent` tinyint DEFAULT 0 comment '是否永久文件',
  `md5` VARCHAR(64) NOT NULL comment '文件md5',
  UNIQUE KEY `idx_md5` (md5)
) ENGINE = InnoDB CHARSET = utf8mb4 comment '文件';