package com.syswin.temail.notification.main.infrastructure;

import org.springframework.stereotype.Repository;

@Repository
public interface NotificationOssMapper {

  void deleteTemail(String to);

}
