package com.syswin.temail.notification.main.application;

import com.google.gson.Gson;
import com.syswin.temail.notification.foundation.application.JsonService;
import org.springframework.stereotype.Service;

@Service
public class GsonService implements JsonService {

  private final Gson gson = new Gson();

  @Override
  public String toJson(Object src) {
    return gson.toJson(src);
  }

  @Override
  public <T> T fromJson(String json, Class<T> classOfT) {
    return gson.fromJson(json, classOfT);
  }
}
