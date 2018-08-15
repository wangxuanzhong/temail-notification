package com.syswin.temail.notification.main.domains;

import java.util.List;

public interface EventRepository {

  int insert(Event event);

  List<Event> selectByToBetweenSeqId(String to, Long begin, Long end);

  List<EventResponse> selectAllUnread(String to);

  int deleteByToBetweenSeqId(String to, Long begin, Long end);

  int deleteUnreadEvent(String msgId);

  Event selectByMsgId(String msgId);

  int updateByMsgId(Event event);
}