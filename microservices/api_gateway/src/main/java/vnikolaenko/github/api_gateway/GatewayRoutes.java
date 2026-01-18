package vnikolaenko.github.api_gateway;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class GatewayRoutes {


    @Bean
    // localhost:8080/auth/** -> http://localhost:8090/auth/**
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("user-service", r -> r
                        .path("/auth/**")
                        // .filters(f -> f.stripPrefix(1))
                        .uri("http://localhost:8090"))

                .route("jarch-service", r -> r
                        .path("/jarch/**")
                        // .filters(f -> f.stripPrefix(1))
                        .uri("http://localhost:8100"))

                .route("jarch-service", r -> r
                        .path("/project-saves/**")
                        // .filters(f -> f.stripPrefix(1))
                        .uri("http://localhost:8100"))

                .route("jarch-service", r -> r
                        .path("/project/**")
                        // .filters(f -> f.stripPrefix(1))
                        .uri("http://localhost:8100"))

                .route("jarch-service", r -> r
                        .path("/team/**")
                        // .filters(f -> f.stripPrefix(1))
                        .uri("http://localhost:8100"))

                .route("jarch-service", r -> r
                        .path("/saving/**")
                        // .filters(f -> f.stripPrefix(1))
                        .uri("http://localhost:8100"))

                .build();
    }


}
