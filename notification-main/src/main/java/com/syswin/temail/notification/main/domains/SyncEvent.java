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

package com.syswin.temail.notification.main.domains;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * @author liusen@syswin.com
 */
@JsonInclude(Include.NON_NULL)
public class SyncEvent {

  @JsonIgnore
  private Long id;
  private String xPacketId;
  private Long eventSeqId;
  private Integer eventType;

  private String from;
  private String to;

  @JsonIgnore
  private String header;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getxPacketId() {
    return xPacketId;
  }

  public void setxPacketId(String xPacketId) {
    this.xPacketId = xPacketId;
  }

  public Long getEventSeqId() {
    return eventSeqId;
  }

  public void setEventSeqId(Long eventSeqId) {
    this.eventSeqId = eventSeqId;
  }

  public Integer getEventType() {
    return eventType;
  }

  public void setEventType(Integer eventType) {
    this.eventType = eventType;
  }

  public String getFrom() {
    return from;
  }

  public void setFrom(String from) {
    this.from = from;
  }

  public String getTo() {
    return to;
  }

  public void setTo(String to) {
    this.to = to;
  }

  public String getHeader() {
    return header;
  }

  public void setHeader(String header) {
    this.header = header;
  }

  @Override
  public String toString() {
    return "SyncEvent{" +
        "id=" + id +
        ", xPacketId='" + xPacketId + '\'' +
        ", eventSeqId=" + eventSeqId +
        ", eventType=" + eventType +
        ", from='" + from + '\'' +
        ", to='" + to + '\'' +
        ", header='" + header + '\'' +
        '}';
  }
}