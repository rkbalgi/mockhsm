package com.github.rkbalgi.apps.mockhsm.thales;

import io.netty.buffer.ByteBufUtil;
import io.vertx.core.buffer.Buffer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.Charset;

public class ThalesRequestHandler {
  private static final Logger log = LogManager.getLogger(ThalesRequestHandler.class);

  public static Buffer handle(Buffer reqBuf) {
    log.debug(() -> "Received HSM request - " + ByteBufUtil.hexDump(reqBuf.getBytes()));

    // the first two bytes are the command code, let's grab that and switch
    String commandCode = reqBuf.slice(0, 2).toString(Charset.forName("US-ASCII"));
    Buffer responseBuf = null;
    switch (commandCode) {
      case "NC":
        {
          responseBuf = Buffer.buffer("ND0089888777");
          break;
        }
      default:
        {
          throw new IllegalArgumentException("Unsupported HSM command code - " + commandCode);
        }
    }

    Buffer responseBufWithMli = Buffer.buffer();
    responseBufWithMli.appendShort((short) responseBuf.length());
    responseBufWithMli.appendBuffer(responseBuf);

    return responseBufWithMli;
  }
}
