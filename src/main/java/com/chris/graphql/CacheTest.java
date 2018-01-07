package com.chris.graphql;

import com.chris.graphql.entity.Assignee;
import com.chris.graphql.entity.FamilyType;
import com.chris.graphql.entity.Legal;
import com.chris.graphql.entity.PatentBiblio;
import com.chris.graphql.entity.PatentBiblioAndLegal;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.execution.preparsed.PreparsedDocumentEntry;
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
public class CacheTest {
    public static void main(String... args) {



        // Define static value
        PatentBiblio patentBiblio = new PatentBiblio();
        patentBiblio.setId("88d378a5-3909-4e9c-82de-55dd70e4685c");
        patentBiblio.setPn("IL168994A");
        patentBiblio.setApno("IL168994");
        patentBiblio.setFamilyType(FamilyType.INPADOC);

        Assignee assignee = new Assignee();
        assignee.setName("ChrisYe");
        assignee.setLang("EN");

        patentBiblio.setAns(assignee);


        Legal legal = new Legal();
        legal.setLegalStatus(Arrays.asList("1", "2"));
        legal.setEventStatus(Arrays.asList("61", "62"));
        legal.setL001ep("EP");

        PatentBiblioAndLegal patentBiblioAndLegal = new PatentBiblioAndLegal();
        patentBiblioAndLegal.setPatentBiblio(patentBiblio);
        patentBiblioAndLegal.setLegal(legal);


        // Define the schema
        File schema = new File(CacheTest.class.getClassLoader().getResource("nestobj.graphqls").getPath());

        // Define the TypeDefineRegistry
        TypeDefinitionRegistry typeDefinitionRegistry = new SchemaParser().parse(schema);

        // Define the run time wiring
        RuntimeWiring runtimeWiring = RuntimeWiring.newRuntimeWiring()
                .type("Query", builder -> builder
                        .dataFetcher("patent_biblio", env -> patentBiblio)
                        .dataFetcher("patent_biblio_legal", env -> legal)
                )
                .type("PatentBiblio", builder -> builder
                        .dataFetcher("id", env -> {
                            Object patentId = env.getArgument("patentId");
                            if (patentId != null) {
                                return patentId;
                            }
                            return patentBiblio.getId();
                        }))
                .type(TypeRuntimeWiring.newTypeWiring("Patent").typeResolver(env -> {
                    Object javaObject = env.getObject();
                    if (javaObject instanceof PatentBiblio) {
                        return (GraphQLObjectType) env.getSchema().getType("PatentBiblio");
                    } else {
                        return (GraphQLObjectType) env.getSchema().getType("PatentBiblio");
                    }
                }))
                .type(TypeRuntimeWiring.newTypeWiring("Person").typeResolver(env -> {
                    Object javaObject = env.getObject();
                    if (javaObject instanceof Assignee) {
                        return (GraphQLObjectType) env.getSchema().getType("Assignee");
                    } else {
                        return (GraphQLObjectType) env.getSchema().getType("Assignee");
                    }
                }))
                .type(TypeRuntimeWiring.newTypeWiring("PatentBiblioAndLegal").typeResolver(env -> {
                    Object javaObject = env.getObject();
                    if (javaObject instanceof PatentBiblio) {
                        return (GraphQLObjectType) env.getSchema().getType("PatentBiblio");
                    }
                    if (javaObject instanceof Legal) {
                        return (GraphQLObjectType) env.getSchema().getType("Legal");
                    } else {
                        return (GraphQLObjectType) env.getSchema().getType("PatentBiblio");
                    }
                }))
                .build();

        // Define the GraphQLSchema
        GraphQLSchema graphQLSchema = new SchemaGenerator().makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);

        Cache<String, PreparsedDocumentEntry> cache = Caffeine.newBuilder().maximumSize(10_000).build();

        // Define the GraphQL
        GraphQL graphQL = GraphQL.newGraphQL(graphQLSchema)
                .preparsedDocumentProvider(cache::get)
                .build();

        // Execute the query
        // String queryString = "{patent_biblio {id pn apno ans{name lang} familyType}}";

        // Execute the union query
//        String queryString = "{patent_biblio_legal {... on PatentBiblio {id} ... on Legal {legalStatus}}}";
//        ExecutionResult executionResult = graphQL.execute(queryString);
//        System.out.println(executionResult.getData().toString());

        // Query by ExecutionInput
        String queryString = "query Query($patentId: String!){patent_biblio {id(patentId: $patentId) pn apno ans{name lang} familyType}}";
        Map<String, Object> variableMap = new HashMap<>();
        variableMap.put("patentId", "1111111");
        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                .query(queryString)
                .operationName("Query")
                .variables(variableMap)
                .build();

        Long startTime = new Date().getTime();
        ExecutionResult executionResult = graphQL.execute(executionInput);
        Map<String, Object> specificationResult = executionResult.toSpecification();
        Long endTime = new Date().getTime();
        System.out.println("First time cost:" + String.valueOf(endTime - startTime));
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            System.out.println(objectMapper.writeValueAsString(specificationResult));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }


        startTime = new Date().getTime();
        executionResult = graphQL.execute(executionInput);
        executionResult.toSpecification();
        endTime = new Date().getTime();
        System.out.println("Second time cost:" + String.valueOf(endTime - startTime));
    }
}
