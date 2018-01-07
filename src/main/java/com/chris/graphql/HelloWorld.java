package com.chris.graphql;

import java.io.File;

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.Scalars;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetcherFactory;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.schema.StaticDataFetcher;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;

/**
 * com.chris.graphql.HelloWorld
 * <p>
 * Author: ChrisYe
 * Date: 11/28/2017
 */

public class HelloWorld {

    public static void main(String... args) {

        // IDL string schema
        //String schema = "type Query{hello: String}";

        //  IDL file shcema
        //File schema = new File(HelloWorld.class.getClassLoader().getResource("helloworld.graphqls").getFile());

//        SchemaParser schemaParser = new SchemaParser();
//
//        TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(schema);
//
//        RuntimeWiring runtimeWiring = RuntimeWiring.newRuntimeWiring()
//                .type("Query", builder -> builder.dataFetcher("hello", new StaticDataFetcher("world")))
//                .build();
//
//        SchemaGenerator schemaGenerator = new SchemaGenerator();
//        GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);
//        GraphQL build = GraphQL.newGraphQL(graphQLSchema).build();

        //  Programmatically
        GraphQLObjectType queryType = GraphQLObjectType.newObject()
                .name("Query")
                .field(GraphQLFieldDefinition.newFieldDefinition()
                        .name("hello")
                        .type(Scalars.GraphQLString)
                        .dataFetcher(env -> "world"))
                .build();

        //Query by
        GraphQLSchema graphQLSchema = GraphQLSchema.newSchema()
                .query(queryType)
                .build();
        GraphQL build = GraphQL.newGraphQL(graphQLSchema).build();
        ExecutionResult executionResult = build.execute("query Query {hello}");

        System.out.println(executionResult.getData().toString());
    }

}
