package com.syswin.temail.notification.main.domains;

import java.util.List;

public interface EventRepository {

  int insert(Event event);

  List<Event> selectByTo(String to);

  List<EventResponse> selectAllUnread(String to);

  int deleteByTo(String to);

  int deleteUnreadEvents(List<String> msgIds);

  Event selectByMsgId(String msgId);

  int updateByMsgId(Event event);
}