-- V4__add_group_type_column.sql
-- 新增字段：群类型（0 普通群，1 超级群）

ALTER TABLE `im_group`
    ADD COLUMN `group_type` tinyint(1) NOT NULL DEFAULT 0 COMMENT '群类型：0普通群 1超级群';
