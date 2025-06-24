package com.bx.implatform.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bx.imcommon.contant.RedisKey;
import com.bx.implatform.entity.ChatViewScope;
import com.bx.implatform.mapper.ChatViewScopeMapper;
import com.bx.implatform.service.IChatViewScopeService;
import com.bx.implatform.session.SessionContext;
import com.bx.implatform.session.UserSession;
import com.bx.imcommon.util.RedisUtils;
import com.bx.implatform.vo.ChatViewScopeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 朋友圈权限范围Service业务层处理
 *
 * @author Blue
 * @date 2025-05-29
 */
@RequiredArgsConstructor
@Service
public class ChatViewScopeServiceImpl extends ServiceImpl<ChatViewScopeMapper, ChatViewScope> implements IChatViewScopeService {

    private final ChatViewScopeMapper baseMapper;

    @Override
    public void setting(ChatViewScopeVO chatViewScopeVO) {
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();
        String key = StrUtil.format(RedisKey.MOMENTS_PERMISSIONS, chatViewScopeVO.getIsFriend(), userId);
        LambdaQueryWrapper<ChatViewScope> wrapper = Wrappers.lambdaQuery(ChatViewScope.class)
                .eq(ChatViewScope::getUserId, userId)
                .eq(ChatViewScope::getIsFriend, chatViewScopeVO.getIsFriend());
        ChatViewScope chatViewScope = baseMapper.selectOne(wrapper);
        ChatViewScope viewScope = new ChatViewScope();
        if (Objects.isNull(chatViewScope)) {
            viewScope.setUserId(userId);
            viewScope.setViewType(chatViewScopeVO.getViewType());
            viewScope.setIsFriend(chatViewScopeVO.getIsFriend());
            baseMapper.insert(viewScope);
        } else {
            chatViewScope.setViewType(chatViewScopeVO.getViewType());
            LambdaUpdateWrapper<ChatViewScope> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(ChatViewScope::getUserId, chatViewScope.getUserId());
            updateWrapper.eq(ChatViewScope::getIsFriend, chatViewScope.getIsFriend());
            updateWrapper.set(ChatViewScope::getViewType, chatViewScope.getViewType());
            baseMapper.update(chatViewScope, updateWrapper);
        }

        RedisUtils.del(key);
    }

    @Override
    public int getScope(int isFriend) {
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();
        if (userId == null) {
            return 0; // 或者根据业务需求返回其他默认值
        }

        String key = StrUtil.format(RedisKey.MOMENTS_PERMISSIONS, isFriend, userId);

        if (RedisUtils.hasKey(key)) {
            return Integer.parseInt(RedisUtils.get(key).toString());
        }

        LambdaQueryWrapper<ChatViewScope> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatViewScope::getUserId, userId);
        queryWrapper.eq(ChatViewScope::getIsFriend, isFriend);
        ChatViewScope chatViewScope = baseMapper.selectOne(queryWrapper);
        if (chatViewScope == null) {
            RedisUtils.set(key, "0", RedisKey.MOMENTS_EXPIRE);
            return 0;
        }
        RedisUtils.set(key, chatViewScope.getViewType().toString(), RedisKey.MOMENTS_EXPIRE);
        return chatViewScope.getViewType();
    }
}
