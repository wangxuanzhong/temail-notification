package com.syswin.temail.notification.main.infrastructure;

import com.syswin.temail.notification.main.domains.Event;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EventMapper {

  int insert(Event event);

  List<Event> selectEventsByTo(@Param("to") String to, @Param("parentMsgId") String parentMsgId, @Param("begin") Long begin, @Param("end") Long end);

  List<Event> selectEvent(Event event);

  Integer deleteReplyEvents(Event event);
}
