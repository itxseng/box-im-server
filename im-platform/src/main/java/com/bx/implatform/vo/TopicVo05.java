package com.bx.implatform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true) // 链式调用
@Schema(description = "帖子详情")
public class TopicVo05 {

    /**
     * 用户id
     */
    @Schema(description = "用户id")
    private Long userId;
    /**
     * 昵称
     */
    @Schema(description = "昵称")
    private String nickName;
    /**
     * 名称
     */
    @Schema(description = "名称")
    private String displayName;
    /**
     * 头像
     */
    @Schema(description = "头像")
    private String portrait;

}
