package com.syswin.temail.notification.main.infrastructure;

import com.syswin.temail.notification.main.domains.TopicEvent;
import com.syswin.temail.notification.main.domains.TopicEventRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TopicEventRepositoryImpl implements TopicEventRepository {

  private final TopicEventMapper topicEventMapper;

  @Autowired
  public TopicEventRepositoryImpl(TopicEventMapper topicEventMapper) {
    this.topicEventMapper = topicEventMapper;
  }

  @Override
  public int insert(TopicEvent topicEvent) {
    return topicEventMapper.insert(topicEvent);
  }

  @Override
  public List<TopicEvent> selectEvents(String to, String topicId, Long begin, Long end) {
    return topicEventMapper.selectEvents(to, topicId, begin, end);
  }

  @Override
  public Long selectLastEventSeqId(String to, String topicId) {
    return topicEventMapper.selectLastEventSeqId(to, topicId);
  }

  @Override
  public List<TopicEvent> selectEventsByMsgId(String msgId) {
    return topicEventMapper.selectEventsByMsgId(msgId);
  }

  @Override
  public void deleteOldTopic(LocalDateTime createTime) {
    topicEventMapper.deleteOldTopic(createTime);
  }
}
