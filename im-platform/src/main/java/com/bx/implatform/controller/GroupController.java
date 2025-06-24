package com.bx.implatform.controller;

import com.bx.implatform.annotation.RepeatSubmit;
import com.bx.implatform.dto.*;
import com.bx.implatform.result.Result;
import com.bx.implatform.result.ResultUtils;
import com.bx.implatform.service.GroupService;
import com.bx.implatform.vo.GroupMemberVO;
import com.bx.implatform.dto.GroupPermDTO;
import com.bx.implatform.vo.GroupVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@Tag(name = "群聊")
@RestController
@RequestMapping("/group")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @RepeatSubmit
    @Operation(summary = "创建群聊", description = "创建群聊")
    @PostMapping("/create")
    public Result<GroupVO> createGroup(@Valid @RequestBody GroupCreateDTO dto) {
        return ResultUtils.success(groupService.createGroup(dto));
    }

    @RepeatSubmit
    @Operation(summary = "修改群聊信息", description = "修改群聊信息")
    @PutMapping("/modify")
    public Result<GroupVO> modifyGroup(@Valid @RequestBody GroupUpdateDTO dto) {
        return ResultUtils.success(groupService.modifyGroup(dto));
    }

    @RepeatSubmit
    @Operation(summary = "修改群聊权限", description = "修改群聊权限")
    @PutMapping("/modifyPerm")
    public Result<GroupVO> modifyPerm(@Valid @RequestBody GroupPermDTO dto) {
        return ResultUtils.success(groupService.modifyPerm(dto));
    }

    @RepeatSubmit
    @Operation(summary = "解散群聊", description = "解散群聊")
    @DeleteMapping("/delete/{groupId}")
    public Result deleteGroup(@NotNull(message = "群聊id不能为空") @PathVariable Long groupId) {
        groupService.deleteGroup(groupId);
        return ResultUtils.success();
    }

    @Operation(summary = "查询群聊", description = "查询单个群聊信息")
    @GetMapping("/find/{groupId}")
    public Result<GroupVO> findGroup(@NotNull(message = "群聊id不能为空") @PathVariable Long groupId) {
        GroupVO groupVO = groupService.findById(groupId);
        //不允许查找
        if (null != groupVO && Objects.equals(groupVO.getQueryGroupPerm(), 2)) {
            return ResultUtils.success();
        }
        return ResultUtils.success(groupVO);
    }

    @Operation(summary = "查询群聊列表", description = "查询群聊列表")
    @GetMapping("/list")
    public Result<List<GroupVO>> findGroups() {
        return ResultUtils.success(groupService.findGroups());
    }

    @RepeatSubmit
    @Operation(summary = "邀请进群", description = "邀请好友进群")
    @PostMapping("/invite")
    public Result<Void> invite(@Valid @RequestBody GroupInviteDTO dto) {
        groupService.invite(dto);
        return ResultUtils.success();
    }
    @RepeatSubmit
    @Operation(summary = "管理员验证邀请进群", description = "管理员验证邀请进群")
    @PostMapping("/inviteManager")
    public Result<Void> inviteManager(@Valid @RequestBody GroupInviteDTO dto) {
        groupService.inviteManager(dto);
        return ResultUtils.success();
    }

    @RepeatSubmit
    @Operation(summary = "主动申请进入群聊", description = "主动申请进入群聊")
    @PostMapping("/join/{groupId}")
    public Result<GroupVO> join( @NotNull(message = "群聊id不能为空") @PathVariable Long groupId) {
        return ResultUtils.success(groupService.join(groupId));
    }

    @Operation(summary = "查询群聊成员", description = "查询群聊成员")
    @GetMapping("/members/{groupId}")
    public Result<List<GroupMemberVO>> findGroupMembers(
            @NotNull(message = "群聊id不能为空") @PathVariable Long groupId) {
        return ResultUtils.success(groupService.findGroupMembers(groupId));
    }

    @RepeatSubmit
    @Operation(summary = "将成员移出群聊", description = "将成员移出群聊")
    @DeleteMapping("/members/remove")
    public Result<Void> removeMembers(@Valid @RequestBody GroupMemberRemoveDTO dto) {
        groupService.removeGroupMembers(dto);
        return ResultUtils.success();
    }


    @RepeatSubmit
    @Operation(summary = "退出群聊", description = "退出群聊")
    @DeleteMapping("/quit/{groupId}")
    public Result<Void> quitGroup(@NotNull(message = "群聊id不能为空") @PathVariable Long groupId) {
        groupService.quitGroup(groupId);
        return ResultUtils.success();
    }

    @RepeatSubmit
    @Operation(summary = "踢出群聊(已废弃)", description = "将用户踢出群聊")
    @DeleteMapping("/kick/{groupId}")
    public Result<Void> kickGroup(@NotNull(message = "群聊id不能为空") @PathVariable Long groupId,
                            @NotNull(message = "用户id不能为空") @RequestParam Long userId) {
        groupService.kickGroup(groupId, userId);
        return ResultUtils.success();
    }

    @RepeatSubmit
    @Operation(summary = "开启/关闭全员禁言", description = "开启/关闭全员禁言")
    @PutMapping("/muted")
    public Result<Void> setGroupMuted(@Valid @RequestBody GroupMutedDTO dto) {
        groupService.setGroupMuted(dto);
        return ResultUtils.success();
    }

    @RepeatSubmit
    @Operation(summary = "对群成员开启/关闭禁言", description = "对群成员开启/关闭禁言")
    @PutMapping("/members/muted")
    public Result<Void> setMemberMuted(@Valid @RequestBody GroupMemberMutedDTO dto) {
        groupService.setMemberMuted(dto);
        return ResultUtils.success();
    }

    @Operation(summary = "新增群置顶消息", description = "新增群置顶消息")
    @PostMapping("/setTopMessage/{groupId}")
    public Result<Void> setTopMessage(@NotNull(message = "群聊id不能为空") @PathVariable Long groupId,
                                @NotNull(message = "消息id不能为空") @RequestParam Long messageId) {
        groupService.setTopMessage(groupId, messageId);
        return ResultUtils.success();
    }

    @Operation(summary = "移除群置顶消息", description = "移除群置顶消息,对所有群成员生效")
    @DeleteMapping("/removeTopMessage/{groupId}")
    public Result<Void> removeTopMessage(@NotNull(message = "群聊id不能为空") @PathVariable Long groupId) {
        groupService.removeTopMessage(groupId);
        return ResultUtils.success();
    }

    @Operation(summary = "移除群单条置顶消息", description = "移除群置顶消息,对所有群成员生效")
    @DeleteMapping("/removeTopMessageById/{groupId}")
    public Result<Void> removeTopMessages(@NotNull(message = "群聊id不能为空") @PathVariable Long groupId,
                                    @NotNull(message = "消息id不能为空") @RequestParam Long messageId) {
        groupService.removeTopMessage(groupId,messageId);
        return ResultUtils.success();
    }

    @Operation(summary = "隐藏群置顶消息", description = "隐藏置顶消息,仅对自己生效")
    @DeleteMapping("/hideTopMessage/{groupId}")
    public Result<Void> hideTopMessage(@NotNull(message = "群聊id不能为空") @PathVariable Long groupId) {
        groupService.hideTopMessage(groupId);
        return ResultUtils.success();
    }

    @Operation(summary = "新增管理员", description = "新增管理员")
    @PostMapping("/manager/add")
    public Result<Void> addManager(@Valid @RequestBody GroupManagerDTO dto) {
        groupService.addManager(dto);
        return ResultUtils.success();
    }

    @Operation(summary = "移除管理员", description = "移除管理员")
    @DeleteMapping("/manager/remove")
    public Result<Void> removeManager(@Valid @RequestBody GroupManagerDTO dto) {
        groupService.removeManager(dto);
        return ResultUtils.success();
    }

    /**
     * 设置群秒打扰
     */
    @Operation(summary = "设置群消息免打扰", description = "设置群消息免打扰")
    @PutMapping("/notify/expire")
    public Result<Void> setGroupMute(@Valid @RequestBody GroupNotifyExpireDto dto) {
        groupService.setGroupNotifyExpire(dto);
        return ResultUtils.success();
    }
}

