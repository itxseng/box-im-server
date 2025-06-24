package com.bx.implatform.service;

import com.bx.implatform.vo.TopicVo01;
import com.bx.implatform.vo.TopicVo03;
import com.bx.implatform.vo.TopicVo06;
import com.bx.implatform.vo.TopicVo07;
import com.bx.implatform.vo.TopicVo09;
import com.bx.implatform.vo.TopicVo10;
import com.bx.implatform.vo.TopicVoCount;

import java.util.List;

/**
 * 主题Service接口
 *
 * @author Blue
 * @date 2025-05-29
 */
public interface IChatTopicService {

    /**
     * 发布帖子
     *
     * @param topicVo 帖子信息
     */
    void sendTopic(TopicVo01 topicVo);

    /**
     * 删除帖子
     *
     * @param topicId 帖子id
     */
    void delTopic(Long topicId);

    /**
     * 指定人的帖子
     *
     * @param friendId 好友id
     * @param page     页码
     * @param pageSize 每页大小
     * @return 帖子列表
     */
    List<TopicVo03> userTopic(Long friendId, Long page, Long pageSize);

    /**
     * 好友的帖子
     *
     * @param page     页码
     * @param pageSize 每页大小
     * @return 帖子列表
     */
    List<TopicVo03> topicList(Long page, Long pageSize);

    /**
     * 朋友圈详情
     *
     * @param topicId 帖子id
     * @return 帖子详情
     */
    TopicVo03 topicInfo(Long topicId);

    /**
     * 查询通知列表
     *
     * @return 通知列表
     */
    List<TopicVo09> queryNoticeList();

    /**
     * 查询通知列表数量
     *
     * @return 通知列表数量
     */
    TopicVoCount noticeListCount();

    /**
     * 清空通知列表
     *
     */
    void clearNotice();

    /**
     * 点赞
     *
     * @param topicId 帖子id
     */
    void like(Long topicId);

    /**
     * 取消点赞
     *
     * @param topicId 帖子id
     */
    void cancelLike(Long topicId);

    /**
     * 回复
     *
     * @param topicVo 帖子id，replyId，content
     * @return 回复信息
     */
    TopicVo06 reply(TopicVo07 topicVo);

    /**
     * 删除回复
     *
     * @param replyId 回复id
     */
    void delReply( Long replyId);

    /**
     * 修改帖子可见范围
     *
     * @param topicVo 帖子id，openType，userIdList
     */
    void editTopic(TopicVo10 topicVo);
}
