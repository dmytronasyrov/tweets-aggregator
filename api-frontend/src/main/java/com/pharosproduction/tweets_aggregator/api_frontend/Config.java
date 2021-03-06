package com.pharosproduction.tweets_aggregator.api_frontend;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

public class Config {

  private static final String CLIENT = "client";
  private static final String CLIENT_METHOD = "method";
  private static final String CLIENT_HOST = "host";
  private static final String CLIENT_PORT = "port";
  private static final String ROUTER = "router";
  private static final String ROUTER_EB = "eventbus";
  private static final String SERVER = "server";
  private static final String SERVER_PORT = "port";
  private static final String EB = "eventbus";
  private static final String EB_ADDRESSES = "addresses";
  private static final String EB_ADDRESSES_TWEETS = "tweets";
  private static final String KAFKA = "kafka";
  private static final String KAFKA_HOST = "host";
  private static final String KAFKA_PORT = "port";
  private static final String KAFKA_TOPICS = "topics";
  private static final String KAFKA_TOPICS_TWEETS = "tweets";

  // Variables

  private final String mClientEndpoint;
  private int mServerPort;
  private final String mRouterEbAddress;
  private final String mEbAddressTweets;
  private final String mKafkaEndpoint;
  private final String mTopicTweets;

  // Constructors

  public Config(JsonObject json) {
    JsonObject client = json.getJsonObject(CLIENT);
    String method = client.getString(CLIENT_METHOD);
    String host = client.getString(CLIENT_HOST);
    int port = client.getInteger(CLIENT_PORT);
    mClientEndpoint = method + "://" + host + ":" + port;

    JsonObject server = json.getJsonObject(SERVER);
    mServerPort = server.getInteger(SERVER_PORT);

    JsonObject router = json.getJsonObject(ROUTER);
    mRouterEbAddress = router.getString(ROUTER_EB);

    JsonObject eb = json.getJsonObject(EB);
    JsonObject addresses = eb.getJsonObject(EB_ADDRESSES);
    mEbAddressTweets = addresses.getString(EB_ADDRESSES_TWEETS);

    JsonObject kafka = json.getJsonObject(KAFKA);
    JsonObject kafkaTopics = kafka.getJsonObject(KAFKA_TOPICS);
    String kafkaHost = kafka.getString(KAFKA_HOST);
    int kafkaPort = kafka.getInteger(KAFKA_PORT);
    mKafkaEndpoint = kafkaHost + ":" + kafkaPort;
    mTopicTweets = kafkaTopics.getString(KAFKA_TOPICS_TWEETS);
  }

  // Accessors

  String getClientEndpoint() {
    return mClientEndpoint;
  }

  int getServerPort() {
    return mServerPort;
  }

  String getRouterEbAddress() {
    return mRouterEbAddress;
  }

  String getEbAddressTweets() {
    return mEbAddressTweets;
  }

  String getKafkaEndpoint() {
    return mKafkaEndpoint;
  }

  String getTopicTweets() {
    return mTopicTweets;
  }
}
