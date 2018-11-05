package com.syswin.temail.notification.main.domains;

import java.util.List;

public interface EventRepository {

  int insert(Event event);

  List<Event> selectByTo(String to, Long begin, Long end);

  List<Event> selectPulledEvent(Event event);

  List<Event> selectReplyEvents(String to, String parentMsgId, Long begin, Long end);

  Integer deleteReplyEvents(Event event);
}