package com.syswin.temail.notification.main.infrastructure;

import com.syswin.temail.notification.main.domains.Event;
import com.syswin.temail.notification.main.domains.EventRepository;
import java.time.LocalDateTime;
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
  public List<Event> selectEvents(String to, String parentMsgId, Long begin, Long end) {
    return eventMapper.selectEvents(to, parentMsgId, begin, end);
  }

  @Override
  public Long selectLastEventSeqId(String to, String parentMsgId) {
    return eventMapper.selectLastEventSeqId(to, parentMsgId);
  }

  @Override
  public List<Event> selectEventsByMsgId(Event event) {
    return eventMapper.selectEventsByMsgId(event);
  }

  @Override
  public List<Long> selectResetEvents(Event event) {
    return eventMapper.selectResetEvents(event);
  }

  @Override
  public void delete(List<Long> ids) {
    eventMapper.delete(ids);
  }

  @Override
  public List<Event> checkUnique(Event event) {
    return eventMapper.checkUnique(event);
  }

  @Override
  public List<String> selectOldTo(LocalDateTime createTime) {
    return eventMapper.selectOldTo(createTime);
  }

  @Override
  public List<Event> selectOldEvent(String to, LocalDateTime createTime) {
    return eventMapper.selectOldEvent(to, createTime);
  }

  @Override
  public void deleteOldEvent(LocalDateTime createTime) {
    eventMapper.deleteOldEvent(createTime);
  }
}
