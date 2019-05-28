package com.syswin.temail.notification.main.util;

import java.util.Base64;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GzipUtilTest {

  @Test
  public void testZip() {
    String s = "test zip";
    byte[] zip = GzipUtil.zip(s.getBytes());
    Assertions.assertThat(new String(GzipUtil.unzip(zip))).isEqualTo(s);
  }

  @Test
  public void testBase64Zip() {
    String s = Base64.getUrlEncoder().encodeToString("test zip".getBytes());
    byte[] zip = GzipUtil.zipWithDecode(s);
    Assertions.assertThat(GzipUtil.unzipWithEncode(zip)).isEqualTo(s);
  }
}