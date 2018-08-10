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
  public int batchInsert(List<Event> events) {
    return eventMapper.batchInsert(events);
  }

  @Override
  public List<Event> selectByTo(String to, Long sequenceNo) {
    return eventMapper.selectByTo(to, sequenceNo);
  }

  @Override
  public int deleteByTo(String to, Long sequenceNo) {
    return eventMapper.deleteByTo(to, sequenceNo);
  }
}
