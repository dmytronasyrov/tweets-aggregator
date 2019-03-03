package com.pharosproduction.tweets_aggregator.tweets_consumer;

import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.stream.Collectors;

class Config {

  // Constants

  private static final String TWITTER = "twitter";
  private static final String TWITTER_CONSUMER_KEY = "consumer_key";
  private static final String TWITTER_CONSUMER_SECRET = "consumer_secret";
  private static final String TWITTER_ACCESS_KEY = "access_token";
  private static final String TWITTER_ACCESS_SECRET = "access_secret";
  private static final String SEARCH_TERMS = "search_terms";

  // Variables

  private final String mTwitterConsumerKey;
  private final String mTwitterConsumerSecret;
  private final String mTwitterAccessKey;
  private final String mTwitterAccessSecret;
  private final List<String> mSearchTerms;

  // Constructors

  Config(JsonObject config) {
    JsonObject twitterConfig = config.getJsonObject(TWITTER);
    mTwitterConsumerKey = twitterConfig.getString(TWITTER_CONSUMER_KEY);
    mTwitterConsumerSecret = twitterConfig.getString(TWITTER_CONSUMER_SECRET);
    mTwitterAccessKey = twitterConfig.getString(TWITTER_ACCESS_KEY);
    mTwitterAccessSecret = twitterConfig.getString(TWITTER_ACCESS_SECRET);

    mSearchTerms = config.getJsonArray(SEARCH_TERMS)
      .stream()
      .map(s -> (String) s)
      .collect(Collectors.toList());
  }

  // Accessors

  String getTwitterConsumerKey() {
    return mTwitterConsumerKey;
  }

  String getTwitterConsumerSecret() {
    return mTwitterConsumerSecret;
  }

  String getTwitterAccessKey() {
    return mTwitterAccessKey;
  }

  String getTwitterAccessSecret() {
    return mTwitterAccessSecret;
  }

  List<String> getSearchTerms() {
    return mSearchTerms;
  }
}
