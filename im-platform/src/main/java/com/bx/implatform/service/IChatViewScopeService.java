package com.bx.implatform.service;

import com.bx.implatform.vo.ChatViewScopeVO;

/**
 * 朋友圈权限范围Service接口
 *
 * @author Blue
 * @date 2025-05-29
 */
public interface IChatViewScopeService {


    /**
     * 设置权限范围
     *
     * @param chatViewScopeVO 权限范围
     */
    void setting(ChatViewScopeVO chatViewScopeVO);

    /**
     * 获取权限范围
     *
     * @param isFriend 1 朋友 2 陌生人
     * @return 权限范围
     */
    int getScope(int isFriend);
}
