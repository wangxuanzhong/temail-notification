package com.syswin.temail.notification.main.infrastructure;

import com.syswin.temail.notification.main.domains.Unread;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 * @author liusen
 */
@Repository
public interface UnreadMapper {

  /**
   * 添加未读数记录
   *
   * @param unread 未读数参数
   * @return 主键
   */
  int insert(Unread unread);

  /**
   * 查询未读数
   *
   * @param to 接收人
   * @return 查询结果
   */
  List<Unread> selectCount(String to);

  /**
   * 清除未读数为零的数据
   */
  void deleteZeroCount();
}
