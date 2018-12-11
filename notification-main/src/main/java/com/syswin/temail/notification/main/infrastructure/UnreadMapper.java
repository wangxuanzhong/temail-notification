package com.syswin.temail.notification.main.infrastructure;

import com.syswin.temail.notification.main.domains.Unread;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface UnreadMapper {

  int insert(Unread unread);

  List<Unread> selectCount(String to);

  void deleteZeroCount();
}
