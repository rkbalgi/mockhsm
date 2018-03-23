package com.github.rkbalgi.apps.mockhsm.thales;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetServerOptions;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.net.NetServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MockThalesHsm {

  public static final Logger log = LogManager.getLogger(MockThalesHsm.class);
  private static final Vertx vertx = Vertx.vertx();

  public static void main(String[] args) {

    RegisterStaticResources.add(vertx);

    ConfigStoreOptions configOptions =
        new ConfigStoreOptions()
            .setType("file")
            .setConfig(new JsonObject().put("path", "mockhsm.json"));

    Future<Object> configFuture = Future.future();
    ConfigRetriever.create(
            vertx.getDelegate(), new ConfigRetrieverOptions().addStore(configOptions))
        .getConfig(
            config -> {
              System.out.println(config.result());
              configFuture.complete();
            });

    NetServer tcpServer = vertx.createNetServer(new NetServerOptions().setPort(1500));
    tcpServer
        .rxListen()
        .subscribe(
            netServer ->
                netServer
                    .connectStream()
                    .handler(
                        netSocket -> {
                          log.debug(
                              () ->
                                  "New client connection - Remote Address: "
                                      + netSocket.remoteAddress());

                          final SocketBuffer buff = new SocketBuffer();
                          buff.handler(
                              buf -> {
                                // execute the message in a separate thread
                                vertx.executeBlocking(
                                    future -> {
                                      // handle message here
                                      Buffer response = ThalesRequestHandler.handle(buf);
                                      future.complete(response);
                                    },
                                    response -> {
                                      // write response here
                                      if (response.succeeded()) {
                                        netSocket.write(
                                            (io.vertx.reactivex.core.buffer.Buffer)
                                                response.result());
                                      } else {
                                        log.error(
                                            "Failed to execute HSM request - "
                                                + buf.toString("hex"));
                                        log.error("Error handling HSM request", response.cause());
                                      }
                                    });
                              });
                          netSocket.handler(buf -> buff.appendBuffer(buf.getDelegate()));
                        }),
            error -> log.error("Failed to start mock hsm", error));
  }
}
