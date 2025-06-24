package com.bx.implatform.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * <p>
 * 投诉
 * </p>
 *
 * @author blue
 * @since 2022-10-22
 */
@Data
@TableName("im_complaint")
public class Complaint {


    /**
     * 主键
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 上传证据（多个用英文逗号分割）
     */
    private String fileUrl;

    /**
     * 状态（1已提交，2处理中，3已处理）
     */
    private Long status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 处理完成时间
     */
    private Date overTime;


}
