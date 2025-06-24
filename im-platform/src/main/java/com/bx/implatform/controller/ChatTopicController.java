package com.bx.implatform.controller;

import com.bx.implatform.result.Result;
import com.bx.implatform.result.ResultUtils;
import com.bx.implatform.service.IChatTopicService;
import com.bx.implatform.vo.TopicVo01;
import com.bx.implatform.vo.TopicVo03;
import com.bx.implatform.vo.TopicVo06;
import com.bx.implatform.vo.TopicVo07;
import com.bx.implatform.vo.TopicVo09;
import com.bx.implatform.vo.TopicVo10;
import com.bx.implatform.vo.TopicVoCount;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 帖子
 */
@Tag(name = "朋友圈-帖子")
@RestController
@Slf4j
@RequestMapping("/topic")
@RequiredArgsConstructor
public class ChatTopicController {


    private final IChatTopicService topicService;


    /**
     * 发布帖子
     */
    @Operation(summary = "发布帖子", description = "发布帖子")
    @PostMapping("/send")
    public Result<Void> sendTopic(@Validated @RequestBody TopicVo01 topicVo) {
        topicService.sendTopic(topicVo);
        return ResultUtils.success();
    }

    /**
     * 修改帖子可见范围
     */
    @Operation(summary = "修改帖子可见范围", description = "修改帖子可见范围")
    @PostMapping("/edit")
    public Result<Void> editTopic(@Validated @RequestBody TopicVo10 topicVo) {
        topicService.editTopic(topicVo);
        return ResultUtils.success();
    }


    /**
     * 删除帖子
     */
    @Operation(summary = "删除帖子", description = "删除帖子")
    @GetMapping("/remove/{topicId}")
    public Result<Void> removeTopic(@PathVariable Long topicId) {
        topicService.delTopic(topicId);
        return ResultUtils.success();
    }

    /**
     * 指定人的帖子
     */
    @Operation(summary = "指定人的帖子", description = "指定人的帖子")
    @GetMapping("/user/{userId}")
    public Result<List<TopicVo03>> userTopic(@PathVariable Long userId, @NotNull(message = "页码不能为空") @RequestParam Long page,
                                             @NotNull(message = "size不能为空") @RequestParam Long size) {
        log.info("开始查询指定人帖子");
        return ResultUtils.success(topicService.userTopic(userId, page, size));
    }

    /**
     * 好友的帖子
     */
    @Operation(summary = "好友帖子", description = "好友帖子")
    @GetMapping("/list")
    public Result<List<TopicVo03>> topicList(@NotNull(message = "页码不能为空") @RequestParam Long page,
                                             @NotNull(message = "size不能为空") @RequestParam Long size) {
        log.info("开始查询动态");
        return ResultUtils.success(topicService.topicList(page, size));
    }

    /**
     * 帖子详情
     */
    @Operation(summary = "帖子详情", description = "帖子详情")
    @GetMapping("/info/{topicId}")
    public Result<TopicVo03> topicInfo(@PathVariable Long topicId) {
        return ResultUtils.success(topicService.topicInfo(topicId));
    }

    /**
     * 点赞
     */
    @Operation(summary = "点赞", description = "点赞")
    @GetMapping("/like/{topicId}")
    public Result<Void> like(@PathVariable Long topicId) {
        topicService.like(topicId);
        return ResultUtils.success();
    }

    /**
     * 取消点赞
     */
    @Operation(summary = "取消点赞", description = "取消点赞")
    @GetMapping("/cancelLike/{topicId}")
    public Result<Void> cancelLike(@PathVariable Long topicId) {
        topicService.cancelLike(topicId);
        return ResultUtils.success();
    }

    /**
     * 回复
     */
    @Operation(summary = "回复", description = "回复")
    @PostMapping("/reply")
    public Result<TopicVo06> reply(@Validated @RequestBody TopicVo07 topicVo) {
        return ResultUtils.success(topicService.reply(topicVo));
    }

    /**
     * 删除回复
     */
    @Operation(summary = "删除回复", description = "删除回复")
    @GetMapping("/removeReply/{replyId}")
    public Result<Void> removeReply(@PathVariable Long replyId) {
        topicService.delReply(replyId);
        return ResultUtils.success();
    }

    /**
     * 查询通知列表
     */
    @Operation(summary = "查询通知列表", description = "查询通知列表")
    @GetMapping("/noticeList")
    public Result<List<TopicVo09>> noticeList() {
        return ResultUtils.success(topicService.queryNoticeList());
    }

    /**
     * 查询通知列表
     */
    @Operation(summary = "查询通知列表数量", description = "查询通知列表数量")
    @GetMapping("/noticeListCount")
    public Result<TopicVoCount> noticeListCount() {
        return ResultUtils.success(topicService.noticeListCount());
    }

    /**
     * 清空通知列表
     */
    @Operation(summary = "清空通知列表", description = "清空通知列表")
    @GetMapping("/clearNotice")
    public Result<Void> clearNotice() {
        topicService.clearNotice();
        return ResultUtils.success();
    }


}
