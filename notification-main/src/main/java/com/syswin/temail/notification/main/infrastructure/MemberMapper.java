package com.syswin.temail.notification.main.infrastructure;

import com.syswin.temail.notification.main.domains.Event;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberMapper {

  int insert(Event event);

  List<String> selectByGroupTemail(Event event);

  int deleteGroupMember(Event event);
}
