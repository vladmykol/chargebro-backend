package com.vladmykol.takeandcharge.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

@Slf4j
public class JwtAuthorizationFilter extends BasicAuthenticationFilter {
    private final TokenService tokenService;

    public JwtAuthorizationFilter(TokenService tokenService, AuthenticationManager authenticationManager) {
        super(authenticationManager);
        this.tokenService = tokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String token = tokenService.getToken(request);
        if (token != null) {
            String userId = tokenService.validateToken(token);

            setUpSpringAuthentication(userId);
        }

        chain.doFilter(request, response);
    }

    private void setUpSpringAuthentication(String userId) {
        if (userId != null) {
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userId, null, new ArrayList<>());
            SecurityContextHolder.getContext().setAuthentication(auth);
        } else {
            SecurityContextHolder.clearContext();
        }
    }


}
