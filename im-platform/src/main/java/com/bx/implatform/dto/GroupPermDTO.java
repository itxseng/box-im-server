package com.bx.implatform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "群信息VO")
public class GroupPermDTO {

    @Schema(description = "群id")
    private Long id;

    /**
     * 允许普通成员发起临时会话（false否  true是）
     */
    @Schema(description = "允许普通成员发起临时会话 false否  true是")
    private Boolean interimPerm;

    /**
     * 允许查看群成员（false否  true是）
     */
    @Schema(description = "允许查看群成员 false否  true是")
    private Boolean queryMemberPerm;

    /**
     * 加群权限（1不限制加入  2群成员可以拉人  3只能管理员拉人  4群成员拉人需要管理员验证）
     */
    @Schema(description = "加群权限（1不限制加入  2群成员可以拉人  3只能管理员拉人  4群成员拉人需要管理员验证）")
    private Integer addGroupPerm;

    /**
     * 查找方式（1允许查找  2不允许查找）
     */
    @Schema(description = "查找方式（1允许查找  2不允许查找）")
    private Integer queryGroupPerm;

    /**
     * 是否关闭群语音/视频通话（false否  true是）
     */
    @Schema(description = "是否关闭群语音/视频通话 false否  true是")
    private Boolean roomGroupPerm;


}
