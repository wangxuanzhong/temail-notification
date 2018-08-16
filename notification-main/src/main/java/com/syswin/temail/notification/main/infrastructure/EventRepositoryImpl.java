package com.syswin.temail.notification.main.infrastructure;

import com.syswin.temail.notification.main.domains.Event;
import com.syswin.temail.notification.main.domains.EventRepository;
import com.syswin.temail.notification.main.domains.EventResponse;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EventRepositoryImpl implements EventRepository {

  private final EventMapper eventMapper;

  @Autowired
  public EventRepositoryImpl(EventMapper eventMapper) {
    this.eventMapper = eventMapper;
  }

  @Override
  public int insert(Event event) {
    return eventMapper.insert(event);
  }

  @Override
  public List<Event> selectByTo(String to) {
    return eventMapper.selectByTo(to);
  }

  @Override
  public List<EventResponse> selectAllUnread(String to) {
    return eventMapper.selectAllUnread(to);
  }

  @Override
  public int deleteByTo(String to) {
    return eventMapper.deleteByTo(to);
  }

  @Override
  public int deleteUnreadEvents(List<String> msgIds) {
    return eventMapper.deleteUnreadEvents(msgIds);
  }

  @Override
  public Event selectByMsgId(String msgId) {
    return eventMapper.selectByMsgId(msgId);
  }

  @Override
  public int updateByMsgId(Event event) {
    return eventMapper.updateByMsgId(event);
  }

}
