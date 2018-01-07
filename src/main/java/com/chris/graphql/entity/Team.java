package com.chris.graphql.entity;

import com.chris.graphql.datafetcher.TeamNameDataFetcher;

import graphql.annotations.GraphQLDataFetcher;
import graphql.annotations.GraphQLField;
import graphql.annotations.GraphQLName;

/**
 * Created by ye830 on 12/22/2017.
 */
@GraphQLName("team")
public class Team {
    @GraphQLName("team_name")
    @GraphQLField
    @GraphQLDataFetcher(TeamNameDataFetcher.class)
    public
    String name;

    @GraphQLName("team_total_members")
    @GraphQLField
    public String total;
}
