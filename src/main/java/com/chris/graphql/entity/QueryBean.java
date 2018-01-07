package com.chris.graphql.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Created by ye830 on 12/21/2017.
 */
public class QueryBean {

    @JsonProperty("query")
    private String query;

    @JsonProperty("variable")
    private Map<String, Object> variable;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Map<String, Object> getVariable() {
        return variable;
    }

    public void setVariable(Map<String, Object> variable) {
        this.variable = variable;
    }
}
