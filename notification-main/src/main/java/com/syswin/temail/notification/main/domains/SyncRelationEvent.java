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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;

/**
 * 关系服务多端同步参数
 *
 * @author liusen@syswin.com
 */
@JsonInclude(Include.NON_NULL)
public class SyncRelationEvent extends SyncEvent {

  private String myVcardId;
  private String oppositeId;
  private String remark;
  private int status;
  private Integer isEmail;
  private Integer contactType;
  private Long createTimeStp;
  private Long updateTimeStp;
  private List<String> deleteList;

  public void setMyVcardId(String myVcardId) {
    this.myVcardId = myVcardId;
  }

  public void setOppositeId(String oppositeId) {
    this.oppositeId = oppositeId;
  }

  public void setRemark(String remark) {
    this.remark = remark;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public void setIsEmail(Integer isEmail) {
    this.isEmail = isEmail;
  }

  public void setContactType(Integer contactType) {
    this.contactType = contactType;
  }

  public void setCreateTimeStp(Long createTimeStp) {
    this.createTimeStp = createTimeStp;
  }

  public void setUpdateTimeStp(Long updateTimeStp) {
    this.updateTimeStp = updateTimeStp;
  }

  public void setDeleteList(List<String> deleteList) {
    this.deleteList = deleteList;
  }

  @Override
  public String toString() {
    return "SyncRelationEvent{" +
        "myVcardId='" + myVcardId + '\'' +
        ", oppositeId='" + oppositeId + '\'' +
        ", remark='" + remark + '\'' +
        ", status=" + status +
        ", isEmail=" + isEmail +
        ", contactType=" + contactType +
        ", createTimeStp=" + createTimeStp +
        ", updateTimeStp=" + updateTimeStp +
        ", deleteList=" + deleteList +
        "} " + super.toString();
  }
}