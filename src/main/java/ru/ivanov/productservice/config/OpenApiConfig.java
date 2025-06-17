package ru.ivanov.productservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(
                        new Info()
                                .contact(new Contact().name("Anton Ivanov").url("https://github.com/Ant0nIvanov"))
                                .title("Product Service API")
                                .description("OpenAPI Documentation for Product Service")
                                .version("1.0.0")
                )
                .addServersItem( new Server().url("http://localhost:8080"));
    }
}
