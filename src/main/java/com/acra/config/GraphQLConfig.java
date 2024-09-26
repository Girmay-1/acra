package com.acra.config;

import graphql.scalars.ExtendedScalars;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

@Configuration
public class GraphQLConfig {

    @Bean
    public RuntimeWiringConfigurer runtimeWiringConfigurer() {
        return wiringBuilder -> wiringBuilder
                .scalar(ExtendedScalars.DateTime)
                .type("Query", typeWiring -> typeWiring
                        .dataFetcher("getReviewForPullRequest", environment -> {
                            // This will be implemented in the resolver
                            return null;
                        })
                        .dataFetcher("getReviewHistory", environment -> {
                            // This will be implemented in the resolver
                            return null;
                        }))
                .type("Mutation", typeWiring -> typeWiring
                        .dataFetcher("triggerReview", environment -> {
                            // This will be implemented in the resolver
                            return null;
                        })
                        .dataFetcher("updateRuleSet", environment -> {
                            // This will be implemented in the resolver
                            return null;
                        }));
    }
}