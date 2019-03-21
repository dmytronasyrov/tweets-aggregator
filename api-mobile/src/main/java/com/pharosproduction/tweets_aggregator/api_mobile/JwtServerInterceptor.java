package com.pharosproduction.tweets_aggregator.api_mobile;

import io.grpc.*;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.impl.JWTUser;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;

public class JwtServerInterceptor implements ServerInterceptor {

  // Variables

  private final JWTAuth mAuth;
  private final String mRole;

  // Constructor

  JwtServerInterceptor(JWTAuth auth, String role) {
    mAuth = auth;
    mRole = role;
  }

  // Overrides

  @Override
  public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata, ServerCallHandler<ReqT, RespT> serverCallHandler) {
    Metadata.Key<String> jwtKey = Metadata.Key.of("jwt", ASCII_STRING_MARSHALLER);
    String token = metadata.get(jwtKey);

    JsonObject authInfo = new JsonObject()
      .put("jwt", token);

    Future<JWTUser> future = Future.future();
    mAuth.authenticate(authInfo, handler -> {
      if (handler.failed()) {
        future.fail(handler.cause());

        return;
      }

      JWTUser user = (JWTUser) handler.result();
      user.isAuthorized(mRole, ar -> {
        if (ar.succeeded()) {
          future.complete(user);
        } else {
          future.fail(ar.cause());
        }
      });
    });

    if (future.succeeded()) {
      return serverCallHandler.startCall(serverCall, metadata);
    } else {
      Status status = Status.UNAUTHENTICATED.withDescription(future.cause().getMessage());
      serverCall.close(status, new Metadata());

      return serverCallHandler.startCall(serverCall, metadata);
    }
  }
}
