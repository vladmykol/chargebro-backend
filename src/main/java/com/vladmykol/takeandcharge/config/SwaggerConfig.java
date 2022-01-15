package com.vladmykol.takeandcharge.config;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(title = "ChargeBro API", version = "2.0", description = "PowerBank renting solution"))
public class SwaggerConfig {
}
