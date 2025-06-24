package com.bx.implatform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bx.implatform.entity.ChatTopicWhite;
import com.bx.implatform.mapper.ChatTopicWhiteMapper;
import com.bx.implatform.service.IChatTopicWhiteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 帖子允许查看Service业务层处理
 *
 * @author Blue
 * @date 2025-05-29
 */
@RequiredArgsConstructor
@Service
public class ChatTopicWhiteServiceImpl extends ServiceImpl<ChatTopicWhiteMapper, ChatTopicWhite> implements IChatTopicWhiteService {

    private final ChatTopicWhiteMapper baseMapper;

}
