package com.pharosproduction.tweets_aggregator.tweets_consumer;

import com.pharosproduction.tweets_aggregator.common.MicroserviceVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;

public class MainVerticle extends MicroserviceVerticle {

  // Variables

  private JsonObject mModuleConfig;

  // Overrides

  @Override
  public void start() throws Exception {
    super.start();

    mModuleConfig = config();

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
