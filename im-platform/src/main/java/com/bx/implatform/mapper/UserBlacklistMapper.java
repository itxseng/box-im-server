package com.bx.implatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bx.implatform.entity.UserBlacklist;
import com.bx.implatform.vo.UserBlacklistVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author: Blue
 * @date: 2024-09-22
 * @version: 1.0
 */
public interface UserBlacklistMapper extends BaseMapper<UserBlacklist> {
    /**
     * 分页查询用户黑名单列表
     *
     * @param userId 用户id
     * @param page   页码
     * @param size   每页数量
     * @return 黑名单列表
     */
    List<UserBlacklistVO> selectPageList(@Param("userId") Long userId, @Param("page") Long page, @Param("size") Long size);
}
