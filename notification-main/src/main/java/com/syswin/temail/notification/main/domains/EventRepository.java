package com.syswin.temail.notification.main.domains;

import java.util.List;

public interface EventRepository {

  int insert(Event event);

  List<Event> selectEventsByTo(String to, String parentMsgId, Long begin, Long end);

  List<Event> selectEvent(Event event);

  Integer deleteReplyEvents(Event event);

  List<Event> selectByXPacketId(Event event);
}