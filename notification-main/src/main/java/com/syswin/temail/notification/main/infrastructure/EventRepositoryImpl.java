package com.syswin.temail.notification.main.infrastructure;

import com.syswin.temail.notification.main.domains.Event;
import com.syswin.temail.notification.main.domains.EventRepository;
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
  public List<Event> selectByTo(String to, Long begin, Long end) {
    return eventMapper.selectByTo(to, begin, end);
  }

  @Override
  public List<Event> selectEvent(Event event) {
    return eventMapper.selectEvent(event);
  }

  @Override
  public List<Event> selectByXPacketId(Event event) {
    return eventMapper.selectByXPacketId(event);
  }
}
