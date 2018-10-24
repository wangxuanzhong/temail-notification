package com.syswin.temail.notification.foundation.application;

public interface JsonService {

  public String toJson(Object src);

  public <T> T fromJson(String json, Class<T> classOfT);

}
