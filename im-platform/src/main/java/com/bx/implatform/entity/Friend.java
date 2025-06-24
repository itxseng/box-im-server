package com.bx.implatform.entity;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * <p>
 * 好友
 * </p>
 *
 * @author blue
 * @since 2022-10-22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("im_friend")
public class Friend{

    /**
     * id
     */
    @TableId
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 好友id
     */
    private Long friendId;

    /**
     * 用户昵称
     */
    private String friendNickName;

    /**
     * 用户头像
     */
    private String friendHeadImage;

    /**
     * 备注昵称
     */
    private String remarkNickName;

    /**
     * 是否已删除
     */
    private Boolean deleted;

    /**
     * 是否标记*
     */
    private Boolean tag;

    /**
     * 创建时间
     */
    private Date createdTime;
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
     * 好友消息通知过期时间
     */
    private Long notifyExpireTs;

    public String getShowNickName() {
        return StrUtil.blankToDefault(remarkNickName, friendNickName);
    }


}
