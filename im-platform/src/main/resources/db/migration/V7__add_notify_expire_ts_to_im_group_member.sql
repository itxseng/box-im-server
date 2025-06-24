-- 添加群成员免打扰时间字段，默认值为 0（表示接收通知）
ALTER TABLE `im_group_member`
    ADD COLUMN `notify_expire_ts` BIGINT NOT NULL DEFAULT 0 COMMENT '免打扰截止时间戳（毫秒），0 表示接收通知';
