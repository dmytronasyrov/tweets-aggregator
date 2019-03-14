package com.pharosproduction.tweets_aggregator.eventbus_bridge;

import com.pharosproduction.tweets_aggregator.common.MicroserviceVerticle;
import io.vertx.core.DeploymentOptions;

public class MainVerticle extends MicroserviceVerticle {

  // Overrides

  @Override
  public void start() throws Exception {
    super.start();

    deployBridge();
  }

  // Private

  private void deployBridge() {
    String className = BridgeVerticle.class.getName();
    DeploymentOptions options = new DeploymentOptions()
      .setConfig(mModuleConfig);

    vertx.deployVerticle(className, options);
  }
}
