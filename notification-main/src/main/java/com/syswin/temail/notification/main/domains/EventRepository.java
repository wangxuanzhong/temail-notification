package com.syswin.temail.notification.main.domains;

import java.util.List;

public interface EventRepository {

  int insert(Event event);

  List<Event> selectByTo(String to, Long begin, Long end);

  List<Event> selectEvent(Event event);

  List<Event> selectByXPacketId(Event event);
}