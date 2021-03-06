package com.pharosproduction.tweets_aggregator.tweets_consumer;

import com.pharosproduction.tweets_aggregator.common.MicroserviceVerticle;
import io.vertx.core.DeploymentOptions;

public class MainVerticle extends MicroserviceVerticle {

  // Overrides

  @Override
  public void start() throws Exception {
    super.start();

    deployTweetsConsumer();
  }

  // Private

  private void deployTweetsConsumer() {
    String className = TweetsConsumerVerticle.class.getName();
    DeploymentOptions options = new DeploymentOptions()
      .setConfig(mModuleConfig);

    vertx.deployVerticle(className, options);
  }
}
