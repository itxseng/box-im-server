CREATE TABLE `im_complaint`
(
    `id`          bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     bigint                                                        DEFAULT NULL COMMENT '用户id',
    `title`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '标题',
    `content`     longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '内容',
    `file_url`    longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '上传证据（多个用英文逗号分割）',
    `status`      tinyint(1) DEFAULT '1' COMMENT '状态（1已提交，2处理中，3已处理）',
    `create_time` datetime                                                      DEFAULT NULL COMMENT '创建时间',
    `over_time`   datetime                                                      DEFAULT NULL COMMENT '处理完成时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='投诉';