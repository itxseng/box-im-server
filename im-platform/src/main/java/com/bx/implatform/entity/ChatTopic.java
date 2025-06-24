package com.bx.implatform.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.util.Date;

/**
 * 主题对象 chat_topic
 *
 * @author Blue
 * @date 2025-05-29
 */
@Data
@TableName("chat_topic")
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatTopic {

    @Serial
    private static final long serialVersionUID = 1L;

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
     * 类型（1文字/表情  2图片/拍照  3视频）
     */
    private Integer topicType;

    /**
     * 内容
     */
    private String content;

    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 经纬度
     */
    private String location;

    /**
     * 纬度
     */
    private String latitude;

    /**
     * 经度
     */
    private String longitude;

    /**
     * 地址
     */
    private String address;

    /**
     * 查看类型（1开放，2私密，3部分可见，4不给谁看）
     */
   private Integer openType;

   @TableField(exist = false)
   private Long page;

   @TableField(exist = false)
   private Long pageSize;


}
