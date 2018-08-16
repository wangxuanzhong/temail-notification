package com.syswin.temail.notification.main.infrastructure;

import com.syswin.temail.notification.main.domains.Event;
import com.syswin.temail.notification.main.domains.UnreadResponse;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface EventMapper {

  int insert(Event event);

  List<Event> selectByTo(String to);

  List<UnreadResponse> selectAllUnread(String to);

  int deleteByTo(String to);

  int deleteUnreadEvents(List<String> msgIds);

  Event selectByMsgId(String msgId);

  int updateByMsgId(Event event);
}
