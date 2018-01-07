package com.chris.graphql.controller;

import com.chris.graphql.entity.QueryBean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;

/**
 * Created by ye830 on 9/11/2017.
 */

@RestController
@RequestMapping(value = "/patent")
public class PatentController {

    @Autowired
    GraphQL citationGraphQL;

    @RequestMapping(
            value = "/citation",
            method = RequestMethod.POST,
            produces = "application/json"
    )
    public Map<String, Object> citation(@RequestBody QueryBean queryBean){
        Long startTime = new Date().getTime();
        // Define the execution
        String queryString = queryBean.getQuery();
        Map<String, Object> variableMap = new HashMap<>();
        for(Map.Entry<String, Object> entry : queryBean.getVariable().entrySet()){
            variableMap.put(entry.getKey(), entry.getValue());
        }
        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                .query(queryString)
                .operationName("Query")
                .variables(variableMap)
                .build();

        ExecutionResult executionResult = citationGraphQL.execute(executionInput);
        Map<String, Object> specificationResult = executionResult.toSpecification();

        Long endTime = new Date().getTime();

        System.out.println("Total Cost: "+(endTime - startTime)+" ms");
        return specificationResult;
    }
}
