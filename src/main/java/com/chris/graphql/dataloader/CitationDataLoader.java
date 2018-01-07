package com.chris.graphql.dataloader;

import com.chris.graphql.entity.Patent;

import org.dataloader.BatchLoader;
import org.dataloader.DataLoader;
import org.dataloader.DataLoaderRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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
@Configuration
public class CitationDataLoader {

    @Bean("CitationGraphQL")
    protected GraphQL getGraphQL() {
        BatchLoader<String, Object> patentBatchLoader = keys -> CompletableFuture.supplyAsync(() -> getPatentByPatentIds(keys));

        DataLoader<String, Object> patentDataLoader = new DataLoader(patentBatchLoader);

        DataFetcher patentDataFetcher = env -> patentDataLoader.load(env.getArgument("patentId"));

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
                )
                .type("Patent", builder -> builder
                        .dataFetcher("citations", patentCitationDataFetcher))
                .build();

        // Define the graphQL schema
        GraphQLSchema graphQLSchema = new SchemaGenerator().makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);

        // Define data loader registry
        DataLoaderRegistry registry = new DataLoaderRegistry();
        registry.register("patent", patentDataLoader);

        // Define the graphQL
        return GraphQL.newGraphQL(graphQLSchema)
                .instrumentation(new DataLoaderDispatcherInstrumentation(registry))
                .build();
    }

    static Map<String, List<String>> patentCitationIdMap = new HashMap();

    static {
        patentCitationIdMap.put("1", Arrays.asList(new String[]{"2", "3", "4"}));
        patentCitationIdMap.put("2", Arrays.asList(new String[]{"1", "3", "4"}));
        patentCitationIdMap.put("3", Arrays.asList(new String[]{"2", "3", "4"}));
        patentCitationIdMap.put("4", Arrays.asList(new String[]{"1", "2", "3"}));
    }

    private List<String> getPatentCitationIds(String patentId) {
        return patentCitationIdMap.get(patentId);
    }

    private List<Object> getPatentByPatentIds(List<String> patentIds) {
        List<Object> patentCitations = patentIds.stream().map(id -> {
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
