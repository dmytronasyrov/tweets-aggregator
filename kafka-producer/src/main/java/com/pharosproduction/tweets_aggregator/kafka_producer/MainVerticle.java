package com.pharosproduction.tweets_aggregator.kafka_producer;

import com.pharosproduction.tweets_aggregator.common.MicroserviceVerticle;
import io.vertx.core.DeploymentOptions;

public class MainVerticle extends MicroserviceVerticle {

  // Overrides

  @Override
  public void start() throws Exception {
    super.start();

    deployTweetsProducer();
  }

  // Private

  private void deployTweetsProducer() {
    String className = TweetsProducerVerticle.class.getName();
    DeploymentOptions options = new DeploymentOptions()
      .setConfig(mModuleConfig);

    vertx.deployVerticle(className, options);
  }
}
