package com.bx.implatform.controller;

import com.bx.implatform.annotation.RepeatSubmit;
import com.bx.implatform.dto.ComplaintDTO;
import com.bx.implatform.result.Result;
import com.bx.implatform.result.ResultUtils;
import com.bx.implatform.service.ComplaintService;
import com.bx.implatform.service.FriendService;
import com.bx.implatform.vo.ComplaintVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户投诉")
@RestController
@RequestMapping("/complaint")
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintService complaintService;


    @GetMapping("/list")
    @Operation(summary = "投诉列表", description = "获取好友列表")
    public Result<List<ComplaintVO>> findComplaintList(@NotNull(message = "页码不能为空") @RequestParam Long page,
                                                       @NotNull(message = "size不能为空") @RequestParam Long size) {
        return ResultUtils.success(complaintService.findComplaintList(page, size));
    }


    @RepeatSubmit
    @PostMapping("/add")
    @Operation(summary = "添加投诉", description = "添加投诉")
    public Result<Void> addComplaint(@Valid @RequestBody ComplaintDTO dto) {
        complaintService.addComplaint(dto);
        return ResultUtils.success();
    }

}

