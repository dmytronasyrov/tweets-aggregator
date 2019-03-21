package com.pharosproduction.tweets_aggregator.api_mobile;

import io.grpc.*;
import io.grpc.protobuf.services.ProtoReflectionService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.jwt.JWTOptions;
import io.vertx.grpc.BlockingServerInterceptor;
import io.vertx.grpc.VertxServerBuilder;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ApiVerticle extends AbstractVerticle {

  // Constants

  private static final String JWT_ALGO = "ES256";
  private static final String CLAIM_KEY = "realm/access/roles";

  // Variables

  private Config mConfig;

  // Overrides

  @Override
  public void start() throws Exception {
    mConfig = new Config(config());

    JWTAuth provider = createJwtAuth();
    JwtServerInterceptor interceptor = new JwtServerInterceptor(provider);
    ServerInterceptor blockingInterceptor = BlockingServerInterceptor.wrap(vertx, interceptor);

    VertxServerBuilder.forAddress(vertx, mConfig.getEndpointHost(), mConfig.getEndpointPort())
      .useTransportSecurity(certChainFile(), privateKeyFile())
      .intercept(blockingInterceptor)
      .addService(new HelloServiceImpl())
      .addService(ProtoReflectionService.newInstance())
      .build()
      .start();

    String token = createToken(provider);
    System.out.println("TOKEN: " + token);
  }

  // Private

  private JsonObject createAuthConfig() {
    return new JsonObject()
      .put("permissionsClaimKey", CLAIM_KEY);
  }

  private PubSecKeyOptions createPubSecOpts() {
    return new PubSecKeyOptions()
      .setAlgorithm(JWT_ALGO)
      .setPublicKey(readFile(mConfig.getKeyPub()))
      .setSecretKey(readFile(mConfig.getKeyPriv()));
  }

  private JWTAuthOptions createJwtAuthOpts(JsonObject authConfig, PubSecKeyOptions pubSecOpts) {
    return new JWTAuthOptions(authConfig)
      .addPubSecKey(pubSecOpts)
      .setPermissionsClaimKey(CLAIM_KEY);
  }

  private JWTAuth createJwtAuth() {
    JsonObject authConfig = createAuthConfig();
    PubSecKeyOptions pubSecOpts = createPubSecOpts();
    JWTAuthOptions jwtAuthOpts = createJwtAuthOpts(authConfig, pubSecOpts);

    return JWTAuth.create(vertx, jwtAuthOpts);
  }

  // iss - issuer
  // sub - subject
  // aud - audience
  // exp - expiration time
  // nbf - not before time
  // jti - JWT ID
  // iat - issued at in ms
  private JsonObject createJwtPayload() {
    long exp = System.currentTimeMillis() / 1000 + 60;

    return new JsonObject()
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
  }

  private JWTOptions createJwtOpts() {
    return new JWTOptions().setAlgorithm(JWT_ALGO);
  }

  private String createToken(JWTAuth provider) {
    JsonObject payload = createJwtPayload();
    JWTOptions jwtOpts = createJwtOpts();

    return provider.generateToken(payload, jwtOpts);
  }

  private File certChainFile() {
    return new File(getResource(mConfig.getTlsCertChain()));
  }

  private File privateKeyFile() {
    return new File(getResource(mConfig.getTlsPrivKey()));
  }

  private String getResource(String path) {
    return Objects.requireNonNull(
      ApiVerticle.class.getClassLoader().getResource(path)
    ).getPath();
  }

  private String readFile(String filePath) {
    String path = getResource(filePath);
    StringBuilder contentBuilder = new StringBuilder();
    List<String> lines = null;

    try {
      lines = Files.readAllLines(Paths.get(path));
    } catch (IOException e) {
      e.printStackTrace();
      return "";
    }

    if (!lines.isEmpty())
      lines.remove(0);
    if (!lines.isEmpty())
      lines.remove(lines.size() - 1);

    lines.forEach(s -> contentBuilder.append(s).append("\n"));

    return contentBuilder.toString();
  }
}
