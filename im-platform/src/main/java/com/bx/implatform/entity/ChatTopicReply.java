package com.bx.implatform.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.util.Date;

/**
 * 帖子回复对象 chat_topic_reply
 *
 * @author Blue
 * @date 2025-05-29
 */
@Data
@TableName("chat_topic_reply")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Accessors(chain = true)
public class ChatTopicReply {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "reply_id")
    private Long replyId;

    /**
     * 回复类型：1帖子  2用户
     */
    private Integer replyType;

    /**
     * 回复状态（Y是  N否）
     */
    private String replyStatus;

    /**
     * 回复内容
     */
    private String content;

    /**
     * 帖子id
     */
    private Long topicId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 目标id
     */
    private Long targetId;

    /**
     * 回复时间
     */
    private Date createTime;
}
