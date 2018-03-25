package com.github.rkbalgi.apps.mockhsm;

import io.netty.buffer.ByteBufUtil;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;

public class MockHsmClient {

  public static void main(String[] args) {

    Vertx vertx = Vertx.vertx();
    NetClient netClient =
        vertx
            .createNetClient()
            .connect(
                8443,
                "localhost",
                res -> {
                  if (res.succeeded()) {
                    NetSocket socket = res.result();
                    socket.handler(
                        buf -> {
                          System.out.println(
                              "Received response - " + ByteBufUtil.hexDump(buf.getBytes()));
                        });

                    socket.write(Buffer.buffer(ByteBufUtil.decodeHexDump("0002")));
                    socket.write(Buffer.buffer("NC"));
                  }
                });
    vertx.setTimer(2000,l->{
       netClient.close();
       vertx.close();
    });
  }
}
