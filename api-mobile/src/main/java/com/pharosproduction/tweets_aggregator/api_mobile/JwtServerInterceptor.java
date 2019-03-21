package com.pharosproduction.tweets_aggregator.api_mobile;

import io.grpc.*;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.impl.JWTUser;

import java.util.Arrays;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;

public class JwtServerInterceptor implements ServerInterceptor {

  private static final Metadata.Key<String> JWT_METADATA_KEY = Metadata.Key.of("jwt", ASCII_STRING_MARSHALLER);
  private final JWTAuth mAuth;

  // Constructor

  JwtServerInterceptor(JWTAuth auth) {
    mAuth = auth;
  }

  // Overrides

  @Override
  public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata, ServerCallHandler<ReqT, RespT> serverCallHandler) {
    String token = metadata.get(JWT_METADATA_KEY);

    JsonObject authInfo = new JsonObject()
      .put("jwt", token);

    Future<JWTUser> future = Future.future();
    mAuth.authenticate(authInfo, handler -> {
      if (handler.succeeded()) {
        JWTUser user = (JWTUser) handler.result();

        user.isAuthorized("role1", ar -> {
          if (ar.succeeded()) {
            if (ar.result()) {
              future.complete(user);
            } else {
              future.fail("Unauthorized");
            }
          } else {
            future.fail(ar.cause());
          }
        });
      } else {
        future.fail(handler.cause());
      }
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
