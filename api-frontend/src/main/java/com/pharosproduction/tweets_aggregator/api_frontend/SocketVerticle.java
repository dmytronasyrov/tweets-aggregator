package com.pharosproduction.tweets_aggregator.api_frontend;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.servicediscovery.rest.ServiceDiscoveryRestEndpoint;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SocketVerticle extends AbstractVerticle {

  // Constants

  private static final Set<String> allowedHeaders = new HashSet<>(Arrays.asList(
    "x-requested-with",
    "origin",
    "Content-Type",
    "accept",
    "Access-Control-Allow-Origin",
    "Access-Control-Request-Method",
    "Access-Control-Allow-Credentials",
    "Access-Control-Allow-Headers"
  ));
  private static final Set<HttpMethod> allowedMethods = new HashSet<>(Arrays.asList(
    HttpMethod.OPTIONS,
    HttpMethod.GET,
    HttpMethod.POST
  ));

  // Overrides

  @Override
  public void start(Future<Void> startFuture) {
    Config config = new Config(config());

    SockJSHandler socket = createSocket(config.getEbAddressTweets());
    CorsHandler cors = createCors(config.getClientEndpoint());
    Router router = createRouter(cors, config.getRouterEbAddress(), socket);
    createServer(router, config.getServerPort(), startFuture);
  }

  // Private

  private SockJSHandler createSocket(String ebAddressTweets) {
    PermittedOptions outbound = new PermittedOptions().setAddressRegex(ebAddressTweets);
    BridgeOptions options = new BridgeOptions();
    options.addOutboundPermitted(outbound);

    SockJSHandler socket = SockJSHandler.create(vertx);
    socket.bridge(options);

    return socket;
  }

  private CorsHandler createCors(String clientEndpoint) {
    return CorsHandler.create(clientEndpoint)
      .allowedHeaders(allowedHeaders)
      .allowedMethods(allowedMethods)
      .allowCredentials(true);
  }

  private Router createRouter(CorsHandler cors, String routerEbAddresss, SockJSHandler socket) {
    Router router = Router.router(vertx);
    router.route().handler(cors);
    router.route(routerEbAddresss).handler(socket);

    return router;
  }

  private void createServer(Router router, int port, Future<Void> startFuture) {
    vertx.createHttpServer()
      .requestHandler(router)
      .listen(port, ar -> {
        if (ar.failed()) {
          startFuture.fail(ar.cause());
        } else {
          startFuture.complete();
        }
      });
  }
}
