-- 为 im_friend 添加消息不提醒到期时间戳字段
ALTER TABLE `im_friend`
    ADD COLUMN `notify_expire_ts` BIGINT DEFAULT NULL COMMENT '消息不提醒到期时间戳，单位：毫秒，NULL 表示提醒开启';
