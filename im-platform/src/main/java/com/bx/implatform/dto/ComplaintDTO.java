package com.bx.implatform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "投诉信息DTO")
public class ComplaintDTO {

    /**
     * 标题
     */
    @Schema(description = "标题")
    @NotNull(message = "好友id不可为空")
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


}
