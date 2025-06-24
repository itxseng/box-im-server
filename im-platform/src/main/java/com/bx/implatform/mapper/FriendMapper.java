package com.bx.implatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bx.implatform.entity.Friend;
import com.bx.implatform.vo.FriendGroupVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FriendMapper extends BaseMapper<Friend> {
    /**
     * 分页查询用户黑名单列表
     *
     * @param userId   用户id
     * @param friendId 好友id
     * @param page     页码
     * @param size     每页数量
     * @return 和好友的共同群列表
     */
    List<FriendGroupVO> selectPageList(@Param("userId") Long userId, @Param("friendId") Long friendId, @Param("page") Long page, @Param("size") Long size);
}
