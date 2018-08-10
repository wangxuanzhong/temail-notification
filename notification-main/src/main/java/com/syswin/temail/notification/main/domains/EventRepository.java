package com.syswin.temail.notification.main.domains;

import java.util.List;

public interface EventRepository {

  int insert(Event event);

  int batchInsert(List<Event> events);

  List<Event> selectByTo(String to, Long sequenceNo);

  int deleteByTo(String to, Long sequenceNo);
}