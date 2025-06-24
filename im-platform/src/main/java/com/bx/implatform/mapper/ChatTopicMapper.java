package com.bx.implatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bx.implatform.entity.ChatTopic;
import com.bx.implatform.vo.TopicVo04;

import java.util.List;

/**
 * 主题Mapper接口
 *
 * @author Blue
 * @date 2025-05-29
 */
public interface ChatTopicMapper extends BaseMapper<ChatTopic> {
    /**
     * 查询列表
     */
    List<ChatTopic> queryList(ChatTopic chatTopic);

    /**
     * 查询
     */
    List<TopicVo04> topicList(ChatTopic chatTopic);
}
