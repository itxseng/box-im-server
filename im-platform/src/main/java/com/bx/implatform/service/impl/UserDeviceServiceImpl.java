package com.bx.implatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bx.implatform.entity.UserDevice;
import com.bx.implatform.mapper.UserDeviceMapper;
import com.bx.implatform.service.UserDeviceService;
import com.bx.implatform.session.SessionContext;
import com.bx.implatform.session.UserSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDeviceServiceImpl extends ServiceImpl<UserDeviceMapper, UserDevice> implements UserDeviceService {

    private final UserDeviceMapper deviceMapper;


    @Override
    public void saveOrUpdateDevice(Long userId, String deviceId, String deviceName, String platform, String ip) {

        UserDevice device = this.findByUserIdAndDeviceId(userId, deviceId);

        if (device == null) {
            device = new UserDevice();
            device.setUserId(userId);
            device.setDeviceId(deviceId);
            device.setDeviceName(deviceName);
            device.setPlatform(platform);
            device.setCreateTime(new Date());
        }
        device.setLastLoginTime(new Date());
        device.setLastIp(ip);
        device.setStatus(1);
        deviceMapper.insertOrUpdate(device);
    }

    @Override
    public UserDevice findByUserIdAndDeviceId(Long userId, String deviceId) {
        LambdaUpdateWrapper<UserDevice> queryWrapper = new LambdaUpdateWrapper<>();
        queryWrapper.eq(UserDevice::getUserId, userId)
                .eq(UserDevice::getDeviceId, deviceId);
        return deviceMapper.selectOne(queryWrapper);
    }

    @Override
    public void updateLastLogin(Long userId, String deviceId, LocalDateTime time, String ip) {
        LambdaUpdateWrapper<UserDevice> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserDevice::getUserId, userId)
                .eq(UserDevice::getDeviceId, deviceId);

        updateWrapper.set(UserDevice::getLastLoginTime, time);
        updateWrapper.set(UserDevice::getLastIp, ip);
        deviceMapper.update(updateWrapper);
    }

    @Override
    public List<UserDevice> getLoginDevices() {
        UserSession userSession = SessionContext.getSession();
        Long userId = userSession.getUserId();
        LambdaUpdateWrapper<UserDevice> queryWrapper = new LambdaUpdateWrapper<>();
        queryWrapper.eq(UserDevice::getUserId, userId)
                .orderByDesc(UserDevice::getLastLoginTime);
        return deviceMapper.selectList(queryWrapper);
    }
}
