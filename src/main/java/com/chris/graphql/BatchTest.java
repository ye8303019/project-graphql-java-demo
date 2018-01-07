package com.chris.graphql;

import com.chris.graphql.entity.AWSCredential;
import com.chris.graphql.entity.Patent;
import com.chris.graphql.entity.Person;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.dataloader.BatchLoader;
import org.dataloader.DataLoader;
import org.dataloader.DataLoaderRegistry;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.GraphQLError;
import graphql.annotations.GraphQLTypeResolver;
import graphql.execution.ExecutionPath;
import graphql.execution.instrumentation.ChainedInstrumentation;
import graphql.execution.instrumentation.Instrumentation;
import graphql.execution.instrumentation.dataloader.DataLoaderDispatcherInstrumentation;
import graphql.execution.instrumentation.fieldvalidation.FieldAndArguments;
import graphql.execution.instrumentation.fieldvalidation.FieldValidation;
import graphql.execution.instrumentation.fieldvalidation.FieldValidationEnvironment;
import graphql.execution.instrumentation.fieldvalidation.FieldValidationInstrumentation;
import graphql.execution.instrumentation.fieldvalidation.SimpleFieldValidation;
import graphql.execution.instrumentation.tracing.TracingInstrumentation;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;

/**
 * Created by ye830 on 12/21/2017.
 */
public class BatchTest {
    public static void main(String... args) {

        BatchLoader<String, Object> patentBatchLoader = keys -> CompletableFuture.supplyAsync(() -> getPatentByPatentIds(keys));

        DataLoader<String, Object> patentDataLoader = new DataLoader(patentBatchLoader);

        DataFetcher patentDataFetcher = env -> patentDataLoader.load(env.getArgument("patentId"));

        DataFetcher offsetDataFetcher = env -> {
            AWSCredential awsCredential = env.getContext();
            System.out.println(awsCredential.getFoo());
            Integer offset = env.getArgument("offset");
            Integer limit = env.getArgument("limit");
            return offset + limit;
        };


        DataFetcher totalDataFetcher = env -> {
            String patentId = env.getArgument("patentId");
            return getTotalCountByPatentId(patentId);
        };

        DataFetcher patentCitationDataFetcher = env -> {
            if (env.getSource() != null) {
                Patent patent = env.getSource();
                return patentDataLoader.loadMany(getPatentCitationIds(patent.getId()));
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
                        .dataFetcher("offset", offsetDataFetcher)
                        .dataFetcher("total", totalDataFetcher)
                )
                .type("Patent", builder -> builder
                        .dataFetcher("citations", patentCitationDataFetcher))
                .build();

        // Define the graphQL schema
        GraphQLSchema graphQLSchema = new SchemaGenerator().makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);

        // Define data loader instrumentation
        DataLoaderRegistry registry = new DataLoaderRegistry();
        registry.register("patent", patentDataLoader);
        DataLoaderDispatcherInstrumentation dataLoaderDispatcherInstrumentation = new DataLoaderDispatcherInstrumentation(registry);

        // Define field validation instrumentation
        ExecutionPath fieldPath = ExecutionPath.parse("/patent");
        FieldValidation fieldValidation = new SimpleFieldValidation()
                .addRule(fieldPath, (fieldAndArguments, fieldValidationEnvironment) -> {
                    Integer offset = fieldAndArguments.getArgumentValue("offset");
                    if(offset > 1000){
                        return Optional.of(fieldValidationEnvironment.mkError("offset should less equal 1000", fieldAndArguments));
                    }
                    return Optional.empty();
                });
        FieldValidationInstrumentation fieldValidationInstrumentation = new FieldValidationInstrumentation(fieldValidation);

        TracingInstrumentation tracingInstrumentation  = new TracingInstrumentation();

        // Define the instrumentation chain
        List<Instrumentation> chainedList = new ArrayList<>();
        chainedList.add(dataLoaderDispatcherInstrumentation);
        chainedList.add(fieldValidationInstrumentation);
        chainedList.add(tracingInstrumentation);
        ChainedInstrumentation chainedInstrumentation = new ChainedInstrumentation(chainedList);

        // Define the graphQL
        GraphQL graphQL = GraphQL.newGraphQL(graphQLSchema)
                .instrumentation(chainedInstrumentation)
                .build();

        // Define the execution
        //String queryString = "query Query($patentId: String!,$offset: Int,$limit: Int){patent(patentId: $patentId, offset: $offset, limit: $limit) {id pn my_name person {name} citations{id pn citations{id pn citations{id pn}}}} total(patentId: $patentId) offset(offset: $offset, limit: $limit)}";
        String queryString = "query Query($patentId: String!, $offset: Int, $limit: Int) {\n" +
                "  patent(patentId: $patentId, offset: $offset, limit: $limit) {\n" +
                "    id\n" +
                "    pn\n" +
                "    my_name\n" +
                "    person {\n" +
                "      name\n" +
                "    }\n" +
                "    citations {\n" +
                "      ...citationPatent\n" +
                "      citations {\n" +
                "        ...citationPatent\n" +
                "        citations {\n" +
                "          ...citationPatent\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "  total(patentId: $patentId)\n" +
                "  offset(offset: $offset, limit: $limit)\n" +
                "}\n" +
                "\n" +
                "fragment citationPatent on Patent {\n" +
                "  id\n" +
                "  pn\n" +
                "}";
        Map<String, Object> variableMap = new HashMap<>();
        variableMap.put("patentId", "1");
        variableMap.put("offset", "1000");
        variableMap.put("limit", "10");

        // Define the context, for example: AWS Credential
        AWSCredential awsCredential = new AWSCredential("foo", "bar");

        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                .query(queryString)
                .context(awsCredential)
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

        System.out.println("Total Cost: " + (endTime - startTime) + " ms");
    }


    static Map<String, List<String>> patentCitationIdMap = new HashMap();

    static {
        patentCitationIdMap.put("1", Arrays.asList(new String[]{"2", "3", "4"}));
        patentCitationIdMap.put("2", Arrays.asList(new String[]{"1", "3", "4"}));
        patentCitationIdMap.put("3", Arrays.asList(new String[]{"2", "3", "4"}));
        patentCitationIdMap.put("4", Arrays.asList(new String[]{"1", "2", "3"}));
    }

    static List<String> getPatentCitationIds(String patentId) {
        return patentCitationIdMap.get(patentId);
    }

    static List<Object> getPatentByPatentIds(List<String> patentIds) {
        List<Object> patentCitations = patentIds.stream().map(id -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Patent patent = new Patent();
            patent.setId(id);
            patent.setPn("AAA");
            patent.setMyName("Patent Demo");
            Person person = new Person();
            person.setName("Chris");
            patent.setPerson(person);
            return patent;
        }).collect(Collectors.toList());
        return patentCitations;
    }

    static Integer getTotalCountByPatentId(String patentId){
        return 100;
    }
}
