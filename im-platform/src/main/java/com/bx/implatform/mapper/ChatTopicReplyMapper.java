package com.bx.implatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bx.implatform.entity.ChatTopicReply;
import com.bx.implatform.vo.TopicVo06;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 帖子回复Mapper接口
 *
 * @author Blue
 * @date 2025-05-29
 */
public interface ChatTopicReplyMapper extends BaseMapper<ChatTopicReply> {

    /**
     * 根据帖子查询
     */
    List<TopicVo06> queryReplyList(@Param("userId") Long userId, @Param("topicId") Long topicId);
}
