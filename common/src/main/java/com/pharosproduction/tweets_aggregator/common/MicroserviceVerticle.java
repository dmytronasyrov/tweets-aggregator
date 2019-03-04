package com.pharosproduction.tweets_aggregator.common;

import io.vertx.core.*;
import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;
import io.vertx.servicediscovery.types.EventBusService;
import io.vertx.servicediscovery.types.HttpEndpoint;
import io.vertx.servicediscovery.types.MessageSource;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MicroserviceVerticle extends AbstractVerticle {

  // Variables

  protected JsonObject mModuleConfig;
  protected ServiceDiscovery mDiscovery;
  private Set<Record> mRegisteredRecords = new ConcurrentHashSet<>();

  // Overrides

  @Override
  public void start() throws Exception {
    super.start();

    mModuleConfig = config();

    createServiceDiscovery();
  }

  @Override
  public void start(Future<Void> startFuture) throws Exception {
    super.start(startFuture);

    mModuleConfig = config();

    createServiceDiscovery();
  }

  @Override
  public void stop(Future<Void> stopFuture) throws Exception {
    super.stop(stopFuture);

    List<Future> futures = mRegisteredRecords
      .stream()
      .map(this::unpublish)
      .collect(Collectors.toList());

    if (futures.isEmpty()) {
      stopDiscovery(stopFuture);
    } else {
      stopServices(futures, stopFuture);
    }
  }

  // Public

  protected void publishHttpEndpoint(String endpoint, String host, int port, Handler<AsyncResult<Void>> completion) throws Exception {
    Record record = HttpEndpoint.createRecord(endpoint, host, port, "/");
    publish(record, completion);
  }

  protected void publishMessageSource(String name, String address, Handler<AsyncResult<Void>> completionHandler) throws Exception {
    Record record = MessageSource.createRecord(name, address);
    publish(record, completionHandler);
  }

  protected void publishEventBusService(String name, String address, Class<?> serviceClass, Handler<AsyncResult<Void>>
    completionHandler) throws Exception {
    Record record = EventBusService.createRecord(name, address, serviceClass);
    publish(record, completionHandler);
  }

  // Private

  private void createServiceDiscovery() {
    JsonObject config = config();
    ServiceDiscoveryOptions opts = new ServiceDiscoveryOptions().setBackendConfiguration(config);
    mDiscovery = ServiceDiscovery.create(vertx, opts);
  }

  private void publish(Record record, Handler<AsyncResult<Void>> completion) throws Exception {
    if (mDiscovery == null) start();

    mDiscovery.publish(record, ar -> {
      if (ar.succeeded()) mRegisteredRecords.add(record);

      completion.handle(ar.map((Void)null));
    });
  }

  private Future<Void> unpublish(Record record) {
    Future<Void> unregisteringFuture = Future.future();
    mDiscovery.unpublish(record.getRegistration(), unregisteringFuture);

    return unregisteringFuture;
  }

  private void stopDiscovery(Future<Void> stopFuture) {
    mDiscovery.close();
    stopFuture.complete();
  }

  private void stopServices(List<Future> futures, Future<Void> stopFuture) {
    CompositeFuture composite = CompositeFuture.all(futures);
    composite.setHandler(ar -> {
      mDiscovery.close();

      if (ar.failed()) {
        stopFuture.fail(ar.cause());
      } else {
        stopFuture.complete();
      }
    });
  }
}

