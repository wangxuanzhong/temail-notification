package com.syswin.temail.notification.main.application;

import com.google.gson.Gson;
import com.syswin.temail.notification.foundation.application.IJsonService;
import java.lang.reflect.Type;
import org.springframework.stereotype.Service;

/**
 * gson工具类
 *
 * @author liusen@syswin.com
 */
@Service
public class NotificationGsonServiceImpl implements IJsonService {

  private final Gson gson = new Gson();

  @Override
  public String toJson(Object src) {
    return gson.toJson(src);
  }

  @Override
  public <T> T fromJson(String json, Class<T> classOfT) {
    return gson.fromJson(json, classOfT);
  }

  @Override
  public <T> T fromJson(String json, Type typeOfT) {
    return gson.fromJson(json, typeOfT);
  }


}
