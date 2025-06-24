CREATE TABLE `im_file_info` (
  `id` BIGINT NOT NULL auto_increment PRIMARY key comment 'id',
  `file_name` VARCHAR(255) NOT NULL comment '文件名',
  `file_path` VARCHAR(255) NOT NULL comment '文件地址',
  `file_size` INTEGER NOT NULL comment '文件大小',
  `file_type` tinyint NOT NULL comment '0:普通文件 1:图片 2:视频',
  `compressed_path` VARCHAR(255) DEFAULT NULL comment '压缩文件路径',
  `cover_path` VARCHAR(255) DEFAULT NULL comment '封面文件路径，仅视频文件有效',
  `upload_time` datetime DEFAULT CURRENT_TIMESTAMP comment '上传时间',
  `is_permanent` tinyint DEFAULT 0 comment '是否永久文件',
  `md5` VARCHAR(64) NOT NULL comment '文件md5',
  UNIQUE KEY `idx_md5` (md5)
) ENGINE = InnoDB CHARSET = utf8mb4 comment '文件';


ALTER TABLE `im_private_message` MODIFY `content` text CHARACTER SET utf8mb4 comment '发送内容';

ALTER TABLE `im_group_message` MODIFY `content` text CHARACTER SET utf8mb4 comment '发送内容';

ALTER TABLE `im_group_member` Add COLUMN `is_manager` tinyint (1) DEFAULT 0 comment '是否管理员 0:否 1:是';
