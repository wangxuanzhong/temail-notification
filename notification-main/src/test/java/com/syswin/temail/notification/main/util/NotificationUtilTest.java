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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class NotificationUtilTest {

  @Test
  public void testCopyField() {
    C1 c1 = new C1();
    c1.typeInt = 1;
    c1.typeLong = 2L;
    c1.typeString = "string";
    c1.typeBoolean = false;
    c1.typeList = Arrays.asList("a", "b", "c");
    c1.typeMap = new HashMap<>();
    c1.typeMap.put("a", "a");
    c1.typeMap.put("b", "b");
    c1.typeMap.put("c", "c");

    C2 c2 = new C2();
    C3 c3 = new C3();

    NotificationUtil.copyField(c1, c2);
    Assertions.assertThat(c2.typeInt).isEqualTo(c1.typeInt);
    Assertions.assertThat(c2.typeLong).isEqualTo(c1.typeLong);
    Assertions.assertThat(c2.typeString).isEqualTo(c1.typeString);
    Assertions.assertThat(c2.typeBoolean).isEqualTo(c1.typeBoolean);
    Assertions.assertThat(c2.typeList).isEqualTo(c1.typeList);
    Assertions.assertThat(c2.typeMap).isEqualTo(c1.typeMap);
    Assertions.assertThat(c2.extParam1).isNull();
    Assertions.assertThat(c2.extParam2).isNull();

    NotificationUtil.copyField(c1, c3);
    Assertions.assertThat(c3.typeInt).isNotEqualTo(c1.typeInt);
    Assertions.assertThat(c3.typeLong).isNotEqualTo(c1.typeLong);
    Assertions.assertThat(c3.typeString).isEqualTo(c1.typeString);
    Assertions.assertThat(c3.typeBoolean).isNotEqualTo(c1.typeBoolean);
    Assertions.assertThat(c3.typeList).isNotEqualTo(c1.typeList);
    Assertions.assertThat(c3.typeMap).isNotEqualTo(c1.typeMap);

  }

  class C1 {

    public Integer typeInt;
    public Long typeLong;
    public String typeString;
    public Boolean typeBoolean;
    public List<String> typeList;
    public Map<String, Object> typeMap;
  }

  class C2 {

    public Integer typeInt;
    public Long typeLong;
    public String typeString;
    public Boolean typeBoolean;
    public List<String> typeList;
    public Map<String, Object> typeMap;
    public String extParam1;
    public List<String> extParam2;
  }

  class C3 {

    public String typeInt;
    public String typeLong;
    public String typeString;
    public String typeBoolean;
    public String typeList;
    public String typeMap;
  }
}