package com.syswin.temail.notification.main.infrastructure;

import com.syswin.temail.notification.main.domains.Event;
import com.syswin.temail.notification.main.domains.EventResponse;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EventMapper {

  int insert(Event event);

  List<Event> selectByToBetweenSeqId(@Param("to") String to, @Param("begin") Long begin, @Param("end") Long end);

  List<EventResponse> selectAllUnread(@Param("to") String to);

  int deleteByToBetweenSeqId(@Param("to") String to, @Param("begin") Long begin, @Param("end") Long end);

  int deleteUnreadEvent(@Param("msgId") String msgId);
}
