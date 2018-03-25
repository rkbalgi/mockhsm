package com.github.rkbalgi.apps.mockhsm.thales;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MockHsmVerticle extends AbstractVerticle {
  public static final Logger log = LogManager.getLogger(MockThalesHsm.class);

  @Override
  public void start(Future<Void> startFuture) throws Exception {

    // RegisterStaticResources.add(new Vertx(vertx));

    ConfigStoreOptions configOptions =
        new ConfigStoreOptions()
            .setType("file")
            .setConfig(new JsonObject().put("path", "mockhsm.json"));

    Future<Object> configFuture = Future.future();
    configFuture.setHandler(
        result -> {
          if (result.succeeded()) {

            JsonObject config = (JsonObject) result.result();
            log.debug("config = {}", config);
            startMockHsm(config, startFuture);
          }
        });
    ConfigRetriever.create(vertx, new ConfigRetrieverOptions().addStore(configOptions))
        .getConfig(
            result -> {
              if (result.succeeded()) {
                configFuture.complete(result.result());
              } else {
                configFuture.fail(result.cause());
              }
            });
  }

  private void startMockHsm(JsonObject config, Future<Void> startFuture) {

    log.debug("Starting Mock Hsm ..");
    System.out.println(config.getInteger("port"));
    log.debug("Attempting to listen on port -  {}", config.getInteger("port"));
    NetServer tcpServer = vertx.createNetServer();
    log.debug("Setting connect handler -");
    tcpServer.connectHandler(
        netSocket -> {
          log.debug(() -> "New client connection - Remote Address: " + netSocket.remoteAddress());

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
                        netSocket.write((Buffer) response.result());
                      } else {
                        log.error("Failed to execute HSM request - " + buf.toString("hex"));
                        log.error("Error handling HSM request", response.cause());
                      }
                    });
              });
          netSocket.handler(buf -> buff.appendBuffer(buf));
        });

    tcpServer.listen(
        config.getInteger("port"),
        result -> {
          if (result.succeeded()) {
            log.info("Mock Thales HSM started OK, listening at {}", config.getInteger("port"));
            startFuture.complete();
          } else {
            log.error("Failed to start mock hsm", result.cause());
            startFuture.fail(result.cause());
          }
        });
    /*.rxListen()
    .subscribe(
        server -> {
          log.info("Mock Thales HSM started OK, listening at {}", config.getInteger("port"));
        },
        error -> log.error("Failed to start mock hsm", error));*/
  }
}
