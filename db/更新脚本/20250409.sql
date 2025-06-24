ALTER TABLE `im_user` ADD COLUMN `phone` varchar(16)  DEFAULT NULL COMMENT '手机号';

ALTER TABLE `im_user` ADD COLUMN `email` varchar(32)  DEFAULT NULL COMMENT '邮箱';

ALTER TABLE `im_user` ADD UNIQUE INDEX idx_phone (`phone`);

ALTER TABLE `im_user` ADD UNIQUE INDEX idx_email (`email`);

ALTER TABLE `im_user` ADD COLUMN `is_manual_approve` tinyint (1) DEFAULT 0 comment '是否手动验证好友请求';

ALTER TABLE `im_friend` ADD COLUMN `deleted` tinyint DEFAULT 0  comment '删除标识  0：正常   1：已删除';

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
  key `idx_recv_id` (`recv_id`)
) ENGINE = InnoDB CHARSET = utf8mb4 comment '好友申请列表';


ALTER TABLE `im_group` ADD COLUMN `top_message_id` BIGINT DEFAULT NULL  comment '置顶消息id';

ALTER TABLE `im_group_member` ADD COLUMN `is_top_message` tinyint (1) DEFAULT 0 comment '是否显示置顶消息';
