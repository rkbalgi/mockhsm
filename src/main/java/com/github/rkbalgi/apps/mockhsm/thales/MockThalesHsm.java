package com.github.rkbalgi.apps.mockhsm.thales;

import io.vertx.core.DeploymentOptions;
import io.vertx.reactivex.core.Vertx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MockThalesHsm {

  public static final Logger log = LogManager.getLogger(MockThalesHsm.class);
  private static final Vertx vertx = Vertx.vertx();

  public static void main(String[] args) {

    vertx.deployVerticle(
        MockHsmVerticle.class.getName(),
        new DeploymentOptions(),
        res -> {
          if (res.succeeded()) {
            log.info("Hsm verticle deployed OK");
          } else {
            log.error("HSM verticle deployment error", res.cause());
          }
        });
  }
}
