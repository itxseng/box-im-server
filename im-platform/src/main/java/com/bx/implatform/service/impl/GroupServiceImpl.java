package com.bx.implatform.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bx.imclient.IMClient;
import com.bx.imcommon.contant.RedisKey;
import com.bx.imcommon.enums.MessageType;
import com.bx.imcommon.model.IMGroupMessage;
import com.bx.imcommon.model.IMUserInfo;
import com.bx.imcommon.util.CommaTextUtils;
import com.bx.implatform.contant.Constant;
import com.bx.implatform.dto.*;
import com.bx.implatform.entity.*;
import com.bx.implatform.enums.MessageStatus;
import com.bx.implatform.exception.GlobalException;
import com.bx.implatform.mapper.GroupMapper;
import com.bx.implatform.mapper.GroupMessageMapper;
import com.bx.implatform.mongo.service.MongoMessageService;
import com.bx.implatform.service.FriendService;
import com.bx.implatform.service.GroupMemberService;
import com.bx.implatform.service.GroupService;
import com.bx.implatform.service.UserService;
import com.bx.implatform.session.SessionContext;
import com.bx.implatform.session.UserSession;
import com.bx.implatform.util.BeanUtils;
import com.bx.implatform.vo.GroupMemberVO;
import com.bx.implatform.vo.GroupMessageVO;
import com.bx.implatform.vo.GroupVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.bx.implatform.enums.ResultCode.SUCCESS;

@Slf4j
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = RedisKey.IM_CACHE_GROUP)
public class GroupServiceImpl extends ServiceImpl<GroupMapper, Group> implements GroupService {
    private final UserService userService;
    private final GroupMemberService groupMemberService;
    private final GroupMessageMapper groupMessageMapper;
    private final FriendService friendsService;
    private final IMClient imClient;
    private final RedisTemplate<String, Object> redisTemplate;
    private final FriendService friendService;

    private final MongoMessageService mongoMessageService;

    @Override
    public GroupVO createGroup(GroupCreateDTO dto) {
        UserSession session = SessionContext.getSession();
        User user = userService.getById(session.getUserId());
        // 保存群组数据
        Group group = BeanUtils.copyProperties(dto, Group.class);
        assert group != null;
        group.setOwnerId(user.getId());
        if (StrUtil.isBlank(dto.getName())) {
            group.setName(user.getNickName() + "的群聊");
        }
        if (StrUtil.isNotBlank(dto.getHeadImage()) && StrUtil.isNotBlank(dto.getHeadImageThumb())) {
            group.setHeadImage(dto.getHeadImage());
            group.setHeadImageThumb(dto.getHeadImageThumb());
        } else {
            group.setHeadImage(Constant.DEFAULT_GROUP_HEAD_IMAGE);
            group.setHeadImageThumb(Constant.DEFAULT_GROUP_HEAD_IMAGE_THUMB);
        }

        this.save(group);
        // 把群主加入群
        GroupMember member = new GroupMember();
        member.setGroupId(group.getId());
        member.setUserId(user.getId());
        member.setUserNickName(user.getNickName());
        member.setHeadImage(user.getHeadImageThumb());
        groupMemberService.save(member);
        GroupVO groupVo = findById(group.getId());
        // 推送同步消息给自己的其他终端
        sendAddGroupMessage(groupVo, List.of(session.getUserId()));
        // 返回
        log.info("创建群聊，群聊id:{},群聊名称:{}", group.getId(), group.getName());

        //  创建群时选择成员只能是好友
        if (!CollUtil.isEmpty(dto.getMemberIds())) {
            GroupInviteDTO inviteDTO = new GroupInviteDTO();
            inviteDTO.setGroupId(group.getId());
            inviteDTO.setFriendIds(dto.getMemberIds());
            invite(inviteDTO);
        }

        return groupVo;
    }

    @CacheEvict(key = "#dto.id")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public GroupVO modifyGroup(GroupUpdateDTO dto) {
        UserSession session = SessionContext.getSession();
        Group group = this.getAndCheckById(dto.getId());
        GroupMember member = groupMemberService.findByGroupAndUserId(dto.getId(), session.getUserId());
        if (Objects.isNull(member) || member.getQuit()) {
            throw new GlobalException("您不是群聊的成员");
        }
        member.setRemarkGroupName(dto.getRemarkGroupName());
        member.setRemarkNickName(dto.getRemarkNickName());
        groupMemberService.updateById(member);
        if (group.getOwnerId().equals(session.getUserId()) || member.getIsManager()) {
            if (!Objects.isNull(dto.getIsMuted()) && !group.getIsMuted().equals(dto.getIsMuted())) {
                sendMutedTip(dto.getId(), dto.getIsMuted());
            }
            // 仅拷贝允许更新的字段，防止误改 groupType
            group.setName(dto.getName());
            group.setNotice(dto.getNotice());
            group.setHeadImage(dto.getHeadImage());
            group.setHeadImageThumb(dto.getHeadImageThumb());
            group.setIsMuted(dto.getIsMuted());
            this.updateById(group);
        }

        log.info("修改群聊信息，群聊id:{}, 群聊名称:{}", group.getId(), group.getName());
        return convert(group, member);
    }


    @Override
    public GroupVO modifyPerm(GroupPermDTO dto) {
        UserSession session = SessionContext.getSession();
        Group group = this.getAndCheckById(dto.getId());

        GroupMember member = groupMemberService.findByGroupAndUserId(dto.getId(), session.getUserId());
        if (Objects.isNull(member) || member.getQuit()) {
            throw new GlobalException("您不是群聊的成员");
        }
        if (group.getOwnerId().equals(session.getUserId()) || member.getIsManager()) {
            boolean roomGroup = Boolean.TRUE.equals(dto.getRoomGroupPerm());
            boolean interim = Boolean.TRUE.equals(dto.getInterimPerm());
            if (roomGroup) {
                this.sendTipMessage(group.getId(), null, MessageType.GROUP_ROOM_PERM.desc(), true, MessageType.GROUP_ROOM_PERM.code());
                group.setRoomGroupPerm(true);
            }
            if (interim) {
                this.sendTipMessage(group.getId(), null, MessageType.GROUP_INTERIM_PERM.desc(), true, MessageType.GROUP_INTERIM_PERM.code());
                group.setInterimPerm(true);
            }
            if (dto.getQueryMemberPerm() != null) {
                group.setQueryMemberPerm(dto.getQueryMemberPerm());
            }
            if (dto.getAddGroupPerm() != null) {
                group.setAddGroupPerm(dto.getAddGroupPerm());
            }
            if (dto.getQueryGroupPerm() != null) {
                group.setQueryGroupPerm(dto.getQueryGroupPerm());
            }
            this.updateById(group);
        }
        log.info("修改群聊权限，群聊id:{}, 群聊名称:{}", group.getId(), group.getName());
        return convert(group, member);
    }

    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(key = "#groupId")
    @Override
    public void deleteGroup(Long groupId) {
        UserSession session = SessionContext.getSession();
        Group group = this.getById(groupId);
        if (!group.getOwnerId().equals(session.getUserId())) {
            throw new GlobalException("只有群主才有权限解除群聊");
        }
        // 群聊用户id
        List<Long> userIds = groupMemberService.findUserIdsByGroupId(groupId);
        // 逻辑删除群数据
        group.setDissolve(true);
        this.updateById(group);
        // 删除成员数据
        groupMemberService.removeByGroupId(groupId);
        // 清理已读缓存
        String key = StrUtil.join(":", RedisKey.IM_GROUP_READED_POSITION, groupId);
        redisTemplate.delete(key);
        // 推送解散群聊提示
        String content = String.format("'%s'解散了群聊", session.getNickName());
        this.sendTipMessage(groupId, userIds, content, true);
        // 推送同步消息所有用户
        this.sendDelGroupMessage(groupId, userIds);
        log.info("删除群聊，群聊id:{},群聊名称:{}", group.getId(), group.getName());
    }

    @Override
    public void quitGroup(Long groupId) {
        Long userId = SessionContext.getSession().getUserId();
        Group group = this.getById(groupId);
        if (group.getOwnerId().equals(userId)) {
            throw new GlobalException("您是群主，不可退出群聊");
        }
        // 删除群聊成员
        groupMemberService.removeByGroupAndUserId(groupId, userId);
        // 推送退出群聊提示
        this.sendTipMessage(groupId, List.of(userId), "您已退出群聊", false);
        // 推送同步消息给其他终端
        this.sendDelGroupMessage(groupId, List.of(userId));
        log.info("退出群聊，群聊id:{},群聊名称:{},用户id:{}", group.getId(), group.getName(), userId);
    }

    @Override
    public void kickGroup(Long groupId, Long userId) {
        UserSession session = SessionContext.getSession();
        Group group = this.getAndCheckById(groupId);
        GroupMember member = groupMemberService.findByGroupAndUserId(groupId, session.getUserId());
        if (!group.getOwnerId().equals(session.getUserId()) && !member.getIsManager()) {
            throw new GlobalException("您没有权限");
        }
        if (group.getOwnerId().equals(userId)) {
            throw new GlobalException("不允许移除群主");
        }
        if (userId.equals(session.getUserId())) {
            throw new GlobalException("不允许移除自己");
        }
        // 删除群聊成员
        groupMemberService.removeByGroupAndUserId(groupId, userId);
        // 推送踢出群聊提示
        this.sendTipMessage(groupId, List.of(userId), "您已被移出群聊", false);
        // 推送同步消息
        this.sendDelGroupMessage(groupId, List.of(userId));
        log.info("踢出群聊，群聊id:{},群聊名称:{},用户id:{}", group.getId(), group.getName(), userId);
    }

    @Override
    public void removeGroupMembers(GroupMemberRemoveDTO dto) {
        UserSession session = SessionContext.getSession();
        Group group = this.getAndCheckById(dto.getGroupId());
        GroupMember member = groupMemberService.findByGroupAndUserId(dto.getGroupId(), session.getUserId());
        boolean isOwner = group.getOwnerId().equals(session.getUserId());
        if (!isOwner && !member.getIsManager()) {
            throw new GlobalException("您没有权限");
        }
        if (dto.getUserIds().contains(group.getOwnerId())) {
            throw new GlobalException("不允许移除群主");
        }
        if (dto.getUserIds().contains(session.getUserId())) {
            throw new GlobalException("不允许移除自己");
        }
        List<GroupMember> members = groupMemberService.findByGroupAndUserIds(dto.getGroupId(), dto.getUserIds());
        boolean hasManager = members.stream().anyMatch(GroupMember::getIsManager);
        if (!isOwner && hasManager) {
            throw new GlobalException("您没有移除管理员的权限");
        }
        // 删除群聊成员
        groupMemberService.removeByGroupAndUserIds(dto.getGroupId(), dto.getUserIds());
        // 推送踢出群聊提示
        this.sendTipMessage(dto.getGroupId(), dto.getUserIds(), "您已被移出群聊", false);
        // 推送同步消息
        this.sendDelGroupMessage(dto.getGroupId(), dto.getUserIds());
        log.info("踢出群聊，群聊id:{},群聊名称:{},用户id:{}", group.getId(), group.getName(), dto.getUserIds());
    }

    @Override
    public GroupVO findById(Long groupId) {
        UserSession session = SessionContext.getSession();
        Group group = super.getById(groupId);
        if (Objects.isNull(group)) {
            throw new GlobalException("群聊不存在");
        }
        GroupMember member = groupMemberService.findByGroupAndUserId(groupId, session.getUserId());
        GroupVO vo = convert(group, member);
        //查询群成员 和 在线人数
        List<GroupMember> members = groupMemberService.findByGroupId(groupId);
        List<Long> userIds = members.stream().map(GroupMember::getUserId).collect(Collectors.toList());
        List<Long> onlineUserIds = imClient.getOnlineUser(userIds);
        vo.setMembersCount(members.size());
        vo.setOnlineCount(onlineUserIds.size());

        // 填充群置顶消息
        if (!Objects.isNull(group.getTopMessageId()) && !Objects.isNull(member) && member.getIsTopMessage()) {
            GroupMessage message = mongoMessageService.findGroupMessageById(group.getTopMessageId());
            if (!Objects.isNull(message)) {
                vo.setTopMessage(BeanUtils.copyProperties(message, GroupMessageVO.class));
            }
        }
        // 填充群置顶消息 多条置顶消息
        if (!Objects.isNull(group.getTopMessageIds()) && member.getIsTopMessage()) {
            List<Long> ids = JSON.parseArray(group.getTopMessageIds(), Long.class);
            List<GroupMessage> messages = mongoMessageService.findGroupMessagesByIds(ids);
            List<GroupMessageVO> messageVOs = messages.stream().map(message -> BeanUtils.copyProperties(message, GroupMessageVO.class)).toList();
            vo.setTopMessages(messageVOs);
        } else {
            vo.setTopMessages(new ArrayList<>(0));
        }

        return vo;
    }

    @Cacheable(key = "#groupId")
    @Override
    public Group getAndCheckById(Long groupId) {
        Group group = super.getById(groupId);
        if (Objects.isNull(group)) {
            throw new GlobalException("群组不存在");
        }
        if (group.getDissolve()) {
            throw new GlobalException("群组'" + group.getName() + "'已解散");
        }
        if (group.getIsBanned()) {
            throw new GlobalException("群组'" + group.getName() + "'已被封禁,原因:" + group.getReason());
        }
        return group;
    }

    @Override
    public List<GroupVO> findGroups() {
        UserSession session = SessionContext.getSession();
        // 查询当前用户的群id列表
        List<GroupMember> groupMembers = groupMemberService.findByUserId(session.getUserId());
        // 一个月内退的群可能存在退群前的离线消息,一并返回作为前端缓存
        groupMembers.addAll(groupMemberService.findQuitInMonth(session.getUserId()));
        if (groupMembers.isEmpty()) {
            return new LinkedList<>();
        }
        // 拉取群列表
        List<Long> ids = groupMembers.stream().map((GroupMember::getGroupId)).collect(Collectors.toList());
        LambdaQueryWrapper<Group> groupWrapper = Wrappers.lambdaQuery();
        groupWrapper.in(Group::getId, ids);
        List<Group> groups = this.list(groupWrapper);
        // 转vo
        return groups.stream().map(group -> {
            GroupMember member =
                    groupMembers.stream().filter(m -> group.getId().equals(m.getGroupId())).findFirst().get();
            return convert(group, member);
        }).collect(Collectors.toList());
    }

    @Override
    public void invite(GroupInviteDTO dto) {
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();
        Group group = this.getAndCheckById(dto.getGroupId());
        GroupMember member = groupMemberService.findByGroupAndUserId(dto.getGroupId(), userId);
        if (Objects.isNull(group) || member.getQuit()) {
            throw new GlobalException("您不在群聊中,邀请失败");
        }

        // 群聊人数校验
        //查询群聊人数
        List<GroupMember> members = groupMemberService.findByGroupId(dto.getGroupId());
        long size = members.stream().filter(m -> !m.getQuit()).count();
        if (dto.getFriendIds().size() + size > Constant.MAX_LARGE_GROUP_MEMBER) {
            throw new GlobalException("群聊人数不能大于" + Constant.MAX_LARGE_GROUP_MEMBER + "人");
        }
        //加群权限（1不限制加入  2群成员可以拉人  3只能管理员拉人  4群成员拉人需要管理员验证）
        if (group.getAddGroupPerm() == 3 && !member.getIsManager() && !group.getOwnerId().equals(session.getUserId())) {
            throw new GlobalException("您不是管理员，不允许邀请");
        }
        if (group.getAddGroupPerm() == 4 && !member.getIsManager()) {
            //查看管理员列表
            List<GroupMember> manager = groupMemberService.findManagerByGroupId(dto.getGroupId());
            List<Long> userIds = manager.stream().map(GroupMember::getGroupId).collect(Collectors.toList());
            userIds.add(group.getOwnerId());
            //发送群管理员验证消息 内容为  群id-群头像-群名称-拉入用户id列表
            this.sendTipMessage(group.getId(), userIds, String.format("%s-%s-%s-%s",
                    group.getId(),
                    group.getHeadImage(),
                    group.getName(),
                    String.join(",", dto.getFriendIds().stream().map(String::valueOf).toList())), false, MessageType.GROUP_MANAGER_INVITE_CHECK.code());
            throw new GlobalException(SUCCESS.getCode(), "已发送管理员验证消息，请等待管理员验证");
        }

        //添加群聊成员
        addGroupMembers(userId, dto, members, group, session);
    }

    @Override
    public void inviteManager(GroupInviteDTO dto) {
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();
        Group group = this.getAndCheckById(dto.getGroupId());
        GroupMember member = groupMemberService.findByGroupAndUserId(dto.getGroupId(), userId);
        if (Objects.isNull(group) || member.getQuit()) {
            throw new GlobalException("您不在群聊中,邀请失败");
        }
        if (!member.getIsManager() && !group.getOwnerId().equals(userId)) {
            throw new GlobalException("您不是管理员，不允许操作");
        }

        // 群聊人数校验
        List<GroupMember> members = groupMemberService.findByGroupId(dto.getGroupId());
        long size = members.stream().filter(m -> !m.getQuit()).count();
        if (dto.getFriendIds().size() + size > Constant.MAX_LARGE_GROUP_MEMBER) {
            throw new GlobalException("群聊人数不能大于" + Constant.MAX_LARGE_GROUP_MEMBER + "人");
        }

        //添加群聊成员
        addGroupMembers(userId, dto, members, group, session);

    }

    /**
     * 批量添加群聊成员
     *
     * @param userId  邀请人ID
     * @param dto     入参
     * @param members 已存在的成员列表
     * @param group   群聊信息
     * @param session 用户信息
     */
    private void addGroupMembers(Long userId, GroupInviteDTO dto, List<GroupMember> members, Group group, UserSession session) {
        // 找出好友信息
        List<Friend> friends = friendsService.findByFriendIds(dto.getFriendIds());
        if (dto.getFriendIds().size() != friends.size()) {
            throw new GlobalException("部分用户不是您的好友，邀请失败");
        }
        // 批量保存成员数据
        List<GroupMember> groupMembers = friends.stream().filter(f -> {
            if (f.getGroupPermNoUser() != null && Arrays.stream(f.getGroupPermNoUser().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::valueOf)
                    .toList()
                    .contains(userId)) {
                return false;
            } else if (f.getGroupPermYesUser() != null && Arrays.stream(f.getGroupPermYesUser().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::valueOf)
                    .toList()
                    .contains(userId)) {
                return true;
            }
            return !Objects.equals(f.getGroupPermStatus(), GroupPermStatus.NO.getCode());
        }).map(f -> {
            Optional<GroupMember> optional =
                    members.stream().filter(m -> m.getUserId().equals(f.getFriendId())).findFirst();
            GroupMember groupMember = optional.orElseGet(GroupMember::new);
            groupMember.setGroupId(dto.getGroupId());
            groupMember.setUserId(f.getFriendId());
            groupMember.setUserNickName(f.getFriendNickName());
            groupMember.setHeadImage(f.getFriendHeadImage());
            groupMember.setCreatedTime(new Date());
            groupMember.setIsManager(false);
            groupMember.setQuit(false);
            return groupMember;
        }).collect(Collectors.toList());

        if (!groupMembers.isEmpty()) {
            groupMemberService.saveOrUpdateBatch(group.getId(), groupMembers);
        }
        // 推送同步消息给被邀请人
        for (GroupMember groupMember : groupMembers) {
            GroupVO groupVo = convert(group, groupMember);
            sendAddGroupMessage(groupVo, List.of(groupMember.getUserId()));
        }
        // 推送进入群聊消息
        String memberNames = groupMembers.stream().map(GroupMember::getShowNickName).collect(Collectors.joining(","));
        String content = String.format("'%s'邀请'%s'加入了群聊", session.getNickName(), memberNames);
        List<Long> userIds = groupMemberService.findUserIdsByGroupId(dto.getGroupId());
        this.sendTipMessage(dto.getGroupId(), userIds, content, true);
        log.info("邀请进入群聊，群聊id:{},群聊名称:{},被邀请用户id:{}", group.getId(), group.getName(),
                dto.getFriendIds());
    }


    @Override
    public GroupVO join(Long groupId) {
        UserSession session = SessionContext.getSession();
        User user = userService.getById(session.getUserId());
        Group group = this.getAndCheckById(groupId);
        GroupMember member = groupMemberService.findByGroupAndUserId(groupId, session.getUserId());
        if (Objects.isNull(member)) {
            member = new GroupMember();
        }
        member.setGroupId(groupId);
        member.setUserId(user.getId());
        member.setUserNickName(user.getNickName());
        member.setHeadImage(user.getHeadImageThumb());
        member.setCreatedTime(new Date());
        member.setIsManager(false);
        member.setQuit(false);
        groupMemberService.saveOrUpdate(member);
        GroupVO vo = convert(group, member);
        // 同步群聊列表
        sendAddGroupMessage(vo, List.of(session.getUserId()));
        // 推送提示语
        String content = String.format("'%s'加入了群聊", session.getNickName());
        List<Long> userIds = groupMemberService.findUserIdsByGroupId(groupId);
        this.sendTipMessage(groupId, userIds, content, true);
        log.info("加入群聊，群聊id:{},群聊名称:{},用户id:{}", group.getId(), group.getName(), session.getUserId());
        return vo;
    }

    @Override
    public List<GroupMemberVO> findGroupMembers(Long groupId) {
        Group group = getAndCheckById(groupId);
        List<GroupMember> members = groupMemberService.findByGroupId(groupId);
        List<Long> userIds = members.stream().map(GroupMember::getUserId).collect(Collectors.toList());
        Map<Long, String> friendRemarkMap = friendService.loadRemark(userIds);
        List<Long> onlineUserIds = imClient.getOnlineUser(userIds);
        return members.stream().map(m -> {
            GroupMemberVO vo = BeanUtils.copyProperties(m, GroupMemberVO.class);
            // 优先使用好友备注昵称
            vo.setShowNickName(
                    friendRemarkMap.containsKey(m.getUserId()) ? friendRemarkMap.get(m.getUserId()) : m.getShowNickName());
            vo.setShowGroupName(StrUtil.blankToDefault(m.getRemarkGroupName(), group.getName()));
            vo.setOnline(onlineUserIds.contains(m.getUserId()));
            return vo;
        }).sorted((m1, m2) -> {
            // 在线的放前面
            if (!m1.getOnline().equals(m2.getOnline())) {
                return m2.getOnline().compareTo(m1.getOnline());
            }
            // 群主在前面
            if (m1.getUserId().equals(group.getOwnerId())) {
                return -1;
            }
            if (m2.getUserId().equals(group.getOwnerId())) {
                return 1;
            }
            // 管理员在前面
            return m2.getIsManager().compareTo(m1.getIsManager());
        }).collect(Collectors.toList());
    }

    @CacheEvict(key = "#dto.id")
    @Transactional
    @Override
    public void setGroupMuted(GroupMutedDTO dto) {
        UserSession session = SessionContext.getSession();
        Group group = getAndCheckById(dto.getId());
        GroupMember member = groupMemberService.findByGroupAndUserId(dto.getId(), session.getUserId());
        if (!session.getUserId().equals(group.getOwnerId()) && !member.getIsManager()) {
            throw new GlobalException("您没有操作权限");
        }
        if (!group.getIsMuted().equals(dto.getIsMuted())) {
            group.setIsMuted(dto.getIsMuted());
            this.updateById(group);
            this.sendMutedTip(dto.getId(), dto.getIsMuted());
        }
    }

    @Override
    public boolean checkManager(Long groupId, GroupMember member) {
        Group group = getAndCheckById(groupId);
        if (group.getOwnerId().equals(member.getUserId()) || (null != member && member.getIsManager())) {
            return true;
        }
        return false;
    }

    @Override
    public void setMemberMuted(GroupMemberMutedDTO dto) {
        UserSession session = SessionContext.getSession();
        Group group = getAndCheckById(dto.getGroupId());
        GroupMember member = groupMemberService.findByGroupAndUserId(dto.getGroupId(), session.getUserId());
        if (!session.getUserId().equals(group.getOwnerId()) && !member.getIsManager()) {
            throw new GlobalException("您没有操作权限");
        }
        if (dto.getUserIds().contains(group.getOwnerId())) {
            throw new GlobalException("不允许禁言群主");
        }
        // 过滤掉禁言状态无需修改的用户，避免出现重复推送提示消息
        List<Long> userIds = groupMemberService.findMutedUserIds(dto.getGroupId(), dto.getUserIds(), !dto.getIsMuted());
        if (!userIds.isEmpty()) {
            groupMemberService.setMuted(dto.getGroupId(), userIds, dto.getIsMuted());
            String tip = dto.getIsMuted() ? "您已被禁言" : "您的禁言已解除";
            this.sendTipMessage(dto.getGroupId(), userIds, tip, false);
        }
    }

    @Transactional
    @Override
    public void setTopMessage(Long groupId, Long messageId) {
        UserSession session = SessionContext.getSession();
        Group group = getAndCheckById(groupId);
        GroupMember member = groupMemberService.findByGroupAndUserId(groupId, session.getUserId());
        if (!session.getUserId().equals(group.getOwnerId()) && !member.getIsManager()) {
            throw new GlobalException("您没有操作权限");
        }
        GroupMessage message = mongoMessageService.findGroupMessageById(messageId);
        if (Objects.isNull(message) || !message.getGroupId().equals(groupId)) {
            throw new GlobalException("消息不存在");
        }
        // 更新群置顶消息id
        group.setTopMessageId(messageId);

        //更新多条置顶消息 最多5条
        List<Long> topMessageIdList;
        if (StrUtil.isNotBlank(group.getTopMessageIds())) {
            topMessageIdList = JSON.parseArray(group.getTopMessageIds(), Long.class);
            // 移除重复（防止同一条重复置顶）
            topMessageIdList.remove(messageId);
            // 保证最多 4 条，腾出一个位置
            while (topMessageIdList.size() >= 5) {
                topMessageIdList.remove(0);
            }
            // 添加新消息
            topMessageIdList.add(messageId);
        } else {
            topMessageIdList = new ArrayList<>();
            topMessageIdList.add(messageId);
        }
        // 存回数据库字段
        group.setTopMessageIds(JSON.toJSONString(topMessageIdList));

        this.updateById(group);
        // 更新用户置顶显示状态
        groupMemberService.updateTopMessage(groupId, true);
        // 推送提示语
        String content = String.format("‘%s'置顶了一条消息", session.getNickName());
        List<Long> userIds = groupMemberService.findUserIdsByGroupId(groupId);
        sendTipMessage(groupId, userIds, content, true);
        // 推送同步消息
        sendTopGroupMessage(groupId, userIds, message);
    }

    @Transactional
    @Override
    public void removeTopMessage(Long groupId) {
        checkPerm(groupId);
        // 清空置顶消息
        LambdaUpdateWrapper<Group> wrapper = Wrappers.lambdaUpdate();
        wrapper.eq(Group::getId, groupId);
        wrapper.set(Group::getTopMessageId, null);
        wrapper.set(Group::getTopMessageIds, null);
        this.update(wrapper);
        // 更新用户置顶显示状态
        groupMemberService.updateTopMessage(groupId, false);
        // 推送同步消息
        List<Long> userIds = groupMemberService.findUserIdsByGroupId(groupId);
        sendTopGroupMessage(groupId, userIds, null);
    }

    @Transactional
    @Override
    public void removeTopMessage(Long groupId, Long messageId) {
        Group group = checkPerm(groupId);
        List<Long> topMessageIdList = JSON.parseArray(group.getTopMessageIds(), Long.class);
        topMessageIdList.remove(messageId);
        // 移除置顶消息
        LambdaUpdateWrapper<Group> wrapper = Wrappers.lambdaUpdate();
        wrapper.eq(Group::getId, groupId);
        if (Objects.equals(messageId, group.getTopMessageId())) {
            wrapper.set(Group::getTopMessageId, null);
        }
        wrapper.set(Group::getTopMessageIds, JSON.toJSONString(topMessageIdList));
        this.update(wrapper);
        // 更新用户置顶显示状态
        groupMemberService.updateTopMessage(groupId, false);
        // 推送同步消息
        List<Long> userIds = groupMemberService.findUserIdsByGroupId(groupId);
        sendTopGroupMessage(groupId, userIds, null);
    }

    /**
     * 校验权限
     *
     * @param groupId 群聊id
     */
    private Group checkPerm(Long groupId) {
        UserSession session = SessionContext.getSession();
        Group group = getAndCheckById(groupId);
        GroupMember member = groupMemberService.findByGroupAndUserId(groupId, session.getUserId());
        if (!session.getUserId().equals(group.getOwnerId()) && !member.getIsManager()) {
            throw new GlobalException("您没有操作权限");
        }
        return group;
    }

    @Override
    public void hideTopMessage(Long groupId) {
        UserSession session = SessionContext.getSession();
        // 更新用户置顶显示状态
        groupMemberService.updateTopMessage(groupId, session.getUserId(), false);
        // 推送同步消息
        sendTopGroupMessage(groupId, List.of(session.getUserId()), null);
    }

    @Override
    public void addManager(GroupManagerDTO dto) {
        UserSession session = SessionContext.getSession();
        Group group = getAndCheckById(dto.getGroupId());
        if (!session.getUserId().equals(group.getOwnerId())) {
            throw new GlobalException("您没有操作权限");
        }
        groupMemberService.setManager(dto.getGroupId(), dto.getUserIds(), true);
        String tip = "群主将你设置为本群管理员";
        this.sendTipMessage(dto.getGroupId(), dto.getUserIds(), tip, false);
    }

    @Override
    public void removeManager(GroupManagerDTO dto) {
        UserSession session = SessionContext.getSession();
        Group group = getAndCheckById(dto.getGroupId());
        if (!session.getUserId().equals(group.getOwnerId())) {
            throw new GlobalException("您没有操作权限");
        }
        groupMemberService.setManager(dto.getGroupId(), dto.getUserIds(), false);
        String tip = "您已被群主从管理员种移出";
        this.sendTipMessage(dto.getGroupId(), dto.getUserIds(), tip, false);
    }

    @Override
    public void setGroupNotifyExpire(GroupNotifyExpireDto dto) {
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();
        //  更新群成员通知过期时间
        LambdaUpdateWrapper<GroupMember> wrapper = Wrappers.lambdaUpdate();
        wrapper.eq(GroupMember::getGroupId, dto.getGroupId());
        wrapper.eq(GroupMember::getUserId, userId);
        Long ts = System.currentTimeMillis() + dto.getNotifyExpireTs();
        if (dto.getNotifyExpireTs() == 0L) {
            //关闭消息通知
            ts = 0L;
        }
        wrapper.set(GroupMember::getNotifyExpireTs, ts);
        groupMemberService.update(wrapper);
    }

    private void sendMutedTip(Long groupId, Boolean isMuted) {
        String tip = isMuted ? "本群开启了全员禁言,只有群主管理员可以发言" : "本群解除了全员禁言";
        this.sendTipMessage(groupId, null, tip, true);
    }


    private void sendTipMessage(Long groupId, List<Long> recvIds, String content, Boolean sendToAll, Integer messageType) {
        UserSession session = SessionContext.getSession();
        // 消息入库
        GroupMessage message = new GroupMessage();
        message.setContent(content);
        message.setType(messageType);
        message.setStatus(MessageStatus.UNSEND.code());
        message.setSendTime(new Date());
        message.setSendNickName(session.getNickName());
        message.setGroupId(groupId);
        message.setSendId(session.getUserId());
        message.setRecvIds(sendToAll ? "" : CommaTextUtils.asText(recvIds));
        mongoMessageService.saveGroupMessage(message);
        // 推送
        GroupMessageVO msgInfo = BeanUtils.copyProperties(message, GroupMessageVO.class);
        IMGroupMessage<GroupMessageVO> sendMessage = new IMGroupMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        if (CollUtil.isEmpty(recvIds)) {
            // 为空表示向全体发送
            List<Long> userIds = groupMemberService.findUserIdsByGroupId(groupId);
            sendMessage.setRecvIds(userIds);
        } else {
            sendMessage.setRecvIds(recvIds);
        }
        sendMessage.setData(msgInfo);
        sendMessage.setSendResult(false);
        sendMessage.setSendToSelf(false);
        imClient.sendGroupMessage(sendMessage);
    }

    private void sendTipMessage(Long groupId, List<Long> recvIds, String content, Boolean sendToAll) {
        UserSession session = SessionContext.getSession();
        // 消息入库
        GroupMessage message = new GroupMessage();
        message.setContent(content);
        message.setType(MessageType.TIP_TEXT.code());
        message.setStatus(MessageStatus.UNSEND.code());
        message.setSendTime(new Date());
        message.setSendNickName(session.getNickName());
        message.setGroupId(groupId);
        message.setSendId(session.getUserId());
        message.setRecvIds(sendToAll ? "" : CommaTextUtils.asText(recvIds));
        mongoMessageService.saveGroupMessage(message);
        // 推送
        GroupMessageVO msgInfo = BeanUtils.copyProperties(message, GroupMessageVO.class);
        IMGroupMessage<GroupMessageVO> sendMessage = new IMGroupMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        if (CollUtil.isEmpty(recvIds)) {
            // 为空表示向全体发送
            List<Long> userIds = groupMemberService.findUserIdsByGroupId(groupId);
            sendMessage.setRecvIds(userIds);
        } else {
            sendMessage.setRecvIds(recvIds);
        }
        sendMessage.setData(msgInfo);
        sendMessage.setSendResult(false);
        sendMessage.setSendToSelf(false);
        imClient.sendGroupMessage(sendMessage);
    }

    private void sendAddGroupMessage(GroupVO group, List<Long> recvIds) {
        UserSession session = SessionContext.getSession();
        GroupMessageVO msgInfo = new GroupMessageVO();
        msgInfo.setContent(JSON.toJSONString(group));
        msgInfo.setType(MessageType.GROUP_NEW.code());
        msgInfo.setSendTime(new Date());
        msgInfo.setGroupId(group.getId());
        msgInfo.setSendId(session.getUserId());
        IMGroupMessage<GroupMessageVO> sendMessage = new IMGroupMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setRecvIds(recvIds);
        sendMessage.setData(msgInfo);
        sendMessage.setSendResult(false);
        sendMessage.setSendToSelf(false);
        imClient.sendGroupMessage(sendMessage);
    }

    private void sendDelGroupMessage(Long groupId, List<Long> recvIds) {
        UserSession session = SessionContext.getSession();
        GroupMessageVO msgInfo = new GroupMessageVO();
        msgInfo.setType(MessageType.GROUP_DEL.code());
        msgInfo.setSendTime(new Date());
        msgInfo.setGroupId(groupId);
        msgInfo.setSendId(session.getUserId());
        IMGroupMessage<GroupMessageVO> sendMessage = new IMGroupMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setRecvIds(recvIds);
        sendMessage.setData(msgInfo);
        sendMessage.setSendResult(false);
        sendMessage.setSendToSelf(false);
        imClient.sendGroupMessage(sendMessage);
    }

    private void sendTopGroupMessage(Long groupId, List<Long> recvIds, GroupMessage message) {
        UserSession session = SessionContext.getSession();
        GroupMessageVO msgInfo = new GroupMessageVO();
        msgInfo.setType(MessageType.GROUP_TOP_MESSAGE.code());
        msgInfo.setSendTime(new Date());
        msgInfo.setGroupId(groupId);
        msgInfo.setSendId(session.getUserId());
        if (!Objects.isNull(message)) {
            GroupMessageVO topMessage = BeanUtils.copyProperties(message, GroupMessageVO.class);
            msgInfo.setContent(JSON.toJSONString(topMessage));
        }
        IMGroupMessage<GroupMessageVO> sendMessage = new IMGroupMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setRecvIds(recvIds);
        sendMessage.setData(msgInfo);
        sendMessage.setSendResult(false);
        sendMessage.setSendToSelf(false);
        imClient.sendGroupMessage(sendMessage);
    }

    private GroupVO convert(Group group, GroupMember member) {
        GroupVO vo = new GroupVO();
        BeanUtils.copyProperties(group, vo);
        if (!Objects.isNull(member)) {
            vo.setRemarkGroupName(member.getRemarkGroupName());
            vo.setRemarkNickName(member.getRemarkNickName());
            vo.setShowNickName(member.getShowNickName());
            vo.setShowGroupName(StrUtil.blankToDefault(member.getRemarkGroupName(), group.getName()));
            vo.setQuit(member.getQuit());
            vo.setNotifyExpireTs(member.getNotifyExpireTs());
        } else {
            vo.setShowGroupName(group.getName());
            vo.setQuit(true);
        }
        return vo;
    }
}
