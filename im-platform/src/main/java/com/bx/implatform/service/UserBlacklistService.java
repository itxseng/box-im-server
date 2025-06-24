package com.bx.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bx.implatform.entity.UserBlacklist;
import com.bx.implatform.vo.UserBlacklistVO;

import java.util.List;

public interface UserBlacklistService extends IService<UserBlacklist> {

    /**
     * 分页查询用户黑名单列表
     *
     * @param page 页码
     * @param size   每页数量
     * @return 黑名单列表
     */
    List<UserBlacklistVO> pageList(Long page, Long size);

    /**
     * 添加到黑名单
     *
     * @param fromUserId 拉黑用户id
     * @param toUserId   被拉黑用户id
     */
    void add(Long fromUserId, Long toUserId);

    /**
     * 从黑名单中移除
     *
     * @param fromUserId 拉黑用户id
     * @param toUserId   被拉黑用户id
     */
    void remove(Long fromUserId, Long toUserId);

    /**
     * 判断是否已经拉黑对方
     *
     * @param fromUserId 拉黑用户id
     * @param toUserId   被拉黑用户id
     * @return boolean
     */
    Boolean isInBlacklist(Long fromUserId, Long toUserId);

}
