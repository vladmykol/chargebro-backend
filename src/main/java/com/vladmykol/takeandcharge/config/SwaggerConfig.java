package com.vladmykol.takeandcharge.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
    public static final Contact DEFAULT_CONTACT = new Contact(
            "Vlad Mykol", null, "your-email@example.com");

    public static final ApiInfo BUSINESS_API_INFO = new ApiInfo(
            "Take&Charge API", "PowerBank renting solution", "1.0",
            "http://your-domain.example.com/policy", DEFAULT_CONTACT,
            null, null, Arrays.asList());

    public static final ApiInfo SERVICE_API_INFO = new ApiInfo(
            "Take&Charge service API", "Service control", "1.0",
            null, DEFAULT_CONTACT,
            null, null, Arrays.asList());

    private static final Set<String> DEFAULT_PRODUCES_AND_CONSUMES =
            new HashSet<String>(Arrays.asList("application/json",
                    "application/xml"));

    @Bean
    public Docket businessApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(BUSINESS_API_INFO)
                .produces(DEFAULT_PRODUCES_AND_CONSUMES)
                .consumes(DEFAULT_PRODUCES_AND_CONSUMES)
                .groupName("business")
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.vladmykol.takeandcharge.controller"))
                .build();
    }

    @Bean
    public Docket serviceApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(SERVICE_API_INFO)
                .produces(DEFAULT_PRODUCES_AND_CONSUMES)
                .consumes(DEFAULT_PRODUCES_AND_CONSUMES)
                .groupName("service")
                .select()
                .apis(Predicate.not(RequestHandlerSelectors.basePackage("com.vladmykol.takeandcharge.controller")))
                .build();
    }

}
