package com.vladmykol.takeandcharge.security;

import com.vladmykol.takeandcharge.entity.IncomingRequest;
import com.vladmykol.takeandcharge.repository.RequestLogRepository;
import com.vladmykol.takeandcharge.service.CustomUserDetailsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtAuthorizationFilter extends BasicAuthenticationFilter {
    private final String[] IP_HEADER_NAMES = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
    };
    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService customUserDetailsService;
    private final RequestLogRepository requestLogRepository;

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager, JwtProvider jwtProvider, CustomUserDetailsService customUserDetailsService, RequestLogRepository requestLogRepository) {
        super(authenticationManager);
        this.customUserDetailsService = customUserDetailsService;
        this.jwtProvider = jwtProvider;
        this.requestLogRepository = requestLogRepository;
    }

    private HttpHeaders getHeaders(HttpServletRequest request) {
        return Collections.list(request.getHeaderNames())
                .stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        h -> Collections.list(request.getHeaders(h)),
                        (oldValue, newValue) -> newValue,
                        HttpHeaders::new
                ));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String ipAddress = getRemoteIP(request);
        try {
            authenticate(request);
            chain.doFilter(request, response);
        } finally {
            var requestUrl = request.getRequestURL().toString();
            if (!requestLogRepository.existsByRequest(requestUrl)) {
                var incomingRequest = new IncomingRequest();
                incomingRequest.setMethod(request.getMethod());
                incomingRequest.setIpAddress(ipAddress);
                incomingRequest.setRequest(request.getRequestURL().toString());
                incomingRequest.setRequestParams(request.getQueryString());
                incomingRequest.setRequestHeaders(getHeaders(request));
                incomingRequest.setResponseStatus(response.getStatus());
                requestLogRepository.save(incomingRequest);
            }
            log.debug("Incoming {} from {} to {} and status {}",
                    request.getMethod(),
                    ipAddress,
                    request.getRequestURL() + (request.getQueryString() == null ? "" : "?" + request.getQueryString()),
                    response.getStatus());
        }
    }

    private void authenticate(HttpServletRequest request) {
        String token = jwtProvider.getToken(request);
        if (token != null) {
            String userId = jwtProvider.parseAuthToken(token);

            setUpSpringAuthentication(userId, request);
        }
    }

    private void setUpSpringAuthentication(String userId, HttpServletRequest request) {
        if (userId != null) {
            var userDetails = customUserDetailsService.loadUserById(userId);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
                    userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            SecurityContextHolder.clearContext();
        }
    }

    public String getRemoteIP(HttpServletRequest request) {
        String ip = Arrays.stream(IP_HEADER_NAMES)
                .map(request::getHeader)
                .filter(h -> h != null && h.length() != 0 && !"unknown".equalsIgnoreCase(h))
                .map(h -> h.split(",")[0])
                .reduce("", (h1, h2) -> h1 + ":" + h2);
        return ip + request.getRemoteAddr();
    }


}
