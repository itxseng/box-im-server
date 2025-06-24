package com.bx.implatform.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * 帖子点赞对象 chat_topic_like
 *
 * @author Blue
 * @date 2025-05-29
 */
@Data
@TableName("chat_topic_like")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Accessors(chain = true)
public class ChatTopicLike {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 帖子id
     */
    private Long topicId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 是否点赞
     */
    private String hasLike;


}
