package com.pharosproduction.tweets_aggregator.api_mobile;

import io.grpc.*;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;

public class JwtClientInterceptor implements ClientInterceptor {

  // Variables

  private final String mToken;

  // Constructor

  public JwtClientInterceptor(String token) {
    mToken = token;
  }

  // Overrides

  @Override
  public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> methodDescriptor, CallOptions callOptions, Channel channel) {
    ClientCall<ReqT, RespT> delegate = channel.newCall(methodDescriptor, callOptions);

    return new ForwardingClientCall.SimpleForwardingClientCall<>(delegate) {
      @Override
      public void start(Listener<RespT> responseListener, Metadata headers) {
        Metadata.Key<String> meta = Metadata.Key.of("jwt", ASCII_STRING_MARSHALLER);
        headers.put(meta, mToken);

        super.start(responseListener, headers);
      }
    };
  }
}
