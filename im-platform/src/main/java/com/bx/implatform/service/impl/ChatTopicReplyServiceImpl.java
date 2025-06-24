package com.bx.implatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bx.implatform.entity.ChatTopicBlack;
import com.bx.implatform.entity.ChatTopicReply;
import com.bx.implatform.entity.User;
import com.bx.implatform.enums.TopicReplyTypeEnum;
import com.bx.implatform.enums.YesOrNoEnum;
import com.bx.implatform.mapper.ChatTopicBlackMapper;
import com.bx.implatform.mapper.ChatTopicReplyMapper;
import com.bx.implatform.service.IChatTopicReplyService;
import com.bx.implatform.vo.TopicVo06;
import com.bx.implatform.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 帖子回复Service业务层处理
 *
 * @author Blue
 * @date 2025-05-29
 */
@RequiredArgsConstructor
@Service
public class ChatTopicReplyServiceImpl  extends ServiceImpl<ChatTopicReplyMapper, ChatTopicReply>  implements IChatTopicReplyService {

    private final ChatTopicReplyMapper baseMapper;

    @Override
    public void delByTopicId(Long topicId) {
        baseMapper.delete(new UpdateWrapper<ChatTopicReply>().lambda().eq(ChatTopicReply::getTopicId, topicId));
    }

    @Override
    public List<TopicVo06> queryReplyList(User chatUser, Long topicId) {
        List<TopicVo06> dataList = baseMapper.queryReplyList(chatUser.getId(), topicId);
        dataList.forEach(e -> {
            // 是否可以删除
            e.setCanDeleted(chatUser.getId().equals(e.getUserId()) ? YesOrNoEnum.YES.getCode() : YesOrNoEnum.NO.getCode());
        });
        return dataList;
    }
}
