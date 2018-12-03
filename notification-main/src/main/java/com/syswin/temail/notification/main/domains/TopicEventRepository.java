package com.syswin.temail.notification.main.domains;

import java.util.List;

public interface TopicEventRepository {

  int insert(TopicEvent topicEvent);

  List<TopicEvent> selectEvents(String to, String topicId, Long begin, Long end);

  List<TopicEvent> selectEventsByMsgId(String msgId);
}