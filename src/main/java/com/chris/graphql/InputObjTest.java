package com.chris.graphql;

import com.chris.graphql.entity.Litigation;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.TypeRuntimeWiring;

/**
 * Created by ye830 on 12/2/2017.
 */
public class InputObjTest {
    public static void main(String... args) {
        Litigation litigation  = new Litigation();
        litigation.setDefendant("Zhang San");
        litigation.setPlaintiff("Li Si");

        // Define the schema
        File schema = new File(InputObjTest.class.getClassLoader().getResource("inputobj.graphqls").getPath());

        // Define the TypeDefineRegistry
        TypeDefinitionRegistry typeDefinitionRegistry = new SchemaParser().parse(schema);

        // Define the run time wiring
        RuntimeWiring runtimeWiring = RuntimeWiring.newRuntimeWiring()
                .type("LitigationQuery", builder -> builder
                        .dataFetcher("litigation", env -> litigation)
                )
                .type("Litigation", builder -> builder
                        .dataFetcher("defendant", env -> {
                            Object queryInput = env.getArgument("queryInput");
                            if(queryInput != null){
                                Object patentId = Optional.ofNullable(((HashMap)queryInput).get("patentId")).orElse("6666666666666");
                                return patentId;
                            }
                            return "6666666666666";
                        }))
                .type(TypeRuntimeWiring.newTypeWiring("Litigation").typeResolver(env -> {
                    Object javaObject = env.getObject();
                    if (javaObject instanceof Litigation) {
                        return (GraphQLObjectType) env.getSchema().getType("Litigation");
                    } else {
                        return (GraphQLObjectType) env.getSchema().getType("Litigation");
                    }
                }))
                .build();

        // Define the GraphQLSchema
        GraphQLSchema graphQLSchema = new SchemaGenerator().makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);

        // Define the GraphQL
        GraphQL graphQL = GraphQL.newGraphQL(graphQLSchema).build();

        String queryString = "query LitigationQuery($input: LitigationQueryInput){litigation{defendant(queryInput: $input)}}";

        Map<String,Object> variableMap = new HashMap<>();
        Map<String,Object> litigationQueryInput = new HashMap<>();
        litigationQueryInput.put("patentId", "2222222222222");
        variableMap.put("input", litigationQueryInput);
        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                .query(queryString)
                .operationName("LitigationQuery")
                .variables(variableMap)
                .build();
        ExecutionResult executionResult = graphQL.execute(executionInput);
        System.out.println(executionResult.getData().toString());

    }
}
