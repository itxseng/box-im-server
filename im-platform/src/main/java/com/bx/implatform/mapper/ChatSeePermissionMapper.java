package com.bx.implatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bx.implatform.entity.ChatSeePermission;

import java.util.Collection;
import java.util.List;

/**
 * 屏蔽统一权限Mapper接口
 *
 * @author Blue
 * @date 2025-05-29
 */
public interface ChatSeePermissionMapper extends BaseMapper<ChatSeePermission> {
    /**
     * 批量插入 仅适用于mysql
     */
    Integer insertBatchSomeColumn(List<ChatSeePermission> batchList);
}
