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
 * 帖子不允许查看对象 chat_topic_black
 *
 * @author Blue
 * @date 2025-05-29
 */
@Data
@TableName("chat_topic_black")
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatTopicBlack {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     *
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


}
