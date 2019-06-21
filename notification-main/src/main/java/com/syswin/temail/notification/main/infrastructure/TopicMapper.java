package com.syswin.temail.notification.main.infrastructure;

import com.syswin.temail.notification.main.domains.TopicEvent;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author liusen@syswin.com
 */
@Repository
public interface TopicMapper {

  /**
   * 插入话题事件
   *
   * @param topicEvent 话题事件参数
   * @return 主键
   */
  int insert(TopicEvent topicEvent);

  /**
   * 查询话题事件
   *
   * @param to 接收方
   * @param begin 分页起始
   * @param pageSize 页大小
   * @return 查询结果
   */
  List<TopicEvent> selectEvents(@Param("to") String to, @Param("begin") Long begin,
      @Param("pageSize") Integer pageSize);

  /**
   * 查询最新序列号
   *
   * @param to 接收方
   * @return 序列号
   */
  Long selectLastEventSeqId(String to);

  /**
   * 根据msgId查询事件
   *
   * @param msgId 消息id
   * @return 查询结果
   */
  List<TopicEvent> selectEventsByMsgId(String msgId);

  /**
   * 根据topicId查询事件
   *
   * @param topicId 话题id
   * @return 查询结果
   */
  List<TopicEvent> selectEventsByTopicId(String topicId);

  /**
   * 删除过期事件
   *
   * @param createTime 截止时间
   */
  void deleteOldTopic(LocalDateTime createTime);
}
