package com.pharosproduction.tweets_aggregator.api_dashboard;

import io.vertx.core.json.JsonObject;

import java.util.Map;

public class GraphQLObject {

  // Variables

  private String mQuery;
  private Map<String, Object> mVariables;
  private String mOperationName;

  // Constructor

  public GraphQLObject() {}

  // Accessors

  public String getQuery() {
    return mQuery;
  }

  public void setQuery(String query) {
    if (query == null) return;

    mQuery = query;
  }

  public Map<String, Object> getVariables() {
    return mVariables;
  }

  public void setVariables(String variables) {
    if (variables == null) return;

    mVariables = new JsonObject(variables).getMap();
  }

  public String getOperationName() {
    return mOperationName;
  }

  public void setOperationName(String operationName) {
    if (operationName == null) return;

    mOperationName = operationName;
  }

  // Overrides

  @Override
  public String toString() {
    return "GraphQLObject{" +
      "mQuery='" + mQuery + '\'' +
      ", mVariables=" + mVariables +
      ", mOperationName='" + mOperationName + '\'' +
      '}';
  }
}
