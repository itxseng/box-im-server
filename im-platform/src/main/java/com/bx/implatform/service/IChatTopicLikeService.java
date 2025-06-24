package com.bx.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bx.implatform.entity.ChatTopicLike;
import com.bx.implatform.vo.TopicVo05;

import java.util.List;

/**
 * 帖子点赞Service接口
 *
 * @author Blue
 * @date 2025-05-29
 */
public interface IChatTopicLikeService extends IService<ChatTopicLike> {
    /**
     * 删除帖子点赞信息
     */
    void delByTopicId(Long topicId);

    /**
     * 查询帖子点赞信息
     */
    List<TopicVo05> queryTopicLike(Long userId, Long topicId);

    /**
     * 查询用户点赞信息
     */
    ChatTopicLike queryUserLike(Long topicId, Long userId);
}
