package com.pharosproduction.tweets_aggregator.flink_consumer;

import com.pharosproduction.tweets_aggregator.common.MicroserviceVerticle;
import io.vertx.core.DeploymentOptions;

public class MainVerticle extends MicroserviceVerticle {

  @Override
  public void start() throws Exception {
    super.start();

    deployFlink();
  }

  // Private

  private void deployFlink() {
    String className = FlinkVerticle.class.getName();
    DeploymentOptions options = new DeploymentOptions()
      .setConfig(mModuleConfig);

    vertx.deployVerticle(className, options);
  }
}
