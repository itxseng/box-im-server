package com.bx.implatform.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bx.imclient.IMClient;
import com.bx.imcommon.model.IMPrivateMessage;
import com.bx.imcommon.model.IMUserInfo;
import com.bx.implatform.contant.Constant;
import com.bx.implatform.dto.FriendRequestApplyDTO;
import com.bx.implatform.entity.FriendRequest;
import com.bx.implatform.entity.User;
import com.bx.implatform.enums.FriendRequestStatus;
import com.bx.imcommon.enums.MessageType;
import com.bx.implatform.exception.GlobalException;
import com.bx.implatform.mapper.FriendRequestMapper;
import com.bx.implatform.service.*;
import com.bx.implatform.session.SessionContext;
import com.bx.implatform.session.UserSession;
import com.bx.implatform.util.BeanUtils;
import com.bx.implatform.vo.FriendRequestVO;
import com.bx.implatform.vo.PrivateMessageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 好友审核
 *
 * @author Blue
 * @version 1.0
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class FriendRequestServiceImpl extends ServiceImpl<FriendRequestMapper, FriendRequest>
    implements FriendRequestService {

    private final UserService userService;
    private final FriendService friendService;
    private final UserBlacklistService userBlacklistService;
    private final PrivateMessageService privateMessageService;
    private final IMClient imClient;

    @Override
    public List<FriendRequestVO> loadPendingList() {
        UserSession session = SessionContext.getSession();
        // 我收到的请求
        LambdaQueryWrapper<FriendRequest> wrapper = Wrappers.lambdaQuery();
//        wrapper.eq(FriendRequest::getStatus, FriendRequestStatus.PENDING.getCode());
        wrapper.eq(FriendRequest::getRecvId, session.getUserId());
        wrapper.orderByDesc(FriendRequest::getId);
        List<FriendRequest> requests = this.list(wrapper);
        // 我发出的请求
        wrapper = Wrappers.lambdaQuery();
//        wrapper.eq(FriendRequest::getStatus, FriendRequestStatus.PENDING.getCode());
        wrapper.eq(FriendRequest::getSendId, session.getUserId());
        wrapper.orderByDesc(FriendRequest::getId);
        requests.addAll(this.list(wrapper));
        return requests.stream().map(o -> {
                    FriendRequestVO friendRequestVO = BeanUtils.copyProperties(o, FriendRequestVO.class);
                    friendRequestVO.setApplyTime2(o.getApplyTime().getTime());
                    return friendRequestVO;
                })
            .collect(Collectors.toList());
    }

    @Override
    public FriendRequestVO apply(FriendRequestApplyDTO dto) {
        UserSession session = SessionContext.getSession();
        if (session.getUserId().equals(dto.getFriendId())) {
            throw new GlobalException("不允许添加自己为好友");
        }
        if (friendService.isFriend(session.getUserId(), dto.getFriendId())) {
            throw new GlobalException("对方已是您的好友");
        }
        if (userBlacklistService.isInBlacklist(dto.getFriendId(), session.getUserId())) {
            throw new GlobalException("对方已将您拉入黑名单");
        }
        // 先查询，防止多次重复申请
        LambdaQueryWrapper<FriendRequest> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(FriendRequest::getSendId, session.getUserId());
        wrapper.eq(FriendRequest::getRecvId, dto.getFriendId());
        wrapper.eq(FriendRequest::getStatus, FriendRequestStatus.PENDING.getCode());
        FriendRequest request = this.getOne(wrapper);
        if (!Objects.isNull(request)) {
            throw new GlobalException("您已在对方的好友申请列表中,无需重复申请");
        }
        // 对方申请列表数量上限校验
        wrapper = Wrappers.lambdaQuery();
        wrapper.eq(FriendRequest::getStatus, FriendRequestStatus.PENDING.getCode());
        wrapper.eq(FriendRequest::getRecvId, dto.getFriendId());
        if(this.count(wrapper) >= Constant.MAX_PRIEND_APPLY){
            throw new GlobalException("对方的好友申请列表已满");
        }
        // 自己的申请列表上限校验
        wrapper = Wrappers.lambdaQuery();
        wrapper.eq(FriendRequest::getStatus, FriendRequestStatus.PENDING.getCode());
        wrapper.eq(FriendRequest::getSendId, session.getUserId());
        if(this.count(wrapper) >= Constant.MAX_PRIEND_APPLY){
            throw new GlobalException("您的好友申请列表已满");
        }
        // 新请求
        User sender = userService.getById(session.getUserId());
        User receiver = userService.getById(dto.getFriendId());
        request = new FriendRequest();
        request.setSendId(session.getUserId());
        request.setSendNickName(sender.getNickName());
        request.setSendHeadImage(sender.getHeadImageThumb());
        request.setRecvId(dto.getFriendId());
        request.setRecvNickName(receiver.getNickName());
        request.setRecvHeadImage(receiver.getHeadImageThumb());
        request.setRemark(dto.getRemark());
        request.setApplyTime(new Date());
        request.setStatus(FriendRequestStatus.PENDING.getCode());
        FriendRequestVO vo = BeanUtils.copyProperties(request, FriendRequestVO.class);
        if (!receiver.getIsManualApprove()) {
            // 自动通过好友
            friendService.addFriend(dto.getFriendId());
            // 推送同意消息
            vo.setStatus(FriendRequestStatus.APPROVED.getCode());
        } else {
            // 入库
            this.saveOrUpdate(request);
            vo.setId(request.getId());
            // 推送添加请求
            sendRequestMessage(dto.getFriendId(), MessageType.FRIEND_REQ_APPLY, vo, false, true);
        }
        return vo;
    }

    @Override
    public void approve(Long id) {
        long totalStart = System.currentTimeMillis();
        log.info("[好友请求处理] 开始处理好友请求，ID：{}", id);

        UserSession session = SessionContext.getSession();
        Long currentUserId = session.getUserId();

        long start = System.currentTimeMillis();
        FriendRequest request = this.getById(id);
        log.info("[好友请求处理] 查询好友请求耗时：{} ms", System.currentTimeMillis() - start);

        if (request == null) {
            throw new GlobalException("好友请求不存在");
        }

        if (!currentUserId.equals(request.getRecvId())) {
            throw new GlobalException("您无法处理该请求");
        }

        if (!FriendRequestStatus.PENDING.getCode().equals(request.getStatus())) {
            throw new GlobalException("该请求已处理");
        }

        start = System.currentTimeMillis();
        friendService.addFriend(request.getSendId());
        log.info("[好友请求处理] 添加好友耗时：{} ms", System.currentTimeMillis() - start);

        start = System.currentTimeMillis();
        request.setStatus(FriendRequestStatus.APPROVED.getCode());
        this.updateById(request);
        log.info("[好友请求处理] 更新请求状态耗时：{} ms", System.currentTimeMillis() - start);

        start = System.currentTimeMillis();
        FriendRequestVO vo = BeanUtils.copyProperties(request, FriendRequestVO.class);
        sendRequestMessage(request.getSendId(), MessageType.FRIEND_REQ_APPROVE, vo, true, false);
        log.info("[好友请求处理] 推送同意消息耗时：{} ms", System.currentTimeMillis() - start);

        log.info("[好友请求处理] 全流程完成，总耗时：{} ms", System.currentTimeMillis() - totalStart);
    }

    @Override
    public void reject(Long id) {
        UserSession session = SessionContext.getSession();
        FriendRequest request = this.getById(id);
        if (!session.getUserId().equals(request.getRecvId())) {
            throw new GlobalException("您无法处理该请求");
        }
        if (!FriendRequestStatus.PENDING.getCode().equals(request.getStatus())) {
            throw new GlobalException("该请求已处理");
        }
        // 更新状态
        request.setStatus(FriendRequestStatus.REJECTED.getCode());
        this.updateById(request);
        // 推送同意消息
        FriendRequestVO vo = BeanUtils.copyProperties(request, FriendRequestVO.class);
        sendRequestMessage(request.getSendId(), MessageType.FRIEND_REQ_REJECT, vo, true, false);
    }

    @Override
    public void recall(Long id) {
        UserSession session = SessionContext.getSession();
        FriendRequest request = this.getById(id);
        if (!session.getUserId().equals(request.getSendId())) {
            throw new GlobalException("您无法处理该请求");
        }
        if (!FriendRequestStatus.PENDING.getCode().equals(request.getStatus())) {
            throw new GlobalException("该请求已处理，无法撤回");
        }
        // 更新状态
        request.setStatus(FriendRequestStatus.RECALL.getCode());
        this.updateById(request);
        // 推送同意消息
        FriendRequestVO vo = BeanUtils.copyProperties(request, FriendRequestVO.class);
        sendRequestMessage(request.getRecvId(), MessageType.FRIEND_REQ_RECALL, vo, true, true);
    }

    void sendRequestMessage(Long fid, MessageType type, FriendRequestVO vo, Boolean sendToSelf, Boolean sendResult) {
        UserSession session = SessionContext.getSession();
        PrivateMessageVO msgInfo = new PrivateMessageVO();
        msgInfo.setSendId(session.getUserId());
        msgInfo.setRecvId(fid);
        msgInfo.setSendTime(vo.getApplyTime());
        msgInfo.setType(type.code());
        msgInfo.setContent(JSON.toJSONString(vo));
        IMPrivateMessage<PrivateMessageVO> sendMessage = new IMPrivateMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setRecvId(fid);
        sendMessage.setData(msgInfo);
        sendMessage.setSendToSelf(sendToSelf);
        sendMessage.setSendResult(sendResult);
        imClient.sendPrivateMessage(sendMessage);
    }

}
