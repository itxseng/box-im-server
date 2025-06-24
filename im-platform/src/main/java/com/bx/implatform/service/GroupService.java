package com.bx.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bx.implatform.dto.*;
import com.bx.implatform.entity.Group;
import com.bx.implatform.entity.GroupMember;
import com.bx.implatform.vo.GroupMemberVO;
import com.bx.implatform.dto.GroupPermDTO;
import com.bx.implatform.vo.GroupVO;

import java.util.List;

public interface GroupService extends IService<Group> {

    /**
     * 创建新群聊
     *
     * @param dto 群聊信息
     * @return 群聊信息
     **/
    GroupVO createGroup(GroupCreateDTO dto);

    /**
     * 修改群聊信息
     *
     * @param dto 群聊信息
     * @return 群聊信息
     **/
    GroupVO modifyGroup(GroupUpdateDTO dto);

    /**
     * 修改群聊权限
     *
     * @param dto 群聊信息
     * @return 群聊信息
     **/
    GroupVO modifyPerm(GroupPermDTO dto);

    /**
     * 删除群聊
     *
     * @param groupId 群聊id
     **/
    void deleteGroup(Long groupId);

    /**
     * 退出群聊
     *
     * @param groupId 群聊id
     */
    void quitGroup(Long groupId);

    /**
     * 将用户踢出群聊
     *
     * @param groupId 群聊id
     * @param userId  用户id
     */
    void kickGroup(Long groupId, Long userId);

    /**
     * 将用户移出群聊
     * @param dto dto
     */
    void removeGroupMembers(GroupMemberRemoveDTO dto);

    /**
     * 查询当前用户的所有群聊
     *
     * @return 群聊信息列表
     **/
    List<GroupVO> findGroups();

    /**
     * 邀请好友进群
     *
     * @param dto 群id、好友id列表
     **/
    void invite(GroupInviteDTO dto);

    /**
     * 加入群聊
     *
     * @param groupId 群id
     **/
    GroupVO join(Long groupId);

    /**
     * 管理员验证邀请进群
     *
     * @param dto 群id、好友id列表
     **/
    void inviteManager(GroupInviteDTO dto);

    /**
     * 根据id查找群聊，并进行缓存
     *
     * @param groupId 群聊id
     * @return 群聊实体
     */
    Group getAndCheckById(Long groupId);

    /**
     * 根据id查找群聊
     *
     * @param groupId 群聊id
     * @return 群聊vo
     */
    GroupVO findById(Long groupId);

    /**
     * 查询群成员
     *
     * @param groupId 群聊id
     * @return List<GroupMemberVO>
     **/
    List<GroupMemberVO> findGroupMembers(Long groupId);

    /**
     * 设置群禁言状态
     *
     * @param dto dto
     */
    void setGroupMuted(GroupMutedDTO dto);

    /**
     * 验证是否是管理员
     *
     * @param groupId groupId
     * @param member  member
     */
    boolean checkManager(Long groupId, GroupMember member);


    /**
     * 设置成员禁言状态
     *
     * @param dto dto
     */
    void setMemberMuted(GroupMemberMutedDTO dto);


    /**
     * 设置群置顶消息
     *
     * @param groupId   群id
     * @param messageId 消息id
     */
    void setTopMessage(Long groupId, Long messageId);

    /**
     * 移除群置顶消息,对所有群成员生效
     *
     * @param groupId 群id
     */
    void removeTopMessage(Long groupId);
    /**
     * 移除群置顶消息,对所有群成员生效
     *
     * @param groupId 群id
     */
    void removeTopMessage(Long groupId, Long messageId);

    /**
     * 隐藏群置顶消息，仅对自己生效
     *
     * @param groupId 群id
     */
    void hideTopMessage(Long groupId);

    /**
     * 新增管理员
     *
     * @param dto dto
     */
    void addManager(GroupManagerDTO dto);

    /**
     * 移除管理员
     *
     * @param dto dto
     */
    void removeManager(GroupManagerDTO dto);

    /**
     * 设置群通知过期时间
     * @param dto
     */
    void setGroupNotifyExpire(GroupNotifyExpireDto dto);
}
