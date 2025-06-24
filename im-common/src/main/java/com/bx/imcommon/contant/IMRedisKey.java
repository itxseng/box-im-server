package com.bx.imcommon.contant;

public final class IMRedisKey {


    /**
     * im-server最大id,从0开始递增
     */
    public static final String  IM_MAX_SERVER_ID = "im:max_server_id";
    /**
     * 用户ID所连接的IM-server的ID
     */
    public static final String  IM_USER_SERVER_ID = "im:user:server_id";
    /**
     * 系统消息队列
     */
    public static final String IM_MESSAGE_SYSTEM_QUEUE = "im:message:system";
    /**
     * 私聊消息队列
     */
    public static final String IM_MESSAGE_PRIVATE_QUEUE = "im:message:private";
    /**
     * 群聊消息队列
     */
    public static final String IM_MESSAGE_GROUP_QUEUE = "im:message:group";

    /**
     * 系统消息发送结果队列
     */
    public static final String IM_RESULT_SYSTEM_QUEUE = "im:result:system";
    /**
     * 私聊消息发送结果队列
     */
    public static final String IM_RESULT_PRIVATE_QUEUE = "im:result:private";
    /**
     * 群聊消息发送结果队列
     */
    public static final String IM_RESULT_GROUP_QUEUE = "im:result:group";

    /**
     * 用户事件队列
     */
    public static final String IM_USER_EVENT_QUEUE = "im:user:event";

    /**
     * 朋友圈消息发送结果队列
     */
    public static final String IM_RESULT_CHAT_QUEUE = "im:result:chat";

    /**
     * 用户设备id
     */
    public static final CharSequence IM_USER_DEVICE_ID = "im:user:device_id" ;


}
