package com.bx.implatform.service;

import com.bx.implatform.vo.ChatSeePermissionVO;

import java.util.List;

/**
 * 屏蔽统一权限Service接口
 *
 * @author Blue
 * @date 2025-05-29
 */
public interface IChatSeePermissionService {


    /**
     * 设置屏蔽权限
     *
     * @param chatSeePermissionVO 屏蔽权限
     */
    void seeSetting(ChatSeePermissionVO chatSeePermissionVO);

    /**
     * 获取屏蔽权限
     *
     * @param userId         用户ID
     * @param permissionType 权限类型 1 不让谁看 2 不看谁
     * @return 屏蔽权限用户ID列表
     */
    List<Long> getSeePermission(Long userId, int permissionType);
}
