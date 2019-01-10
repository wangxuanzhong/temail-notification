package com.syswin.temail.notification.main.infrastructure;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OssEventMapper {

  void deleteTemail(@Param("to") String to);

}
