package com.apollostore.apigateway.filters;



import com.apollostore.apigateway.payload.response.ConnValidationResponse;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import com.apollostore.apigateway.payload.response.Authorities;
import java.util.List;
import java.util.function.Predicate;

@Component
@Slf4j
public class AuthenticationPrefilter extends AbstractGatewayFilterFactory<AuthenticationPrefilter.Config> {
//dd
    @Value("${jwt.internal_url}")
    private String internal_url;

    private final WebClient.Builder webClientBuilder;

    public AuthenticationPrefilter(WebClient.Builder webClientBuilder) {
        super(Config.class);
        this.webClientBuilder=webClientBuilder;
    }


    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            log.info("**************************************************************************");
            log.info("URL is - " + request.getURI().getPath());


            Predicate<ServerHttpRequest> isSecured = getServerHttpRequestPredicate();

            if(isSecured.test(request)) {
                /* Ok --> So it looks like the point of the first one was to validate the jwt for
                 * syntax and to see if it was actually there
                 *
                 * Now --> What we have here is we are sending the valid token to our auth-service so that
                 * it can validate the token by connecting with the user service and returning important user
                 * information like username and the users authorithies
                 * and we are also given it a bounus "auth-token" property
                 * ||--> Next up see how the other services uses this auth-token propety
                 *       - We may have to come up with this ourselves becuase this does not give us an example of this */
                final String authorization = request.getHeaders().getOrEmpty("Authorization").get(0);
                String bearerToken = authorization.replace("Bearer ", "");
                log.info("Bearer Token: "+ bearerToken);

                return webClientBuilder.baseUrl(internal_url).build().get()
                        .uri("/api/v1/auth/validateToken")
                        .header("Authorization", bearerToken)
                        .retrieve().bodyToMono(ConnValidationResponse.class)
                        .map(response -> {
                            exchange.getRequest().mutate().header("username", response.getUsername());
                            exchange.getRequest().mutate().header("authorities", response.getAuthorities().stream().map(Authorities::getAuthority).reduce("", (a, b) -> a + "," + b));
                            exchange.getRequest().mutate().header("auth-token", response.getToken());

                            System.out.println("------------------------- AUTHENTICATION SUCCESS! /api/v1/validateToken ! ---------------------------------");

                            return exchange;
                        }).flatMap(chain::filter).onErrorResume(error -> {
                            log.info("Error Happened " + error.getMessage());

                            System.out.println("------------------------- ERROR AUTHENTICATING: /api/v1/validateToken ! ---------------------------------");
                            ServerHttpResponse response = exchange.getResponse();
                            response.setStatusCode(HttpStatus.UNAUTHORIZED);

                            return response.setComplete();
                        });
            }

            return chain.filter(exchange);
        };
    }

    private static Predicate<ServerHttpRequest> getServerHttpRequestPredicate() {
        final List<String> apiEndpoints = List.of(
//                    "/api/v1/auth/**",
                "/api/v1/auth/register",
                "/api/v1/auth/authenticate",
                "/api/v1/auth/refresh-token",
                "/v2/api-docs",
                "/v3/api-docs",
                "/v3/api-docs/**",
                "/swagger-resources",
                "/swagger-resources/**",
                "/configuration/ui",
                "/configuration/security",
                "/swagger-ui/**",
                "/webjars/**",
                "/swagger-ui.html");

        return r -> apiEndpoints.stream()
                .noneMatch(uri -> r.getURI().getPath().contains(uri));
    }

    @NoArgsConstructor
    public static class Config {


    }
}
