package com.bx.implatform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bx.implatform.entity.ChatTopicBlack;
import com.bx.implatform.mapper.ChatTopicBlackMapper;
import com.bx.implatform.service.IChatTopicBlackService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 帖子不允许查看Service业务层处理
 *
 * @author Blue
 * @date 2025-05-29
 */
@RequiredArgsConstructor
@Service
public class ChatTopicBlackServiceImpl extends ServiceImpl<ChatTopicBlackMapper, ChatTopicBlack> implements IChatTopicBlackService {

    private final ChatTopicBlackMapper baseMapper;

}
