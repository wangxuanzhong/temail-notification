package com.syswin.temail.notification.main.domains;

import java.util.List;

public interface UnreadRepository {

  int insert(Unread unread);

  List<Unread> selectCount(String to);

  void deleteZeroCount();
}