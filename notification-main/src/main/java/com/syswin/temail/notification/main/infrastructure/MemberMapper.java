package com.syswin.temail.notification.main.infrastructure;

import com.syswin.temail.notification.main.domains.Event;
import com.syswin.temail.notification.main.domains.Member;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberMapper {

  int insert(Event event);

  List<String> selectByGroupTemail(Event event);

  int deleteGroupMember(Event event);

  List<String> selectAvaliableMember(Event event);

  void updateUserStatus(Member member);

  void updateGroupStatus(@Param("groupStatus") int groupStatus, @Param("groupTemail") String groupTemail, @Param("temail") String temail);

  List<String> selectDoNotDisturbGroups(String temail);
}
