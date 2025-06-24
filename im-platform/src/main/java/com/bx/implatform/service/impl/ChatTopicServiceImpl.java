package com.bx.implatform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.druid.util.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bx.imclient.IMClient;
import com.bx.imcommon.contant.RedisKey;
import com.bx.imcommon.enums.MessageType;
import com.bx.imcommon.model.IMChatMessage;
import com.bx.imcommon.model.IMUserInfo;
import com.bx.imcommon.util.RedisUtils;
import com.bx.implatform.entity.ChatTopic;
import com.bx.implatform.entity.ChatTopicBlack;
import com.bx.implatform.entity.ChatTopicLike;
import com.bx.implatform.entity.ChatTopicReply;
import com.bx.implatform.entity.ChatTopicWhite;
import com.bx.implatform.entity.Friend;
import com.bx.implatform.entity.User;
import com.bx.implatform.enums.SeePermissionTypeEnum;
import com.bx.implatform.enums.TopicNoticeTypeEnum;
import com.bx.implatform.enums.TopicReplyTypeEnum;
import com.bx.implatform.enums.YesOrNoEnum;
import com.bx.implatform.exception.GlobalException;
import com.bx.implatform.mapper.ChatTopicMapper;
import com.bx.implatform.service.FriendService;
import com.bx.implatform.service.IChatSeePermissionService;
import com.bx.implatform.service.IChatTopicBlackService;
import com.bx.implatform.service.IChatTopicLikeService;
import com.bx.implatform.service.IChatTopicReplyService;
import com.bx.implatform.service.IChatTopicService;
import com.bx.implatform.service.IChatTopicWhiteService;
import com.bx.implatform.service.UserService;
import com.bx.implatform.session.SessionContext;
import com.bx.implatform.vo.ChatMessageVO;
import com.bx.implatform.vo.TopicVo01;
import com.bx.implatform.vo.TopicVo03;
import com.bx.implatform.vo.TopicVo04;
import com.bx.implatform.vo.TopicVo05;
import com.bx.implatform.vo.TopicVo06;
import com.bx.implatform.vo.TopicVo07;
import com.bx.implatform.vo.TopicVo09;
import com.bx.implatform.vo.TopicVo10;
import com.bx.implatform.vo.TopicVoCount;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 主题Service业务层处理
 *
 * @author Blue
 * @date 2025-05-29
 */
@RequiredArgsConstructor
@Service
public class ChatTopicServiceImpl extends ServiceImpl<ChatTopicMapper, ChatTopic> implements IChatTopicService {
    private static final Logger log = LoggerFactory.getLogger(ChatTopicServiceImpl.class);
    private final ChatTopicMapper baseMapper;
    private final IChatSeePermissionService chatSeePermissionService;
    private final IChatTopicLikeService chatTopicLikeService;

    private final IChatTopicReplyService chatTopicReplyService;

    private final IChatTopicWhiteService chatTopicWhiteService;

    private final IChatTopicBlackService chatTopicBlackService;

    private final FriendService friendService;
    private final UserService userService;
    private final IMClient imClient;

    @Override
    public void sendTopic(TopicVo01 topicVo) {
        Long userId = SessionContext.getSession().getUserId();
        // 防刷
        String key = RedisKey.SEND_TOPIC + userId;
        if (RedisUtils.hasKey(key)) {
            return;
        }
        RedisUtils.set(key, userId, RedisKey.TOW_SECOND_EXPIRE);

        // 保存主题信息
        ChatTopic topic = new ChatTopic()
                .setUserId(userId)
                .setTopicType(topicVo.getTopicType())
                .setContent(topicVo.getContent())
                .setLocation(topicVo.getLocation())
                .setAddress(topicVo.getAddress())
                .setLatitude(topicVo.getLatitude())
                .setLongitude(topicVo.getLongitude())
                .setCreateTime(DateUtil.date())
                .setOpenType(topicVo.getOpenType() == null ? 1 : topicVo.getOpenType());

        baseMapper.insert(topic);

        // openType : "1开放，2私密，3部分可见，4不给谁看"
        Map<Long, Long> checkMap = Optional.ofNullable(topicVo.getUserIdList())
                .orElse(Collections.emptyList())
                .stream()
                .peek(s -> {
                    if (topic.getOpenType().equals(3)) {
                        chatTopicWhiteService.save(new ChatTopicWhite().setTopicId(topic.getId()).setUserId(s));
                    } else if (topic.getOpenType().equals(4)) {
                        chatTopicBlackService.save(new ChatTopicBlack().setTopicId(topic.getId()).setUserId(s));
                    }
                }).collect(Collectors.toMap(Function.identity(), Function.identity()));

        // 获取好友列表
        List<Long> userList = friendService.findByUserId(userId).stream().map(Friend::getFriendId).toList();

        if (userList.isEmpty()) {
            return;
        }


        User chatUser = userService.findUserInfoById(userId);

        //不让谁看
        List<Long> notSee = chatSeePermissionService.getSeePermission(userId, SeePermissionTypeEnum.NO_SEED_USER.getCode());
        if (!CollectionUtils.isEmpty(notSee)) {
            for (Long notUserId : notSee) {
                checkMap.put(notUserId, notUserId);
            }
        }

        // 构造推送列表
        List<Long> paramList = userList.stream().filter(e -> {
                    switch (topic.getOpenType()) {
                        case 1 -> {
                            return true; // 开放
                        }
                        case 3 -> {
                            return checkMap.containsKey(e); // 部分可见
                        }
                        case 4 -> {
                            return !checkMap.containsKey(e); // 不给谁看
                        }
                        default -> {
                            return false; // 私密 或 未知类型
                        }
                    }
                })
                .collect(Collectors.toList());

        if (paramList.isEmpty()) {
            return;
        }
        //推送消息
        sendPushMessage(MessageType.CHAT_TOPIC_SEND_MESSAGE, chatUser, userList, topic.getId());
    }

    @Override
    public void delTopic(Long topicId) {
        ChatTopic topic = this.getById(topicId);
        if (topic == null) {
            return;
        }
        Long userId = SessionContext.getSession().getUserId();
        if (!topic.getUserId().equals(userId)) {
            return;
        }
        // 删除帖子
        baseMapper.deleteById(topicId);
        // 删除点赞
        chatTopicLikeService.delByTopicId(topicId);
        // 删除回复
        chatTopicReplyService.delByTopicId(topicId);
    }

    @Override
    public List<TopicVo03> userTopic(Long friendId, Long page, Long pageSize) {
        page = page > 0 ? page : 1;
        pageSize = pageSize > 0 ? pageSize : 10;
        long stIdx = (page - 1) * pageSize;
        // 查询用户
        User chatUser = userService.findUserInfoById(friendId);
        // 昵称
        String nickName = chatUser.getNickName();

        ChatTopic checkTopic = new ChatTopic().setUserId(friendId);
        Long userId = SessionContext.getSession().getUserId();

        // 判断是否是自己
        if (userId.equals(friendId)) {
            //自己的主题
            checkTopic.setOpenType(2);
        } else {
            // 判断是否是好友
            Friend friend = friendService.findFriend(userId, friendId);
            // 好友
            if (friend != null && !StringUtils.isEmpty(friend.getRemarkNickName())) {
                nickName = friend.getRemarkNickName();
            }
        }
        //分页查询数据
        checkTopic.setPage(stIdx)
                .setPageSize(pageSize);
        List<ChatTopic> topicList = baseMapper.queryList(checkTopic);
        List<TopicVo03> dataList = new ArrayList<>();
        String finalNickName = nickName;
        topicList.forEach(e -> {
            TopicVo04 topic = BeanUtil.copyProperties(e, TopicVo04.class)
                    .setNickName(finalNickName)
                    .setDisplayName(finalNickName)
                    .setPortrait(chatUser.getHeadImageThumb())
                    .setTopicId(e.getId());
            dataList.add(formatTopic(chatUser, topic));
        });
        return dataList;
    }

    /**
     * 格式化帖子
     */
    private TopicVo03 formatTopic(User chatUser, TopicVo04 topic) {
        // 查询点赞信息
        List<TopicVo05> likeList = chatTopicLikeService.queryTopicLike(chatUser.getId(), topic.getTopicId());
        // 查询自己点赞
        String like = selfLike(chatUser.getId(), likeList);
        // 查询评论信息
        List<TopicVo06> replyList = chatTopicReplyService.queryReplyList(chatUser, topic.getTopicId());
        return new TopicVo03()
                .setTopic(topic)
                .setLikeList(likeList)
                .setLike(like)
                .setReplyList(replyList);
    }

    /**
     * 是否点赞
     */
    private String selfLike(Long userId, List<TopicVo05> likeList) {
        List<Long> userList = likeList.stream().map(TopicVo05::getUserId).toList();
        return userList.contains(userId) ? YesOrNoEnum.YES.getCode() : YesOrNoEnum.NO.getCode();
    }

    @Override
    public List<TopicVo03> topicList(Long page, Long pageSize) {
        // 分页
        page = page > 0 ? page : 1;
        pageSize = pageSize > 0 ? pageSize : 10;
        long stIdx = (page - 1) * pageSize;
        Long userId = SessionContext.getSession().getUserId();
        ChatTopic checkTopic = new ChatTopic().setUserId(userId);
        //分页查询数据
        checkTopic.setPage(stIdx)
                .setPageSize(pageSize);
        List<TopicVo04> topicList = baseMapper.topicList(checkTopic);
        List<TopicVo03> dataList = new ArrayList<>();

        User chatUser = userService.findUserInfoById(userId);

        topicList.forEach(e -> {
            dataList.add(formatTopic(chatUser, e));
        });
        return dataList;
    }

    @Override
    public TopicVo03 topicInfo(Long topicId) {
        ChatTopic topic = this.getById(topicId);
        if (topic == null) {
            throw new GlobalException("帖子不存在");
        }
        // 查询用户
        Long userId = SessionContext.getSession().getUserId();
        User chatUser = userService.findUserInfoById(userId);

        String nickName = chatUser.getNickName();
        // 判断是否是自己
        if (!userId.equals(topic.getUserId())) {
            // 判断是否是好友
            Friend friend = friendService.findFriend(userId, topic.getUserId());
            // 好友
            if (friend != null && !StringUtils.isEmpty(friend.getRemarkNickName())) {
                nickName = friend.getRemarkNickName();
            }
        }
        TopicVo04 topicVo = BeanUtil.copyProperties(topic, TopicVo04.class)
                .setNickName(nickName)
                .setDisplayName(nickName)
                .setPortrait(chatUser.getHeadImage())
                .setTopicId(topic.getId())
                .setCanDeleted(userId.equals(topic.getUserId()) ? YesOrNoEnum.YES.getCode() : YesOrNoEnum.NO.getCode());
        return formatTopic(chatUser, topicVo);
    }

    @Override
    public List<TopicVo09> queryNoticeList() {
        Long userId = SessionContext.getSession().getUserId();
        // 清空通知数
        RedisUtils.del(RedisKey.REDIS_TOPIC_NOTICE + userId);
        String key = RedisKey.REDIS_TOPIC_REPLY + userId;
        if (!RedisUtils.hasKey(key)) {
            return new ArrayList<>();
        }
        List<Object> jsonStr = RedisUtils.lAll(key);
        List<TopicVo09> dataList = new ArrayList<>();
        jsonStr.forEach(e -> {
            dataList.add(JSONUtil.toBean(e.toString(), TopicVo09.class));
        });
        RedisUtils.del(key);

        // 获取好友列表
        List<Long> userList = friendService.findByUserId(userId).stream().map(Friend::getFriendId).toList();
        User user = userService.findUserInfoById(userId);
        // 发送502通知消息，前端拉取数量
        sendPushMessage(MessageType.CHAT_TOPIC_REDIS_NOTICE, user, userList, null);
        return dataList;
    }

    @Override
    public TopicVoCount noticeListCount() {
        Long userId = SessionContext.getSession().getUserId();
        long count;
        Object countStr = RedisUtils.get(RedisKey.REDIS_TOPIC_NOTICE + userId);
        if (!Objects.isNull(countStr)) {
            count = (long) countStr;
        } else {
            count = 0;
        }
        TopicVoCount topicVoCount = new TopicVoCount();
        topicVoCount.setCount(count);

        String key = RedisKey.REDIS_TOPIC_REPLY + userId;
        if (RedisUtils.hasKey(key)) {
            List<Object> jsonStr = RedisUtils.lAll(key);
            topicVoCount.setPortrait(JSONUtil.toBean(jsonStr.get(jsonStr.size() - 1).toString(), TopicVo09.class).getPortrait());
        }
        return topicVoCount;
    }

    @Override
    public void clearNotice() {
        RedisUtils.del(RedisKey.REDIS_TOPIC_REPLY + SessionContext.getSession().getUserId());
    }

    @Override
    public void like(Long topicId) {
        ChatTopic topic = this.getById(topicId);
        if (topic == null) {
            return;
        }
        Long userId = SessionContext.getSession().getUserId();
        ChatTopicLike topicLike = chatTopicLikeService.queryUserLike(topicId, userId);
        // 点过赞
        if (topicLike != null) {
            // 未点赞状态
            if (!YesOrNoEnum.YES.getCode().equals(topicLike.getHasLike())) {
                chatTopicLikeService.updateById(new ChatTopicLike().setId(topicLike.getId()).setHasLike(YesOrNoEnum.YES.getCode()));
            }
            return;
        }
        // 点赞操作
        chatTopicLikeService.save(new ChatTopicLike().setTopicId(topicId).setUserId(userId).setHasLike(YesOrNoEnum.YES.getCode()));
        // 如果是自己给自己点赞，则不处理
        if (userId.equals(topic.getUserId())) {
            return;
        }
        // 给贴主发送通知
        User chatUser = userService.findUserInfoById(userId);
        sendPushMessage(MessageType.CHAT_TOPIC_TOPIC_REPLY, chatUser, Collections.singletonList(topic.getUserId()), topicId);
        // 通知
        this.addNotice(topic.getUserId(), topic, chatUser, TopicNoticeTypeEnum.LIKE.getCode(), null);
    }


    @Override
    public void cancelLike(Long topicId) {
        ChatTopic topic = this.getById(topicId);
        if (topic == null) {
            return;
        }
        Long userId = SessionContext.getSession().getUserId();
        ChatTopicLike topicLike = chatTopicLikeService.queryUserLike(topicId, userId);
        if (topicLike == null) {
            return;
        }
        if (YesOrNoEnum.YES.getCode().equals(topicLike.getHasLike())) {
            chatTopicLikeService.updateById(new ChatTopicLike().setId(topicLike.getId()).setHasLike(YesOrNoEnum.NO.getCode()));
        }
    }

    @Override
    public TopicVo06 reply(TopicVo07 topicVo) {
        // 回复帖子
        Long userId = SessionContext.getSession().getUserId();
        if (TopicReplyTypeEnum.TOPIC.getCode().equals(topicVo.getReplyType())) {
            return replyTopic(userId, topicVo);
        }
        // 回复回复
        return replyReply(userId, topicVo);
    }


    @Override
    public void delReply(Long replyId) {
        ChatTopicReply reply = chatTopicReplyService.getById(replyId);
        if (reply == null || YesOrNoEnum.NO.getCode().equals(reply.getReplyStatus())) {
            return;
        }
        String errMsg = "不能删除此评论";
        Long userId = SessionContext.getSession().getUserId();
        if (TopicReplyTypeEnum.TOPIC.getCode().equals(reply.getReplyType())) {
            ChatTopic topic = this.getById(reply.getTopicId());
            if (topic == null) {
                return;
            }
            if (!topic.getUserId().equals(userId)) {
                throw new GlobalException(errMsg);
            }
        } else if (!reply.getUserId().equals(userId)) {
            throw new GlobalException(errMsg);
        }
        // 帖主/自己发布的帖子
        chatTopicReplyService.updateById(new ChatTopicReply().setReplyId(replyId).setReplyStatus(YesOrNoEnum.NO.getCode()));
    }

    @Override
    public void editTopic(TopicVo10 topicVo) {
        Long userId = SessionContext.getSession().getUserId();
        log.info("修改帖可见范围，{}，{}", userId, JSONUtil.toJsonStr(topicVo));
        //查询修改的帖子
        ChatTopic chatTopic = this.getById(topicVo.getTopicId());
        //判断是否是自己发布的
        if (!chatTopic.getUserId().equals(userId)) {
            throw new GlobalException("不能修改非自己发布的帖子");
        }
        //修改帖子查看权限范围
        chatTopic.setOpenType(topicVo.getOpenType());
        int i = baseMapper.updateById(chatTopic);
        if (i > 0) {
            log.info("修改[{}]帖子查看权限范围成功", topicVo.getTopicId());
        } else {
            log.info("修改[{}]帖子查看权限范围失败", topicVo.getTopicId());
        }
    }


    /**
     * 回复帖子
     */
    private TopicVo06 replyTopic(Long userId, TopicVo07 topicVo) {
        Long topicId = topicVo.getReplyId();
        String content = topicVo.getContent();
        // 查询帖子
        ChatTopic topic = this.getById(topicId);
        if (topic == null) {
            return new TopicVo06();
        }
        ChatTopicReply topicReply = new ChatTopicReply()
                .setReplyType(TopicReplyTypeEnum.TOPIC.getCode())
                .setContent(content)
                .setTopicId(topic.getId())
                .setUserId(userId)
                .setTargetId(topic.getUserId())
                .setReplyStatus(YesOrNoEnum.YES.getCode())
                .setCreateTime(DateUtil.date());
        chatTopicReplyService.save(topicReply);
        // 给贴主发送通知
        User fromUser = userService.findUserInfoById(userId);
        TopicVo06 result = BeanUtil.toBean(topicReply, TopicVo06.class)
                .setUserId(fromUser.getId())
                .setNickName(fromUser.getNickName())
                .setDisplayName(fromUser.getNickName())
                .setPortrait(fromUser.getHeadImageThumb())
                .setCanDeleted(YesOrNoEnum.YES.getCode());
        if (!topic.getUserId().equals(userId)) {
            // 帖主推送
            sendPushMessage(MessageType.CHAT_TOPIC_TOPIC_REPLY, fromUser, Collections.singletonList(topic.getUserId()), topicId);
            // 通知
            this.addNotice(topic.getUserId(), topic, fromUser, TopicNoticeTypeEnum.LIKE.getCode(), null);
        }
        return result;
    }

    /**
     * 回复回复
     */
    private TopicVo06 replyReply(Long userId, TopicVo07 topicVo) {
        Long replyId = topicVo.getReplyId();
        String content = topicVo.getContent();
        // 回复评论
        ChatTopicReply reply = chatTopicReplyService.getById(replyId);
        if (reply == null) {
            return new TopicVo06();
        }
        ChatTopic topic = this.getById(reply.getTopicId());
        if (topic == null) {
            return new TopicVo06();
        }
        ChatTopicReply topicReply = new ChatTopicReply()
                .setReplyType(TopicReplyTypeEnum.USER.getCode())
                .setContent(content)
                .setTopicId(reply.getTopicId())
                .setUserId(userId)
                .setTargetId(reply.getUserId())
                .setReplyStatus(YesOrNoEnum.YES.getCode())
                .setCreateTime(DateUtil.date());
        chatTopicReplyService.save(topicReply);
        // 给贴主发送通知
        User fromUser = userService.findUserInfoById(userId);
        // 帖主推送
        if (!topic.getUserId().equals(userId)) {
            // 帖主推送
            sendPushMessage(MessageType.CHAT_TOPIC_TOPIC_REPLY, fromUser, Collections.singletonList(topic.getUserId()), topic.getId());
            // 通知
            this.addNotice(topic.getUserId(), topic, fromUser, TopicNoticeTypeEnum.LIKE.getCode(), null);
        }
        // 用户推送
        if (!reply.getUserId().equals(userId)) {
            sendPushMessage(MessageType.CHAT_TOPIC_TOPIC_REPLY, fromUser, Collections.singletonList(reply.getUserId()), topic.getId());
            // 通知
            this.addNotice(reply.getUserId(), topic, fromUser, TopicNoticeTypeEnum.LIKE.getCode(), null);
        }

        User toUser = userService.findUserInfoById(reply.getUserId());
        String nickName = toUser.getNickName();
        Friend friend = friendService.findFriend(userId, reply.getUserId());
        // 好友
        if (friend != null && !StringUtils.isEmpty(friend.getRemarkNickName())) {
            nickName = friend.getRemarkNickName();
        }
        return BeanUtil.toBean(topicReply, TopicVo06.class)
                .setUserId(fromUser.getId())
                .setNickName(fromUser.getNickName())
                .setPortrait(fromUser.getHeadImageThumb())
                .setCanDeleted(YesOrNoEnum.YES.getCode())
                .setToUserId(toUser.getId())
                .setToNickName(nickName)
                .setToPortrait(toUser.getHeadImageThumb());
    }

    /**
     * 发送推送消息
     */
    private void sendPushMessage(MessageType chatTopicTopicReply, User chatUser, List<Long> recvIds, Long topicId) {
        ChatMessageVO msgInfo = new ChatMessageVO();
        msgInfo.setType(chatTopicTopicReply.code());
        msgInfo.setSendTime(new Date());
        msgInfo.setSendId(chatUser.getId());
        msgInfo.setTopicId(topicId);
        msgInfo.setSendNickName(chatUser.getNickName());
        msgInfo.setSendHeadImage(chatUser.getHeadImageThumb());
        IMChatMessage<ChatMessageVO> sendMessage = new IMChatMessage<>();
        sendMessage.setRecvIds(recvIds);
        sendMessage.setData(msgInfo);
        sendMessage.setSendResult(false);
        sendMessage.setType(chatTopicTopicReply.code());
        imClient.sendChatMessage(sendMessage);
    }


    /**
     * 添加通知数量
     */
    private void addNotice(Long userId, ChatTopic topic, User fromUser, Integer noticeType, String content) {
        ThreadUtil.execAsync(() -> {
            TopicVo09 topicVo = new TopicVo09()
                    .setTopicId(topic.getId())
                    .setTopicType(topic.getTopicType())
                    .setTopicContent(topic.getContent())
                    .setNoticeType(noticeType)
                    .setUserId(fromUser.getId())
                    .setNickName(fromUser.getNickName())
                    .setPortrait(fromUser.getHeadImageThumb())
                    .setReplyContent(content)
                    .setReplyTime(DateUtil.date());
            RedisUtils.lLeftPush(RedisKey.REDIS_TOPIC_REPLY + userId, JSONUtil.toJsonStr(topicVo));
        });
    }
}
