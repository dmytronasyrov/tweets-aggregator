package com.pharosproduction.tweets_aggregator.api_mobile;

import io.grpc.*;
import io.grpc.protobuf.services.ProtoReflectionService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.jwt.JWTOptions;
import io.vertx.grpc.BlockingServerInterceptor;
import io.vertx.grpc.VertxServerBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class ApiVerticle extends AbstractVerticle {

  // Overrides

  @Override
  public void start() throws Exception {
    JsonObject authConfig = new JsonObject()
      .put("public-key", "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEIhhhqGgo42Wd1++zH+ndgCrUx8Yh\n" +
        "J3CWz6NhuZSxQxJ1raJmUChUIz68ZlUy4dlwcSjN0C1J2E10LxCh2IpJTg==")
      .put("permissionsClaimKey", "realm/access/roles");

    JWTAuth provider = JWTAuth.create(vertx, new JWTAuthOptions(authConfig)
      .addPubSecKey(new PubSecKeyOptions()
        .setAlgorithm("ES256")
        .setPublicKey("MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEIhhhqGgo42Wd1++zH+ndgCrUx8Yh\n" +
          "J3CWz6NhuZSxQxJ1raJmUChUIz68ZlUy4dlwcSjN0C1J2E10LxCh2IpJTg==")
        .setSecretKey("MIGHAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBG0wawIBAQQgxgewcbmh0aO7l0PB\n" +
          "hsrjMiYJ0L8wafncFEHudBJoOwuhRANCAAQiGGGoaCjjZZ3X77Mf6d2AKtTHxiEn\n" +
          "cJbPo2G5lLFDEnWtomZQKFQjPrxmVTLh2XBxKM3QLUnYTXQvEKHYiklO")
//        .setPublicKey(readFile("keys/public.pem"))
//        .setSecretKey(readFile("keys/private_key.pem"))
      ));
//    String token = provider.generateToken(new JsonObject(), new JWTOptions().setAlgorithm("ES256"));
//    System.out.println("TOKEN: " + token);

    ClassLoader loader = ApiVerticle.class.getClassLoader();
    String certChainPath = loader.getResource("ssl/server.crt").getPath();
    String privateKeyPath = loader.getResource("ssl/server.pem").getPath();

    JwtServerInterceptor interceptor = new JwtServerInterceptor(provider);
    ServerInterceptor blockingInterceptor = BlockingServerInterceptor.wrap(vertx, interceptor);

    VertxServerBuilder.forAddress(vertx, "localhost", 4000)
      .useTransportSecurity(
        new File(certChainPath),
        new File(privateKeyPath)
      )
      .intercept(blockingInterceptor)
      .addService(new HelloServiceImpl())
      .addService(ProtoReflectionService.newInstance())
      .build()
      .start();
  }

  // Private

  private static String readFile(String filePath) {
    ClassLoader loader = ApiVerticle.class.getClassLoader();
    String path = loader.getResource(filePath).getPath();
    StringBuilder contentBuilder = new StringBuilder();

    try (Stream<String> stream = Files.lines(Paths.get(path), StandardCharsets.UTF_8)) {
      stream.forEach(s -> contentBuilder.append(s).append("\n"));
    } catch (IOException e) {
      e.printStackTrace();
    }

    return contentBuilder.toString();
  }
}
