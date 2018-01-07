package com.chris.graphql;

import com.chris.graphql.entity.Team;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.annotations.GraphQLAnnotations;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;

/**
 * Created by ye830 on 12/22/2017.
 */
public class AnnotationTest {
    public static void main(String... args) {
        //  Programmatically
        GraphQLObjectType queryType = GraphQLAnnotations.object(Team.class);

        //Query by
        GraphQLSchema graphQLSchema = GraphQLSchema.newSchema()
                .query(queryType)
                .build();
        GraphQL build = GraphQL.newGraphQL(graphQLSchema).build();
        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                .operationName("team")
                .query("query team{team_name}")
                .context(new Team())
                .root(new Team())
                .build();
        ExecutionResult executionResult = build.execute(executionInput);

        //ExecutionResult executionResult = build.execute("query team{team_name}", new Team());

        System.out.println(executionResult.getData().toString());
    }
}
