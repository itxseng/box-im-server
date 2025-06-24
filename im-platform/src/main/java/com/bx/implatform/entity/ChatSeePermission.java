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
 * 屏蔽统一权限对象 chat_see_permission
 *
 * @author Blue
 * @date 2025-05-29
 */
@Data
@TableName("chat_see_permission")
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatSeePermission {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @TableId(value = "user_id")
    private Long userId;

    /**
     * 权限类型：1 不让谁看 2 不看谁
     */
    private Integer permissionType;

    /**
     * 目标用户ID
     */
    private Long targetId;


}
