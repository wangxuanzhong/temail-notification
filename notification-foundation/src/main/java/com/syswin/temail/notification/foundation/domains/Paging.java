package com.syswin.temail.notification.foundation.domains;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class Paging {

  private Integer total;
  private Integer totalPages;
  private Integer pageSize;
  private Integer page;

  private Integer start;

  public Paging() {

  }

  public Paging(Integer total, Integer totalPages, Integer pageSize, Integer page) {
    this.total = total;
    this.totalPages = totalPages;
    this.pageSize = pageSize;
    this.page = page;
  }

  public Paging(Integer total, Integer pageSize, Integer page) {
    this.total = total;
    this.totalPages = (int) Math.ceil((double) total / pageSize);
    this.pageSize = pageSize;
    this.page = page;
  }

  public Integer getTotal() {
    return total;
  }

  public void setTotal(Integer total) {
    this.total = total;
  }

  public Integer getTotalPages() {
    return totalPages;
  }

  public void setTotalPages(Integer totalPages) {
    this.totalPages = totalPages;
  }

  public Integer getPageSize() {
    return pageSize != null ? pageSize : 20;
  }

  public void setPageSize(Integer pageSize) {
    this.pageSize = pageSize;
  }

  public Integer getPage() {
    return page != null ? page : 1;
  }

  public void setPage(Integer page) {
    this.page = page;
  }

  public Integer getStart() {
    if (pageSize == null) {
      pageSize = 20;
    }
    if (page == null) {
      page = 1;
    }
    return (page - 1) * pageSize;
  }

  public void setStart(Integer start) {
    this.start = start;
  }

  @Override
  public String toString() {
    return "Paging:{total=" + total + ", totalPages=" + totalPages + ", pageSize=" + pageSize + ", page=" + page
        + ", start=" + start + "}";
  }
}
