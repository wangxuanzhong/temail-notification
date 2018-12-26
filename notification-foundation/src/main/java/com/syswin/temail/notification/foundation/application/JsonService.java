package com.syswin.temail.notification.foundation.application;

import java.lang.reflect.Type;

public interface JsonService {

  String toJson(Object src);

  <T> T fromJson(String json, Class<T> classOfT);

  <T> T fromJson(String json, Type typeOfT);
}
