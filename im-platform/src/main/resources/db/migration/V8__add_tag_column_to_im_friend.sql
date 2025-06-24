ALTER TABLE im_friend
    ADD COLUMN `tag` TINYINT DEFAULT 0 COMMENT '是否标记（0：未标记，1：已标记）';
