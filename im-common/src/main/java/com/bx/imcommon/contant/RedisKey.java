package com.bx.imcommon.contant;

public final class RedisKey {

    /**
     *  用户状态 无值:空闲  1:正在忙
     */
    public static final String IM_USER_STATE = "im:user:state";

    /**
     *  用户cid
     */
    public static final String IM_USER_CID = "im:user:cid";

    /**
     *  验证码
     */
    public static final String IM_CAPTCHA = "im:captcha";

    /**
     *  图形验证码
     */
    public static final String IM_CAPTCHA_IMAGE = "im:captcha:img";

    /**
     *  短信验证码
     */
    public static final String IM_CAPTCHA_SMS = "im:captcha:sms";

    /**
     *  邮箱验证码
     */
    public static final String IM_CAPTCHA_MAIL = "im:captcha:mail";

    /**
     * 已读群聊消息位置(已读最大id)
     */
    public static final String IM_GROUP_READED_POSITION = "im:readed:group:position";
    /**
     * 已读系统消息位置(已读最大seqNo)
     */
    public static final String IM_SYSTEM_READED_POSITION = "im:readed:system:position";
    /**
     * 离线通知
     */
    public static final String IM_NOTIFY_OFFLINE_SESSION = "im:notify:offline";

    /**
     *  系统消息推送序列号
     */
    public static final String IM_SM_TASK_SEQ = "im:task:sm:seq";
    /**
     * webrtc 单人通话
     */
    public static final String IM_WEBRTC_PRIVATE_SESSION = "im:webrtc:private:session";
    /**
     * webrtc 群通话
     */
    public static final String IM_WEBRTC_GROUP_SESSION = "im:webrtc:group:session";

    /**
     * 用户被封禁消息队列
     */
    public static final String IM_QUEUE_USER_BANNED = "im:queue:user:banned";

    /**
     * 群聊被封禁消息队列
     */
    public static final String IM_QUEUE_GROUP_BANNED = "im:queue:group:banned";

    /**
     * 群聊解封消息队列
     */
    public static final String IM_QUEUE_GROUP_UNBAN = "im:queue:group:unban";

    /**
     * 缓存是否好友：bool
     */
    public static final String IM_CACHE_FRIEND = "im:cache:friend";
    /**
     * 缓存群聊信息
     */
    public static final String IM_CACHE_GROUP =  "im:cache:group";
    /**
     * 缓存是否在黑名单中：bool
     */
    public static final String IM_CACHE_BLACKLIST = "im:cache:blacklist";
    /**
     * 缓存群聊成员id
     */
    public static final String IM_CACHE_GROUP_MEMBER_ID = "im:cache:group_member_ids";
    /**
     * 分布式锁-群通话
     */
    public static final String IM_LOCK_RTC_GROUP =  "im:lock:rtc:group";
    /**
     * 分布式锁-系统消息推送
     */
    public static final String IM_LOCK_SM_TASK =  "im:lock:task:sm";

    /**
     * 分布式锁-清理过期文件
     */
    public static final String IM_LOCK_FILE_TASK =  "im:lock:task:file";
    /**
     * 重复提交
     */
    public static final String IM_REPEAT_SUBMIT = "im:repeat:submit";

    /**
     * 超级验证码 redis key
     */
    public static final String SUPER_CAPTCHA_CODE_KEY = "sys_config::im:super_captcha_codes";
    /**
     * 查看群聊历史消息条数 redis key
     */
    public static final String GROUP_MESSAGE_HISTORY_LIMIT = "sys_config::im:group_message_history_limit";


    /**
     * 朋友圈权限 : 好友|陌生人 : 用户ID
     */
    public static final String MOMENTS_PERMISSIONS = "moments_permissions:{}:{}";

    /**
     * 朋友圈查看 : 能不看谁|谁不能看我 : 用户ID
     */
    public static final String MOMENTS_SEE_PERMISSIONS = "moments_see_permissions:{}:{}";


    /**
     * 30天过期时间
     */
    public static final Long MOMENTS_EXPIRE = 2592000L;

    /**
     * 2分钟过期时间
     */
    public static final Long TOW_SECOND_EXPIRE = 120L;

    /**
     * 用户扫码登录信息
     */
    public static final CharSequence QR_SESSION_PREFIX = "LOGIN_QR_SESSION" ;



    //***********朋友圈相关***********//

    /**
     * 帖子_发帖
     */
    public static final String SEND_TOPIC = "topic:sendTopic:";

    /**
     * 帖子_通知
     */
    public static final String REDIS_TOPIC_NOTICE = "topic:notice:";

    /**
     * 帖子_回复
     */
    public static final String REDIS_TOPIC_REPLY = "topic:reply:";
}
