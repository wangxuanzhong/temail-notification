package com.syswin.temail.notification.foundation.application;

import java.lang.reflect.Type;

/**
 * @author liusen
 */
public interface IJsonService {

  /**
   * 将对象转化为json
   *
   * @param src 对象
   * @return json
   */
  String toJson(Object src);

  /**
   * 转换json为指定类型实体对象
   *
   * @param json json串
   * @param classOfT 类型
   * @param <T> 泛型
   * @return 对象
   */
  <T> T fromJson(String json, Class<T> classOfT);

  /**
   * 转换json为指定类型对象
   *
   * @param json json
   * @param typeOfT 类型
   * @param <T> 泛型
   * @return 对象
   */
  <T> T fromJson(String json, Type typeOfT);
}
