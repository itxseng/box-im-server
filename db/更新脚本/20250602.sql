CREATE TABLE `im_user_device`
(
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`         BIGINT       NOT NULL COMMENT '用户ID',
    `device_id`       VARCHAR(100) NOT NULL COMMENT '设备唯一ID（如UUID或设备码）',
    `device_name`     VARCHAR(100) DEFAULT NULL COMMENT '设备名称（如 iPhone 14 Pro）',
    `platform`        VARCHAR(50)  DEFAULT NULL COMMENT '设备平台（如 0-web 1-app 2-pc 3-ipad）',
    `last_login_time` DATETIME     DEFAULT NULL COMMENT '最后一次登录时间',
    `last_ip`         VARCHAR(50)  DEFAULT NULL COMMENT '最后登录IP地址',
    `status`          TINYINT      DEFAULT 1 COMMENT '状态（1：正常，0：禁用）',
    `create_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY               `idx_user_id` (`user_id`),
    KEY               `idx_device_id` (`device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户设备表';



ALTER TABLE `im_user`
    ADD COLUMN `last_online_time` DATETIME DEFAULT NULL COMMENT '最后在线时间';



ALTER TABLE `im_user`
    ADD COLUMN `group_perm_status` TINYINT DEFAULT 0 COMMENT '群权限控制状态（0：默认，1：白名单，2：黑名单）',
ADD COLUMN `group_perm_yes_user` TEXT COMMENT '群聊白名单用户列表',
ADD COLUMN `group_perm_no_user` TEXT COMMENT '群聊黑名单用户列表',
ADD COLUMN `online_perm_status` TINYINT DEFAULT 0 COMMENT '在线状态展示权限（0公开，1隐藏）';

ALTER TABLE `im_user`
    ADD COLUMN `region_code` VARCHAR(20) DEFAULT NULL COMMENT '用户所在地区代码（如 CN、US）';



ALTER TABLE `im_friend`
    ADD COLUMN `group_perm_status` TINYINT DEFAULT 0 COMMENT '好友群权限控制状态（0：默认，1：白名单，2：黑名单）',
ADD COLUMN `group_perm_yes_user` TEXT COMMENT '允许该好友进入的群聊白名单',
ADD COLUMN `group_perm_no_user` TEXT COMMENT '禁止该好友进入的群聊黑名单';


ALTER TABLE `im_private_message`
    ADD COLUMN `is_edited` TINYINT DEFAULT 0 COMMENT '是否被编辑，0 否 1 是',
ADD COLUMN `content_edit` TEXT COMMENT '编辑后的内容';
