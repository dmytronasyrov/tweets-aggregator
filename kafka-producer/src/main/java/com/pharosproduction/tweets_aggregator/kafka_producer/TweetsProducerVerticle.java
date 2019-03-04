package com.pharosproduction.tweets_aggregator.kafka_producer;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TweetsProducerVerticle extends AbstractVerticle {

  private static final String ADDRESS_TWEETS_RAW = "tweets.raw";
  private static final String TWEETS_KEY = "tweets";
  private static final String STREAM_TWEETS_RAW = "streaming.tweets.raw";
  private static final String ACKS = "1";
  private static final String SERIALIZER = "org.apache.kafka.common.serialization.StringSerializer";
  private static final String BOOTSTRAP_SERVERS_KEY = "bootstrap.servers";
  private static final String SERIALIZER_KEY = "key.serializer";
  private static final String SERIALIZER_VALUE = "value.serializer";
  private static final String ACKS_KEY = "acks";

  // Variables

  private final Logger mLogger;
  private Config mConfig;
  private KafkaProducer<String, String> mProducer;

  // Constructor

  public TweetsProducerVerticle() {
    mLogger = LoggerFactory.getLogger(TweetsProducerVerticle.class.getName());
  }

  // Overrides

  // kafka-topics --zookeeper 127.0.0.1:2181 --topic streaming.tweets.raw --create --partitions 6 --replication-factor 2
  // kafka-console-consumer --bootstrap-server 127.0.0.1:9092 --topic streaming.tweets.raw --from-beginning

  @Override
  public void start() throws Exception {
    super.start();

    mConfig = new Config(config());

    createKafkaProducer();
    createTweetsConsumer();
  }

  // Private

  private void createKafkaProducer() {
    Map<String, String> config = new HashMap<>();
    config.put(BOOTSTRAP_SERVERS_KEY, mConfig.getKafkaEndpoint());
    config.put(SERIALIZER_KEY, SERIALIZER);
    config.put(SERIALIZER_VALUE, SERIALIZER);
    config.put(ACKS_KEY, ACKS);
    mProducer = KafkaProducer.create(vertx, config);
  }

  private void createTweetsConsumer() {
    vertx.eventBus().<JsonObject>consumer(ADDRESS_TWEETS_RAW, msg -> {
      msg.body()
        .getJsonArray(TWEETS_KEY)
        .stream()
        .parallel()
        .map(s -> (String) s)
        .map((Function<String, KafkaProducerRecord<String, String>>) tweet -> KafkaProducerRecord.create(STREAM_TWEETS_RAW, tweet))
        .forEach(record -> mProducer.write(record));
    });
  }
}
