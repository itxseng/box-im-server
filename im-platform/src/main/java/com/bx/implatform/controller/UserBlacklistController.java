package com.bx.implatform.controller;

import com.bx.implatform.annotation.RepeatSubmit;
import com.bx.implatform.result.Result;
import com.bx.implatform.result.ResultUtils;
import com.bx.implatform.service.UserBlacklistService;
import com.bx.implatform.session.SessionContext;
import com.bx.implatform.vo.UserBlacklistVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author: Blue
 * @date: 2024-09-22
 * @version: 1.0
 */
@Tag(name = "用户黑名单")
@RestController
@RequestMapping("/blacklist")
@RequiredArgsConstructor
public class UserBlacklistController {

    private final UserBlacklistService userBlacklistService;

    @GetMapping("/list")
    @Operation(summary = "分页列表", description = "获取黑名单列表")
    public Result<List<UserBlacklistVO>> blacklist(@NotNull(message = "页码不能为空") @RequestParam Long page,
                                                   @NotNull(message = "size不能为空") @RequestParam Long size) {
        return ResultUtils.success(userBlacklistService.pageList(page, size));
    }

    @RepeatSubmit
    @PostMapping("/add")
    @Operation(summary = "加入黑名单", description = "加入黑名单")
    public Result<Void> add(@RequestParam Long userId) {
        userBlacklistService.add(SessionContext.getSession().getUserId(), userId);
        return ResultUtils.success();
    }

    @DeleteMapping("/remove")
    @Operation(summary = "移除黑名单", description = "移除黑名单")
    public Result<Void> remove(@RequestParam Long userId) {
        userBlacklistService.remove(SessionContext.getSession().getUserId(), userId);
        return ResultUtils.success();
    }
}
