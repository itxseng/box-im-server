package com.bx.implatform.service;

import com.bx.implatform.dto.GroupMessageDTO;
import com.bx.implatform.dto.GroupUpdateMessageDTO;
import com.bx.implatform.dto.MessageOperateDTO;
import com.bx.implatform.entity.GroupMessage;
import com.bx.implatform.vo.GroupMessageVO;

import java.util.List;

public interface GroupMessageService{

    /**
     * 发送群聊消息(高并发接口，查询mysql接口都要进行缓存)
     *
     * @param dto 群聊消息
     * @return 群聊id
     */
    GroupMessageVO sendMessage(GroupMessageDTO dto);

    /**
     * 删除消息消息
     *
     * @param dto 消息id
     */
    GroupMessageVO delMessage(MessageOperateDTO dto);

    /**
     * 撤回消息
     *
     * @param id 消息id
     */
    GroupMessageVO recallMessage(Long id);

    /**
     * 撤回消息
     *
     * @param id 消息id
     */
    GroupMessageVO manageRecallMessage(Long id);

    /**
     * 修改消息
     *
     * @param dto 修改消息
     */
    GroupMessageVO updateMessage(GroupUpdateMessageDTO dto);

    /**
     * 拉取离线消息，只能拉取最近1个月的消息，最多拉取1000条
     *
     * @param minId 消息起始id
     */
    void  pullOfflineMessage(Long minId);

    /**
     * 消息已读,同步其他终端，清空未读数量
     *
     * @param groupId 群聊
     */
    void readedMessage(Long groupId);

    /**
     * 查询群里消息已读用户id列表
     * @param groupId 群里id
     * @param messageId 消息id
     * @return 已读用户id集合
     */
    List<Long> findReadedUsers(Long groupId,Long messageId);
    /**
     * 拉取历史聊天记录
     *
     * @param groupId 群聊id
     * @param page    页码
     * @param size    页码大小
     * @return 聊天记录列表
     */
    List<GroupMessageVO> findHistoryMessage(Long groupId, Long page, Long size);

    void save(GroupMessage msg);
}
