package com.bx.implatform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true) // 链式调用
@Schema(description = "帖子详情")
public class TopicVo03 {

    /**
     * 帖子
     */
    @Schema(description = "帖子")
    private TopicVo04 topic;

    /**
     * 点赞信息
     */
    @Schema(description = "点赞信息")
    private List<TopicVo05> likeList;

    /**
     * 是否点赞
     */
    @Schema(description = "是否点赞Y,N")
    private String like;

    /**
     * 评论信息
     */
    @Schema(description = "评论信息")
    private List<TopicVo06> replyList;

}
