package com.chris.graphql;

import com.chris.graphql.entity.Patent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.dataloader.BatchLoader;
import org.dataloader.DataLoader;
import org.dataloader.DataLoaderRegistry;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.execution.instrumentation.dataloader.DataLoaderDispatcherInstrumentation;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;

/**
 * Created by ye830 on 12/21/2017.
 */
public class SingleTest {
    public static void main(String... args) {

        DataFetcher patentDataFetcher = env -> getPatentByPatentIds(Arrays.asList(((String)env.getArgument("patentId")))).get(0);

        DataFetcher patentCitationDataFetcher = env -> {
            if(env.getSource()!=null){
                Patent patent = env.getSource();
                List<String> patentIds = getPatentCitationIds(patent.getId());
                return getPatentByPatentIds(patentIds);
            }
            return Collections.EMPTY_LIST;
        };

        Long startTime = new Date().getTime();

        // Define the schema
        File schemaFile = new File(DataLoader.class.getClassLoader().getResource("dataloader.graphqls").getPath());

        // Define the type definition registry
        TypeDefinitionRegistry typeDefinitionRegistry = new SchemaParser().parse(schemaFile);



        // Define the runtime wiring
        RuntimeWiring runtimeWiring = RuntimeWiring.newRuntimeWiring()
                .type("Query", builder -> builder
                        .dataFetcher("patent", patentDataFetcher)
                )
                .type("Patent", builder -> builder
                        .dataFetcher("citations", patentCitationDataFetcher))
                .build();

        // Define the graphQL schema
        GraphQLSchema graphQLSchema = new SchemaGenerator().makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);

        // Define the graphQL
        GraphQL graphQL = GraphQL.newGraphQL(graphQLSchema)
                .build();

        // Define the execution
        String queryString = "query Query($patentId: String!){patent(patentId: $patentId) {id pn citations{id pn citations{id pn citations{id pn}}}}}";
        Map<String, Object> variableMap = new HashMap<>();
        variableMap.put("patentId", "1");
        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                .query(queryString)
                .operationName("Query")
                .variables(variableMap)
                .build();

        ExecutionResult executionResult = graphQL.execute(executionInput);
        Map<String, Object> specificationResult = executionResult.toSpecification();

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            System.out.println(objectMapper.writeValueAsString(specificationResult));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        Long endTime = new Date().getTime();

        System.out.println("Total Cost: "+(endTime - startTime)+" ms");
    }



    static Map<String, List<String>> patentCitationIdMap = new HashMap();
    static{
        patentCitationIdMap.put("1", Arrays.asList(new String[]{"2","3","4"}));
        patentCitationIdMap.put("2", Arrays.asList(new String[]{"1","3","4"}));
        patentCitationIdMap.put("3", Arrays.asList(new String[]{"2","3","4"}));
        patentCitationIdMap.put("4", Arrays.asList(new String[]{"1","2","3"}));
    }

    static List<String> getPatentCitationIds(String patentId){
        return patentCitationIdMap.get(patentId);
    }

    static List<Object> getPatentByPatentIds(List<String> patentIds){
        List<Object> patentCitations  = patentIds.stream().map(id -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Patent patent = new Patent();
            patent.setId(id);
            patent.setPn("AAA");
            return patent;
        }).collect(Collectors.toList());
        return patentCitations;
    }
}
