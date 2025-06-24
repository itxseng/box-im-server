package com.bx.implatform.controller;

import com.bx.implatform.annotation.RepeatSubmit;
import com.bx.implatform.dto.FriendNotifyExpireDto;
import com.bx.implatform.dto.FriendRemarkDTO;
import com.bx.implatform.dto.FriendTagDTO;
import com.bx.implatform.result.Result;
import com.bx.implatform.result.ResultUtils;
import com.bx.implatform.service.FriendService;
import com.bx.implatform.vo.FriendGroupVO;
import com.bx.implatform.vo.FriendVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "好友")
@RestController
@RequestMapping("/friend")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;


    @GetMapping("/list")
    @Operation(summary = "好友列表", description = "获取好友列表")
    public Result<List<FriendVO>> findFriends(@NotNull(message = "页码不能为空") @RequestParam Long page,
                                              @NotNull(message = "size不能为空") @RequestParam Long size) {
        return ResultUtils.success(friendService.findPageFriends(page, size));
    }

    @GetMapping("/group/list")
    @Operation(summary = "好友共同群聊列表", description = "好友共同群聊列表")
    public Result<List<FriendGroupVO>> findGroupList(@NotNull(message = "好友ID不能为空") @RequestParam Long friendId,
                                                     @NotNull(message = "页码不能为空") @RequestParam Long page,
                                                     @NotNull(message = "size不能为空") @RequestParam Long size) {
        return ResultUtils.success(friendService.findPageFriendGroup(page, size, friendId));
    }

    @RepeatSubmit
    @PostMapping("/add")
    @Operation(summary = "添加好友(已废弃)", description = "双方建立好友关系")
    public Result<Void> addFriend(@NotNull(message = "好友id不可为空") @RequestParam Long friendId) {
        friendService.addFriend(friendId);
        return ResultUtils.success();
    }


    @PutMapping("/update/remark")
    @Operation(summary = "修改好友备注", description = "修改好友备注")
    public Result<FriendVO> modifyRemark(@Valid @RequestBody FriendRemarkDTO dto) {
        return ResultUtils.success(friendService.modifyRemark(dto));
    }
    @PutMapping("/update/tag")
    @Operation(summary = "修改好友标记", description = "修改好友标记")
    public Result<FriendVO> modifyTag(@Valid @RequestBody FriendTagDTO dto) {
        return ResultUtils.success(friendService.modifyTag(dto));
    }

    @GetMapping("/find/{friendId}")
    @Operation(summary = "查找好友信息", description = "查找好友信息")
    public Result<FriendVO> findFriend(@NotNull(message = "好友id不可为空") @PathVariable Long friendId) {
        return ResultUtils.success(friendService.findFriend(friendId));
    }

    @DeleteMapping("/delete/{friendId}")
    @Operation(summary = "删除好友", description = "解除好友关系")
    public Result<Void> delFriend(@NotNull(message = "好友id不可为空") @PathVariable Long friendId) {
        friendService.delFriend(friendId);
        return ResultUtils.success();
    }

    @PutMapping("/update")
    @Operation(summary = "更新好友信息(已废弃)", description = "更新好友头像或昵称")
    public Result<Void> modifyFriend(@Valid @RequestBody FriendVO vo) {
        friendService.update(vo);
        return ResultUtils.success();
    }

    @PostMapping("/notify/expire")
    @Operation(summary = "设置好友不通知时间", description = "设置好友不通知时间")
    public Result<Void> setNotifyExpireTime(@RequestBody FriendNotifyExpireDto request) {
        friendService.updateNotifyExpireTime(request.getFriendId(), request.getNotifyExpireTs());
        return ResultUtils.success("设置成功");
    }

}

