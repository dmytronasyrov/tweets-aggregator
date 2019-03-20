package com.pharosproduction.tweets_aggregator.api_frontend;

import com.pharosproduction.tweets_aggregator.common.MicroserviceVerticle;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import io.vertx.servicediscovery.rest.ServiceDiscoveryRestEndpoint;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class MainVerticle extends MicroserviceVerticle {

  @Override
  public void start(Future<Void> future) throws Exception {
    super.start();

    Router router = Router.router(vertx);

    SockJSHandler sockJSHandler = SockJSHandler.create(vertx);
    BridgeOptions options = new BridgeOptions();
    options.addOutboundPermitted(new PermittedOptions().setAddress("api.tweets"));

    sockJSHandler.bridge(options);

    Set<String> allowedHeaders = new HashSet<>();
    allowedHeaders.add("x-requested-with");
    allowedHeaders.add("origin");
    allowedHeaders.add("Content-Type");
    allowedHeaders.add("accept");
    allowedHeaders.add("Access-Control-Allow-Origin");
    allowedHeaders.add("Access-Control-Request-Method");
    allowedHeaders.add("Access-Control-Allow-Credentials");
    allowedHeaders.add("Access-Control-Allow-Headers");

    Set<HttpMethod> allowedMethods = new HashSet<>();
    allowedMethods.add(HttpMethod.GET);
    allowedMethods.add(HttpMethod.POST);
    allowedMethods.add(HttpMethod.OPTIONS);

    CorsHandler cors = CorsHandler.create("http://localhost:3000")
      .allowedHeaders(allowedHeaders)
      .allowedMethods(allowedMethods)
      .allowCredentials(true);

    router.route().handler(cors);
    router.route("/eventbus/*").handler(sockJSHandler);

    ServiceDiscoveryRestEndpoint.create(router, mDiscovery);

    vertx.createHttpServer()
      .requestHandler(router::accept)
      .listen(8080, ar -> {
        if (ar.failed()) {
          future.fail(ar.cause());
        } else {
          future.complete();
        }
      });


    Map<String, String> config = new HashMap<>();
    config.put("bootstrap.servers", "localhost:9092");
    config.put("group.id", "frontend.tweets");
    config.put("auto.offset.reset", "earliest");
    config.put("enable.auto.commit", "false");
    config.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    config.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

    KafkaConsumer<String, String> consumer = KafkaConsumer.create(vertx, config);

    consumer.subscribe("streaming.tweets", ar -> {
      if (ar.succeeded()) {
        System.out.println("subscribed");
      } else {
        System.out.println("Could not subscribe " + ar.cause().getMessage());
      }
    });

    consumer.handler(record -> {
      System.out.println("Processing key=" + record.key() + ",value=" + record.value() +
        ",partition=" + record.partition() + ",offset=" + record.offset());
      JsonObject msg = new JsonObject()
        .put("id", record.key())
        .put("text", record.value());
      vertx.eventBus().publish("api.tweets", msg);
    });
  }
}
