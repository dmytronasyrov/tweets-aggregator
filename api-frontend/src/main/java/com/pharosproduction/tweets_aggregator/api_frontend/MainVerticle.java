package com.pharosproduction.tweets_aggregator.api_frontend;

import com.pharosproduction.tweets_aggregator.common.MicroserviceVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import io.vertx.servicediscovery.rest.ServiceDiscoveryRestEndpoint;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MainVerticle extends MicroserviceVerticle {

  @Override
  public void start(Future<Void> future) throws Exception {
    super.start();

    deploySocket();
    deployKafkaConsumer();
//    ServiceDiscoveryRestEndpoint.create(router, mDiscovery);
  }

  private void deploySocket() {
    String className = SocketVerticle.class.getName();
    DeploymentOptions options = new DeploymentOptions()
      .setConfig(mModuleConfig);

    vertx.deployVerticle(className, options);
  }

  private void deployKafkaConsumer() {
    String className = KafkaVerticle.class.getName();
    DeploymentOptions options = new DeploymentOptions()
      .setConfig(mModuleConfig);

    vertx.deployVerticle(className, options);
  }
}
