package com.bx.implatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bx.implatform.dto.ComplaintDTO;
import com.bx.implatform.entity.Complaint;
import com.bx.implatform.mapper.ComplaintMapper;
import com.bx.implatform.service.ComplaintService;
import com.bx.implatform.session.SessionContext;
import com.bx.implatform.util.BeanUtils;
import com.bx.implatform.vo.ComplaintVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ComplaintServiceImpl extends ServiceImpl<ComplaintMapper, Complaint> implements ComplaintService {


    @Override
    public List<ComplaintVO> findComplaintList(Long page, Long size) {
        page = page > 0 ? page : 1;
        size = size > 0 ? size : 10;
        long stIdx = (page - 1) * size;
        Long userId = SessionContext.getSession().getUserId();
        LambdaQueryWrapper<Complaint> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Complaint::getUserId, userId);
        wrapper.last("limit " + stIdx + "," + size);
        List<Complaint> list = this.list(wrapper);
        if (CollectionUtils.isNotEmpty(list)) {
            return list.stream().map(this::conver).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public void addComplaint(ComplaintDTO dto) {
        Complaint complaint = BeanUtils.copyProperties(dto, Complaint.class);
        complaint.setUserId(SessionContext.getSession().getUserId());
        if (CollectionUtils.isNotEmpty(dto.getFileUrlList())) {
            complaint.setFileUrl(String.join(",", dto.getFileUrlList()));
        }
        complaint.setCreateTime(new Date());
        this.save(complaint);
    }

    private ComplaintVO conver(Complaint f) {
        ComplaintVO vo = new ComplaintVO();
        vo.setId(f.getId());
        vo.setUserId(f.getUserId());
        vo.setContent(f.getContent());
        vo.setStatus(f.getStatus());
        vo.setTitle(f.getTitle());
        vo.setCreateTime(f.getCreateTime());
        vo.setOverTime(f.getOverTime());
        if (StringUtils.isNotBlank(f.getFileUrl())) {
            vo.setFileUrlList(Arrays.stream(f.getFileUrl().split(",")).collect(Collectors.toList()));
        }
        return vo;
    }
}
