package com.microservice.apigateway.config;

import com.microservice.apigateway.filters.AuthenticationPrefilter;
import com.microservice.apigateway.filters.AuthorizationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class GatewayConfig {

    private final AuthorizationFilter filter;

//    @Bean
//    public RouteLocator routes(RouteLocatorBuilder builder) {
//        return builder.routes().route("AUTH-SERVICE", r -> r.path("/api/v1/auth/**").filters(f -> f.filter(filter)).uri("lb://AUTH-SERVICE"))
//                .route("PRODUCT-SERVICE", r -> r.path("/api/v1/product/**").filters(f -> f.filter(filter)).uri("lb://PRODUCT-SERVICE"))
//                .route("PAYMENT-SERVICE", r -> r.path("/api/v1/payment/**").filters(f -> f.filter(filter)).uri("lb://PAYMENT-SERVICE"))
//                .route("ORDER-SERVICE", r -> r.path("/api/v1/order/**").filters(f -> f.filter(filter)).uri("lb://ORDER-SERVICE")).build();
//    }
    //

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder, AuthenticationPrefilter authFilter ) {
        return builder.routes().route("AUTH-SERVICE", r -> r.path("/api/v1/auth/**").filters(f -> f.filter(authFilter.apply(
                        new AuthenticationPrefilter.Config()))).uri("lb://AUTH-SERVICE"))

                .route("PRODUCT-SERVICE", r -> r.path("/api/v1/product/**").filters(f -> f.filter(authFilter.apply(
                        new AuthenticationPrefilter.Config()))).uri("lb://PRODUCT-SERVICE"))

                .route("PAYMENT-SERVICE", r -> r.path("/api/v1/payment/**").filters(f -> f.filter(authFilter.apply(
                        new AuthenticationPrefilter.Config()))).uri("lb://PAYMENT-SERVICE"))

                .route("ORDER-SERVICE", r -> r.path("/api/v1/order/**").filters(f -> f.filter(authFilter.apply(
                        new AuthenticationPrefilter.Config()))).uri("lb://ORDER-SERVICE")).build();
    }
}
