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
  List<Event> selectEventsByPacketIdAndEventType(Event event);

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
