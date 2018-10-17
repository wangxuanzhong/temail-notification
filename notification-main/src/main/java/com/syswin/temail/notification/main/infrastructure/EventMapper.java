package com.syswin.temail.notification.main.infrastructure;

import com.syswin.temail.notification.main.domains.Event;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EventMapper {

  int insert(Event event);

  List<Event> selectByTo(@Param("to") String to, @Param("begin") Long begin, @Param("end") Long end);

  List<Event> selectPulledEvent(Event event);
}
