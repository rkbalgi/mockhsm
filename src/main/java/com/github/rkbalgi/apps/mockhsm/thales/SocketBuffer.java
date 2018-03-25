package com.github.rkbalgi.apps.mockhsm.thales;

import io.netty.buffer.ByteBufUtil;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SocketBuffer {
  private static final Logger log = LogManager.getLogger(SocketBuffer.class);
  private Buffer buff = Buffer.buffer();
  private Handler<Buffer> handler;

  public void handler(Handler<Buffer> handler) {
    this.handler = handler;
  }

  public void appendBuffer(Buffer buf) {
    log.debug(() -> "Appending - " + ByteBufUtil.hexDump(buf.getBytes()));

    buff.appendBuffer(buf);
    handleInternal();
  }

  private void handleInternal() {

    if (buff.length() > 2) {
      int len = buff.getUnsignedShort(0);
      if (len <= buff.length() - 2) {
        // we have enough data
        Buffer msgBuf = buff.getBuffer(2, 2 + len);
        buff = buff.slice(2 + len, buff.length());
        if (handler != null) {
          handler.handle(msgBuf);
          // make another attempt if there are more queued
          // up messages
          handleInternal();
        }
      }
    }
  }
}
