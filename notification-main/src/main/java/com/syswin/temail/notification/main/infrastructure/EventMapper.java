package com.syswin.temail.notification.main.infrastructure;

import com.syswin.temail.notification.main.domains.Event;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EventMapper {

  int insert(Event event);

  List<Event> selectEvents(@Param("to") String to, @Param("parentMsgId") String parentMsgId, @Param("begin") Long begin, @Param("end") Long end);

  Long selectLastEventSeqId(@Param("to") String to, @Param("parentMsgId") String parentMsgId);

  List<Event> selectEventsByMsgId(Event event);

  List<Long> selectResetEvents(Event event);

  void delete(List<Long> ids);

  List<Event> checkUnique(Event event);


  List<String> selectOldTo(LocalDateTime createTime);

  List<Event> selectOldEvent(@Param("to") String to, @Param("createTime") LocalDateTime createTime);

  void deleteOldEvent(LocalDateTime createTime);
}
