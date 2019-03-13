package com.pharosproduction.tweets_aggregator.api_dashboard;

public class Tweet {

  String id = "aaa";
  String details = "bbb";

  Tweet() {
  }

  @Override
  public String toString() {
    return "Tweet{" +
      "id='" + id + '\'' +
      ", details='" + details + '\'' +
      '}';
  }
}
