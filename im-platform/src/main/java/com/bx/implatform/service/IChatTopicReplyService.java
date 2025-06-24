package com.bx.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bx.implatform.entity.ChatTopicReply;
import com.bx.implatform.entity.User;
import com.bx.implatform.vo.TopicVo06;

import java.util.List;

/**
 * 帖子回复Service接口
 *
 * @author Blue
 * @date 2025-05-29
 */
public interface IChatTopicReplyService extends IService<ChatTopicReply> {

    void delByTopicId(Long topicId);

    /**
     * 根据帖子查询
     */
    List<TopicVo06> queryReplyList(User chatUser, Long topicId);
}
