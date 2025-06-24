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
 * 朋友圈权限范围对象 chat_view_scope
 *
 * @author Blue
 * @date 2025-05-29
 */
@Data
@TableName("chat_view_scope")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Accessors(chain = true)
public class ChatViewScope {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户表ID
     */
    @TableId(value = "user_id")
    private Long userId;

    /**
     * 0-全部，1-私密，2-最近三天，3-最近7天
     */
    private Integer viewType;

    /**
     * 1 朋友 2 陌生人
     */
    private Integer isFriend;


}
