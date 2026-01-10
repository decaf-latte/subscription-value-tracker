package com.tracker.subscriptionvaluetracker.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("구독 가성비 트래커 API")
                        .version("1.0.0")
                        .description("구독 서비스의 사용 대비 가성비를 계산하는 REST API")
                        .contact(new Contact()
                                .name("Subscription Value Tracker")
                                .url("https://github.com/decaf-latte/subscription-value-tracker"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Development Server"),
                        new Server().url("https://your-production-url.com").description("Production Server")
                ));
    }
}
