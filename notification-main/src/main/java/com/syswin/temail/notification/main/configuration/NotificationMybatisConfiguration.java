package com.syswin.temail.notification.main.configuration;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * mybatis配置类
 *
 * @author liusen
 */
@Configuration
@MapperScan("com.syswin.temail.notification.main.infrastructure")
public class NotificationMybatisConfiguration {

}
