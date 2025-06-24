package com.bx.implatform.controller;

import com.bx.implatform.annotation.RepeatSubmit;
import com.bx.implatform.dto.*;
import com.bx.implatform.entity.User;
import com.bx.implatform.result.Result;
import com.bx.implatform.result.ResultUtils;
import com.bx.implatform.service.UserService;
import com.bx.implatform.session.SessionContext;
import com.bx.implatform.session.UserSession;
import com.bx.implatform.util.BeanUtils;
import com.bx.implatform.vo.OnlineTerminalVO;
import com.bx.implatform.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.List;

@Tag(name = "用户相关")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping("/terminal/online")
    @Operation(summary = "判断用户哪个终端在线(已废弃)", description = "返回在线的用户id的终端集合")
    public Result<List<OnlineTerminalVO>> getOnlineTerminal(@NotNull @RequestParam("userIds") String userIds) {
        return ResultUtils.success(userService.getOnlineTerminals(userIds));
    }


    @GetMapping("/self")
    @Operation(summary = "获取当前用户信息", description = "获取当前用户信息")
    public Result<UserVO> findSelfInfo() {
        UserSession session = SessionContext.getSession();
        User user = userService.findUserInfoById(session.getUserId());
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);

        userVO.setLastLoginTime(user.getLastLoginTime().getTime());
        FindMyWayDto findMyWayDto = new FindMyWayDto();
        findMyWayDto.setUsername(user.getUserNameSearchable());
        findMyWayDto.setNickname(user.getNickNameSearchable());
        findMyWayDto.setPhone(user.getPhoneSearchable());
        findMyWayDto.setEmail(user.getEmailSearchable());
        userVO.setFindMyWay(findMyWayDto);


        return ResultUtils.success(userVO);
    }


    @GetMapping("/find/{id}")
    @Operation(summary = "查找用户", description = "根据id查找用户")
    public Result<UserVO> findById(@NotNull @PathVariable("id") Long id) {
        return ResultUtils.success(userService.findUserById(id));
    }

    @RepeatSubmit
    @PutMapping("/update")
    @Operation(summary = "修改用户信息", description = "修改用户信息，仅允许修改登录用户信息")
    public Result<UserVO> update(@Valid @RequestBody UserDTO dto) {
        userService.update(dto);
        return ResultUtils.success(userService.findUserById(dto.getId()));
    }

    @RepeatSubmit
    @PutMapping("/updateGroup")
    @Operation(summary = "修改用户拉群权限", description = "修改用户拉群权限")
    public Result<Void> updateGroup(@Valid @RequestBody UserGroupPermDTO dto) {
        userService.updateGroup(dto);
        return ResultUtils.success();
    }

    @RepeatSubmit
    @PutMapping("/updateHeadImage")
    @Operation(summary = "修改用户头像", description = "修改用户头像")
    public Result<Void> updateHeadImage(@Valid @RequestBody UserHeadImageDTO dto) {
        userService.updateHeadImage(dto);
        return ResultUtils.success();
    }

    @GetMapping("/findByName")
    @Operation(summary = "查找用户", description = "根据用户名或昵称查找用户(已废弃)")
    public Result<List<UserVO>> findByName(@RequestParam String name) {
        return ResultUtils.success(userService.findUserByName(name));
    }

    @GetMapping("/search")
    @Operation(summary = "查找用户", description = "根据用户名/昵称/手机/邮件查找用户")
    public Result<List<UserVO>> search(@RequestParam String name) {
        //url编码解码
        try {
            name = java.net.URLDecoder.decode(name, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("url解码失败", e);
        }
        return ResultUtils.success(userService.search(name));
    }


    @PostMapping("/reportCid")
    @Operation(summary = "上报用户cid", description = "上报用户cid")
    public Result<Void> reportCid(@RequestParam String cid){
        userService.reportCid(cid);
        return ResultUtils.success();
    }


    @DeleteMapping("/removeCid")
    @Operation(summary = "清理用户cid", description = "清理用户cid")
    public Result<Void> removeCid(){
        userService.removeCid();
        return ResultUtils.success();
    }

    @PutMapping("/manualApprove")
    @Operation(summary = "修改好友验证标记", description = "修改好友验证标记")
    public Result<Void> setManualApprove(@RequestParam Boolean enabled){
        userService.setManualApprove(enabled);
        return ResultUtils.success();
    }

    @RepeatSubmit
    @PutMapping("/bindPhone")
    @Operation(summary = "绑定手机", description = "绑定手机")
    public Result<Void> bindPhone(@Valid @RequestBody BindPhoneDTO dto){
        userService.bindPhone(dto);
        return ResultUtils.success();
    }

    @RepeatSubmit
    @PutMapping("/bindEmail")
    @Operation(summary = "绑定邮箱", description = "绑定邮箱")
    public Result<Void> bindEmail(@Valid @RequestBody BindEmailDTO dto){
        userService.bindEmail(dto);
        return ResultUtils.success("绑定邮箱成功");
    }

    /**
     * 设置可以搜索到我的方式
     */
    @PutMapping("/setSearchable")
    @Operation(summary = "设置可以搜索到我的方式", description = "设置可以搜索到我的方式")
    public Result<Void> setSearchable(@Validated @RequestBody FindMyWayDto dto){
        userService.setSearchable(dto);
        return ResultUtils.success("设置成功");
    }

}

