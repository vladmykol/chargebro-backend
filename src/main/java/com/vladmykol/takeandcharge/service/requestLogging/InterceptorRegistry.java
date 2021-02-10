package com.vladmykol.takeandcharge.service.requestLogging;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Component
@RequiredArgsConstructor
public class InterceptorRegistry implements WebMvcConfigurer {

    private final IPAddressInterceptor ipAddressInterceptor;

    @Override
    public void addInterceptors(org.springframework.web.servlet.config.annotation.InterceptorRegistry registry) {
        registry.addInterceptor(ipAddressInterceptor);
    }
}
