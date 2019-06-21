package com.syswin.temail.notification.main.infrastructure;

import com.syswin.temail.notification.main.domains.Event;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author liusen@syswin.com
 */
@Repository
public interface EventMapper {

  /**
   * 插入事件
   *
   * @param event 事件
   * @return 主键
   */
  int insert(Event event);

  /**
   * 查询事件
   *
   * @param to 接收方
   * @param begin 分页起始
   * @param pageSize 页大小
   * @return 查询结果
   */
  List<Event> selectEvents(@Param("to") String to, @Param("begin") Long begin, @Param("pageSize") Integer pageSize);

  /**
   * 查询指定eventType的事件
   *
   * @param to 接收方
   * @param eventTypes 事件类型
   * @return 查询结果
   */
  List<Event> selectPartEvents(@Param("to") String to, @Param("eventTypes") List<Integer> eventTypes);

  /**
   * 查询最新序列号
   *
   * @param to 接收方
   * @return 序列号
   */
  Long selectLastEventSeqId(String to);

  /**
   * 根据msgId查询事件
   *
   * @param event 事件条件
   * @return 查询结果
   */
  List<Event> selectEventsByMsgId(Event event);

  /**
   * 查询重置事件主键
   *
   * @param event 事件条件
   * @return 主键列表
   */
  List<Long> selectResetEvents(Event event);

  /**
   * 根据主键删除
   *
   * @param ids 主键列表
   */
  void delete(List<Long> ids);

  /**
   * 判断事件是否存在
   *
   * @param event 事件条件
   * @return 查询结果
   */
  List<Event> checkUnique(Event event);

  // 以下为清除历史数据功能所用sql

  /**
   * 查询所有接收人
   *
   * @param createTime 截止时间
   * @return 接收人列表
   */
  List<String> selectOldTo(LocalDateTime createTime);

  /**
   * 查询过期事件
   *
   * @param to 接收人
   * @param createTime 截止时间
   * @param eventTypes 事件类型
   * @return 返回结果
   */
  List<Event> selectOldEvent(@Param("to") String to, @Param("createTime") LocalDateTime createTime,
      @Param("eventTypes") List<Integer> eventTypes);

  /**
   * 分页查询过期事件
   *
   * @param createTime 截止时间
   * @param start 分页起始点
   * @param pageSize 页大小
   * @return 返回结果
   */
  List<Long> selectOldEventId(@Param("createTime") LocalDateTime createTime, @Param("start") int start,
      @Param("pageSize") int pageSize);
}
