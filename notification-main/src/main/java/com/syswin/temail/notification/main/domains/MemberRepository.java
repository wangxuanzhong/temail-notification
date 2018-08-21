package com.syswin.temail.notification.main.domains;

import java.util.List;

public interface MemberRepository {

  int insert(Event event);

  List<String> selectByGroupTemail(Event event);

  int deleteGroupMember(Event event);
}