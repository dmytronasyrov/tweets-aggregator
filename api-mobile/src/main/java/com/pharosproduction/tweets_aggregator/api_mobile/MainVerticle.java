package com.pharosproduction.tweets_aggregator.api_mobile;

import com.pharosproduction.tweets_aggregator.common.MicroserviceVerticle;
import io.vertx.core.DeploymentOptions;

public class MainVerticle extends MicroserviceVerticle {

  // Overrides

  @Override
  public void start() throws Exception {
    super.start();

    deployApi();
  }

  // Private

  private void deployApi() {
    String className = ApiVerticle.class.getName();
    DeploymentOptions options = new DeploymentOptions()
      .setConfig(mModuleConfig);

    vertx.deployVerticle(className, options);
  }
}
