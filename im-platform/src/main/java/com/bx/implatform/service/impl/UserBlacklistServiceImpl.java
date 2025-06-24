package com.bx.implatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bx.imcommon.contant.RedisKey;
import com.bx.implatform.entity.UserBlacklist;
import com.bx.implatform.exception.GlobalException;
import com.bx.implatform.mapper.UserBlacklistMapper;
import com.bx.implatform.service.UserBlacklistService;
import com.bx.implatform.session.SessionContext;
import com.bx.implatform.session.UserSession;
import com.bx.implatform.vo.UserBlacklistVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author: Blue
 * @date: 2024-09-22
 * @version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = RedisKey.IM_CACHE_BLACKLIST)
public class UserBlacklistServiceImpl extends ServiceImpl<UserBlacklistMapper, UserBlacklist>
        implements UserBlacklistService {

    public final UserBlacklistMapper userBlacklistMapper;

    @Override
    public List<UserBlacklistVO> pageList(Long page, Long size) {
        page = page > 0 ? page : 1;
        size = size > 0 ? size : 10;
        long stIdx = (page - 1) * size;
        return userBlacklistMapper.selectPageList(SessionContext.getSession().getUserId(), stIdx, size);
    }

    @CacheEvict(key = "#fromUserId+':'+#toUserId")
    @Override
    public void add(Long fromUserId, Long toUserId) {
        if (isInBlacklist(fromUserId, toUserId)) {
            throw new GlobalException("对方已在您的黑名单列表中");
        }
        UserBlacklist bl = new UserBlacklist();
        bl.setFromUserId(fromUserId);
        bl.setToUserId(toUserId);
        bl.setCreateTime(new Date());
        this.save(bl);
    }

    @CacheEvict(key = "#fromUserId+':'+#toUserId")
    @Override
    public void remove(Long fromUserId, Long toUserId) {
        UserSession session = SessionContext.getSession();
        LambdaQueryWrapper<UserBlacklist> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(UserBlacklist::getFromUserId, session.getUserId());
        wrapper.eq(UserBlacklist::getToUserId, toUserId);
        this.remove(wrapper);
    }

    @Cacheable(key = "#fromUserId+':'+#toUserId")
    @Override
    public Boolean isInBlacklist(Long fromUserId, Long toUserId) {
        LambdaQueryWrapper<UserBlacklist> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(UserBlacklist::getFromUserId, fromUserId);
        wrapper.eq(UserBlacklist::getToUserId, toUserId);
        return this.count(wrapper) > 0;

    }
}
