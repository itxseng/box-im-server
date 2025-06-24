package com.bx.implatform.vo;

import com.bx.imcommon.serializer.DateToLongSerializer;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Schema(description = "投诉信息VO")
public class ComplaintVO {

    @Schema(description = "投诉id")
    private Long id;

    /**
     * 用户id
     */
    @Schema(description = "用户id")
    private Long userId;

    /**
     * 标题
     */
    @Schema(description = "标题")
    private String title;

    /**
     * 内容
     */
    @Schema(description = "内容")
    private String content;

    /**
     * 上传证据集合
     */
    @Schema(description = "上传证据集合")
    private List<String> fileUrlList;

    /**
     * 状态（1已提交，2处理中，3已处理）
     */
    @Schema(description = "状态（1已提交，2处理中，3已处理）")
    private Long status;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @JsonSerialize(using = DateToLongSerializer.class)
    private Date createTime;

    /**
     * 处理完成时间
     */
    @Schema(description = "处理完成时间")
    @JsonSerialize(using = DateToLongSerializer.class)
    private Date overTime;

}
