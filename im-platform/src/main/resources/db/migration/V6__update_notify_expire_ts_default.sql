-- 修改 notify_expire_ts 默认值为 0
ALTER TABLE `im_friend`
    MODIFY COLUMN `notify_expire_ts` BIGINT DEFAULT 0 COMMENT '消息不提醒到期时间戳，单位：毫秒，0 表示提醒开启';
