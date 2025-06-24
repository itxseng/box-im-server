package com.bx.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bx.implatform.dto.ComplaintDTO;
import com.bx.implatform.entity.Complaint;
import com.bx.implatform.vo.ComplaintVO;

import java.util.List;

public interface ComplaintService extends IService<Complaint> {


    /**
     * 分页查询当前用户的投诉列表
     *
     * @param page 页码
     * @param size 页码大小
     * @return 投诉列表
     */
    List<ComplaintVO> findComplaintList(Long page, Long size);


    /**
     * 添加投诉
     *
     * @param dto 投诉信息
     */
    void addComplaint(ComplaintDTO dto);
}
