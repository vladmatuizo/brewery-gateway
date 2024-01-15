package com.example.brewerygateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("local-discovery")
@Configuration
public class LoadBalancedRoutesConfig {

    @Bean
    public RouteLocator loadBalancedRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(r -> r.path("/api/v1/beer*", "/api/v1/beer/*", "/api/v1/beerUpc/*")
                        .metadata("id", "beer-service")
                        .uri("lb://beer-service"))
                .route(r -> r.path("/api/v1/customers/**")
                        .metadata("id", "order-service")
                        .uri("lb://order-service"))
                .route(r -> r.path("/api/v1/beer/*/inventory")
                        .filters(f -> f.circuitBreaker(config -> config
                                .setName("inventoryCB")
                                .setFallbackUri("forward:/inventory-fallback")
                                .setRouteId("inv-failover")))
                        .metadata("id", "inventory-service")
                        .uri("lb://inventory-service"))
                .route(r -> r.path("/inventory-failover")
                        .metadata("id", "inventory-service")
                        .uri("lb://inventory-failover"))
                .build();
    }
}
