package com.apollostore.apigateway.config;

import com.apollostore.apigateway.filters.AuthenticationPrefilter;
import com.apollostore.apigateway.filters.AuthorizationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class GatewayConfig {

    private final AuthorizationFilter filter;


    @Value("${jwt.auth_service_url}")
    private String auth_service_url;

    @Value("${jwt.course_service_url}")
    private String course_service_url;

    @Value("${jwt.product_service_url}")
    private String product_service_url;

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
        return builder.routes().route(auth_service_url, r -> r.path("/api/v1/auth/**").filters(f -> f.filter(authFilter.apply(
                        new AuthenticationPrefilter.Config()))).uri(auth_service_url))

                .route(course_service_url, r -> r.path("/api/v1/course/**").filters(f -> f.filter(authFilter.apply(
                        new AuthenticationPrefilter.Config()))).uri(course_service_url)) // <-- Gotta change for each jawn

                .route(product_service_url, r -> r.path("/api/v1/product/**").filters(f -> f.filter(authFilter.apply(
                        new AuthenticationPrefilter.Config()))).uri(product_service_url))// <-- Gotta change for each jawn

//                .route(internal_url, r -> r.path("/api/v1/payment/**").filters(f -> f.filter(authFilter.apply(
//                        new AuthenticationPrefilter.Config()))).uri("lb://PAYMENT-SERVICE"))
//
//                .route(internal_url, r -> r.path("/api/v1/order/**").filters(f -> f.filter(authFilter.apply(
//                        new AuthenticationPrefilter.Config()))).uri("lb://ORDER-SERVICE"))

                .build();
    }
}
