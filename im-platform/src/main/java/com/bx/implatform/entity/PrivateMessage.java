package com.bx.implatform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.bx.implatform.util.MessageIdGenerator;
import lombok.Data;

import java.util.Date;

/**
 * <p>
 *
 * </p>
 *
 * @author blue
 * @since 2022-10-01
 */
@Data
@TableName("im_private_message")
public class PrivateMessage {

    /**
     * id
     */
    private Long id;

    /**
     * 发送用户id
     */
    private Long sendId;

    /**
     * 接收用户id
     */
    private Long recvId;

    /**
     * 发送内容
     */
    private String content;

    /**
     * 是否编辑
     */
    private boolean isEdited;

    /**
     * 编辑后的内容
     */
    private String contentEdit;

    /**
     * 消息类型 MessageType
     */
    private Integer type;

    /**
     * 引用消息id
     */
    private Long quoteMessageId;

    /**
     * 状态
     */
    private Integer status;


    /**
     * 发送时间
     */
    private Date sendTime;

    /**
     * 客户端消息id
     */
    private String clientMsgId;

    public PrivateMessage() {
        this.id = MessageIdGenerator.nextId();
    }
}
