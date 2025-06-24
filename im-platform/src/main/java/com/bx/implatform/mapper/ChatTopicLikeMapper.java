package com.bx.implatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bx.implatform.entity.ChatTopicLike;
import com.bx.implatform.vo.TopicVo05;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 帖子点赞Mapper接口
 *
 * @author Blue
 * @date 2025-05-29
 */
public interface ChatTopicLikeMapper extends BaseMapper<ChatTopicLike> {

    /**
     * 查询点赞信息
     */
    List<TopicVo05> queryTopicLike(@Param("topicId") Long topicId, @Param("userId") Long userId);
}
