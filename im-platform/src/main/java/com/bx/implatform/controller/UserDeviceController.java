package com.bx.implatform.controller;


import cn.hutool.core.util.StrUtil;
import com.bx.imcommon.contant.IMRedisKey;
import com.bx.imcommon.mq.RedisMQTemplate;
import com.bx.imclient.IMClient;
import com.bx.implatform.entity.UserDevice;
import com.bx.implatform.result.Result;
import com.bx.implatform.result.ResultUtils;
import com.bx.implatform.service.UserDeviceService;
import com.bx.implatform.session.SessionContext;
import com.bx.implatform.session.UserSession;
import com.bx.implatform.vo.UserDeviceListVo;
import com.bx.implatform.vo.UserDeviceVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 *
 */
@Tag(name = "用户登录设备")
@RestController
@RequestMapping("/user/login/device")
@RequiredArgsConstructor
public class UserDeviceController {

    private final UserDeviceService userDeviceService;

    private final RedisMQTemplate redisMQTemplate;

    private final IMClient imClient;
    @Operation(summary = "用户登录设备列表", description = "用户登录设备列表")
    @GetMapping("/list")
    public Result<UserDeviceVo> getLoginDevices() {
        UserSession userSession = SessionContext.getSession();
        String deviceId = userSession.getDeviceId();
        // 1. 拿到所有登录设备（防 NPE）
        List<UserDevice> devices = Optional.ofNullable(userDeviceService.getLoginDevices())
                .orElse(Collections.emptyList());

        // 2. 准备返回 VO
        UserDeviceVo result = new UserDeviceVo();
        UserDeviceListVo nowVo = null;
        List<UserDeviceListVo> otherVos = new ArrayList<>(devices.size());

        // 3. 遍历，构造每个设备 VO 并从 Redis 判断是否在线
        for (UserDevice d : devices) {
            UserDeviceListVo vo = new UserDeviceListVo(d);
            // 用 SISMEMBER 判断指定 deviceId 是否在线
            String redisKey = StrUtil.join(":",
                    IMRedisKey.IM_USER_DEVICE_ID, d.getUserId(), d.getPlatform());
            Boolean online = redisMQTemplate.opsForSet()
                    .isMember(redisKey, d.getDeviceId());
            vo.setOnline(Boolean.TRUE.equals(online));

            // 分流：匹配到当前 deviceId 放到 nowVo，否则放到 otherVos
            if (d.getDeviceId().equals(deviceId)) {
                nowVo = vo;
            } else {
                otherVos.add(vo);
            }
        }

        // 4. 如果没匹配到“当前设备”，给个空 VO（默认为 offline）
        if (nowVo == null) {
            nowVo = new UserDeviceListVo();
            nowVo.setOnline(false);
        }

        // 5. 封装并返回
        result.setNowDevice(nowVo);
        result.setOtherDeviceList(otherVos);
        return ResultUtils.success(result);
    }

    @Operation(summary = "踢设备下线", description = "踢指定设备下线")
    @GetMapping("/kick")
    public Result<Void> kickDevice(String deviceId) {
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();
        UserDevice device = userDeviceService.findByUserIdAndDeviceId(userId, deviceId);
        if (device != null) {
            Integer terminal = Integer.valueOf(device.getPlatform());
            imClient.forceLogout(userId, terminal, deviceId);
        }
        return ResultUtils.success();
    }

}
