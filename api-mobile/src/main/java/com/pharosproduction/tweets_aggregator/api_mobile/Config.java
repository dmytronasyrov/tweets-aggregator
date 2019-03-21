package com.pharosproduction.tweets_aggregator.api_mobile;

import io.vertx.core.json.JsonObject;

public class Config {

  // Constants

  private static final String ENDPOINT = "endpoint";
  private static final String ENDPOINT_HOST = "host";
  private static final String ENDPOINT_PORT = "port";
  private static final String TLS = "tls";
  private static final String TLS_CERT_CHAIN = "cert_chain";
  private static final String TLS_PRIV_KEY = "priv_key";
  private static final String KEYS = "keys";
  private static final String KEYS_PUB = "pub_key";
  private static final String KEYS_PRIV = "priv_key";

  // Variables

  private final String mEndpointHost;
  private final int mEndpointPort;
  private final String mTlsCertChain;
  private final String mTlsPrivKey;
  private final String mKeyPub;
  private final String mKeyPriv;

  // Constructors

  public Config(JsonObject config) {
    JsonObject endpoint = config.getJsonObject(ENDPOINT);
    mEndpointHost = endpoint.getString(ENDPOINT_HOST);
    mEndpointPort = endpoint.getInteger(ENDPOINT_PORT);

    JsonObject tls = config.getJsonObject(TLS);
    mTlsCertChain = tls.getString(TLS_CERT_CHAIN);
    mTlsPrivKey = tls.getString(TLS_PRIV_KEY);

    JsonObject keys = config.getJsonObject(KEYS);
    mKeyPub = keys.getString(KEYS_PUB);
    mKeyPriv = keys.getString(KEYS_PRIV);
  }

  // Accessors

  String getEndpointHost() {
    return mEndpointHost;
  }

  int getEndpointPort() {
    return mEndpointPort;
  }

  String getTlsCertChain() {
    return mTlsCertChain;
  }

  String getTlsPrivKey() {
    return mTlsPrivKey;
  }

  String getKeyPub() {
    return mKeyPub;
  }

  String getKeyPriv() {
    return mKeyPriv;
  }
}
