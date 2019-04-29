package com.syswin.temail.notification.main.infrastructure;

import com.syswin.temail.notification.main.domains.Event;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EventMapper {

  int insert(Event event);

  List<Event> selectEvents(@Param("to") String to, @Param("begin") Long begin, @Param("end") Long end);

  List<Event> selectPartEvents(@Param("to") String to, @Param("eventTypes") List<Integer> eventTypes);

  Long selectLastEventSeqId(String to);

  List<Event> selectEventsByMsgId(Event event);

  List<Long> selectResetEvents(Event event);

  void delete(List<Long> ids);

  List<Event> checkUnique(Event event);

  // 清除历史数据功能所用sql
  List<String> selectOldTo(LocalDateTime createTime);

  List<Event> selectOldEvent(@Param("to") String to, @Param("createTime") LocalDateTime createTime, @Param("eventTypes") List<Integer> eventTypes);

  List<Long> selectOldEventId(@Param("createTime") LocalDateTime createTime, @Param("start") int start, @Param("pageSize") int pageSize);
}
