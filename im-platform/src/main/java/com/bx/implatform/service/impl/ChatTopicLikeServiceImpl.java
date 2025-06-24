package com.bx.implatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bx.implatform.entity.ChatTopicLike;
import com.bx.implatform.mapper.ChatTopicLikeMapper;
import com.bx.implatform.service.IChatTopicLikeService;
import com.bx.implatform.vo.TopicVo05;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 帖子点赞Service业务层处理
 *
 * @author Blue
 * @date 2025-05-29
 */
@RequiredArgsConstructor
@Service
public class ChatTopicLikeServiceImpl extends ServiceImpl<ChatTopicLikeMapper, ChatTopicLike>  implements IChatTopicLikeService {

    private final ChatTopicLikeMapper baseMapper;


    @Override
    public void delByTopicId(Long topicId) {
        baseMapper.delete(new UpdateWrapper<ChatTopicLike>().lambda().eq(ChatTopicLike::getTopicId, topicId));
    }

    @Override
    public List<TopicVo05> queryTopicLike(Long userId, Long topicId) {
        return baseMapper.queryTopicLike(topicId, userId);
    }

    @Override
    public ChatTopicLike queryUserLike(Long topicId, Long userId) {
        return baseMapper.selectOne(new QueryWrapper<ChatTopicLike>().lambda().eq(ChatTopicLike::getTopicId, topicId).eq(ChatTopicLike::getUserId, userId));
    }
}
