package com.syswin.temail.notification.main.domains;

import java.time.LocalDateTime;
import java.util.List;

public interface TopicEventRepository {

  int insert(TopicEvent topicEvent);

  List<TopicEvent> selectEvents(String to, String topicId, Long begin, Long end);

  Long selectLastEventSeqId(String to, String topicId);

  List<TopicEvent> selectEventsByMsgId(String msgId);

  List<TopicEvent> selectEventsByTopicId(String topicId);

  void deleteOldTopic(LocalDateTime createTime);
}