package com.syswin.temail.notification.main.infrastructure;

import com.syswin.temail.notification.main.domains.Event;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EventMapper {

  int insert(Event event);

  List<Event> selectEvents(@Param("to") String to, @Param("parentMsgId") String parentMsgId, @Param("begin") Long begin, @Param("end") Long end);

  List<Event> selectEventsByMsgId(Event event);

  Integer deleteResetEvents(Event event);

  List<Event> selectEventsByParentMsgIds(List<String> parentMsgIds);

  List<Event> checkUnique(Event event);
}
