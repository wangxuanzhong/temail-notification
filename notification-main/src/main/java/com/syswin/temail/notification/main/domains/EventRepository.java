package com.syswin.temail.notification.main.domains;

import java.util.List;

public interface EventRepository {

  int insert(Event event);

  List<Event> selectEvents(String to, String parentMsgId, Long begin, Long end);

  List<Event> selectEventsByMsgId(Event event);

  List<Long> selectResetEvents(Event event);

  void delete(List<Long> ids);

  List<Event> checkUnique(Event event);
}