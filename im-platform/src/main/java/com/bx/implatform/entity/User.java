package com.bx.implatform.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * <p>
 * 用户
 * </p>
 *
 * @author blue
 * @since 2022-10-01
 */
@Data
@TableName("im_user")
public class User {

    /**
     * id
     */
    @TableId
    private Long id;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户昵称
     */
    private String nickName;

    /**
     * 用户头像
     */
    private String headImage;

    /**
     * 用户头像缩略图
     */
    private String headImageThumb;

    /**
     * 密码(明文)
     */
    private String password;

    /**
     * 性别 0:男 1::女
     */
    private Integer sex;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 个性签名
     */
    private String signature;

    /**
     * 账号是否被封禁
     */
    private Boolean isBanned;

    /**
     * 账号被封禁原因
     */
    private String reason;


    /**
     * 是否手动验证好友请求
     */
    private Boolean isManualApprove;

    /**
     * 客户端id,用于uni-push推送
     */
    private String cid;

    /**
     * 最后登录时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastLoginTime;

    /**
     * 状态 0:正常 1:已注销
     */
    private Integer status;

    /**
     * 创建时间(注册时间)
     */
    private Date createdTime;

    /**
     * 账号类型 1:普通用户 2:测试账户
     */
    private Integer type;
    /**
     * 拉群权限（1所有人  2联系人  3没有人）
     */
    private Integer groupPermStatus;

    /**
     * 总是允许用户id集合，英文逗号分割
     */
    private String groupPermYesUser;

    /**
     * 永不允许用户id集合，英文逗号分割
     */
    private String groupPermNoUser;

    /**
     * 在线显示 1:所有人 2:联系人 3:无
     */
    private Integer onlinePermStatus;

    /**
     * 区号
     */
    private String regionCode;

    /**
     * 是否修改过user_name
     */
    private Integer isModifiedUsername;

    /**
     * 是否可以用户名搜索
     */
    private Integer userNameSearchable;

    private Integer phoneSearchable;

    private Integer emailSearchable;

    private Integer nickNameSearchable;

    private Date birthday;

    private String country;
}
