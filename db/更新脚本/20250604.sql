ALTER TABLE im_user
    ADD COLUMN user_name_searchable TINYINT DEFAULT 1;
ALTER TABLE im_user
    ADD COLUMN nick_name_searchable TINYINT DEFAULT 1;
ALTER TABLE im_user
    ADD COLUMN phone_searchable TINYINT DEFAULT 1;
ALTER TABLE im_user
    ADD COLUMN email_searchable TINYINT DEFAULT 1;



CREATE TABLE `im_session`
(
    `id`           BIGINT  NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`      BIGINT  NOT NULL COMMENT '会话所属用户 ID',
    `session_type` TINYINT NOT NULL COMMENT '会话类型：0=私聊，1=群聊',
    `target_id`    BIGINT  NOT NULL COMMENT '对方用户ID或群ID',
    `last_message` VARCHAR(255) DEFAULT NULL COMMENT '最后一条消息内容',
    `last_time`    DATETIME     DEFAULT NULL COMMENT '最后一条消息时间',
    `unread_count` INT          DEFAULT 0 COMMENT '未读消息数量',
    `top_flag`     TINYINT      DEFAULT 0 COMMENT '是否置顶 0=否 1=是',
    `deleted`      TINYINT      DEFAULT 0 COMMENT '是否删除 0=否 1=是',
    `create_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `update_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_target` (`user_id`, `session_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户会话表';
