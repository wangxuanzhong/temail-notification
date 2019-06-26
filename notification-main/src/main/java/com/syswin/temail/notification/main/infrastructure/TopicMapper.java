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

import com.syswin.temail.notification.main.domains.TopicEvent;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author liusen@syswin.com
 */
@Repository
public interface TopicMapper {

  /**
   * 插入话题事件
   *
   * @param topicEvent 话题事件参数
   * @return 主键
   */
  int insert(TopicEvent topicEvent);

  /**
   * 查询话题事件
   *
   * @param to 接收方
   * @param begin 分页起始
   * @param pageSize 页大小
   * @return 查询结果
   */
  List<TopicEvent> selectEvents(@Param("to") String to, @Param("begin") Long begin,
      @Param("pageSize") Integer pageSize);

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
   * @param msgId 消息id
   * @return 查询结果
   */
  List<TopicEvent> selectEventsByMsgId(String msgId);

  /**
   * 根据topicId查询事件
   *
   * @param topicId 话题id
   * @return 查询结果
   */
  List<TopicEvent> selectEventsByTopicId(String topicId);

  /**
   * 删除过期事件
   *
   * @param createTime 截止时间
   */
  void deleteOldTopic(LocalDateTime createTime);
}
