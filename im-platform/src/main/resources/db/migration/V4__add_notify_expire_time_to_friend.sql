-- 为 im_friend 添加消息不提醒到期时间字段
ALTER TABLE `im_friend`
    ADD COLUMN `notify_expire_time` datetime DEFAULT NULL COMMENT '消息不提醒到期时间，NULL 表示开启提醒';
