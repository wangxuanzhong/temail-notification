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

public class GzipUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final NotificationPacketUtil notificationPacketUtil = new NotificationPacketUtil();

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
      LOGGER.error("zip data error: ", e);
      throw new BaseException("zip data error: ", e);
    }
    return bos.toByteArray();
  }

  public static byte[] zipWithDecode(final String data) {
    return zip(notificationPacketUtil.decodeData(data));
  }

  public static byte[] unzip(final byte[] data) {
    ByteArrayInputStream bis = new ByteArrayInputStream(data);
    try {
      GZIPInputStream gzip = new GZIPInputStream(bis);
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      return unzipByteArrayOutputStream(gzip, bos).toByteArray();
    } catch (IOException e) {
      LOGGER.error("unzip data error: ", e);
      throw new BaseException("unzip data error: ", e);
    }
  }

  private static ByteArrayOutputStream unzipByteArrayOutputStream(GZIPInputStream gzip, ByteArrayOutputStream bos) throws IOException {
    byte[] buf = new byte[1024];
    int num = -1;
    while ((num = gzip.read(buf, 0, buf.length)) != -1) {
      bos.write(buf, 0, num);
    }
    return bos;
  }

  public static String unzipWithEncode(final byte[] data) {
    return notificationPacketUtil.encodeData(unzip(data));
  }
}