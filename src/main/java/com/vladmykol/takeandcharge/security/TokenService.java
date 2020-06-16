package com.vladmykol.takeandcharge.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Service
@Slf4j
public class TokenService {
    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String HEADER_WITH_TOKEN = "Authorization";

    @Value("${take-and-charge.api.jwt-secret}")
    private String jwtSecret;

    @Value("${take-and-charge.api.jwt-expiration}")
    private int jwtExpirationMs;

    public String generateJwtToken(String subject) {
        return Jwts
                .builder()
                .setId("ApiJwt")
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(SignatureAlgorithm.HS512,
                        jwtSecret.getBytes()).compact();
    }

    public String getToken(HttpServletRequest request) {
        String token = request.getHeader(HEADER_WITH_TOKEN);
        if (!StringUtils.startsWithIgnoreCase(token, TOKEN_PREFIX)) return null;
        else return StringUtils.replace(token, TOKEN_PREFIX, "");
    }

    public void addToken(HttpServletResponse response, String generateJwtToken) {
        response.addHeader(HEADER_WITH_TOKEN, TOKEN_PREFIX + generateJwtToken);
    }

    public String validateToken(String token) {
        return Jwts.parser().setSigningKey(jwtSecret.getBytes()).parseClaimsJws(token).getBody().getSubject();
    }


}
