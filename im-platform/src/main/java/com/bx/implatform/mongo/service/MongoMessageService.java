package com.bx.implatform.mongo.service;

import com.bx.implatform.entity.GroupMessage;
import com.bx.implatform.entity.PrivateMessage;
import com.bx.implatform.enums.MessageStatus;
import com.bx.implatform.mongo.document.GroupMessageDoc;
import com.bx.implatform.mongo.document.PrivateMessageDoc;
import com.bx.implatform.mongo.repository.GroupMessageRepository;
import com.bx.implatform.mongo.repository.PrivateMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 统一的 MongoDB 消息存储服务
 * <p>
 * 约定：
 * <ul>
 *     <li>所有 <b>私聊</b>（PrivateMessage）相关方法写在 <b>最上面</b>；</li>
 *     <li>所有 <b>群聊</b>（GroupMessage）相关方法写在 <b>最下面</b>；</li>
 *     <li>每个方法都保持单一职责并加上清晰 Javadoc。</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class MongoMessageService {

    // ------------------------------------------------------------------
    // 🔗 依赖注入
    // ------------------------------------------------------------------
    private final PrivateMessageRepository privateRepo;
    private final GroupMessageRepository groupRepo;
    private final MongoTemplate mongo;

    // ==================================================================
    // 📨 私聊消息（PrivateMessage）相关方法
    // ==================================================================

    /**
     * 保存一条私聊消息。
     */
    public void savePrivateMessage(PrivateMessage msg) {
        privateRepo.save(PrivateMessageDoc.fromEntity(msg));
    }

    /**
     * 根据消息 ID 更新私聊消息的 <code>status</code> 字段。
     */
    public void updatePrivateMessageStatus(Long id, Integer status) {
        PrivateMessageDoc doc = privateRepo.findById(id).orElse(null);
        if (doc != null) {
            doc.setStatus(status);
            privateRepo.save(doc);
        }
    }

    /**
     * 整条替换（findAndReplace）更新私聊消息。
     */
    public void updatePrivateMessage(PrivateMessage msg) {
        Query q = Query.query(Criteria.where("id").is(msg.getId()));
        mongo.findAndReplace(q, PrivateMessageDoc.fromEntity(msg));
    }

    /**
     * 批量更新多条私聊消息的状态。
     */
    public void updatePrivateMessagesStatus(Collection<Long> ids, Integer status) {
        if (ids == null || ids.isEmpty()) return;
        Query q = Query.query(Criteria.where("id").in(ids));
        Update u = Update.update("status", status);
        mongo.updateMulti(q, u, PrivateMessageDoc.class);
    }

    public void updatePrivateMessagesStatus(List<Long> ids, Long operatorId, boolean deleteBoth) {
        Query query = Query.query(Criteria.where("id").in(ids));
        Update update = new Update();
        if (deleteBoth) {
            update.set("deletedBySender", true);
            update.set("deletedByReceiver", true);
        } else {
            // 查询消息确定操作方是发送方还是接收方
            List<PrivateMessageDoc> messages = mongo.find(query, PrivateMessageDoc.class);
            for (PrivateMessageDoc message : messages) {
                Update singleUpdate = new Update();
                if (Objects.equals(message.getSendId(), operatorId)) {
                    singleUpdate.set("deletedBySender", true);
                } else if (Objects.equals(message.getRecvId(), operatorId)) {
                    singleUpdate.set("deletedByReceiver", true);
                }
                mongo.updateFirst(Query.query(Criteria.where("_id").is(message.getId())), singleUpdate, PrivateMessageDoc.class);
            }
            return;
        }
        mongo.updateMulti(query, update, PrivateMessageDoc.class);
    }


    /**
     * 清空我——>peer 的会话（只改我发出的消息状态）。
     */
    public void clearConversation(Long selfId, Long peerId, Integer status) {
        Query q = Query.query(Criteria.where("sendId").is(selfId).and("recvId").is(peerId));
        Update u = Update.update("status", status);
        mongo.updateMulti(q, u, PrivateMessageDoc.class);
    }

    public void clearConversation(Long operatorId, Long peerId, boolean deleteBoth) {
        Criteria criteria = new Criteria().orOperator(
                Criteria.where("sendId").is(operatorId).and("recvId").is(peerId),
                Criteria.where("sendId").is(peerId).and("recvId").is(operatorId)
        );

        Query query = Query.query(criteria);
        List<PrivateMessageDoc> messages = mongo.find(query, PrivateMessageDoc.class);
        for (PrivateMessageDoc message : messages) {
            Update update = new Update();
            if (deleteBoth) {
                update.set("deletedBySender", true);
                update.set("deletedByReceiver", true);
            } else {
                if (Objects.equals(message.getSendId(), operatorId)) {
                    update.set("deletedBySender", true);
                } else if (Objects.equals(message.getRecvId(), operatorId)) {
                    update.set("deletedByReceiver", true);
                }
            }
            mongo.updateFirst(Query.query(Criteria.where("_id").is(message.getId())), update, PrivateMessageDoc.class);
        }
    }


    /**
     * 根据 ID 获取一条私聊消息。
     */
    public PrivateMessage findPrivateMessageById(Long id) {
        PrivateMessageDoc doc = privateRepo.findById(id).orElse(null);
        return doc == null ? null : doc.toEntity();
    }

    /**
     * 拉取当前用户最近未同步的私聊消息。
     */
    public List<PrivateMessage> findPrivateMessages(Long userId, Long minId, Date minDate) {
        Criteria timeRange = Criteria.where("sendTime").gte(minDate);
        Criteria idRange = Criteria.where("id").gt(minId);
        Criteria userSide = new Criteria().orOperator(
                Criteria.where("sendId").is(userId),
                Criteria.where("recvId").is(userId)
        );
        Criteria notDeleted = new Criteria().orOperator(
                Criteria.where("deletedBySender").exists(false).orOperator(Criteria.where("deletedBySender").is(false)),
                Criteria.where("deletedByReceiver").exists(false).orOperator(Criteria.where("deletedByReceiver").is(false))
        );


        Criteria all = new Criteria().andOperator(idRange, timeRange, userSide, notDeleted);

        Query q = Query.query(all)
                .with(Sort.by(Sort.Direction.ASC, "id"));

        return mongo.find(q, PrivateMessageDoc.class)
                .stream()
                .map(PrivateMessageDoc::toEntity)
                .collect(Collectors.toList());
    }

    /**
     * 查询与 peer 的历史消息（倒序分页，自动过滤掉自己已删的）。
     *
     * @param selfId 当前登录用户
     * @param peerId 对端用户
     * @param skip   跳过条数（分页）
     * @param limit  返回条数（分页）
     */
    public List<PrivateMessage> findPrivateHistory(Long selfId, Long peerId, long skip, long limit) {

        // 1. 我是发送方，看 deletedBySender
        Criteria iAmSender = new Criteria().andOperator(
                Criteria.where("sendId").is(selfId),
                Criteria.where("recvId").is(peerId),
                // 不存在字段或为 false 视为“未删除”
                new Criteria().orOperator(
                        Criteria.where("deletedBySender").exists(false),
                        Criteria.where("deletedBySender").is(false)
                )
        );

        // 2. 我是接收方，看 deletedByReceiver
        Criteria iAmReceiver = new Criteria().andOperator(
                Criteria.where("sendId").is(peerId),
                Criteria.where("recvId").is(selfId),
                new Criteria().orOperator(
                        Criteria.where("deletedByReceiver").exists(false),
                        Criteria.where("deletedByReceiver").is(false)
                )
        );

        Query q = Query.query(new Criteria().orOperator(iAmSender, iAmReceiver))
                // 若仍保留 status，可继续排除撤回/屏蔽等状态
                //.addCriteria(Criteria.where("status").ne(MessageStatus.RECALL.code()))
                .with(Sort.by(Sort.Direction.DESC, "id"))
                .skip(skip)
                .limit(Math.toIntExact(limit));

        return mongo.find(q, PrivateMessageDoc.class)
                .stream()
                .map(PrivateMessageDoc::toEntity)
                .toList();
    }

    /**
     * 批量根据 ID 查询私聊消息。
     */
    public List<PrivateMessage> findPrivateMessagesByIds(Collection<Long> ids) {
        Query q = Query.query(Criteria.where("id").in(ids));
        return mongo.find(q, PrivateMessageDoc.class)
                .stream().map(PrivateMessageDoc::toEntity).collect(Collectors.toList());
    }

    /**
     * 把 peer ➜ self 的 <code>sended</code> 状态消息批量改为 <code>readed</code>。
     */
    public void markPrivateMessagesRead(Long peerId, Long selfId, Integer sended, Integer readed) {
        Query q = Query.query(Criteria.where("sendId").is(peerId)
                .and("recvId").is(selfId)
                .and("status").is(sended));
        Update u = Update.update("status", readed);
        mongo.updateMulti(q, u, PrivateMessageDoc.class);
    }

    /**
     * 查询我发给 peer 的最大已读消息 ID。
     */
    public Long getMaxReadedId(Long selfId, Long peerId, Integer readed) {
        Query q = Query.query(Criteria.where("sendId").is(selfId)
                        .and("recvId").is(peerId)
                        .and("status").is(readed))
                .with(Sort.by(Sort.Direction.DESC, "id")).limit(1);
        PrivateMessageDoc doc = mongo.findOne(q, PrivateMessageDoc.class);
        return doc == null ? -1L : doc.getId();
    }

    /**
     * 批量将消息状态从 UNSEND 更新为 SENDED。
     */
    public void markMessagesSendedIfUnsend(Collection<Long> messageIds, Integer unsend, Integer sended) {
        if (messageIds == null || messageIds.isEmpty()) return;
        Query q = Query.query(Criteria.where("id").in(messageIds).and("status").is(unsend));
        Update u = Update.update("status", sended);
        mongo.updateMulti(q, u, PrivateMessageDoc.class);
    }


    // ==================================================================
    // 👥 群聊消息（GroupMessage）相关方法
    // ==================================================================

    /**
     * 保存一条群聊消息。
     */
    public void saveGroupMessage(GroupMessage msg) {
        groupRepo.save(GroupMessageDoc.fromEntity(msg));
    }

    /**
     * 根据 ID 更新群聊消息状态。
     */
    public void updateGroupMessageStatus(Long id, Integer status) {
        GroupMessageDoc doc = groupRepo.findById(id).orElse(null);
        if (doc != null) {
            doc.setStatus(status);
            groupRepo.save(doc);
        }
    }

    /**
     * 根据消息 ID 查询群聊消息。
     */
    public GroupMessage findGroupMessageById(Long id) {
        GroupMessageDoc doc = groupRepo.findById(id).orElse(null);
        return doc == null ? null : doc.toEntity();
    }

    /**
     * 查询指定群集合在 minId、minDate 之后的所有消息。
     */
    public List<GroupMessage> findGroupMessages(Long minId, Date minDate, Collection<Long> groupIds, Integer recall) {
        Criteria c = new Criteria().andOperator(
                Criteria.where("id").gt(minId),
                Criteria.where("sendTime").gte(minDate),
                Criteria.where("groupId").in(groupIds),
                Criteria.where("status").ne(recall),
                Criteria.where("status").ne(MessageStatus.DELETED.code())
        );
        Query q = Query.query(c).with(Sort.by(Sort.Direction.ASC, "id"));
        return mongo.find(q, GroupMessageDoc.class)
                .stream().map(GroupMessageDoc::toEntity).collect(Collectors.toList());
    }

    /**
     * 查询用户退群前的群聊消息（时间 + id 双条件）。
     */
    public List<GroupMessage> findQuitGroupMessages(Long groupId, Date minDate, Date quitTime, Long minId) {
        Criteria criteria = new Criteria().andOperator(
                Criteria.where("id").gt(minId),
                Criteria.where("sendTime").gte(minDate).lte(quitTime),
                Criteria.where("groupId").is(groupId),
                validGroupMessageCriteria()
        );

        Query q = Query.query(criteria)
                .with(Sort.by(Sort.Direction.ASC, "id"));

        return mongo.find(q, GroupMessageDoc.class)
                .stream().map(GroupMessageDoc::toEntity).collect(Collectors.toList());
    }

    /**
     * 批量 ID 查询群聊消息。
     */
    public List<GroupMessage> findGroupMessagesByIds(Collection<Long> ids) {
        Query q = Query.query(Criteria.where("id").in(ids));
        return mongo.find(q, GroupMessageDoc.class)
                .stream().map(GroupMessageDoc::toEntity).collect(Collectors.toList());
    }

    /**
     * 群聊历史记录查询（倒序分页）。
     */
    public List<GroupMessage> findGroupHistory(Long groupId, Date joinTime, long skip, long limit, Integer recall) {
        Criteria c = new Criteria().andOperator(
                Criteria.where("groupId").is(groupId),
                Criteria.where("sendTime").gt(joinTime),
                Criteria.where("status").ne(recall),
                Criteria.where("status").ne(MessageStatus.DELETED.code())
        );
        Query q = Query.query(c)
                .with(Sort.by(Sort.Direction.DESC, "id"))
                .skip(skip).limit((int) limit);
        return mongo.find(q, GroupMessageDoc.class)
                .stream().map(GroupMessageDoc::toEntity).collect(Collectors.toList());
    }

    /**
     * 查询群的最后一条消息。
     */
    public GroupMessage findLastGroupMessage(Long groupId) {
        Query q = Query.query(Criteria.where("groupId").is(groupId))
                .with(Sort.by(Sort.Direction.DESC, "id")).limit(1);
        GroupMessageDoc doc = mongo.findOne(q, GroupMessageDoc.class);
        return doc == null ? null : doc.toEntity();
    }

    /**
     * 查询群聊收据消息（回执）。
     */
    public List<GroupMessage> findReceiptMessages(Long groupId, Long startId, Long endId, Integer recall) {
        Criteria range = Criteria.where("id").gt(startId).lte(endId);
        Criteria others = new Criteria().andOperator(
                Criteria.where("groupId").is(groupId),
                Criteria.where("status").ne(recall),
                Criteria.where("receipt").is(true));
        Query q = Query.query(new Criteria().andOperator(range, others));
        return mongo.find(q, GroupMessageDoc.class)
                .stream().map(GroupMessageDoc::toEntity).collect(Collectors.toList());
    }

    /**
     * 更新群聊消息的 receiptOk 标志位。
     */
    public void updateGroupMessageReceiptOk(Long id, boolean ok) {
        Query q = Query.query(Criteria.where("id").is(id));
        Update u = Update.update("receiptOk", ok);
        mongo.updateFirst(q, u, GroupMessageDoc.class);
    }


    /**
     * 更新群聊消息的删除状态：
     *
     * @param messageId    消息 ID
     * @param operatorId   当前操作用户 ID
     */
    public void updateGroupMessageDeleteStatus(Long messageId, Long operatorId) {
        Query query = Query.query(Criteria.where("id").is(messageId));
        Update update = new Update();

        // 管理员删除所有人：设置状态和作用域
        update.set("status", MessageStatus.DELETED.code());
        // 普通成员：仅删除自己
        update.set("operatorId", operatorId);


        mongo.updateFirst(query, update, GroupMessageDoc.class);
    }




    /**
     * 群聊消息过滤条件：排除 status = 5 的消息
     */
    private Criteria validGroupMessageCriteria() {
        return Criteria.where("status").ne(5);
    }


}
