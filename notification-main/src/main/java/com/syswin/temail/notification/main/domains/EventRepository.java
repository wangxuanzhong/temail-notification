package com.syswin.temail.notification.main.domains;

import java.util.List;

public interface EventRepository {

  int insert(Event event);

  List<Event> selectEvents(String to, String parentMsgId, Long begin, Long end);

  List<Event> selectPulledEvents(Event event);

  Integer deleteReplyEvents(Event event);
}