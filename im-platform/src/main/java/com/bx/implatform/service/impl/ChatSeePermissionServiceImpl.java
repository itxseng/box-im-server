package com.bx.implatform.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bx.imcommon.contant.RedisKey;
import com.bx.implatform.entity.ChatSeePermission;
import com.bx.implatform.mapper.ChatSeePermissionMapper;
import com.bx.implatform.service.IChatSeePermissionService;
import com.bx.implatform.session.SessionContext;
import com.bx.implatform.session.UserSession;
import com.bx.imcommon.util.RedisUtils;
import com.bx.implatform.vo.ChatSeePermissionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 屏蔽统一权限Service业务层处理
 *
 * @author Blue
 * @date 2025-05-29
 */
@RequiredArgsConstructor
@Service
public class ChatSeePermissionServiceImpl extends ServiceImpl<ChatSeePermissionMapper, ChatSeePermission> implements IChatSeePermissionService {

    private final ChatSeePermissionMapper baseMapper;


    @Override
    public void seeSetting(ChatSeePermissionVO chatSeePermissionVO) {
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();
        String key = StrUtil.format(RedisKey.MOMENTS_SEE_PERMISSIONS, chatSeePermissionVO.getPermissionType(), userId);
        List<Long> oldTargetIds = getSeePermission(userId,chatSeePermissionVO.getPermissionType());
        List<Long> targetIds = chatSeePermissionVO.getTargetIds();

        //去除旧名单
        List<Long> onlyInOld = oldTargetIds.stream()
                .filter(item -> !targetIds.contains(item))
                .toList();

        //新增新名单
        List<Long> onlyInNew = targetIds.stream()
                .filter(item -> !oldTargetIds.contains(item))
                .toList();


        // true 是插入名单
        if (!onlyInNew.isEmpty()) {
            List<ChatSeePermission> chatSeePermissions = onlyInNew.stream().map(vo -> ChatSeePermission.builder()
                            .userId(userId)
                            .permissionType(chatSeePermissionVO.getPermissionType())
                            .targetId(vo).build())
                    .toList();
            //批量插入
            baseMapper.insertBatchSomeColumn(chatSeePermissions);
        }

        // 移除名单
        if (!onlyInOld.isEmpty()) {
            LambdaQueryWrapper<ChatSeePermission> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(ChatSeePermission::getTargetId, onlyInOld)
                    .eq(ChatSeePermission::getUserId, userId)
                    .eq(ChatSeePermission::getPermissionType, chatSeePermissionVO.getPermissionType());
            baseMapper.delete(wrapper);
        }

        //数据有更新 清除缓存
        RedisUtils.del(key);
    }

    @Override
    public List<Long> getSeePermission(Long userId,int permissionType) {

        String key = StrUtil.format(RedisKey.MOMENTS_SEE_PERMISSIONS, permissionType, userId);

        if (RedisUtils.hasKey(key)) {
            Object obj = RedisUtils.get(key);
            if (null!=obj) {
                return JSONUtil.toList(obj.toString(),Long.class);
            }
        }

        List<ChatSeePermission> chatSeePermissions = baseMapper.selectList(new LambdaQueryWrapper<ChatSeePermission>()
                .eq(ChatSeePermission::getUserId, userId)
                .eq(ChatSeePermission::getPermissionType, permissionType));

        if (chatSeePermissions.isEmpty()) {
            RedisUtils.set(key, Collections.emptyList(), RedisKey.MOMENTS_EXPIRE);
            return Collections.emptyList();
        }

        List<Long> collect = chatSeePermissions.stream().map(ChatSeePermission::getTargetId).toList();
        RedisUtils.set(key, JSONUtil.parse(collect).toJSONString(0), RedisKey.MOMENTS_EXPIRE);
        return collect;
    }
}
