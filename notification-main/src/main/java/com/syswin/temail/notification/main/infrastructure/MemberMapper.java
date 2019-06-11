package com.syswin.temail.notification.main.infrastructure;

import com.syswin.temail.notification.main.domains.Event;
import com.syswin.temail.notification.main.domains.Member;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author liusen
 */
@Repository
public interface MemberMapper {

  /**
   * 插入成员
   *
   * @param event 群成员参数
   * @return 主键
   */
  int insert(Event event);


  /**
   * 查询群成员
   *
   * @param event 群成员条件
   * @return 成员列表
   */
  List<String> selectMember(Event event);

  /**
   * 删除群成员
   *
   * @param event 群成员条件
   */
  void deleteGroupMember(Event event);

  /**
   * 更新群成员个人状态
   *
   * @param member 群成员条件
   */
  void updateUserStatus(Member member);

  /**
   * 查询群成员个人状态
   *
   * @param temail 群成员
   * @param groupTemail 群
   * @return 修改结果
   */
  Integer selectUserStatus(@Param("temail") String temail, @Param("groupTemail") String groupTemail);

  /**
   * 更新角色
   *
   * @param event 群成员条件
   */
  void updateRole(Event event);
}
