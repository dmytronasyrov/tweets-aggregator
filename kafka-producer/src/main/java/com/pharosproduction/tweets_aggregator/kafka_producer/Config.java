package com.pharosproduction.tweets_aggregator.kafka_producer;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.stream.Collectors;

class Config {

  // Constants

  private static final String ZOOKEEPER = "zookeeper";
  private static final String ZOOKEEPER_HOSTS = "hosts";
  private static final String ZOOKEEPER_PORT = "port";
  private static final String KAFKA = "kafka";
  private static final String KAFKA_HOSTS = "hosts";
  private static final String KAFKA_PORT = "port";

  // Variables

  private final String mZookeeperEndpoints;
  private final List<String> mKafkaEndpoints;

  // Constructor

  Config(JsonObject config) {
    JsonObject zookeeper = config.getJsonObject(ZOOKEEPER);
    JsonArray zookeeperHosts = zookeeper.getJsonArray(ZOOKEEPER_HOSTS);
    int zookeeperPort = zookeeper.getInteger(ZOOKEEPER_PORT);
    mZookeeperEndpoints = zookeeperHosts.stream().map(s -> (String)s).map(s -> s + ":" + zookeeperPort).collect(Collectors.joining(","));

    JsonObject kafka = config.getJsonObject(KAFKA);
    JsonArray kafkaHosts = kafka.getJsonArray(KAFKA_HOSTS);
    int kafkaPort = kafka.getInteger(KAFKA_PORT);
    mKafkaEndpoints = kafkaHosts.stream().map(s -> (String)s).map(s -> s + ":" + kafkaPort).collect(Collectors.toList());
  }

  // Accessors

  String getZookeeperEndpoints() {
    return mZookeeperEndpoints;
  }

  String getKafkaEndpoint() {
    return mKafkaEndpoints.get(0);
  }
}
