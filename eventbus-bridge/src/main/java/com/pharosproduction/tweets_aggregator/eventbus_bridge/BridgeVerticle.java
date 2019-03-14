package com.pharosproduction.tweets_aggregator.eventbus_bridge;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.bridge.BridgeOptions;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.eventbus.bridge.tcp.TcpEventBusBridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BridgeVerticle extends AbstractVerticle {

  // Variables

  private Config mConfig;
  private final Logger mLogger;

  // Constructor

  public BridgeVerticle() {
    mLogger = LoggerFactory.getLogger(BridgeVerticle.class.getName());
  }


  // Overrides

  @Override
  public void start() throws Exception {
    super.start();

    mConfig = new Config(config());

    Future<TcpEventBusBridge> future = Future.future();
    future.setHandler(bridge -> {
      if (bridge.failed()) {
        mLogger.error("Unable to start TCP bridge: " + bridge.cause().getMessage());
        return;
      }

      testConsumer();
//      testPeriodicConsumer();
    });
    createBridge(mConfig.getBridgePort(), future);
  }

  // Private

  private void createBridge(int port, Future<TcpEventBusBridge> future) {
    final String prefix = mConfig.getChannelPrefix();

    BridgeOptions options = new BridgeOptions()
      .addOutboundPermitted(new PermittedOptions().setAddressRegex(prefix + ".*"))
      .addInboundPermitted(new PermittedOptions().setAddressRegex(prefix + ".*"));

    TcpEventBusBridge.create(vertx, options).listen(port, res -> {
      if (res.succeeded()) {
        future.complete();
      } else {
        future.fail(res.cause());
      }
    });
  }

  private void testConsumer() {
    System.out.println("Listening to: " + mConfig.getChannelPrefix());

    vertx.eventBus().consumer(mConfig.getChannelEcho(), msg -> {
      System.out.println("echo: " + msg.body());

      JsonObject reply = createReply(msg);
      msg.reply(reply);

      if (msg.isSend()) {
        resendMsg(reply);
      } else {
        republishMsg(reply);
      }
    });
  }

  private void testPeriodicConsumer() {
    //      vertx.setPeriodic(100, timer -> {
    //        //System.out.println("Sending the time...");
    //        vertx.eventBus().publish("test.time", new JsonObject().put("now", System.currentTimeMillis()));
    //        vertx.eventBus().send("test.time-send", new JsonObject().put("now", System.currentTimeMillis()));
    //      });
  }

  private JsonObject createReply(Message msg) {
    JsonObject headers = new JsonObject();
    JsonObject reply = new JsonObject();
    msg.headers().forEach(e -> headers.put(e.getKey(), e.getValue()));

    reply.put("original-headers", headers)
      .put("original-body", msg.body());

    return reply;
  }

  private void republishMsg(JsonObject reply) {
    final String prefix = mConfig.getChannelPrefix();
    vertx.eventBus().publish(prefix + ".echo.responses", reply);
  }

  private void resendMsg(JsonObject reply) {
    final String prefix = mConfig.getChannelPrefix();
    vertx.eventBus().send(prefix + ".echo.responses", reply);
  }
}
