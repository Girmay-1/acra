package com.acra.resolver;

import com.acra.exception.ReviewNotFoundException;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.security.core.AuthenticationException;

@Component
class GraphQLExceptionHandler extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
        if (ex instanceof ReviewNotFoundException) {
            return GraphqlErrorBuilder.newError()
                    .message(ex.getMessage())
                    .location(env.getField().getSourceLocation())
                    .path(env.getExecutionStepInfo().getPath())
                    .errorType(ErrorType.NOT_FOUND)
                    .build();
        } else if (ex instanceof AuthenticationException) {
            return GraphqlErrorBuilder.newError()
                    .message("Authentication failed: Please provide valid credentials")
                    .location(env.getField().getSourceLocation())
                    .path(env.getExecutionStepInfo().getPath())
                    .errorType(ErrorType.UNAUTHORIZED)
                    .build();
        } else {
            return GraphqlErrorBuilder.newError()
                    .message("An unexpected error occurred")
                    .location(env.getField().getSourceLocation())
                    .path(env.getExecutionStepInfo().getPath())
                    .errorType(ErrorType.INTERNAL_ERROR)
                    .build();
        }
    }
}