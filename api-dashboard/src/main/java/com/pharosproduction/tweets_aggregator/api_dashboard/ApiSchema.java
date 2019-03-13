package com.pharosproduction.tweets_aggregator.api_dashboard;

import graphql.TypeResolutionEnvironment;
import graphql.schema.*;

import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLInterfaceType.newInterface;
import static graphql.schema.GraphQLObjectType.newObject;

public class ApiSchema {

  // Variables

  private GraphQLSchema mSchema;
  private GraphQLObjectType mQueryType;
  private GraphQLInterfaceType mTweetInterface;

  // Constructors

  ApiSchema() {
    mTweetInterface = createTweetInterface();
    mQueryType = createQueryType();
    mSchema = createSchema();
  }

  // Private

  private GraphQLSchema createSchema() {
    return GraphQLSchema.newSchema()
      .query(mQueryType)
      .build();
  }

  private GraphQLObjectType createQueryType() {
    return newObject().name("QueryType")
      .field(
        newFieldDefinition().name("tweet")
        .type(mTweetInterface)
        .argument(
          GraphQLArgument.newArgument()
            .name("tweetFromTheStream")
            .description("Hello there")
            .type(GraphQLString)
        )
        .dataFetcher(env -> {
          return new Tweet();
        })
      )
      .build();
  }

  private GraphQLInterfaceType createTweetInterface() {
    return newInterface().name("Tweet")
      .description("A tweet")

      .field(newFieldDefinition().name("id")
        .description("The id of the tweet.")
        .type(new GraphQLNonNull(GraphQLString)))

      .field(newFieldDefinition().name("details")
        .description("The details of the tweet.")
        .type(new GraphQLNonNull(GraphQLString)))

      .typeResolver(typeResolutionEnvironment -> {
        Object javaObject = typeResolutionEnvironment.getObject();
        System.out.println("OBJECT: " + javaObject);
        if (javaObject instanceof Tweet) {
          return typeResolutionEnvironment.getSchema().getObjectType("TweetType");
        } else {
          throw new RuntimeException("HELLO WRONG TYPE: " + javaObject.getClass().toString());
        }
      })
      .build();
  }


  // Accessors

  GraphQLSchema getSchema() {
    return mSchema;
  }
}
