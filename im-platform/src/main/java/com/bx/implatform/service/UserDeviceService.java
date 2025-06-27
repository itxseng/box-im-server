package com.bx.implatform.service;

import com.bx.implatform.entity.UserDevice;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author wx
 */
public interface UserDeviceService {

    void saveOrUpdateDevice(Long userId, String deviceId, String deviceName, String platform, String ip);

    UserDevice findByUserIdAndDeviceId(Long userId, String deviceId);

    void updateLastLogin(Long userId, String deviceId, LocalDateTime time, String ip);

    List<UserDevice> getLoginDevices();

    void deleteByUserIdAndDeviceId(Long userId, String deviceId);
}
