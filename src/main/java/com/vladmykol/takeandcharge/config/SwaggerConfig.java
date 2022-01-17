package com.vladmykol.takeandcharge.config;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(title = "ChargeBro API", version = "2.0", description = "PowerBank renting solution"))
public class SwaggerConfig {
    @Bean
    GroupedOpenApi mobileApis() {
        return GroupedOpenApi.builder().group("mobile").pathsToMatch("/**/a/**").build();
    }

    @Bean
    GroupedOpenApi adminApis() {
        return GroupedOpenApi.builder().group("admin").pathsToMatch("/**/admin/**").build();
    }

    @Bean
    GroupedOpenApi allApis() {
        return GroupedOpenApi.builder().group("all").pathsToMatch("/**").build();
    }
}
