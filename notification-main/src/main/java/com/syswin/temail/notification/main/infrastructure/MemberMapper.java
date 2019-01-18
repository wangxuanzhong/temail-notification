package com.syswin.temail.notification.main.infrastructure;

import com.syswin.temail.notification.main.domains.Event;
import com.syswin.temail.notification.main.domains.Member;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberMapper {

  int insert(Event event);

  List<String> selectMember(Event event);

  void deleteGroupMember(Event event);

  void updateUserStatus(Member member);

  Integer selectUserStatus(@Param("temail") String temail, @Param("groupTemail") String groupTemail);

  void updateRole(Event event);
}
