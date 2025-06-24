package com.bx.imcommon.enums;

import lombok.AllArgsConstructor;

/**
 * 0-9: 真正的消息，需要存储到数据库
 * 10-19: 状态类消息: 撤回、已读、回执
 * 20-29: 提示类消息: 在会话中间显示的提示
 * 30-39: UI交互类消息: 显示加载状态等
 * 40-49: 操作交互类消息: 语音通话、视频通话消息等
 * 50-69: 后台操作类消息: 用户封禁、群组封禁等
 * 70-79: 好友申请类消息
 * 80-89: 好友状态变化消息
 * 90-99: 群状态变化消息
 * 100-199: 单人语音通话rtc信令
 * 200-299: 多人语音通话rtc信令
 *
 */
@AllArgsConstructor
public enum MessageType {
    TEXT(0, "文字消息"),
    IMAGE(1, "图片消息"),
    FILE(2, "文件消息"),
    AUDIO(3, "语音消息"),
    VIDEO(4, "视频消息"),
    USER_CARD(5,"个人名片"),
    GROUP_CARD(6,"群名片"),
    FORWARD_THE_MESSAGE(7,"转发消息"),
    RECALL(10, "撤回"),
    READED(11, "已读"),
    RECEIPT(12, "消息已读回执"),
    EDIT(13, "消息已编辑"),
    DELETE_MESSAGE(14, "删除消息"),
    TIP_TIME(20,"时间提示"),
    TIP_TEXT(21,"文字提示"),
    LOADING(30,"加载中标记"),
    ACT_RT_VOICE(40,"语音通话"),
    ACT_RT_VIDEO(41,"视频通话"),
    USER_BANNED(50,"用户封禁"),
    GROUP_BANNED(51,"群聊封禁"),
    GROUP_UNBAN(52,"群聊解封"),
    SYSTEM_MESSAGE(53,"系统消息"),
    USER_UNREG(54,"账号注销"),
    FRIEND_REQ_APPLY(70,"好友添加申请"),
    FRIEND_REQ_APPROVE(71,"同意好友申请"),
    FRIEND_REQ_REJECT(72,"拒绝好友申请"),
    FRIEND_REQ_RECALL(73,"撤回好友申请"),
    FRIEND_NEW(80, "新增好友"),
    FRIEND_DEL(81, "删除好友"),
    FRIEND_ONLINE(82, "好友在线状态变化"),
    GROUP_NEW(90, "新增群聊"),
    GROUP_DEL(91, "删除群聊"),

    GROUP_MESSAGE_UPDATE(401, "消息更新"),
    GROUP_MESSAGE_DEL(402, "消息删除"),
    //群聊权限变化
    GROUP_ROOM_PERM(301, "关闭语音/视频通话"),
    GROUP_INTERIM_PERM(302, "禁止发起临时会话"),
    GROUP_MANAGER_INVITE_CHECK(303, "管理员验证拉人入群"),


    GROUP_TOP_MESSAGE(92, "消息置顶"),
    RTC_SETUP_VOICE(100, "语音呼叫"),
    RTC_SETUP_VIDEO(101, "视频呼叫"),
    RTC_ACCEPT(102, "接受"),
    RTC_REJECT(103, "拒绝"),
    RTC_CANCEL(104, "取消呼叫"),
    RTC_FAILED(105, "呼叫失败"),
    RTC_HANDUP(106, "挂断"),
    RTC_OFFER(107,"推送offer信息"),
    RTC_ANSWER(108,"推送answer信息"),
    RTC_CANDIDATE(109, "同步candidate"),
    RTC_GROUP_SETUP(200,"发起群视频通话"),
    RTC_GROUP_ACCEPT(201,"接受通话呼叫"),
    RTC_GROUP_REJECT(202,"拒绝通话呼叫"),
    RTC_GROUP_FAILED(203,"拒绝通话呼叫"),
    RTC_GROUP_CANCEL(204,"取消通话呼叫"),
    RTC_GROUP_QUIT(205,"退出通话"),
    RTC_GROUP_INVITE(206,"邀请进入通话"),
    RTC_GROUP_JOIN(207,"主动进入通话"),
    RTC_GROUP_OFFER(208,"推送offer信息"),
    RTC_GROUP_ANSWER(209,"推送answer信息"),
    RTC_GROUP_CANDIDATE(210,"同步candidate"),
    RTC_GROUP_DEVICE(211,"设备操作"),

    //朋友圈消息
    CHAT_TOPIC_SEND_MESSAGE(501,"发帖"),
    CHAT_TOPIC_REDIS_NOTICE(502,"拉取通知数量"),
    CHAT_TOPIC_TOPIC_RED(503, "帖子_小红点"),
    CHAT_TOPIC_TOPIC_REPLY(504, "帖子_回复"),
    ;

    private final Integer code;

    private final String desc;

    public Integer code(){
        return code;
    }
    public String desc(){
        return desc;
    }

    public static MessageType fromCode(Integer code) {
        for (MessageType typeEnum : values()) {
            if (typeEnum.code.equals(code)) {
                return typeEnum;
            }
        }
        return null;
    }

    public Boolean isAct(){
        return this.code >=40 && this.code < 50;
    }

    public Boolean isNormal(){
        return this.code >=0 && this.code < 10;
    }

    public Boolean isRequest(){
        return this.code >=70 && this.code < 80;
    }

    public Boolean isTip() {
        return this.code >=20 && this.code < 30;
    }
}
