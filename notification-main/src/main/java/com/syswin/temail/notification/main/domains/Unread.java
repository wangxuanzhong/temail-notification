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

package com.syswin.temail.notification.main.domains;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;
import java.util.Map;

/**
 * @author liusen@syswin.com
 */
@JsonInclude(Include.NON_NULL)
public class Unread {

  private String to;
  private Map<String, Integer> cleardUnreadMap;
  private Map<String, List<String>> unreadMap;

  public Unread(String to, Map<String, Integer> cleardUnreadMap, Map<String, List<String>> unreadMap) {
    this.to = to;
    this.cleardUnreadMap = cleardUnreadMap;
    this.unreadMap = unreadMap;
  }

  public String getTo() {
    return to;
  }

  public Map<String, Integer> getCleardUnreadMap() {
    return cleardUnreadMap;
  }

  public Map<String, List<String>> getUnreadMap() {
    return unreadMap;
  }
}