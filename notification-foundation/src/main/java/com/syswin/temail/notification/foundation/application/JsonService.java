package com.syswin.temail.notification.foundation.application;

public interface JsonService {

  String toJson(Object src);

  <T> T fromJson(String json, Class<T> classOfT);

}
