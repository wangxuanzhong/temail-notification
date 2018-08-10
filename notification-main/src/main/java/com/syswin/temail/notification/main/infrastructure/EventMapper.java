package com.syswin.temail.notification.main.infrastructure;

import com.syswin.temail.notification.main.domains.Event;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EventMapper {

  int batchInsert(List<Event> events);

  List<Event> selectByTo(@Param("to") String to, @Param("sequenceNo") Long sequenceNo);

  int deleteByTo(@Param("to") String to, @Param("sequenceNo") Long sequenceNo);
}
