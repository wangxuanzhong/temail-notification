package com.syswin.temail.notification.main.domains;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

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

  public SyncRelationEvent(String xPacketId, Integer eventType, String from, String to, String myVcardId,
      String oppositeId, String remark, int status, Integer isEmail, Integer contactType, Long createTimeStp,
      Long updateTimeStp) {
    super(xPacketId, eventType, from, to);
    this.myVcardId = myVcardId;
    this.oppositeId = oppositeId;
    this.remark = remark;
    this.status = status;
    this.isEmail = isEmail;
    this.contactType = contactType;
    this.createTimeStp = createTimeStp;
    this.updateTimeStp = updateTimeStp;
  }

  public String getMyVcardId() {
    return myVcardId;
  }

  public void setMyVcardId(String myVcardId) {
    this.myVcardId = myVcardId;
  }

  public String getOppositeId() {
    return oppositeId;
  }

  public void setOppositeId(String oppositeId) {
    this.oppositeId = oppositeId;
  }

  public String getRemark() {
    return remark;
  }

  public void setRemark(String remark) {
    this.remark = remark;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public Integer getIsEmail() {
    return isEmail;
  }

  public void setIsEmail(Integer isEmail) {
    this.isEmail = isEmail;
  }

  public Integer getContactType() {
    return contactType;
  }

  public void setContactType(Integer contactType) {
    this.contactType = contactType;
  }

  public Long getCreateTimeStp() {
    return createTimeStp;
  }

  public void setCreateTimeStp(Long createTimeStp) {
    this.createTimeStp = createTimeStp;
  }

  public Long getUpdateTimeStp() {
    return updateTimeStp;
  }

  public void setUpdateTimeStp(Long updateTimeStp) {
    this.updateTimeStp = updateTimeStp;
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
        "} " + super.toString();
  }
}