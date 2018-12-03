package com.syswin.temail.notification.main.infrastructure;

import com.syswin.temail.notification.main.domains.TopicEvent;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TopicEventMapper {

  int insert(TopicEvent topicEvent);

  List<TopicEvent> selectEvents(@Param("to") String to, @Param("topicId") String topicId, @Param("begin") Long begin, @Param("end") Long end);

  List<TopicEvent> selectEventsByMsgId(String msgId);
}
