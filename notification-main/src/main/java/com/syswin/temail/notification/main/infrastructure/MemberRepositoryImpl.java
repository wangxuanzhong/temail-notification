package com.syswin.temail.notification.main.infrastructure;

import com.syswin.temail.notification.main.domains.Event;
import com.syswin.temail.notification.main.domains.MemberRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MemberRepositoryImpl implements MemberRepository {

  private final MemberMapper eventMapper;

  @Autowired
  public MemberRepositoryImpl(MemberMapper eventMapper) {
    this.eventMapper = eventMapper;
  }


  @Override
  public int insert(Event event) {
    return eventMapper.insert(event);
  }

  @Override
  public List<String> selectByGroupTemail(Event event) {
    return eventMapper.selectByGroupTemail(event);
  }

  @Override
  public int deleteGroupMember(Event event) {
    return eventMapper.deleteGroupMember(event);
  }
}
