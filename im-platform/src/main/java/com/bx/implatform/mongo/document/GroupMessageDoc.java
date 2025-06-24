package com.bx.implatform.mongo.document;

import com.bx.implatform.entity.GroupMessage;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document(collection = "im_group_message")
public class GroupMessageDoc {
    @Id
    private Long id;
    private Long groupId;
    private Long sendId;
    private String sendNickName;
    private String recvIds;
    private String atUserIds;
    private String content;
    private Integer type;
    private Long quoteMessageId;
    private Boolean receipt;
    private Boolean receiptOk;
    private Integer status;
    private Date sendTime;

    public static GroupMessageDoc fromEntity(GroupMessage message) {
        GroupMessageDoc doc = new GroupMessageDoc();
        doc.setId(message.getId());
        doc.setGroupId(message.getGroupId());
        doc.setSendId(message.getSendId());
        doc.setSendNickName(message.getSendNickName());
        doc.setRecvIds(message.getRecvIds());
        doc.setAtUserIds(message.getAtUserIds());
        doc.setContent(message.getContent());
        doc.setType(message.getType());
        doc.setQuoteMessageId(message.getQuoteMessageId());
        doc.setReceipt(message.getReceipt());
        doc.setReceiptOk(message.getReceiptOk());
        doc.setStatus(message.getStatus());
        doc.setSendTime(message.getSendTime());
        return doc;
    }

    public GroupMessage toEntity() {
        GroupMessage msg = new GroupMessage();
        msg.setId(this.id);
        msg.setGroupId(this.groupId);
        msg.setSendId(this.sendId);
        msg.setSendNickName(this.sendNickName);
        msg.setRecvIds(this.recvIds);
        msg.setAtUserIds(this.atUserIds);
        msg.setContent(this.content);
        msg.setType(this.type);
        msg.setQuoteMessageId(this.quoteMessageId);
        msg.setReceipt(this.receipt);
        msg.setReceiptOk(this.receiptOk);
        msg.setStatus(this.status);
        msg.setSendTime(this.sendTime);
        return msg;
    }
}
