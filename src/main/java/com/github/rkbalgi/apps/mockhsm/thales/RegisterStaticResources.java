package com.github.rkbalgi.apps.mockhsm.thales;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.handler.StaticHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RegisterStaticResources {
  public static final Logger log = LogManager.getLogger(MockThalesHsm.class);

  public static void add(Vertx vertx) {

    log.info("Starting Vertx Play ..");
    System.setProperty("vertx.disableFileCPResolving", "false");
    System.setProperty("vertx.disableFileCaching", "true");

    Router router = Router.router(vertx);
    router.route(HttpMethod.GET, "/static/*").handler(StaticHandler.create());
    router.route(HttpMethod.GET, "/static_/*").handler(StaticHandler.create());
    router.route(HttpMethod.GET, "/test/*").handler(StaticHandler.create());
    router
        .route(HttpMethod.GET, "/sayhello")
        .handler(
            routingContext -> {
              routingContext.request().response().end("Hello World\n");
            });

    vertx
        .createHttpServer(new HttpServerOptions().setPort(12345))
        .requestHandler(router::accept)
        .rxListen()
        .subscribe(
            (httpServer, t) -> {
              if (t != null) {
                log.error("MockThalesHsm HTTP server failed", t);

                vertx.close();
              } else {
                // success
                log.debug(
                    "{} at port {}",
                    () -> "MockThalesHsm HTTP server started",
                    () -> httpServer.actualPort());
              }
            });
  }
}
