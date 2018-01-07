package com.chris.graphql.datafetcher;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

/**
 * Created by ye830 on 12/22/2017.
 */
public class TeamNameDataFetcher implements DataFetcher<String> {

    @Override
    public String get(DataFetchingEnvironment environment) {
        return "Backend Service Team";
    }
}
