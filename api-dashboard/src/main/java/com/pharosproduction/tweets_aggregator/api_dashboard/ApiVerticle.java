package com.pharosproduction.tweets_aggregator.api_dashboard;

import graphql.*;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.vertx.core.http.HttpMethod.GET;
import static io.vertx.core.http.HttpMethod.POST;

public class ApiVerticle extends AbstractVerticle {

  // Constants

  private static final String ENDPOINT_API = "/graphql";
  private static final String ENDPOINT_GRAPHIQL = "/graphiql";

  // Variables

  private GraphQL mGraphQL;

  // Constructors

  public ApiVerticle() {
    InputStream stream = getClass().getClassLoader().getResourceAsStream("schema.graphqls");
    Reader streamReader = new InputStreamReader(stream);
    TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(streamReader);
    RuntimeWiring wiring = buildRuntimeWiring();
    GraphQLSchema schema = new SchemaGenerator().makeExecutableSchema(typeRegistry, wiring);
    mGraphQL = GraphQL.newGraphQL(schema).build();
  }

  // Overrides

  // http://localhost:3000/browser/index.html

  @Override
  public void start() throws Exception {
    super.start();

    Router router = Router.router(vertx);
    addApiEndpoint(router);
    startServer(router);
  }

  // Private

  RuntimeWiring buildRuntimeWiring() {
    return RuntimeWiring.newRuntimeWiring()
      .type("Query", typeWiring ->
        typeWiring.dataFetcher("tweet", env -> TweetsData.getTweet())
      )
      .build();
  }

  private void addApiEndpoint(Router router) {
    router.route(ENDPOINT_API).handler(requestHandler -> {
      requestHandler.request().bodyHandler(body -> {
        JsonObject json = body.toJsonObject();
        GraphQLObject gql = json.mapTo(GraphQLObject.class);

        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
          .query(gql.getQuery())
          .operationName(gql.getOperationName())
          .variables(gql.getVariables())
          .build();

        ExecutionResult executionResult = mGraphQL.execute(executionInput);
        Object gqlResponse = executionResult.getData();
        List<GraphQLError> errors = executionResult.getErrors();

        if (gqlResponse != null) {
          JsonObject response = new JsonObject();
          Map<String, Object> data = (Map<String, Object>) gqlResponse;
          response.put("data", new JsonObject(Json.encode(data)));

          requestHandler.response()
            .putHeader("Content-Type", "application/json")
            .setStatusCode(OK.code())
            .end(response.toString());
        } else if (errors != null) {
          requestHandler.response()
            .putHeader("Content-Type", "application/json")
            .setStatusCode(UNPROCESSABLE_ENTITY.code())
            .end(errors.toString());
        } else {
          requestHandler.response()
            .putHeader("Content-Type", "application/json")
            .setStatusCode(FORBIDDEN.code())
            .end();
        }
      });
    });
  }

  private void startServer(Router router) {
    router.route(ENDPOINT_GRAPHIQL).method(GET).handler(rc -> {
      if (ENDPOINT_GRAPHIQL.equals(rc.request().path())) {
        rc.response().setStatusCode(302);
        rc.response().headers().set("Location", rc.request().path() + "/index.html");
        rc.response().end();
      } else {
        rc.next();
      }
    });

    StaticHandler staticHandler = StaticHandler.create("graphiql");
    staticHandler.setDirectoryListing(false);
    staticHandler.setCachingEnabled(false);
    staticHandler.setIndexPage("index.html");
    router.route(ENDPOINT_GRAPHIQL + "/*").method(GET).handler(staticHandler);

    vertx.createHttpServer()
      .requestHandler(router)
      .listen(3000);
  }

  //  private void handleQuery(RoutingContext rc, String json) {
  //    GraphQL gql = GraphQL.newGraphQL(mSchema).build();
  //    ExecutionInput input = ExecutionInput.newExecutionInput().query("query { tweet { id, details } }")
  //      .build();
  //    ExecutionResult result = gql.execute(input);
  //    Object data = result.getData();
  //    List<GraphQLError> errors = result.getErrors();
  //
  //    System.out.println("DATA: " + data.toString());
  //    System.out.println("ERRORS: " + errors.toString());
  //
  //    JsonObject response = new JsonObject();
  //
  //    if (result.getData() != null) {
  //      Map<String, Object> obj = result.getData();
  //      response.put("data", new JsonObject(Json.encode(obj)));
  //    }
  //
  //    HttpResponseStatus statusCode = (result.getErrors() != null && !result.getErrors().isEmpty()) ? BAD_REQUEST : OK;
  //
  //    rc.response().putHeader("Content-Type", "application/json");
  //    rc.response().setStatusCode(statusCode.code());
  //    rc.response().end(data.toString());
  //  }
}
