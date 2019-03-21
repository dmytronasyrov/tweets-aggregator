package com.pharosproduction.tweets_aggregator.api_mobile;

import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.jwt.JWTOptions;

import javax.net.ssl.SSLException;
import java.io.File;
import java.util.Arrays;

public class GrpcClient {

  public static void main(String[] args) throws SSLException {
    String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NiJ9.eyJpc3MiOiJhbmRyb2lkIG1vYmlsZSBkZXZpY2UiLCJzdWIiOiJ1aWQgMTEzMTIzMTIzMTIzMTMzMSIsImF1ZCI6InBsYWluIHVzZXIiLCJleHAiOjE1NTMyMDMzNTAsImp0aSI6ImFpc2Rpb2ZqZHNvaWFmamlvYWpkc2ZvaWphb2lkamZpb2FqaXNvamZpbyIsInJlYWxtIjp7ImFjY2VzcyI6eyJyb2xlcyI6WyJyb2xlMSIsInJvbGUyIl19fSwic29tZWtleSI6InNvbWV2YWx1ZSIsImlhdCI6MTU1MzIwMzI5MH0.Tc1P_-4fGgE6D8Qt0yTBrJEfDOrw7DS3rybHmfEN0AxA1dznJ7jtJaxI3Mp6RSCMx_SpG2TttotqMdRAlaOWfw";
    JwtClientInterceptor interceptor = new JwtClientInterceptor(token);

    ClassLoader loader = ApiVerticle.class.getClassLoader();
    String trustCertPath = loader.getResource("ssl/ca.crt").getPath();
    File trustCert = new File(trustCertPath);
    SslContext sslContext = GrpcSslContexts.forClient().trustManager(trustCert).build();

    ManagedChannel channel = NettyChannelBuilder.forAddress("localhost", 4000)
      .sslContext(sslContext)
      .intercept(interceptor)
      .build();

    HelloServiceGrpc.HelloServiceBlockingStub stub = HelloServiceGrpc.newBlockingStub(channel);
    HelloResponse response = stub.hello(HelloRequest.newBuilder().setHelloId("asidjfiajsdifa").build());
    System.out.println(response.getHello().getName() + ", " + response.getHello().getMsg());
  }
}
