
ALTER TABLE `im_friend` ADD COLUMN `remark_nick_name` varchar(255)  DEFAULT '' COMMENT '备注昵称';

ALTER TABLE `im_group` ADD COLUMN `is_muted` tinyint(1) NULL DEFAULT 0 COMMENT '是否全体禁言 0:否 1:是' ;

ALTER TABLE `im_group_member` ADD COLUMN `is_muted` tinyint(1) NULL DEFAULT 0 COMMENT '是否被禁言 0:否 1:是';

ALTER TABLE `im_group_message` ADD COLUMN `quote_message_id` bigint NULL DEFAULT NULL COMMENT '引用消息id';

ALTER TABLE `im_private_message` ADD COLUMN `quote_message_id` bigint NULL DEFAULT NULL COMMENT '引用消息id';