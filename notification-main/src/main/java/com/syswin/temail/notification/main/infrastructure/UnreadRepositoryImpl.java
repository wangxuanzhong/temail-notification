package com.syswin.temail.notification.main.infrastructure;

import com.syswin.temail.notification.main.domains.Unread;
import com.syswin.temail.notification.main.domains.UnreadRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UnreadRepositoryImpl implements UnreadRepository {

  private final UnreadMapper unreadMapper;

  @Autowired
  public UnreadRepositoryImpl(UnreadMapper unreadMapper) {
    this.unreadMapper = unreadMapper;
  }

  @Override
  public int insert(Unread unread) {
    return unreadMapper.insert(unread);
  }

  @Override
  public List<Unread> selectCount(String to) {
    return unreadMapper.selectCount(to);
  }
}
