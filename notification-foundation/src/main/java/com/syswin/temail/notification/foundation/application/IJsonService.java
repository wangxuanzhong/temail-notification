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

package com.syswin.temail.notification.foundation.application;

import java.lang.reflect.Type;

/**
 * @author liusen@syswin.com
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
