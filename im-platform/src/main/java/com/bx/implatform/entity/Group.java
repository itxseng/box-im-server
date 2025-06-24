package com.bx.implatform.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 群
 *
 * @author blue
 * @since 2022-10-31
 */
@Data
@TableName("im_group")
public class Group {

    /**
     * id
     */
    @TableId
    private Long id;

    /**
     * 群名字
     */
    private String name;

    /**
     * 群主id
     */
    private Long ownerId;

    /**
     * 群头像
     */
    private String headImage;

    /**
     * 群头像缩略图
     */
    private String headImageThumb;

    /**
     * 群公告
     */
    private String notice;

    /**
     * 是否开启全体禁言
     */
    private Boolean isMuted;

    /**
     * 置顶消息id
     */
    private Long topMessageId;

    /**
     * 置顶消息ids
     */
    private String topMessageIds;

    /**
     * 是否被封禁
     */
    private Boolean isBanned;

    /**
     * 被封禁原因
     */
    private String reason;

    /**
     * 创建时间
     */
    private Date createdTime;

    /**
     * 是否已解散
     */
    private Boolean dissolve;


    /**
     * 允许普通成员发起临时会话（false否  true是）
     */
    private Boolean interimPerm;

    /**
     * 允许查看群成员（false否  true是）
     */
    private Boolean queryMemberPerm;

    /**
     * 加群权限（1不限制加入  2群成员可以拉人  3只能管理员拉人  4群成员拉人需要管理员验证）
     */
    private Integer addGroupPerm;

    /**
     * 查找方式（1允许查找  2不允许查找）
     */
    private Integer queryGroupPerm;

    /**
     * 是否关闭群语音/视频通话（false否  true是）
     */
    private Boolean roomGroupPerm;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private Date updateTime;

    /**
     * 群类型（0普通群  1超级群）
     */
    private Integer groupType;
}
