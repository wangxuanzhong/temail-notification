/*
 * MIT License
 *
 * Copyright (c) 2019 Syswin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.syswin.temail.notification.main.infrastructure;

import com.syswin.temail.notification.main.domains.Event;
import com.syswin.temail.notification.main.domains.Member;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author liusen@syswin.com
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
