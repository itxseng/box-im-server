package com.bx.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bx.implatform.entity.SmPushTask;

import java.util.List;

/**
 * 系统消息推送任务
 *
 * @author: Blue
 * @date: 2024-09-07
 * @version: 1.0
 */
public interface SmPushTaskService  extends IService<SmPushTask>  {

    /**
     * 查询准备被推送的任务
     *
     * @return
     */
    SmPushTask findOneReadyTask();

    /**
     * 获取一个月内已发送的任务
     * @param minSeqNo 最小发送序列号
     * @return
     */
    List<SmPushTask> findSendedTaskInMonth(Long minSeqNo);

    /**
     * 获取下一个推送序列号
     * @return
     */
    Long nextSeqNo();

}
