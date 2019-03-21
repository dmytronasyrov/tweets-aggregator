package com.pharosproduction.tweets_aggregator.api_mobile;

import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
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
    JsonObject authConfig = new JsonObject()
      .put("public-key", "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEIhhhqGgo42Wd1++zH+ndgCrUx8Yh\n" +
        "J3CWz6NhuZSxQxJ1raJmUChUIz68ZlUy4dlwcSjN0C1J2E10LxCh2IpJTg==")
      .put("permissionsClaimKey", "realm/access/roles");

    PubSecKeyOptions pubSecOpts = new PubSecKeyOptions()
      .setAlgorithm("ES256")
      .setPublicKey("MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEIhhhqGgo42Wd1++zH+ndgCrUx8Yh\n" +
        "J3CWz6NhuZSxQxJ1raJmUChUIz68ZlUy4dlwcSjN0C1J2E10LxCh2IpJTg==")
      .setSecretKey("MIGHAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBG0wawIBAQQgxgewcbmh0aO7l0PB\n" +
        "hsrjMiYJ0L8wafncFEHudBJoOwuhRANCAAQiGGGoaCjjZZ3X77Mf6d2AKtTHxiEn\n" +
        "cJbPo2G5lLFDEnWtomZQKFQjPrxmVTLh2XBxKM3QLUnYTXQvEKHYiklO");

    JWTAuthOptions jwtAuthOpts = new JWTAuthOptions(authConfig)
      .addPubSecKey(pubSecOpts)
      .setPermissionsClaimKey("realm_access/roles");

    long exp = System.currentTimeMillis() + 10 * 60 * 1000;
    // iss - issuer
    // sub - subject
    // aud - audience
    // exp - expiration time
    // nbf - not before time
    // jti - JWT ID
    // iat - issued at in ms
    JsonObject payload = new JsonObject()
      .put("iss", "android mobile device")
      .put("sub", "uid 1131231231231331")
      .put("aud", "plain user")
      .put("exp", exp)
      .put("jti", "aisdiofjdsoiafjioajdsfoijaoidjfioajisojfio")
      .put("realm", new JsonObject()
        .put("access", new JsonObject()
          .put("roles", new JsonArray(Arrays.asList("role1", "role2")))
        )
      )
      .put("somekey", "somevalue");
    JWTOptions jwtOpts = new JWTOptions().setAlgorithm("ES256");

    JWTAuth provider = JWTAuth.create(Vertx.vertx(), jwtAuthOpts);
    String token = provider.generateToken(payload, jwtOpts);
    JwtClientInterceptor interceptor = new JwtClientInterceptor(token);

    ClassLoader loader = ApiVerticle.class.getClassLoader();
    String trustCertPath = loader.getResource("ssl/ca.crt").getPath();

    ManagedChannel channel = NettyChannelBuilder.forAddress("localhost", 4000)
      .sslContext(GrpcSslContexts.forClient().trustManager(new File(trustCertPath)).build())
      .intercept(interceptor)
      .build();
    HelloServiceGrpc.HelloServiceBlockingStub stub = HelloServiceGrpc.newBlockingStub(channel);
    HelloResponse response = stub.hello(HelloRequest.newBuilder().setHelloId("asidjfiajsdifa").build());
    System.out.println(response.getHello().getName() + ", " + response.getHello().getMsg());
  }
}
