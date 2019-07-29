/*
 * MIT License
 *
 * Copyright (c) 2019 Syswin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.syswin.temail.notification.main.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.syswin.temail.notification.foundation.exceptions.BaseException;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author liusen@syswin.com
 */
public class NotificationUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  /**
   * 将obj1的字段不为空的值复制到obj2的同名字段
   */
  public static <T1, T2> T2 copyField(T1 obj1, T2 obj2) {
    if (obj1 == null || obj2 == null) {
      return null;
    }

    Class clazz1 = obj1.getClass();
    Class clazz2 = obj2.getClass();

    Field[] fields1 = clazz1.getDeclaredFields();
    Field[] fields2 = clazz2.getDeclaredFields();

    for (Field f1 : fields1) {
      if (Modifier.isFinal(f1.getModifiers())) {
        continue;
      }
      for (Field f2 : fields2) {
        if (f1.getName().equals(f2.getName()) && f1.getType().getName().equals(f2.getType().getName())) {
          try {
            f1.setAccessible(true);
            Object val = f1.get(obj1);
            if (val != null) {
              f2.setAccessible(true);
              f2.set(obj2, val);
              f2.setAccessible(false);
            }
            f1.setAccessible(false);
          } catch (IllegalAccessException e) {
            throw new BaseException("copy field error!", e);
          }
        }
      }
    }

    return obj2;
  }

  /**
   * 删除入参json中非透传的参数
   */
  public static JsonObject removeUsedField(String params, List<String> removeKeys) {
    // 如果传入的
    if (params != null && !params.isEmpty()) {
      JsonObject jsonObject = new JsonParser().parse(params).getAsJsonObject();
      if (removeKeys != null) {
        removeKeys.forEach(jsonObject::remove);
      }
      return jsonObject;
    } else {
      return new JsonObject();
    }
  }

  /**
   * 合并两个json
   */
  public static String combineTwoJson(String json1, String json2) {
    if (json1 == null || json1.isEmpty()) {
      return json2;
    }

    if (json2 == null || json2.isEmpty()) {
      return json1;
    }

    JsonObject result = new JsonObject();
    JsonParser parser = new JsonParser();
    parser.parse(json1).getAsJsonObject().entrySet().forEach(entry -> result.add(entry.getKey(), entry.getValue()));
    parser.parse(json2).getAsJsonObject().entrySet().forEach(entry -> result.add(entry.getKey(), entry.getValue()));

    return result.toString();
  }
}
