package com.syswin.temail.notification.foundation.application;

import java.lang.reflect.Type;

public interface IJsonService {

  String toJson(Object src);

  <T> T fromJson(String json, Class<T> classOfT);

  <T> T fromJson(String json, Type typeOfT);
}
