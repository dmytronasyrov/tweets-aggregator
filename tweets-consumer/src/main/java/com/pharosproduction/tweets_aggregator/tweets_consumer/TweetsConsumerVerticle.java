package com.pharosproduction.tweets_aggregator.tweets_consumer;

import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.Hosts;
import com.twitter.hbc.core.HttpHosts;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

public class TweetsConsumerVerticle extends AbstractVerticle {

  // Constants

  private static final int sQueueSize = 100;
  private static final int sTwitterShutdownTime = 5000;
  private static final int sVerticleShutdownDelay = 1000;
  private static final int sPollDelay = 3000;
  private static final String TWITTER_CLIENT = "TWEETS_CONSUMER";
  private static final String ADDRESS_TWEETS_RAW = "tweets.raw";
  private static final String TWEETS_KEY = "tweets";

  // Variables

  private final BlockingQueue<String> mTweets;
  private Config mConfig;
  private Client mTwitter;
  private long mPollTimerId;

  // Constructor

  public TweetsConsumerVerticle() {
    mTweets = new LinkedBlockingDeque<>(sQueueSize);
  }

  // Overrides

  @Override
  public void start(Future<Void> startFuture) {
    mConfig = new Config(config());

    startTwitter(startFuture);
    mPollTimerId = pollTweets();
  }

  @Override
  public void stop(Future<Void> stopFuture) {
    vertx.cancelTimer(mPollTimerId);
    stopTwitter(stopFuture);
  }

  // Private

  private void startTwitter(Future<Void> startFuture) {
    vertx.executeBlocking(call -> {
      mTwitter = createTwitterClient(mTweets);
      mTwitter.connect();

      call.complete();
    }, ar -> {
      startFuture.complete();
    });
  }

  private void stopTwitter(Future<Void> stopFuture) {
    vertx.executeBlocking(call -> {
      mTwitter.stop(sTwitterShutdownTime);

      vertx.setTimer(sTwitterShutdownTime + sVerticleShutdownDelay, timerId -> {
        vertx.cancelTimer(timerId);

        call.complete();
      });
    }, ar -> {
      stopFuture.complete();
    });
  }

  private Client createTwitterClient(BlockingQueue<String> msgQueue) {
    Hosts hosts = new HttpHosts(Constants.STREAM_HOST);
    StatusesFilterEndpoint endpoint = new StatusesFilterEndpoint();
    endpoint.trackTerms(mConfig.getSearchTerms());

    Authentication auth = new OAuth1(
      mConfig.getTwitterConsumerKey(),
      mConfig.getTwitterConsumerSecret(),
      mConfig.getTwitterAccessKey(),
      mConfig.getTwitterAccessSecret()
    );
    ClientBuilder builder = new ClientBuilder()
      .name(TWITTER_CLIENT)
      .hosts(hosts)
      .authentication(auth)
      .endpoint(endpoint)
      .processor(new StringDelimitedProcessor(msgQueue));

    return builder.build();
  }

  private long pollTweets() {
    return vertx.setPeriodic(sPollDelay, timerId -> {
      if (mTwitter.isDone()) return;

      List<String> tweets = new ArrayList<>();
      mTweets.drainTo(tweets);

      JsonObject payload = new JsonObject()
        .put(TWEETS_KEY, tweets);
      vertx.eventBus().send(ADDRESS_TWEETS_RAW, payload);
    });
  }
}
