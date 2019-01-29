package com.syswin.temail.notification.main.infrastructure;

import com.syswin.temail.notification.main.domains.TopicEvent;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TopicMapper {

  int insert(TopicEvent topicEvent);

  List<TopicEvent> selectEvents(@Param("to") String to, @Param("begin") Long begin, @Param("end") Long end);

  Long selectLastEventSeqId(String to);

  List<TopicEvent> selectEventsByMsgId(String msgId);

  List<TopicEvent> selectEventsByTopicId(String topicId);

  void deleteOldTopic(LocalDateTime createTime);
}
