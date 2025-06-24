package com.bx.implatform.controller;


import com.bx.implatform.enums.ResultCode;
import com.bx.implatform.enums.SeePermissionTypeEnum;
import com.bx.implatform.enums.SeeUserTypeEnum;
import com.bx.implatform.result.Result;
import com.bx.implatform.result.ResultUtils;
import com.bx.implatform.service.IChatSeePermissionService;
import com.bx.implatform.service.IChatViewScopeService;
import com.bx.implatform.session.SessionContext;
import com.bx.implatform.session.UserSession;
import com.bx.implatform.vo.ChatPermissionVO;
import com.bx.implatform.vo.ChatSeePermissionVO;
import com.bx.implatform.vo.ChatViewScopeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@Tag(name = "朋友圈-权限")
@RequestMapping("/topic/permission")
@RequiredArgsConstructor
@RestController
public class ChatPermissionController {

    private final IChatViewScopeService chatViewScopeService;

    private final IChatSeePermissionService chatSeePermissionService;

    /**
     * 朋友圈范围权限设置
     *
     * @param chatViewScopeVO 朋友圈范围权限设置
     */
    @Operation(summary = "设置朋友圈时间范围", description = "设置朋友圈时间范围")
    @PostMapping("/scope/setting")
    public Result<Void> scopeSetting(@Validated @RequestBody ChatViewScopeVO chatViewScopeVO) {
        chatViewScopeService.setting(chatViewScopeVO);
        return ResultUtils.success();
    }

    /**
     * 朋友圈查看权限设置
     *
     * @param chatSeePermissionVO 朋友圈查看权限设置
     */
    @Operation(summary = "朋友圈查看权限设置", description = "朋友圈查看权限设置")
    @PostMapping("/see/setting")
    public Result<Void> seeSetting(@Validated @RequestBody ChatSeePermissionVO chatSeePermissionVO) {
        //校验参数 permissionType 权限类型是能是1和2
        if (!Objects.equals(chatSeePermissionVO.getPermissionType(), SeeUserTypeEnum.FRIEND.getCode()) && !Objects.equals(chatSeePermissionVO.getPermissionType(), SeeUserTypeEnum.NO_FRIEND.getCode())) {
            return ResultUtils.error(ResultCode.CHAT_PERM_ERROR);
        }
        chatSeePermissionService.seeSetting(chatSeePermissionVO);
        return ResultUtils.success();
    }

    /**
     * 获取朋友圈权限设置
     *
     * @return 朋友圈权限设置
     */
    @Operation(summary = "获取朋友圈权限设置", description = "获取朋友圈权限设置")
    @GetMapping("/info")
    public Result<ChatPermissionVO> getInfo() {
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();
        //好友查看权限
        int friend = chatViewScopeService.getScope(SeeUserTypeEnum.FRIEND.getCode());
        //陌生人查看权限
        int stranger = chatViewScopeService.getScope(SeeUserTypeEnum.NO_FRIEND.getCode());
        //不让谁看
        List<Long> notSee = chatSeePermissionService.getSeePermission(userId, SeePermissionTypeEnum.NO_SEED_USER.getCode());
        //不看谁
        List<Long> notSeeMe = chatSeePermissionService.getSeePermission(userId, SeePermissionTypeEnum.NO_USER.getCode());

        ChatPermissionVO data = new ChatPermissionVO();
        data.setIsFriend(friend);
        data.setStranger(stranger);
        data.setNotSee(notSee);
        data.setNotSeeMe(notSeeMe);
        return ResultUtils.success(data);
    }


}
