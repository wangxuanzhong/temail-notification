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
public class GsonServiceImpl implements IJsonService {

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
