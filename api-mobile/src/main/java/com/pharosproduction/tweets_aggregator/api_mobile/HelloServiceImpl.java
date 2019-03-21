package com.pharosproduction.tweets_aggregator.api_mobile;

import io.grpc.stub.StreamObserver;

public class HelloServiceImpl extends HelloServiceGrpc.HelloServiceImplBase {

  @Override
  public void hello(HelloRequest request, StreamObserver<HelloResponse> responseObserver) {
    Hello hello = Hello.newBuilder().setName("John Doe").setMsg("Hello there").build();

    HelloResponse response = HelloResponse.newBuilder().setHello(hello).build();
    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }
}
