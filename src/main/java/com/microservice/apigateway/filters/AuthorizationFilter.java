package com.microservice.apigateway.filters;

import com.microservice.apigateway.exception.JwtTokenMalformedException;
import com.microservice.apigateway.exception.JwtTokenMissingException;
import com.microservice.apigateway.util.JwtUtils;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Predicate;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthorizationFilter implements GlobalFilter { // <-- Might have to change this to GlobalFilter, so it can run everywhere!

    private final JwtUtils jwtUtils;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        System.out.println("ONNN ATHORIZINGGGGGGGG ------->>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        log.info("JwtAuthenticationFilter | filter is working");
        ServerHttpRequest request = exchange.getRequest();
//      TODO:  Figure out how to get below in Centralized place
        final List<String> apiEndpoints = List.of("/api/v1/auth/register", "/login","/refreshtoken");

        Predicate<ServerHttpRequest> isApiSecured = r -> apiEndpoints.stream()
                .noneMatch(uri -> r.getURI().getPath().contains(uri));

        log.info("JwtAuthenticationFilter | filter | isApiSecured.test(request) : " + isApiSecured.test(request));

        if (isApiSecured.test(request)) {
            /* Below here is important because if there is no Authrization key in the headers
            * then obiously the user never logged in the system in the first place! */
            if (!request.getHeaders().containsKey("Authorization")) {
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                System.out.println("ONNN MEEEEE ------->>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                return response.setComplete();
            }

            final String authorization = request.getHeaders().getOrEmpty("Authorization").get(0);
            String token = authorization.replace("Bearer ", "");

            log.info("JwtAuthenticationFilter | filter | token : " + token);

            try {
                jwtUtils.validateJwtToken(token);
            } catch (ExpiredJwtException e) {
                /* Hmmm --> Maybe the freaking token itself has info on weather
                * the token is expired or not!! */
                log.info("JwtAuthenticationFilter | filter | ExpiredJwtException | error : " + e.getMessage());
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.UNAUTHORIZED);

                return response.setComplete();

            } catch (IllegalArgumentException | JwtTokenMalformedException | JwtTokenMissingException
                      | UnsupportedJwtException e) {
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.BAD_REQUEST);

                System.out.println("jwtUtils.validateJwtToken(token) NOT WORKING! BAD REQUESTTT JAWN ------->>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                return response.setComplete();
            }

            /* For some reason here we are attaching a key/value of username:string
            * to the header --> See if you can notice this in the get request of where ever this
            * request is being mapped to ! */
            Claims claims = jwtUtils.getClaims(token);
            exchange.getRequest().mutate().header("username", String.valueOf(claims.get("username"))).build();
        }

        return chain.filter(exchange);
    }

}
