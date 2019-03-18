package com.pharosproduction.tweets_aggregator.api_mobile;

import io.vertx.core.AbstractVerticle;

public class ApiVerticle extends AbstractVerticle {

  @Override
  public void start() throws Exception {
    super.start();

    HelloRequest request = HelloRequest.newBuilder().setHelloId("helloId").build();
  }
}
