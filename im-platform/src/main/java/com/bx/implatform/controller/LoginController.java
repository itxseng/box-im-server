package com.bx.implatform.controller;

import cn.hutool.core.util.StrUtil;
import com.bx.imcommon.contant.RedisKey;
import com.bx.imcommon.util.JwtUtil;
import com.bx.implatform.dto.LoginDTO;
import com.bx.implatform.dto.ModifyPwdCodeDTO;
import com.bx.implatform.dto.ModifyPwdDTO;
import com.bx.implatform.dto.RegisterDTO;
import com.bx.implatform.entity.User;
import com.bx.implatform.enums.ResultCode;
import com.bx.implatform.exception.GlobalException;
import com.bx.implatform.result.Result;
import com.bx.implatform.result.ResultUtils;
import com.bx.implatform.service.UserService;
import com.bx.implatform.util.IpUtils;
import com.bx.implatform.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.bx.imcommon.contant.RedisKey.QR_SESSION_PREFIX;


@Tag(name = "注册登录")
@RestController
@RequiredArgsConstructor
@Slf4j
public class LoginController {

    private final UserService userService;

    private final RedisTemplate<String, Object> redisTemplate;

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto, HttpServletRequest request) {
        String clientIp = IpUtils.getClientIp(request);
        LoginVO vo = userService.login(dto,clientIp);
        return ResultUtils.success(vo);
    }

    @PutMapping("/refreshToken")
    @Operation(summary = "刷新token", description = "用refreshtoken换取新的token")
    public Result<LoginVO> refreshToken(@RequestHeader("refreshToken") String refreshToken) {
        LoginVO vo = userService.refreshToken(refreshToken);
        return ResultUtils.success(vo);
    }

    @PostMapping("/qr/generate")
    @Operation(summary = "扫码登录二维码获取", description = "扫码登录二维码")
    public Result generateQrCode(@RequestParam String deviceId,
                                              @RequestParam String deviceName,HttpServletRequest request) {
        //url编码解码
        try {
            deviceName = java.net.URLDecoder.decode(deviceName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("url解码失败", e);
        }
        String clientIp = IpUtils.getClientIp(request);
        Map<String, Object> stringObjectMap = userService.generateQrCode(deviceId, deviceName,  clientIp);
        return ResultUtils.success(stringObjectMap);
    }

    /**
     * 2. 获取二维码信息（App 展示设备信息）
     */
    @GetMapping("/qr/info")
    public Result getQrInfo(@RequestParam String uuid) {
        String key = StrUtil.join(":", RedisKey.QR_SESSION_PREFIX, uuid);
        Map<Object, Object> data = redisTemplate.opsForHash().entries(key);
        if (data == null || data.isEmpty()) {
            throw new GlobalException("二维码不存在或已过期");
        }
        Map<String, Object> objectMap = Map.of(
                "deviceId", data.get("deviceId"),
                "deviceName", data.get("deviceName"),
                "status", data.get("status")
        );
        return ResultUtils.success(objectMap);
    }

    @PostMapping("/qr/direct")
    @Operation(summary = "直接登录", description = "直接登录")
    public Result<LoginVO> directLogin(@RequestParam String uuid) {
        userService.directLogin(uuid);
        return ResultUtils.success();
    }

    @GetMapping("/qr/status")
    public Result getQrStatus(@RequestParam String uuid) {
        String key = StrUtil.join(":", RedisKey.QR_SESSION_PREFIX, uuid);
        Map<Object, Object> data = redisTemplate.opsForHash().entries(key);
        if (data == null || data.isEmpty()) {
            throw new GlobalException("二维码不存在或已过期");
        }
        Map<Object, Object> voData = Map.of(
                "status", data.get("status")
                , "token", data.get("vo")
        );
        return ResultUtils.success(voData);
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "用户注册")
    public Result<Void> register(@Valid @RequestBody RegisterDTO dto) {
        userService.register(dto);
        return ResultUtils.success();
    }

    @DeleteMapping("/unregister")
    @Operation(summary = "用户注销", description = "用户注销")
    public Result<Void> unregister() {
        userService.unregister();
        return ResultUtils.success();
    }

    @PutMapping("/modifyPwd")
    @Operation(summary = "修改密码", description = "修改用户密码")
    public Result<Void> modifyPassword(@Valid @RequestBody ModifyPwdDTO dto) {
        userService.modifyPassword(dto);
        return ResultUtils.success();
    }

    /**
     * 使用验证码修改密码
     */
    @PutMapping("/modifyPwdByCode")
    @Operation(summary = "修改密码", description = "修改用户密码")
    public Result<Void> modifyPasswordByCode(@Valid @RequestBody ModifyPwdCodeDTO dto) {
        userService.modifyPasswordByCode(dto);
        return ResultUtils.success();
    }
}
