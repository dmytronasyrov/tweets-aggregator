package com.pharosproduction.tweets_aggregator.api_frontend;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import io.vertx.kafka.client.consumer.KafkaConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.HashMap;
import java.util.Map;

public class KafkaVerticle extends AbstractVerticle {

  // Constants

  private static final String KAFKA_GROUP = "streaming.frontend";
  private static final String KAFKA_OFFSET_RESET = "earliest";
  private static final String KAFKA_AUTO_COMMIT = "false";

  // Variables

  private Config mConfig;

  // Overrides

  @Override
  public void start(Future<Void> startFuture) {
    mConfig= new Config(config());

    Map kafkaConfig = createKafkaConfig(mConfig.getKafkaEndpoint());
    KafkaConsumer<String, String> consumer = createConsumer(kafkaConfig, mConfig, startFuture);
    consumer.handler(this::handleTweets);
  }

  // Private

  private Map<String, String> createKafkaConfig(String endpoint) {
    Map<String, String> kafkaConfig = new HashMap<>();
    kafkaConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, endpoint);
    kafkaConfig.put(ConsumerConfig.GROUP_ID_CONFIG, KAFKA_GROUP);
    kafkaConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, KAFKA_OFFSET_RESET);
    kafkaConfig.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, KAFKA_AUTO_COMMIT);
    kafkaConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    kafkaConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

    return kafkaConfig;
  }

  private KafkaConsumer<Object, Object> createConsumer(Map<String, String> kafkaConfig, Config config, Future<Void> startFuture) {
    return KafkaConsumer.create(vertx, kafkaConfig)
      .subscribe(config.getTopicTweets(), ar -> {
        if (ar.succeeded()) {
          startFuture.complete();
        } else {
          startFuture.fail(ar.cause());
        }
      });
  }

  private void handleTweets(KafkaConsumerRecord<String, String> event) {
    JsonObject msg = new JsonObject()
      .put("id", event.key())
      .put("text", event.value());
    vertx.eventBus().publish(mConfig.getEbAddressTweets(), msg);
  }
}
