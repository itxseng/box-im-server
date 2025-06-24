package com.bx.implatform.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bx.imclient.IMClient;
import com.bx.imcommon.contant.RedisKey;
import com.bx.imcommon.enums.IMTerminalType;
import com.bx.imcommon.enums.MessageType;
import com.bx.imcommon.model.IMSystemMessage;
import com.bx.imcommon.util.JwtUtil;
import com.bx.imcommon.util.RedisUtils;
import com.bx.implatform.config.props.JwtProperties;
import com.bx.implatform.config.props.NotifyProperties;
import com.bx.implatform.dto.*;
import com.bx.implatform.entity.Friend;
import com.bx.implatform.entity.GroupMember;
import com.bx.implatform.entity.User;
import com.bx.implatform.enums.*;
import com.bx.implatform.exception.GlobalException;
import com.bx.implatform.mapper.UserMapper;
import com.bx.implatform.service.*;
import com.bx.implatform.session.SessionContext;
import com.bx.implatform.session.UserSession;
import com.bx.implatform.util.*;
import com.bx.implatform.vo.LoginVO;
import com.bx.implatform.vo.OnlineTerminalVO;
import com.bx.implatform.vo.SystemMessageVO;
import com.bx.implatform.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.apache.logging.log4j.util.Strings;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;


/**
 * @author sclt
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final GroupMemberService groupMemberService;
    private final UserBlacklistService userBlacklistService;
    private final FriendService friendService;
    private final JwtProperties jwtProps;
    private final NotifyProperties notifyProps;
    private final IMClient imClient;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CaptchaService captchaService;
    private final UserDeviceService userDeviceService;
    private final ShortUUIDGenerator userNameGenerator;


    private final SensitiveFilterUtil sensitiveFilterUtil;

    @Override
    public LoginVO login(LoginDTO dto, String cip) {
        User user = this.findUserByLoginName(dto.getUserName());
        if (Objects.isNull(user)) {
            throw new GlobalException("用户不存在");
        }
        if (user.getStatus().equals(UserStatus.UN_REG.getValue())) {
            throw new GlobalException("您的账号已注销");
        }
        if (user.getIsBanned()) {
            String tip = String.format("您的账号因'%s'已被管理员封禁,请联系客服!", user.getReason());
            throw new GlobalException(ResultCode.USER_BANNED, tip);
        }

        // 手机、验证码校验
        if (RegisterMode.PHONE.getCode().equals(dto.getMode())) {
            if (!RegexUtil.isPhone(dto.getUserName())) {
                throw new GlobalException("手机号格式不合法");
            }
            String superCode = (String) RedisUtils.get(RedisKey.SUPER_CAPTCHA_CODE_KEY);
            if ((null != superCode && superCode.equals(dto.getCode()))) {
                log.info("手机号超级管理员验证码校验通过");
            } else if (!captchaService.vertify(CaptchaType.SMS, dto.getUserName(), dto.getCode())) {
                throw new GlobalException("验证码错误");
            }
        } else if (RegisterMode.EMAIL.getCode().equals(dto.getMode())) {// 邮箱、验证码校验
            if (!RegexUtil.isEmail(dto.getUserName())) {
                throw new GlobalException("邮箱格式不合法");
            }
            String superCode = (String) RedisUtils.get(RedisKey.SUPER_CAPTCHA_CODE_KEY);
            if ((null != superCode && superCode.equals(dto.getCode()))) {
                log.info("邮箱超级管理员验证码校验通过");
            } else if (!captchaService.vertify(CaptchaType.MAIL, dto.getUserName(), dto.getCode())) {
                throw new GlobalException("验证码错误");
            }
        } else if (RegisterMode.PASSWORD.getCode().equals(dto.getMode())) {
            if (StringUtils.isBlank(dto.getPassword())) {
                throw new GlobalException("密码不能为空");
            }
            if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
                throw new GlobalException(ResultCode.PASSWOR_ERROR);
            }
        } else {
            throw new GlobalException("登录方式不支持");
        }

        // 更新用户登录时间
        User updateUser = new User();
        updateUser.setLastLoginTime(new Date());
        updateUser.setId(user.getId());
        this.updateById(updateUser);
        // 生成token
        UserSession session = BeanUtils.copyProperties(user, UserSession.class);
        assert session != null;
        session.setUserId(user.getId());
        session.setTerminal(dto.getTerminal());
        session.setDeviceId(dto.getDeviceId());
        String strJson = JSON.toJSONString(session);
        String accessToken =
                JwtUtil.sign(user.getId(), strJson, jwtProps.getAccessTokenExpireIn(), jwtProps.getAccessTokenSecret());
        String refreshToken =
                JwtUtil.sign(user.getId(), strJson, jwtProps.getRefreshTokenExpireIn(), jwtProps.getRefreshTokenSecret());
        LoginVO vo = new LoginVO();
        vo.setAccessToken(accessToken);
        vo.setUserId(user.getId());
        vo.setAccessTokenExpiresIn(jwtProps.getAccessTokenExpireIn());
        vo.setRefreshToken(refreshToken);
        vo.setRefreshTokenExpiresIn(jwtProps.getRefreshTokenExpireIn());
        //  保存用户登录信息
        try {
            userDeviceService.saveOrUpdateDevice(user.getId(), dto.getDeviceId(), dto.getDeviceName(), dto.getTerminal().toString(), cip);
        } catch (Exception e) {
            log.error("保存用户登录信息异常，{}", e.getMessage());
        }
        return vo;
    }

    @Override
    public LoginVO refreshToken(String refreshToken) {
        //验证 token
        if (!JwtUtil.checkSign(refreshToken, jwtProps.getRefreshTokenSecret())) {
            throw new GlobalException("您的登录信息已过期，请重新登录");
        }
        String strJson = JwtUtil.getInfo(refreshToken);
        Long userId = JwtUtil.getUserId(refreshToken);
        User user = this.getById(userId);
        if (Objects.isNull(user)) {
            throw new GlobalException("用户不存在");
        }
        if (user.getIsBanned()) {
            String tip = String.format("您的账号因'%s'被管理员封禁,请联系客服!", user.getReason());
            throw new GlobalException(ResultCode.USER_BANNED, tip);
        }
        // 更新用户登录时间
        user.setLastLoginTime(new Date());
        this.updateById(user);
        String accessToken =
                JwtUtil.sign(userId, strJson, jwtProps.getAccessTokenExpireIn(), jwtProps.getAccessTokenSecret());
        String newRefreshToken =
                JwtUtil.sign(userId, strJson, jwtProps.getRefreshTokenExpireIn(), jwtProps.getRefreshTokenSecret());
        LoginVO vo = new LoginVO();
        vo.setAccessToken(accessToken);
        vo.setUserId(user.getId());
        vo.setAccessTokenExpiresIn(jwtProps.getAccessTokenExpireIn());
        vo.setRefreshToken(newRefreshToken);
        vo.setRefreshTokenExpiresIn(jwtProps.getRefreshTokenExpireIn());
        return vo;
    }


    @Override
    public LoginVO directLogin(String uuid) {
        String key = StrUtil.join(":", RedisKey.QR_SESSION_PREFIX, uuid);
        Map<Object, Object> data = redisTemplate.opsForHash().entries(key);

        if (data == null || data.isEmpty()) {
            throw new GlobalException("二维码无效或已过期");
        }

        if (!"waiting".equals(data.get("status"))) {
            throw new GlobalException("二维码状态异常");
        }

        String deviceId = (String) data.get("deviceId");
        String deviceName = (String) data.get("deviceName");
        String cip = (String) data.get("cip");
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();
        User user = getById(userId);
        if (user == null) {
            throw new GlobalException("用户不存在");
        }
        if (Boolean.TRUE.equals(user.getIsBanned())) {
            throw new GlobalException(ResultCode.USER_BANNED,
                    String.format("您的账号因'%s'被管理员封禁,请联系客服!", user.getReason()));
        }

        user.setLastLoginTime(new java.util.Date());
        updateById(user);

        // 生成token
        session.setUserId(user.getId());
        session.setTerminal(0);
        session.setDeviceId(deviceId);
        String strJson = JSON.toJSONString(session);
        String accessToken = JwtUtil.sign(userId, strJson, jwtProps.getAccessTokenExpireIn(), jwtProps.getAccessTokenSecret());
        String refreshToken = JwtUtil.sign(userId, strJson, jwtProps.getRefreshTokenExpireIn(), jwtProps.getRefreshTokenSecret());

        LoginVO vo = new LoginVO();
        vo.setUserId(user.getId());
        vo.setAccessToken(accessToken);
        vo.setRefreshToken(refreshToken);
        vo.setAccessTokenExpiresIn(jwtProps.getAccessTokenExpireIn());
        vo.setRefreshTokenExpiresIn(jwtProps.getRefreshTokenExpireIn());

        redisTemplate.opsForHash().put(key, "status", "confirmed");
        redisTemplate.opsForHash().put(key, "vo", vo);
        redisTemplate.expire(key, 1, TimeUnit.MINUTES);

        //  保存用户登录信息
        try {
            userDeviceService.saveOrUpdateDevice(user.getId(), deviceId, deviceName, "0", cip);
        } catch (Exception e) {
            log.error("保存用户登录信息异常，{}", e.getMessage());
        }

        return vo;
    }


    @Override
    public void register(RegisterDTO dto) {
        //获取用户名。如果用的是shortUUID生成器，是有极小概率会重复的，所以需要去检查是否已经存在相同的userName。
        //ShortUUIDGenerator内的main函数有测试代码，可以观察一下碰撞的概率，这个重复是理论上的，作者测试了几千万次次都没有产生碰撞。
        //另外由于并发的问题，也有同时生成相同的id并同时去检查的并同时通过的情况，但这种情况概率极低，可以忽略不计。
        String name = RegexUtil.isEmail(dto.getEmail()) ? dto.getEmail() : dto.getPhone();
        int tryCount = 0;
        String userName;
        do {
            tryCount++;
            userName = userNameGenerator.getUserName(name);
            if (tryCount > 10) {
                throw new GlobalException(ResultCode.USERNAME_ALREADY_REGISTER);
            }
            String lockKey = "lock:register:username:" + userName;
            boolean locked = RedisUtils.tryLock(lockKey, 3000); // 尝试加锁，3秒自动释放
            if (!locked) {
                continue; // 加锁失败，可能有并发冲突，继续下一次尝试
            }
            try {
                if (isUsernameAvailable(userName)) {
                    break; // 用户名可用且加锁成功，退出循环
                }
            } finally {
                RedisUtils.unlock(lockKey);
            }
        } while (true);

        User user = new User();
        // 手机、验证码校验
        if (RegisterMode.PHONE.getCode().equals(dto.getMode())) {
            if (!RegexUtil.isPhone(dto.getPhone())) {
                throw new GlobalException("手机号格式不合法");
            }
            if (isExistPhone(dto.getPhone())) {
                throw new GlobalException("该手机号已被注册");
            }
            String superCode = (String) RedisUtils.get(RedisKey.SUPER_CAPTCHA_CODE_KEY);
            if ((null != superCode && superCode.equals(dto.getCode()))) {
                log.info("手机号超级管理员验证码校验通过");
            } else if (!captchaService.vertify(CaptchaType.SMS, dto.getPhone(), dto.getCode())) {
                throw new GlobalException("验证码错误");
            }
            user.setPhone(dto.getPhone());
        } else if (RegisterMode.EMAIL.getCode().equals(dto.getMode())) {
            if (!RegexUtil.isEmail(dto.getEmail())) {
                throw new GlobalException("邮箱格式不合法");
            }
            if (isExistEmail(dto.getEmail())) {
                throw new GlobalException("该邮箱已被注册");
            }
            String superCode = (String) RedisUtils.get(RedisKey.SUPER_CAPTCHA_CODE_KEY);
            if ((null != superCode && superCode.equals(dto.getCode()))) {
                log.info("邮箱超级管理员验证码校验通过");
            } else if (!captchaService.vertify(CaptchaType.MAIL, dto.getEmail(), dto.getCode())) {
                throw new GlobalException("验证码错误");
            }
            user.setEmail(dto.getEmail());
        } else {
            throw new GlobalException("注册方式不支持");
        }
        // 保存用户信息
        user.setUserName(userName);
        user.setNickName(dto.getNickName());
        if (StringUtils.isBlank(dto.getNickName())) {
            user.setNickName("用户" + userName.substring(0, 6));
        }
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        String defaultHeadImage = ImageUtil.getDefaultHeadImage();
        user.setHeadImage(defaultHeadImage);
        user.setSex(dto.getSex());
        user.setHeadImageThumb(defaultHeadImage);

        this.save(user);
        log.info("注册用户，用户id:{},用户名:{},昵称:{}", user.getId(), userName, dto.getNickName());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void unregister() {
        UserSession session = SessionContext.getSession();
        // 修改用户状态
        User user = this.getById(session.getUserId());
        if (user.getType().equals(UserType.OPEN_ACCOUNT.getValue())) {
            throw new GlobalException("您当前使用的是公开体验账号,不允许注销");
        }
        LambdaUpdateWrapper<User> wrapper = Wrappers.lambdaUpdate();
        wrapper.eq(User::getId, user.getId());
        wrapper.set(User::getCid, Strings.EMPTY);
        wrapper.set(User::getStatus, UserStatus.UN_REG.getValue());
        // 释放手机号和邮箱，否则会无法重新注册
        wrapper.set(User::getPhone, null);
        wrapper.set(User::getEmail, null);
        this.update(wrapper);
        // 清理redis中的cid
        String key1 = StrUtil.join(":", RedisKey.IM_USER_CID, user.getId());
        redisTemplate.delete(key1);
        // 推送消息让用户下线
        SystemMessageVO msgInfo = new SystemMessageVO();
        msgInfo.setType(MessageType.USER_UNREG.code());
        msgInfo.setSendTime(new Date());
        IMSystemMessage<SystemMessageVO> sendMessage = new IMSystemMessage<>();
        sendMessage.setRecvIds(Collections.singletonList(session.getUserId()));
        sendMessage.setData(msgInfo);
        sendMessage.setSendResult(false);
        imClient.sendSystemMessage(sendMessage);
    }

    private boolean isUsernameAvailable(String username) {
        return this.findUserByUserName(username) == null;
    }

    @Override
    public void modifyPassword(ModifyPwdDTO dto) {
        UserSession session = SessionContext.getSession();
        User user = this.getById(session.getUserId());
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new GlobalException("旧密码不正确");
        }
        if (user.getType().equals(UserType.OPEN_ACCOUNT.getValue())) {
            throw new GlobalException("您当前使用的是公开体验账号,不允许修改密码");
        }
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        this.updateById(user);
        log.info("用户修改密码，用户id:{},用户名:{},昵称:{}", user.getId(), user.getUserName(), user.getNickName());
    }

    @Override
    public void modifyUserLastOnlineTime(Long userId) {
        User user = new User();
        user.setId(userId);
        user.setLastLoginTime(new Date());
        this.updateById(user);
    }

    @Override
    public User findUserByUserName(String username) {
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(User::getUserName, username);
        return this.getOne(queryWrapper);
    }

    @Override
    public User findUserByPhone(String phone) {
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(User::getPhone, phone);
        return this.getOne(queryWrapper);
    }

    public User searchUserByPhone(String phone) {
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(User::getPhone, phone)
                .eq(User::getPhoneSearchable, 1);
        return this.getOne(queryWrapper);
    }

    @Override
    public User findUserByEmail(String email) {
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(User::getEmail, email);
        return this.getOne(queryWrapper);
    }

    public User searchUserByEmail(String email) {
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(User::getEmail, email)
                .eq(User::getEmailSearchable, 1);
        return this.getOne(queryWrapper);
    }

    @Override
    public User findUserByLoginName(String loginName) {
        // 优先用户名登陆
//        User user = findUserByUserName(loginName);  //用户名登陆已去掉
        User user = null;
        // 手机号登陆
        if (RegexUtil.isPhone(loginName)) {
            user = findUserByPhone(loginName);
        }
        // 邮箱登陆
        if (Objects.isNull(user) && RegexUtil.isEmail(loginName)) {
            user = findUserByEmail(loginName);
        }
        return user;
    }

    @Override
    public Boolean isExistPhone(String phone) {
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(User::getPhone, phone);
        return this.exists(queryWrapper);
    }

    @Override
    public Boolean isExistEmail(String email) {
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(User::getEmail, email);
        return this.exists(queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(UserDTO dto) {

        // 1️⃣ 获取当前登录用户信息 & 基本校验
        UserSession session = SessionContext.getSession();
        Long sessionUserId = session.getUserId();

        if (!sessionUserId.equals(dto.getId())) {
            throw new GlobalException("不允许修改其他用户的信息!");
        }

        // 敏感词过滤（昵称）
        if (StrUtil.isNotBlank(dto.getNickName()) &&
                !dto.getNickName().equals(sensitiveFilterUtil.filter(dto.getNickName()))) {
            throw new GlobalException("昵称包含敏感字符");
        }

        User user = this.getById(sessionUserId);
        if (user == null) {
            throw new GlobalException("用户不存在");
        }

        // 2️⃣ 头像更新（原图 + 缩略图）
        if (StrUtil.isNotBlank(dto.getHeadImageThumb())
                && !dto.getHeadImageThumb().equals(user.getHeadImageThumb())) {

            // 更新好友表头像
            friendService.update(
                    Friend.builder().friendHeadImage(dto.getHeadImageThumb()).build(),
                    new LambdaUpdateWrapper<Friend>().eq(Friend::getFriendId, sessionUserId)
            );

            // 更新群成员头像
            groupMemberService.update(
                    GroupMember.builder().headImage(dto.getHeadImageThumb()).build(),
                    new LambdaUpdateWrapper<GroupMember>().eq(GroupMember::getUserId, sessionUserId)
            );

            user.setHeadImage(StrUtil.blankToDefault(dto.getHeadImage(), user.getHeadImage()));
            user.setHeadImageThumb(dto.getHeadImageThumb());
        }

        // 3️⃣ 用户名更新（仅一次机会）
        if (StrUtil.isNotBlank(dto.getUserName())
                && !dto.getUserName().equals(user.getUserName())) {

            if (user.getIsModifiedUsername() == 1) {
                throw new GlobalException("用户名已修改过");
            }
            if (!isUsernameAvailable(dto.getUserName())) {
                throw new GlobalException("用户名已存在");
            }
            user.setUserName(dto.getUserName());
            user.setIsModifiedUsername(1);
        }

        // 4️⃣ 昵称更新（同步 Friend / GroupMember）
        if (StrUtil.isNotBlank(dto.getNickName())
                && !dto.getNickName().equals(user.getNickName())) {

            friendService.update(
                    Friend.builder().friendNickName(dto.getNickName()).build(),
                    new LambdaUpdateWrapper<Friend>()
                            .eq(Friend::getUserId, sessionUserId)
                            .eq(Friend::getFriendId, sessionUserId)
            );

            groupMemberService.update(
                    GroupMember.builder().userNickName(dto.getNickName()).build(),
                    new LambdaUpdateWrapper<GroupMember>().eq(GroupMember::getUserId, sessionUserId)
            );

            user.setNickName(dto.getNickName());
        }

        // 5️⃣ 其余简单字段（仅当非空且变更时才 set）
        setIfChanged(user::getSex, user::setSex, dto.getSex());
        setIfChanged(user::getSignature, user::setSignature, dto.getSignature());
        setIfChanged(user::getOnlinePermStatus,
                user::setOnlinePermStatus, dto.getOnlinePermStatus());
        setIfChanged(user::getIsManualApprove,
                user::setIsManualApprove, dto.getIsManualApprove());

        // 6️⃣ ➕ 新增字段：生日 / 国家
        setIfChanged(user::getBirthday, user::setBirthday, dto.getBirthday());
        setIfChanged(user::getCountry, user::setCountry, dto.getCountry());

        // 7️⃣ 持久化修改
        this.updateById(user);
        log.info("用户信息已更新: {}", user);
    }

    /**
     * 通用字段变更检测 & 赋值工具
     * - 值为空（null）直接忽略
     * - 新值与旧值相等则忽略
     */
    private <T> void setIfChanged(Supplier<T> getter, Consumer<T> setter, T newVal) {
        if (newVal != null && !Objects.equals(getter.get(), newVal)) {
            setter.accept(newVal);
        }
    }

    @Override
    public void updateGroup(UserGroupPermDTO dto) {
        UserSession session = SessionContext.getSession();
        Long sessionUserId = session.getUserId();

        User user = this.getById(sessionUserId);
        if (user == null) {
            throw new GlobalException("用户不存在");
        }

        // ========== 设置拉群权限 ==========
        if (dto.getGroupPermStatus() != null) {
            Friend friendPerm = new Friend();
            friendPerm.setGroupPermStatus(dto.getGroupPermStatus());
//            friendPerm.setUserId(sessionUserId);
            friendPerm.setFriendId(sessionUserId);

            user.setGroupPermStatus(dto.getGroupPermStatus());

//            if (GroupPermStatus.FRIEND.getCode().equals(dto.getGroupPermStatus())) {
            String yes = Optional.ofNullable(dto.getGroupPermYesUser())
                    .orElse(Collections.emptyList())
                    .stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));

            String no = Optional.ofNullable(dto.getGroupPermNoUser())
                    .orElse(Collections.emptyList())
                    .stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));

            friendPerm.setGroupPermYesUser(yes);
            friendPerm.setGroupPermNoUser(no);

            user.setGroupPermYesUser(yes);
            user.setGroupPermNoUser(no);
//            }

            friendService.update(friendPerm, new LambdaUpdateWrapper<Friend>()
//                    .eq(Friend::getUserId, sessionUserId)
                    .eq(Friend::getFriendId, sessionUserId));
            this.updateById(user);
        }
    }

    @Override
    public void updateHeadImage(UserHeadImageDTO dto) {
        UserSession session = SessionContext.getSession();
        Long sessionUserId = session.getUserId();
//        if (!sessionUserId.equals(dto.getId())) {
//            throw new GlobalException("不允许修改其他用户的信息!");
//        }

        User user = this.getById(sessionUserId);
        if (user == null) {
            throw new GlobalException("用户不存在");
        }

        // ========== 更新头像 ==========
        if (StrUtil.isNotBlank(dto.getHeadImageThumb()) && !dto.getHeadImageThumb().equals(user.getHeadImageThumb())) {
            Friend friendAvatar = new Friend();
            friendAvatar.setFriendHeadImage(dto.getHeadImageThumb());
            friendService.update(friendAvatar, new LambdaUpdateWrapper<Friend>()
                    .eq(Friend::getFriendId, sessionUserId));

            GroupMember groupAvatar = new GroupMember();
            groupAvatar.setHeadImage(dto.getHeadImageThumb());
            groupMemberService.update(groupAvatar, new LambdaUpdateWrapper<GroupMember>()
                    .eq(GroupMember::getUserId, sessionUserId));

            user.setHeadImage(dto.getHeadImage()); // 原图
            user.setHeadImageThumb(dto.getHeadImageThumb()); // 缩略图
            this.updateById(user);
        }
    }

    @Override
    public UserVO findUserById(Long id) {
        UserSession session = SessionContext.getSession();
        User user = this.getById(id);
        if (Objects.isNull(user)) {
            throw new GlobalException("用户不存在");
        }
        UserVO vo = BeanUtils.copyProperties(user, UserVO.class);
        if (StringUtils.isNotBlank(user.getGroupPermYesUser())) {
            assert vo != null;
            vo.setGroupPermYesUser(Arrays.stream(user.getGroupPermYesUser().split(",")).map(Long::valueOf).collect(Collectors.toList()));
        }
        if (StringUtils.isNotBlank(user.getGroupPermNoUser())) {
            assert vo != null;
            vo.setGroupPermNoUser(Arrays.stream(user.getGroupPermNoUser().split(",")).map(Long::valueOf).collect(Collectors.toList()));
        }
        assert vo != null;
        vo.setOnline(imClient.isOnline(id));
        vo.setIsInBlacklist(userBlacklistService.isInBlacklist(session.getUserId(), id));
        Boolean isFriend = friendService.isFriend(session.getUserId(), id);
        vo.setIsFriend(isFriend);
        if (isFriend) {
            Long notifyExpireTs = friendService.getNotifyExpireTs(session.getUserId(), id);
            vo.setNotifyExpireTs(notifyExpireTs == null ? 0L : notifyExpireTs);
        } else {
            vo.setNotifyExpireTs(0L);
        }
        vo.setLastLoginTime(user.getLastLoginTime().getTime());
        return vo;
    }

    @Override
    public User findUserInfoById(Long id) {
        User user = this.getById(id);
        if (Objects.isNull(user)) {
            throw new GlobalException("用户不存在");
        }
        return user;
    }

    @Override
    public List<UserVO> findUserByName(String name) {
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.like(User::getUserName, name).or().like(User::getNickName, name).last("limit 20");
        List<User> users = this.list(queryWrapper);
        return convert(users);
    }

    public List<UserVO> searchUserByName(String name) {
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.nested(qw -> qw
                .like(User::getUserName, name)
                .eq(User::getUserNameSearchable, 1)
        );
        queryWrapper.or();
        queryWrapper.nested(qw -> qw
                .like(User::getNickName, name)
                .eq(User::getNickNameSearchable, 1)
        );
        queryWrapper.last("LIMIT 20");
        List<User> users = this.list(queryWrapper);
        return convert(users);
    }

    @Override
    public List<UserVO> search(String name) {
        if (RegexUtil.isPhone(name)) {
            // 查询手机号
            User user = searchUserByPhone(name);
            if (!Objects.isNull(user)) {
                return convert(List.of(user));
            }
        }
        if (RegexUtil.isEmail(name)) {
            // 查询邮箱
            User user = searchUserByEmail(name);
            if (!Objects.isNull(user)) {
                return convert(List.of(user));
            }
        } else {
            // 查询用户名和昵称
            return searchUserByName(name);
        }
        return Lists.newArrayList();
    }

    @Override
    public List<OnlineTerminalVO> getOnlineTerminals(String userIds) {
        List<Long> userIdList = Arrays.stream(userIds.split(",")).map(Long::parseLong).collect(Collectors.toList());
        // 查询在线的终端
        Map<Long, List<IMTerminalType>> terminalMap = imClient.getOnlineTerminal(userIdList);
        // 组装vo
        List<OnlineTerminalVO> vos = new LinkedList<>();
        terminalMap.forEach((userId, types) -> {
            List<Integer> terminals = types.stream().map(IMTerminalType::code).collect(Collectors.toList());
            vos.add(new OnlineTerminalVO(userId, terminals));
        });
        return vos;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void reportCid(String cid) {
        UserSession session = SessionContext.getSession();
        // 清理该设备以前登录过的cid
        LambdaQueryWrapper<User> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(User::getCid, cid);
        wrapper.ne(User::getId, session.getUserId());
        List<User> users = this.list(wrapper);
        users.forEach(user -> {
            // 清理redis中的cid
            String key1 = StrUtil.join(":", RedisKey.IM_USER_CID, user.getId());
            redisTemplate.delete(key1);
            // 清空通知会话信息
            String key2 = StrUtil.join(":", RedisKey.IM_NOTIFY_OFFLINE_SESSION, user.getId());
            redisTemplate.delete(key2);
            user.setCid(Strings.EMPTY);
            this.updateById(user);
        });
        // 保存当前用户的cid
        User user = this.getById(session.getUserId());
        user.setCid(cid);
        this.updateById(user);
        // 缓存cid到redis
        String key = StrUtil.join(":", RedisKey.IM_USER_CID, user.getId());
        redisTemplate.opsForValue().set(key, cid, notifyProps.getActiveDays(), TimeUnit.DAYS);
        // 清空通知会话信息
        String key2 = StrUtil.join(":", RedisKey.IM_NOTIFY_OFFLINE_SESSION, user.getId());
        redisTemplate.delete(key2);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void removeCid() {
        UserSession session = SessionContext.getSession();
        User user = this.getById(session.getUserId());
        user.setCid(Strings.EMPTY);
        this.updateById(user);
        // 清理redis中的cid
        String key1 = StrUtil.join(":", RedisKey.IM_USER_CID, user.getId());
        redisTemplate.delete(key1);
    }

    @Override
    public void setManualApprove(Boolean enabled) {
        UserSession session = SessionContext.getSession();
        LambdaUpdateWrapper<User> wrapper = Wrappers.lambdaUpdate();
        wrapper.eq(User::getId, session.getUserId());
        wrapper.set(User::getIsManualApprove, enabled);
        this.update(wrapper);
    }

    @Transactional
    @Override
    public void bindPhone(BindPhoneDTO dto) {
        UserSession session = SessionContext.getSession();
        User user = getById(session.getUserId());
        if (StrUtil.isNotEmpty(user.getPhone())) {
            throw new GlobalException("您已绑定了手机号,不可重复绑定");
        }
        if (!RegexUtil.isPhone(dto.getPhone())) {
            throw new GlobalException("手机号格式不合法");
        }
        if (isExistPhone(dto.getPhone())) {
            throw new GlobalException("该手机号已被注册");
        }
        if (!captchaService.vertify(CaptchaType.SMS, dto.getPhone(), dto.getCode())) {
            throw new GlobalException("验证码错误");
        }
        user.setPhone(dto.getPhone());
        this.updateById(user);
    }

    @Transactional
    @Override
    public void bindEmail(BindEmailDTO dto) {
        UserSession session = SessionContext.getSession();
        User user = getById(session.getUserId());
        if (StrUtil.isNotEmpty(user.getEmail())) {
            throw new GlobalException("您已绑定了邮箱,不可重复绑定");
        }
        if (!RegexUtil.isEmail(dto.getEmail())) {
            throw new GlobalException("邮箱格式不合法");
        }
        if (isExistEmail(dto.getEmail())) {
            throw new GlobalException("该邮箱已被注册");
        }
        if (!captchaService.vertify(CaptchaType.MAIL, dto.getEmail(), dto.getCode())) {
            throw new GlobalException("验证码错误");
        }
        user.setEmail(dto.getEmail());
        this.updateById(user);
        redisTemplate.delete("user:" + session.getUserId());
    }

    @Override
    public void modifyPasswordByCode(ModifyPwdCodeDTO dto) {
        if (RegisterMode.PHONE.getCode().equals(dto.getMode())) {
            User user = findUserByPhone(dto.getPhone());
            String superCode = (String) RedisUtils.get(RedisKey.SUPER_CAPTCHA_CODE_KEY);
            if ((null != superCode && superCode.equals(dto.getCode()))) {
                log.info("手机号超级管理员验证码校验通过");
            } else if (!captchaService.vertify(CaptchaType.SMS, dto.getPhone(), dto.getCode())) {
                throw new GlobalException("验证码错误");
            }
            user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
            this.updateById(user);
        } else if (RegisterMode.EMAIL.getCode().equals(dto.getMode())) {
            User user = findUserByEmail(dto.getEmail());
            String superCode = (String) RedisUtils.get(RedisKey.SUPER_CAPTCHA_CODE_KEY);
            if ((null != superCode && superCode.equals(dto.getCode()))) {
                log.info("邮箱超级管理员验证码校验通过");
            } else if (!captchaService.vertify(CaptchaType.MAIL, dto.getEmail(), dto.getCode())) {
                throw new GlobalException("验证码错误");
            }
            user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
            this.updateById(user);
        } else {
            throw new GlobalException("未知方式不支持");
        }
    }

    @Override
    public void setSearchable(FindMyWayDto dto) {
        UserSession session = SessionContext.getSession();
        User user = getById(session.getUserId());
        user.setUserNameSearchable(dto.getUsername());
        user.setNickNameSearchable(dto.getNickname());
        user.setPhoneSearchable(dto.getPhone());
        user.setEmailSearchable(dto.getEmail());
        this.updateById(user);
    }

    @Override
    public User getUserById(Long userId) {
        return baseMapper.selectById(userId);
    }

    @Override
    public Map<String, Object> generateQrCode(String deviceId, String deviceName, String cip) {
        String uuid = IdUtil.simpleUUID();
        String key = StrUtil.join(":", RedisKey.QR_SESSION_PREFIX, uuid);
        redisTemplate.opsForHash().put(key, "status", "waiting");
        redisTemplate.opsForHash().put(key, "deviceId", deviceId);
        redisTemplate.opsForHash().put(key, "deviceName", deviceName);
        redisTemplate.opsForHash().put(key, "cip", cip);
        redisTemplate.expire(key, 2, TimeUnit.MINUTES);

        return Map.of("uuid", uuid, "expire", 120);
    }


    List<UserVO> convert(List<User> users) {
        List<Long> userIds = users.stream().map(User::getId).collect(Collectors.toList());
        List<Long> onlineUserIds = imClient.getOnlineUser(userIds);
        return users.stream().map(u -> {
            UserVO vo = BeanUtils.copyProperties(u, UserVO.class);
            vo.setOnline(onlineUserIds.contains(u.getId()));
            if (StringUtils.isNotBlank(u.getGroupPermYesUser())) {
                vo.setGroupPermYesUser(Arrays.stream(u.getGroupPermYesUser().split(",")).map(Long::valueOf).collect(Collectors.toList()));
            }
            if (StringUtils.isNotBlank(u.getGroupPermNoUser())) {
                vo.setGroupPermNoUser(Arrays.stream(u.getGroupPermNoUser().split(",")).map(Long::valueOf).collect(Collectors.toList()));
            }
            return vo;
        }).collect(Collectors.toList());
    }



}
