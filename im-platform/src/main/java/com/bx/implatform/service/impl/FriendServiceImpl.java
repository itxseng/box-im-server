package com.bx.implatform.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bx.imclient.IMClient;
import com.bx.imcommon.contant.RedisKey;
import com.bx.imcommon.enums.IMTerminalType;
import com.bx.imcommon.enums.MessageType;
import com.bx.imcommon.model.IMPrivateMessage;
import com.bx.imcommon.model.IMUserInfo;
import com.bx.implatform.dto.FriendRemarkDTO;
import com.bx.implatform.dto.FriendTagDTO;
import com.bx.implatform.entity.Friend;
import com.bx.implatform.entity.PrivateMessage;
import com.bx.implatform.entity.User;
import com.bx.implatform.enums.MessageStatus;
import com.bx.implatform.exception.GlobalException;
import com.bx.implatform.mapper.FriendMapper;
import com.bx.implatform.mapper.PrivateMessageMapper;
import com.bx.implatform.mapper.UserMapper;
import com.bx.implatform.mongo.service.MongoMessageService;
import com.bx.implatform.service.FriendService;
import com.bx.implatform.service.UserBlacklistService;
import com.bx.implatform.session.SessionContext;
import com.bx.implatform.session.UserSession;
import com.bx.implatform.util.BeanUtils;
import com.bx.implatform.vo.FriendGroupVO;
import com.bx.implatform.vo.FriendVO;
import com.bx.implatform.vo.PrivateMessageVO;
import com.bx.implatform.vo.UserOnlineVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.aop.framework.AopContext;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = RedisKey.IM_CACHE_FRIEND)
public class FriendServiceImpl extends ServiceImpl<FriendMapper, Friend> implements FriendService {

    private final PrivateMessageMapper privateMessageMapper;
    private final UserMapper userMapper;
    private final UserBlacklistService userBlacklistService;
    private final IMClient imClient;
    private final MongoMessageService mongoMessageService;

    @Override
    public List<Friend> findAllPageFriends(Long page, Long size) {
        page = page > 0 ? page : 1;
        size = size > 0 ? size : 10;
        long stIdx = (page - 1) * size;
        Long userId = SessionContext.getSession().getUserId();
        LambdaQueryWrapper<Friend> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Friend::getUserId, userId);
        wrapper.eq(Friend::getDeleted, false);
        wrapper.last("limit " + stIdx + "," + size);
        return this.list(wrapper);
    }

    @Override
    public List<Friend> findByFriendIds(List<Long> friendIds) {
        Long userId = SessionContext.getSession().getUserId();
        LambdaQueryWrapper<Friend> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Friend::getUserId, userId);
        wrapper.in(Friend::getFriendId, friendIds);
        wrapper.eq(Friend::getDeleted, false);
        return this.list(wrapper);
    }

    @Override
    public List<Friend> findByUserId(Long userId) {
        LambdaQueryWrapper<Friend> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Friend::getUserId, userId);
        wrapper.eq(Friend::getDeleted, false);
        return this.list(wrapper);
    }


    @Override
    public List<FriendVO> findPageFriends(Long page, Long size) {
        List<Friend> friends = this.findAllPageFriends(page, size);
        List<Long> ids = friends.stream().map(Friend::getFriendId).collect(Collectors.toList());

        Map<Long, List<IMTerminalType>> terminalMap = imClient.getOnlineTerminal(ids);
        List<User> users = CollectionUtils.isEmpty(ids) ? Collections.emptyList() : userMapper.selectBatchIds(ids);
        Map<Long, User> userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));

        return friends.stream().map(f -> {
            FriendVO vo = this.conver(f, terminalMap.get(f.getFriendId()));
            User user = userMap.get(f.getFriendId());
            if (user != null) {
                log.info("用户信息:{}", user);
                vo.setLastLoginTime(user.getLastLoginTime() != null ? user.getLastLoginTime().getTime() : null);
                vo.setOnlinePermStatus(user.getOnlinePermStatus());
            }
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<FriendGroupVO> findPageFriendGroup(Long page, Long size, Long friendId) {
        page = page > 0 ? page : 1;
        size = size > 0 ? size : 10;
        long stIdx = (page - 1) * size;
        return baseMapper.selectPageList(SessionContext.getSession().getUserId(), friendId, stIdx, size);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addFriend(Long friendId) {
        long userId = SessionContext.getSession().getUserId();
        if (friendId.equals(userId)) {
            throw new GlobalException("不允许添加自己为好友");
        }
        // 互相绑定好友关系
        FriendServiceImpl proxy = (FriendServiceImpl) AopContext.currentProxy();
        proxy.bindFriend(userId, friendId);
        proxy.bindFriend(friendId, userId);
        // 推送添加好友提示
        sendAddTipMessage(friendId);
        log.info("添加好友，用户id:{},好友id:{}", userId, friendId);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delFriend(Long friendId) {
        Long userId = SessionContext.getSession().getUserId();
        // 互相解除好友关系，走代理清理缓存
        FriendServiceImpl proxy = (FriendServiceImpl) AopContext.currentProxy();
        proxy.unbindFriend(userId, friendId);
        proxy.unbindFriend(friendId, userId);
        // 推送解除好友提示
        sendDelTipMessage(friendId);
        log.info("删除好友，用户id:{},好友id:{}", userId, friendId);
    }

    @Cacheable(key = "#userId1+':'+#userId2")
    @Override
    public Boolean isFriend(Long userId1, Long userId2) {
        LambdaQueryWrapper<Friend> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Friend::getUserId, userId1);
        wrapper.eq(Friend::getFriendId, userId2);
        wrapper.eq(Friend::getDeleted, false);
        return this.exists(wrapper);
    }

    @Override
    public void update(FriendVO vo) {
        long userId = SessionContext.getSession().getUserId();
        LambdaQueryWrapper<Friend> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Friend::getUserId, userId);
        wrapper.eq(Friend::getFriendId, vo.getId());
        Friend friend = this.getOne(wrapper);
        if (Objects.isNull(friend)) {
            throw new GlobalException("对方不是您的好友");
        }
        friend.setFriendHeadImage(vo.getHeadImage());
        friend.setFriendNickName(vo.getNickName());
        this.updateById(friend);
    }

    /**
     * 单向绑定好友关系
     *
     * @param userId   用户id
     * @param friendId 好友的用户id
     */
    @CacheEvict(key = "#userId+':'+#friendId")
    public void bindFriend(Long userId, Long friendId) {
        LambdaQueryWrapper<Friend> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Friend::getUserId, userId)
                .eq(Friend::getFriendId, friendId)
                .last("LIMIT 1");

        Friend friend = this.getOne(wrapper);
        boolean exists = friend != null;
        if (!exists) {
            friend = new Friend();
            friend.setUserId(userId);
            friend.setFriendId(friendId);
        }

        // 获取好友资料（建议用缓存优化）
        User friendInfo = userMapper.selectById(friendId); // Redis 缓存推荐
        friend.setFriendHeadImage(friendInfo.getHeadImage());
        friend.setFriendNickName(friendInfo.getNickName());
        friend.setDeleted(false);

        // 只执行必需的 DB 操作
        if (exists) {
            this.updateById(friend);
        } else {
            this.save(friend);
        }

        // 异步推送好友添加消息（推荐线程池或MQ）
        Friend finalFriend = friend;
        CompletableFuture.runAsync(() -> sendAddFriendMessage(userId, friendId, finalFriend));
    }

    /**
     * 单向解除好友关系
     *
     * @param userId   用户id
     * @param friendId 好友的用户id
     */
    @CacheEvict(key = "#userId+':'+#friendId")
    public void unbindFriend(Long userId, Long friendId) {
        // 逻辑删除
        LambdaUpdateWrapper<Friend> wrapper = Wrappers.lambdaUpdate();
        wrapper.eq(Friend::getUserId, userId);
        wrapper.eq(Friend::getFriendId, friendId);
        wrapper.set(Friend::getDeleted, true);
        this.update(wrapper);
        // 推送好友变化信息
        sendDelFriendMessage(userId, friendId);
    }

    @Override
    public FriendVO findFriend(Long friendId) {
        UserSession session = SessionContext.getSession();
        LambdaQueryWrapper<Friend> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Friend::getUserId, session.getUserId());
        wrapper.eq(Friend::getFriendId, friendId);
        Friend friend = this.getOne(wrapper);
        if (Objects.isNull(friend)) {
            throw new GlobalException("对方不是您的好友");
        }

        List<IMTerminalType> terminals = imClient.getOnlineTerminal(friendId);
        FriendVO friendVO = conver(friend, terminals);
        User user = userMapper.selectById(friendId);
        if (user != null) {
            friendVO.setLastLoginTime(user.getLastLoginTime().getTime());
        }
        friendVO.setBlacklist(userBlacklistService.isInBlacklist(session.getUserId(), friendId));
        return friendVO;
    }

    @Override
    public Friend findFriend(Long userId, Long friendId) {
        UserSession session = SessionContext.getSession();
        LambdaQueryWrapper<Friend> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Friend::getUserId, session.getUserId());
        wrapper.eq(Friend::getFriendId, friendId);
        wrapper.eq(Friend::getDeleted, false);
        return this.getOne(wrapper);
    }

    @Override
    public FriendVO modifyRemark(FriendRemarkDTO dto) {
        UserSession session = SessionContext.getSession();
        LambdaQueryWrapper<Friend> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Friend::getUserId, session.getUserId());
        wrapper.eq(Friend::getFriendId, dto.getFriendId());
        Friend friend = this.getOne(wrapper);
        if (Objects.isNull(friend)) {
            throw new GlobalException("对方不是您的好友");
        }
        friend.setRemarkNickName(dto.getRemarkNickName());
        this.updateById(friend);
        List<IMTerminalType> terminals = imClient.getOnlineTerminal(session.getUserId());
        return conver(friend, terminals);
    }

    @Override
    public FriendVO modifyTag(FriendTagDTO dto) {
        UserSession session = SessionContext.getSession();
        LambdaQueryWrapper<Friend> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Friend::getUserId, session.getUserId());
        wrapper.eq(Friend::getFriendId, dto.getFriendId());
        Friend friend = this.getOne(wrapper);
        if (Objects.isNull(friend)) {
            throw new GlobalException("对方不是您的好友");
        }
        friend.setTag(dto.getTag());
        this.updateById(friend);
        List<IMTerminalType> terminals = imClient.getOnlineTerminal(session.getUserId());
        return conver(friend, terminals);
    }

    @Override
    public Map<Long, String> loadRemark(List<Long> friendIds) {
        UserSession session = SessionContext.getSession();
        LambdaQueryWrapper<Friend> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Friend::getUserId, session.getUserId());
        wrapper.in(Friend::getFriendId, friendIds);
        wrapper.ne(Friend::getRemarkNickName, Strings.EMPTY);
        wrapper.isNotNull(Friend::getRemarkNickName);
        wrapper.select(Friend::getFriendId, Friend::getRemarkNickName);
        List<Friend> friends = this.list(wrapper);
        return friends.stream().collect(Collectors.toMap(Friend::getFriendId, Friend::getRemarkNickName));
    }

    @Override
    public void sendOnlineStatus(Long userId, Integer terminal) {
        List<Long> fids = loadAllFriendIds(userId);
        UserOnlineVO vo = new UserOnlineVO();
        vo.setUserId(userId);
        vo.setTerminal(terminal);
        vo.setOnline(imClient.isOnline(userId, IMTerminalType.fromCode(terminal)));
        // 广播给所有好友
        for (Long fid : fids) {
            PrivateMessageVO msgInfo = new PrivateMessageVO();
            msgInfo.setSendId(userId);
            msgInfo.setRecvId(fid);
            msgInfo.setSendTime(new Date());
            msgInfo.setType(MessageType.FRIEND_ONLINE.code());
            msgInfo.setContent(JSON.toJSONString(vo));
            IMPrivateMessage<PrivateMessageVO> sendMessage = new IMPrivateMessage<>();
            sendMessage.setSender(new IMUserInfo(userId, IMTerminalType.UNKNOW.code()));
            sendMessage.setRecvId(fid);
            sendMessage.setData(msgInfo);
            sendMessage.setSendResult(false);
            sendMessage.setSendToSelf(false);
            imClient.sendPrivateMessage(sendMessage);
        }
    }


    @Override
    public void updateNotifyExpireTime(Long friendId, Long durationMillis) {
        if (friendId == null || durationMillis == null || durationMillis < 0) {
            throw new IllegalArgumentException("参数不合法");
        }
        //  设置过期时间
        long expireTs = System.currentTimeMillis() + durationMillis;
        if (durationMillis == 0L) {
            //关闭消息通知
            expireTs = 0L;
        }

        LambdaUpdateWrapper<Friend> wrapper = Wrappers.lambdaUpdate();
        wrapper.eq(Friend::getFriendId, friendId)
                .set(Friend::getNotifyExpireTs, expireTs);

        this.update(wrapper);
    }

    @Override
    public Long getNotifyExpireTs(Long userId, Long id) {
        LambdaQueryWrapper<Friend> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Friend::getUserId, userId);
        wrapper.eq(Friend::getFriendId, id);
        wrapper.select(Friend::getNotifyExpireTs);
        Friend friend = getOne(wrapper);
        if (friend == null) {
            return 0L;
        }
        return friend.getNotifyExpireTs();
    }

    List<Long> loadAllFriendIds(Long userId) {
        LambdaQueryWrapper<Friend> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Friend::getUserId, userId);
        wrapper.eq(Friend::getDeleted, false);
        wrapper.select(Friend::getFriendId);
        List<Friend> friends = this.list(wrapper);
        return friends.stream().map(f -> f.getFriendId()).collect(Collectors.toList());
    }

    private FriendVO conver(Friend f, List<IMTerminalType> onlineTerminals) {
        FriendVO vo = new FriendVO();
        vo.setId(f.getFriendId());
        vo.setHeadImage(f.getFriendHeadImage());
        vo.setHeadImageThumb(f.getFriendHeadImage());
        vo.setNickName(f.getFriendNickName());
        vo.setRemarkNickName(f.getRemarkNickName());
        vo.setShowNickName(f.getShowNickName());
        vo.setTag(f.getTag());
        vo.setDeleted(f.getDeleted());
        vo.setOnline(false);
        vo.setOnlineWeb(false);
        vo.setOnlineApp(false);
        vo.setGroupPermStatus(f.getGroupPermStatus());
        vo.setNotifyExpireTs(f.getNotifyExpireTs());
        if (StringUtils.isNotBlank(f.getGroupPermYesUser())) {
            vo.setGroupPermYesUser(Arrays.stream(f.getGroupPermYesUser().split(",")).map(Long::valueOf).collect(Collectors.toList()));
        }
        if (StringUtils.isNotBlank(f.getGroupPermNoUser())) {
            vo.setGroupPermNoUser(Arrays.stream(f.getGroupPermNoUser().split(",")).map(Long::valueOf).collect(Collectors.toList()));
        }
        if (CollectionUtil.isNotEmpty(onlineTerminals)) {
            vo.setOnline(true);
            vo.setOnlineWeb(onlineTerminals.indexOf(IMTerminalType.WEB) >= 0);
            vo.setOnlineApp(onlineTerminals.indexOf(IMTerminalType.APP) >= 0);
        }
        return vo;
    }

    void sendAddFriendMessage(Long userId, Long friendId, Friend friend) {
        // 推送好友状态信息
        PrivateMessageVO msgInfo = new PrivateMessageVO();
        msgInfo.setSendId(friendId);
        msgInfo.setRecvId(userId);
        msgInfo.setSendTime(new Date());
        msgInfo.setType(MessageType.FRIEND_NEW.code());
        List<IMTerminalType> terminals = imClient.getOnlineTerminal(friendId);
        FriendVO vo = conver(friend, terminals);
        msgInfo.setContent(JSON.toJSONString(vo));
        IMPrivateMessage<PrivateMessageVO> sendMessage = new IMPrivateMessage<>();
        sendMessage.setSender(new IMUserInfo(friendId, IMTerminalType.UNKNOW.code()));
        sendMessage.setRecvId(userId);
        sendMessage.setData(msgInfo);
        sendMessage.setSendToSelf(false);
        imClient.sendPrivateMessage(sendMessage);
    }

    void sendDelFriendMessage(Long userId, Long friendId) {
        // 推送好友状态信息
        PrivateMessageVO msgInfo = new PrivateMessageVO();
        msgInfo.setSendId(friendId);
        msgInfo.setRecvId(userId);
        msgInfo.setSendTime(new Date());
        msgInfo.setType(MessageType.FRIEND_DEL.code());
        IMPrivateMessage<PrivateMessageVO> sendMessage = new IMPrivateMessage<>();
        sendMessage.setSender(new IMUserInfo(friendId, IMTerminalType.UNKNOW.code()));
        sendMessage.setRecvId(userId);
        sendMessage.setData(msgInfo);
        sendMessage.setSendToSelf(false);
        imClient.sendPrivateMessage(sendMessage);
    }

    void sendAddTipMessage(Long friendId) {
        UserSession session = SessionContext.getSession();
        PrivateMessage msg = new PrivateMessage();
        msg.setSendId(session.getUserId());
        msg.setRecvId(friendId);
        msg.setContent("你们已成为好友，现在可以开始聊天了");
        msg.setSendTime(new Date());
        msg.setStatus(MessageStatus.UNSEND.code());
        msg.setType(MessageType.TIP_TEXT.code());
        mongoMessageService.savePrivateMessage( msg);
        // 推给对方
        PrivateMessageVO messageInfo = BeanUtils.copyProperties(msg, PrivateMessageVO.class);
        IMPrivateMessage<PrivateMessageVO> sendMessage = new IMPrivateMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setRecvId(friendId);
        sendMessage.setSendToSelf(false);
        sendMessage.setData(messageInfo);
        imClient.sendPrivateMessage(sendMessage);
        // 推给自己
        sendMessage.setRecvId(session.getUserId());
        imClient.sendPrivateMessage(sendMessage);
    }

    void sendDelTipMessage(Long friendId) {
        UserSession session = SessionContext.getSession();
        // 推送好友状态信息
        PrivateMessage msg = new PrivateMessage();
        msg.setSendId(session.getUserId());
        msg.setRecvId(friendId);
        msg.setSendTime(new Date());
        msg.setType(MessageType.TIP_TEXT.code());
        msg.setStatus(MessageStatus.UNSEND.code());
        msg.setContent("你们的好友关系已解除");
        mongoMessageService.savePrivateMessage(msg);
        // 推送
        PrivateMessageVO messageInfo = BeanUtils.copyProperties(msg, PrivateMessageVO.class);
        IMPrivateMessage<PrivateMessageVO> sendMessage = new IMPrivateMessage<>();
        sendMessage.setSender(new IMUserInfo(friendId, IMTerminalType.UNKNOW.code()));
        sendMessage.setRecvId(friendId);
        sendMessage.setData(messageInfo);
        imClient.sendPrivateMessage(sendMessage);
    }

}
