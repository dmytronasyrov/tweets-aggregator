package com.pharosproduction.tweets_aggregator.eventbus_bridge;

import io.vertx.core.json.JsonObject;

public class Config {

  // Constants

  private static final String BRIDGE = "bridge";
  private static final String BRIDGE_PORT = "port";
  private static final String CHANNEL = "channel";
  private static final String CHANNEL_PREFIX = "prefix";
  private static final String CHANNEL_ECHO = "echo";

  // Variables

  private final int mBridgePort;
  private final String mChannelPrefix;
  private final String mChannelEcho;

  // Constructor

  Config(JsonObject config) {
    JsonObject bridge = config.getJsonObject(BRIDGE);
    mBridgePort = bridge.getInteger(BRIDGE_PORT);

    JsonObject channel = config.getJsonObject(CHANNEL);
    mChannelPrefix = channel.getString(CHANNEL_PREFIX);
    mChannelEcho = mChannelPrefix + "." + channel.getString(CHANNEL_ECHO);
  }

  // Accessors

  int getBridgePort() {
    return mBridgePort;
  }

  String getChannelPrefix() {
    return mChannelPrefix;
  }

  String getChannelEcho() {
    return mChannelEcho;
  }
}
