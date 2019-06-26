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

import com.syswin.temail.notification.foundation.exceptions.BaseException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author liusen@syswin.com
 */
public class GzipUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final NotificationPacketUtil NOTIFICATION_PACKET_UTIL = new NotificationPacketUtil();

  private GzipUtil() {
    throw new IllegalStateException("Utility class");
  }

  public static byte[] zip(final byte[] data) {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try {
      GZIPOutputStream gzip = new GZIPOutputStream(bos);
      gzip.write(data);
      gzip.finish();
    } catch (IOException e) {
      throw new BaseException("zip data error: ", e);
    }
    return bos.toByteArray();
  }

  public static byte[] zipWithDecode(final String data) {
    return zip(NOTIFICATION_PACKET_UTIL.decodeData(data));
  }

  public static byte[] unzip(final byte[] data) {
    ByteArrayInputStream bis = new ByteArrayInputStream(data);
    try {
      GZIPInputStream gzip = new GZIPInputStream(bis);
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      return unzipByteArrayOutputStream(gzip, bos).toByteArray();
    } catch (IOException e) {
      throw new BaseException("unzip data error: ", e);
    }
  }

  private static ByteArrayOutputStream unzipByteArrayOutputStream(GZIPInputStream gzip, ByteArrayOutputStream bos)
      throws IOException {
    byte[] buf = new byte[1024];
    int num = -1;
    while ((num = gzip.read(buf, 0, buf.length)) != -1) {
      bos.write(buf, 0, num);
    }
    return bos;
  }

  public static String unzipWithEncode(final byte[] data) {
    return NOTIFICATION_PACKET_UTIL.encodeData(unzip(data));
  }
}