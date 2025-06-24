package com.bx.implatform.controller;

import com.bx.implatform.result.Result;
import com.bx.implatform.result.ResultUtils;
import com.bx.implatform.service.SystemMessageService;
import com.bx.implatform.vo.SystemMessageContentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @author: Blue
 * @date: 2024-09-07
 * @version: 1.0
 */
@Tag(name = "系统消息")
@RestController
@RequestMapping("/message/system")
@RequiredArgsConstructor
public class SystemMessageController {

    private final SystemMessageService systemMessageService;

    @GetMapping("/content")
    @Operation(summary = "获取系统消息内容", description = "获取系统消息内容")
    public Result<SystemMessageContentVO> content(@RequestParam Long id) {
        return ResultUtils.success(systemMessageService.getMessageContent(id));
    }

    @GetMapping("/pullOfflineMessage")
    @Operation(summary = "拉取离线消息", description = "拉取离线消息,消息将通过webscoket异步推送")
    public Result<Void> pullOfflineMessage(@RequestParam Long minSeqNo) {
        systemMessageService.pullOfflineMessage(minSeqNo);
        return ResultUtils.success();
    }

    @PutMapping("/readed")
    @Operation(summary = "消息已读", description = "将系统消息状态置为已读")
    public Result<Void> readedMessage(@RequestParam Long maxSeqNo) {
        systemMessageService.readedMessage(maxSeqNo);
        return ResultUtils.success();
    }




}
